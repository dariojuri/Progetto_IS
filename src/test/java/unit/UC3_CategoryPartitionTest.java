package unit;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class UC3_CategoryPartitionTest {

    // --- Exceptions (allineale alle tue se le hai già) ---
    static class PostiEsauritiException extends RuntimeException { PostiEsauritiException(String m){super(m);} }
    static class PagamentoFallitoException extends RuntimeException { PagamentoFallitoException(String m){super(m);} }
    static class DatiNonValidiException extends RuntimeException { DatiNonValidiException(String m){super(m);} }
    static class EntitaNonTrovataException extends RuntimeException { EntitaNonTrovataException(String m){super(m);} }

    // --- DTO minimal ---
    record DatiPasseggeroDTO(String nome, String cognome, String documento) {}

    // --- Fakes / Stubs ---
    static class FakeVoloDAO {
        boolean exists = true;
        int posti;

        FakeVoloDAO(int posti) { this.posti = posti; }
        int getPosti(String idVolo) {
            if (!exists) throw new EntitaNonTrovataException("Volo non trovato");
            return posti;
        }
        void consumaPosto(String idVolo) {
            if (posti <= 0) throw new PostiEsauritiException("Posti esauriti");
            posti--;
        }
    }

    static class FakeTariffaDAO {
        boolean exists = true;
        double prezzo = 100.0;

        double getPrezzo(String idTariffa) {
            if (!exists) throw new EntitaNonTrovataException("Tariffa non trovata");
            return prezzo;
        }
    }

    static class FakePrenotazioneDAO {
        boolean confirmed = false;
        String bookingCode;

        String salvaBozza() { return "DRAFT-1"; }
        void conferma(String codice) { confirmed = true; bookingCode = codice; }
    }

    static class FakePaymentGateway {
        boolean ok;
        FakePaymentGateway(boolean ok) { this.ok = ok; }
        boolean pay(double amount) { return ok; }
    }

    // --- Unit under test: UC3 service minimal ---
    static class UC3Service {
        private final FakeVoloDAO voloDAO;
        private final FakeTariffaDAO tariffaDAO;
        private final FakePrenotazioneDAO prenDAO;
        private final FakePaymentGateway payment;

        UC3Service(FakeVoloDAO v, FakeTariffaDAO t, FakePrenotazioneDAO p, FakePaymentGateway pg) {
            this.voloDAO = v; this.tariffaDAO = t; this.prenDAO = p; this.payment = pg;
        }

        String avviaPrenotazione(String idVolo, String idTariffa) {
            // lookup check
            int posti = voloDAO.getPosti(idVolo);
            tariffaDAO.getPrezzo(idTariffa);

            if (posti <= 0) throw new PostiEsauritiException("Posti esauriti");
            return prenDAO.salvaBozza();
        }

        String confermaPrenotazione(String draftId, String idVolo, String idTariffa, DatiPasseggeroDTO passeggero) {
            validaPasseggero(passeggero);

            double prezzo = tariffaDAO.getPrezzo(idTariffa);

            // pagamento
            if (!payment.pay(prezzo)) throw new PagamentoFallitoException("Pagamento fallito");

            // consuma posto e conferma
            voloDAO.consumaPosto(idVolo);
            String codice = "BOOK-001";
            prenDAO.conferma(codice);
            return codice;
        }

        private void validaPasseggero(DatiPasseggeroDTO p) {
            if (p == null) throw new DatiNonValidiException("Passeggero nullo");
            if (p.nome() == null || p.nome().isBlank()) throw new DatiNonValidiException("Nome mancante");
            if (p.cognome() == null || p.cognome().isBlank()) throw new DatiNonValidiException("Cognome mancante");
            if (p.documento() == null || p.documento().isBlank() || p.documento().length() < 3)
                throw new DatiNonValidiException("Documento non valido");
        }
    }

    // -----------------------
    // Category Partition Tests
    // -----------------------

    @Test
    void CP_UC3_01_seatsMany_paymentOk_passengerValid_shouldPass() {
        var voloDAO = new FakeVoloDAO(10);
        var tariffaDAO = new FakeTariffaDAO();
        var prenDAO = new FakePrenotazioneDAO();
        var pay = new FakePaymentGateway(true);

        var sut = new UC3Service(voloDAO, tariffaDAO, prenDAO, pay);

        String draft = sut.avviaPrenotazione("V1", "T1");
        String code = sut.confermaPrenotazione(draft, "V1", "T1", new DatiPasseggeroDTO("Mario", "Rossi", "ID123"));

        assertNotNull(code);
        assertTrue(prenDAO.confirmed);
        assertEquals(9, voloDAO.posti);
    }

    @Test
    void CP_UC3_02_seatsBoundary1_paymentOk_passengerValid_shouldPass() {
        var voloDAO = new FakeVoloDAO(1);
        var tariffaDAO = new FakeTariffaDAO();
        var prenDAO = new FakePrenotazioneDAO();
        var pay = new FakePaymentGateway(true);

        var sut = new UC3Service(voloDAO, tariffaDAO, prenDAO, pay);

        String draft = sut.avviaPrenotazione("V1", "T1");
        String code = sut.confermaPrenotazione(draft, "V1", "T1", new DatiPasseggeroDTO("Mario", "Rossi", "ID123"));

        assertNotNull(code);
        assertTrue(prenDAO.confirmed);
        assertEquals(0, voloDAO.posti);
    }

    @Test
    void CP_UC3_03_seatsZero_shouldFail() {
        var sut = new UC3Service(new FakeVoloDAO(0), new FakeTariffaDAO(), new FakePrenotazioneDAO(), new FakePaymentGateway(true));

        assertThrows(PostiEsauritiException.class, () -> sut.avviaPrenotazione("V1", "T1"));
    }

    @Test
    void CP_UC3_04_paymentKo_shouldFail() {
        var voloDAO = new FakeVoloDAO(10);
        var sut = new UC3Service(voloDAO, new FakeTariffaDAO(), new FakePrenotazioneDAO(), new FakePaymentGateway(false));

        String draft = sut.avviaPrenotazione("V1", "T1");
        assertThrows(PagamentoFallitoException.class, () ->
                sut.confermaPrenotazione(draft, "V1", "T1", new DatiPasseggeroDTO("Mario", "Rossi", "ID123"))
        );

        assertEquals(10, voloDAO.posti, "Seat must not be consumed if payment fails");
    }

    @Test
    void CP_UC3_05_passengerMissingName_shouldFail() {
        var sut = new UC3Service(new FakeVoloDAO(10), new FakeTariffaDAO(), new FakePrenotazioneDAO(), new FakePaymentGateway(true));

        String draft = sut.avviaPrenotazione("V1", "T1");
        assertThrows(DatiNonValidiException.class, () ->
                sut.confermaPrenotazione(draft, "V1", "T1", new DatiPasseggeroDTO("", "Rossi", "ID123"))
        );
    }

    @Test
    void CP_UC3_06_flightNotFound_shouldFail() {
        var voloDAO = new FakeVoloDAO(10);
        voloDAO.exists = false;

        var sut = new UC3Service(voloDAO, new FakeTariffaDAO(), new FakePrenotazioneDAO(), new FakePaymentGateway(true));

        assertThrows(EntitaNonTrovataException.class, () -> sut.avviaPrenotazione("V404", "T1"));
    }

    @Test
    void CP_UC3_07_fareNotFound_shouldFail() {
        var tariffaDAO = new FakeTariffaDAO();
        tariffaDAO.exists = false;

        var sut = new UC3Service(new FakeVoloDAO(10), tariffaDAO, new FakePrenotazioneDAO(), new FakePaymentGateway(true));

        assertThrows(EntitaNonTrovataException.class, () -> sut.avviaPrenotazione("V1", "T404"));
    }
}
