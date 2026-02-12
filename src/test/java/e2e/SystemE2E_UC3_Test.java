package e2e;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

class SystemE2E_UC3_Test {

    private static final List<TestResult> results = new ArrayList<>();

    @AfterAll
    static void writeReport() throws Exception {
        E2EReportWriter.writeHtmlReport(results, Paths.get("target/e2e-report.html"));
        System.out.println("E2E report generated: target/e2e-report.html");
    }

    @Test
    void UC3_E2E_Pass_BookingConfirmed() {
        String testName = "UC3_E2E_Pass_BookingConfirmed";
        try {
            // Arrange (in-memory fakes)
            var voloDao = new FakeVoloDAO(10);
            var prenDao = new FakePrenotazioneDAO();
            var passDao = new FakePasseggeroDAO();
            var tariffaDao = new FakeTariffaDAO(100.0);
            var payment = new FakePaymentGateway(true);

            var service = new FakeServizioPrenotazioni(voloDao, prenDao, passDao, tariffaDao, payment);
            var controller = new FakeControllerPrenotazioni(service);

            // Act
            String draftId = controller.avvia("user1", "flight1", "fare1");
            controller.aggiungiPasseggero(draftId, "Mario", "Rossi");
            String bookingCode = controller.conferma(draftId);

            // Assert
            Assertions.assertNotNull(bookingCode);
            Assertions.assertTrue(prenDao.isConfirmed(bookingCode));

            results.add(TestResult.pass(testName));
        } catch (Throwable t) {
            results.add(TestResult.fail(testName, t));
            throw t;
        }
    }

    @Test
    void UC3_E2E_Fail_PaymentRejected() {
        String testName = "UC3_E2E_Fail_PaymentRejected";
        try {
            var voloDao = new FakeVoloDAO(10);
            var prenDao = new FakePrenotazioneDAO();
            var passDao = new FakePasseggeroDAO();
            var tariffaDao = new FakeTariffaDAO(100.0);
            var payment = new FakePaymentGateway(false);

            var service = new FakeServizioPrenotazioni(voloDao, prenDao, passDao, tariffaDao, payment);
            var controller = new FakeControllerPrenotazioni(service);

            String draftId = controller.avvia("user1", "flight1", "fare1");
            controller.aggiungiPasseggero(draftId, "Mario", "Rossi");

            Assertions.assertThrows(RuntimeException.class, () -> controller.conferma(draftId));

            results.add(TestResult.pass(testName));
        } catch (Throwable t) {
            results.add(TestResult.fail(testName, t));
            throw t;
        }
    }

    @Test
    void UC3_E2E_Fail_NoSeatsAvailable() {
        String testName = "UC3_E2E_Fail_NoSeatsAvailable";
        try {
            var voloDao = new FakeVoloDAO(0); // no seats
            var prenDao = new FakePrenotazioneDAO();
            var passDao = new FakePasseggeroDAO();
            var tariffaDao = new FakeTariffaDAO(100.0);
            var payment = new FakePaymentGateway(true);

            var service = new FakeServizioPrenotazioni(voloDao, prenDao, passDao, tariffaDao, payment);
            var controller = new FakeControllerPrenotazioni(service);

            Assertions.assertThrows(RuntimeException.class, () -> controller.avvia("user1", "flight1", "fare1"));

            results.add(TestResult.pass(testName));
        } catch (Throwable t) {
            results.add(TestResult.fail(testName, t));
            throw t;
        }
    }

    // ---------- Minimal support types for the template ----------

    record TestResult(String name, boolean passed, String details, String timestamp) {
        static TestResult pass(String name) {
            return new TestResult(name, true, "", Instant.now().toString());
        }
        static TestResult fail(String name, Throwable t) {
            return new TestResult(name, false, t.getClass().getSimpleName() + ": " + t.getMessage(), Instant.now().toString());
        }
    }

    static class E2EReportWriter {
        static void writeHtmlReport(List<TestResult> results, Path out) throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><head><meta charset='utf-8'><title>E2E Report</title></head><body>");
            sb.append("<h1>System Test (E2E) Report</h1>");
            sb.append("<table border='1' cellpadding='6' cellspacing='0'>");
            sb.append("<tr><th>Test</th><th>Status</th><th>Details</th><th>Timestamp</th></tr>");
            for (TestResult r : results) {
                sb.append("<tr>");
                sb.append("<td>").append(r.name()).append("</td>");
                sb.append("<td>").append(r.passed() ? "PASS" : "FAIL").append("</td>");
                sb.append("<td>").append(r.details()).append("</td>");
                sb.append("<td>").append(r.timestamp()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table></body></html>");
            Files.createDirectories(out.getParent());
            Files.writeString(out, sb.toString());
        }
    }

    // Fakes (minimal)
    static class FakeVoloDAO {
        int seats;
        FakeVoloDAO(int seats){ this.seats = seats; }
        int getSeats(){ return seats; }
        void reserveSeat(){ if(seats<=0) throw new RuntimeException("No seats"); seats--; }
    }
    static class FakePrenotazioneDAO {
        final Set<String> confirmed = new HashSet<>();
        String saveDraft(){ return UUID.randomUUID().toString(); }
        void confirm(String bookingCode){ confirmed.add(bookingCode); }
        boolean isConfirmed(String bookingCode){ return confirmed.contains(bookingCode); }
    }
    static class FakePasseggeroDAO { void save(String draftId, String n, String c){} }
    static class FakeTariffaDAO { final double price; FakeTariffaDAO(double p){price=p;} double getPrice(){return price;} }
    static class FakePaymentGateway {
        final boolean ok; FakePaymentGateway(boolean ok){this.ok=ok;}
        boolean pay(double amount){ return ok; }
    }

    interface PrenotazioniApp {
        String avviaPrenotazione(String userId, String flightId, String fareId);
        void aggiungiPasseggero(String draftId, String nome, String cognome);
        String confermaPrenotazione(String draftId);
    }

    static class FakeServizioPrenotazioni implements PrenotazioniApp {
        private final FakeVoloDAO volo;
        private final FakePrenotazioneDAO pren;
        private final FakePasseggeroDAO pas;
        private final FakeTariffaDAO tariffa;
        private final FakePaymentGateway pay;

        FakeServizioPrenotazioni(FakeVoloDAO v, FakePrenotazioneDAO p, FakePasseggeroDAO pas, FakeTariffaDAO t, FakePaymentGateway pg){
            this.volo=v; this.pren=p; this.pas=pas; this.tariffa=t; this.pay=pg;
        }

        public String avviaPrenotazione(String userId, String flightId, String fareId) {
            if (volo.getSeats() <= 0) throw new RuntimeException("No seats available");
            return pren.saveDraft();
        }

        public void aggiungiPasseggero(String draftId, String nome, String cognome) {
            pas.save(draftId, nome, cognome);
        }

        public String confermaPrenotazione(String draftId) {
            volo.reserveSeat();
            boolean ok = pay.pay(tariffa.getPrice());
            if (!ok) throw new RuntimeException("Payment rejected");
            String bookingCode = "BOOK-" + draftId.substring(0, 6);
            pren.confirm(bookingCode);
            return bookingCode;
        }
    }

    static class FakeControllerPrenotazioni {
        private final PrenotazioniApp app;
        FakeControllerPrenotazioni(PrenotazioniApp app){ this.app=app; }

        String avvia(String userId, String flightId, String fareId){
            return app.avviaPrenotazione(userId, flightId, fareId);
        }
        void aggiungiPasseggero(String draftId, String nome, String cognome){
            app.aggiungiPasseggero(draftId, nome, cognome);
        }
        String conferma(String draftId){
            return app.confermaPrenotazione(draftId);
        }
    }
}
