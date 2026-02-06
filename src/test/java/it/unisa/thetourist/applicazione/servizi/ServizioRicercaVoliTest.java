package it.unisa.thetourist.applicazione.servizi;

import it.unisa.thetourist.applicazione.dto.CriteriRicercaDTO;
import it.unisa.thetourist.applicazione.eccezioni.DatiNonValidiException;
import it.unisa.thetourist.dominio.entita.Volo;
import it.unisa.thetourist.persistenza.dao.VoloDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServizioRicercaVoliTest {

    private VoloDAO voloDAO;
    private ServizioRicercaVoli servizio;

    @BeforeEach
    void setUp() {
        voloDAO = mock(VoloDAO.class);
        servizio = new ServizioRicercaVoli(voloDAO);
    }

    private CriteriRicercaDTO criteriValidi() {
        CriteriRicercaDTO c = new CriteriRicercaDTO();
        c.setPartenza("NAP");
        c.setArrivo("FCO");
        c.setDataPartenza(LocalDate.now().plusDays(7));
        c.setNumeroPasseggeri(1);
        return c;
    }

    @Test
    void validCriteria_returnsResults() throws Exception {
        CriteriRicercaDTO c = criteriValidi();
        when(voloDAO.findByCriteria(c)).thenReturn(List.of(new Volo()));

        List<Volo> res = servizio.cercaVoli(c);

        assertEquals(1, res.size());
        verify(voloDAO).findByCriteria(c);
    }

    @Test
    void sameDepartureArrival_throws() {
        CriteriRicercaDTO c = criteriValidi();
        c.setArrivo("NAP");

        assertThrows(DatiNonValidiException.class, () -> servizio.cercaVoli(c));
        verifyNoInteractions(voloDAO);
    }

    @Test
    void pastDate_throws() {
        CriteriRicercaDTO c = criteriValidi();
        c.setDataPartenza(LocalDate.now().minusDays(1));

        assertThrows(DatiNonValidiException.class, () -> servizio.cercaVoli(c));
        verifyNoInteractions(voloDAO);
    }
}
