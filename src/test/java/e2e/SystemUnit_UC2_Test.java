package e2e;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

class SystemUnit_UC2_Test {

    private static final List<TestResult> results = new ArrayList<>();

    @AfterAll
    static void writeReport() throws Exception {
        // Genera il report specifico per UC2
        UC2ReportWriter.writeHtmlReport(results, Paths.get("target/uc2-unit-report.html"));
        System.out.println("UC2 report generated: target/uc2-unit-report.html");
    }

    @Test
    void UC2_Unit_Pass_BookingsFound() {
        String testName = "UC2_Unit_Pass_BookingsFound";
        try {
            // Category Partition: Utente Esistente, N. Prenotazioni > 0
            var prenDao = new FakePrenotazioneDAO();
            prenDao.addMockBooking("user1", "AZ123", "CONFIRMED");
            prenDao.addMockBooking("user1", "FR456", "DELAYED");

            var service = new FakeServizioVisualizzazione(prenDao);
            var controller = new FakeControllerVisualizzazione(service);

            // Act
            List<BookingDTO> bookings = controller.visualizzaPrenotazioni("user1");

            // Assert
            Assertions.assertFalse(bookings.isEmpty());
            Assertions.assertEquals(2, bookings.size());
            Assertions.assertEquals("AZ123", bookings.get(0).flightCode());

            results.add(TestResult.pass(testName));
        } catch (Throwable t) {
            results.add(TestResult.fail(testName, t));
            throw t;
        }
    }

    @Test
    void UC2_Unit_Pass_NoBookingsFound() {
        String testName = "UC2_Unit_Pass_NoBookingsFound";
        try {
            // Category Partition: Utente Esistente, N. Prenotazioni = 0
            var prenDao = new FakePrenotazioneDAO(); // Vuoto
            var service = new FakeServizioVisualizzazione(prenDao);
            var controller = new FakeControllerVisualizzazione(service);

            // Act
            List<BookingDTO> bookings = controller.visualizzaPrenotazioni("user_nuovo");

            // Assert
            Assertions.assertTrue(bookings.isEmpty());

            results.add(TestResult.pass(testName));
        } catch (Throwable t) {
            results.add(TestResult.fail(testName, t));
            throw t;
        }
    }

    @Test
    void UC2_Unit_Fail_InvalidUser() {
        String testName = "UC2_Unit_Fail_InvalidUser";
        try {
            // Category Partition: Input Utente non valido (null)
            var prenDao = new FakePrenotazioneDAO();
            var service = new FakeServizioVisualizzazione(prenDao);
            var controller = new FakeControllerVisualizzazione(service);

            // Act & Assert
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                controller.visualizzaPrenotazioni(null);
            });

            results.add(TestResult.pass(testName));
        } catch (Throwable t) {
            results.add(TestResult.fail(testName, t));
            throw t;
        }
    }

    // ---------- Minimal support types per UC2 ----------

    record BookingDTO(String id, String flightCode, String status) {}

    record TestResult(String name, boolean passed, String details, String timestamp) {
        static TestResult pass(String name) {
            return new TestResult(name, true, "", Instant.now().toString());
        }
        static TestResult fail(String name, Throwable t) {
            return new TestResult(name, false, t.getClass().getSimpleName() + ": " + t.getMessage(), Instant.now().toString());
        }
    }

    static class UC2ReportWriter {
        static void writeHtmlReport(List<TestResult> results, Path out) throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><head><meta charset='utf-8'><title>UC2 Report</title></head><body>");
            sb.append("<h1>Unit Test Report - UC2 (Visualizzazione Prenotazioni)</h1>");
            sb.append("<table border='1' cellpadding='6' cellspacing='0'>");
            sb.append("<tr bgcolor='#cccccc'><th>Test Case</th><th>Status</th><th>Details</th><th>Timestamp</th></tr>");
            for (TestResult r : results) {
                String color = r.passed() ? "#d4edda" : "#f8d7da";
                sb.append("<tr bgcolor='").append(color).append("'>");
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

    // Fakes per la visualizzazione
    static class FakePrenotazioneDAO {
        private final Map<String, List<BookingDTO>> storage = new HashMap<>();

        void addMockBooking(String userId, String flight, String status) {
            storage.computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(new BookingDTO(UUID.randomUUID().toString(), flight, status));
        }

        List<BookingDTO> getBookings(String userId) {
            return storage.getOrDefault(userId, Collections.emptyList());
        }
    }

    static class FakeServizioVisualizzazione {
        private final FakePrenotazioneDAO dao;
        FakeServizioVisualizzazione(FakePrenotazioneDAO dao) { this.dao = dao; }

        public List<BookingDTO> recuperaLista(String userId) {
            if (userId == null || userId.isEmpty()) throw new IllegalArgumentException("User ID mancante");
            return dao.getBookings(userId);
        }
    }

    static class FakeControllerVisualizzazione {
        private final FakeServizioVisualizzazione service;
        FakeControllerVisualizzazione(FakeServizioVisualizzazione service) { this.service = service; }

        List<BookingDTO> visualizzaPrenotazioni(String userId) {
            return service.recuperaLista(userId);
        }
    }
}