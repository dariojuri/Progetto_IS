package it.unisa.thetourist.applicazione.servizi;

import it.unisa.thetourist.applicazione.dto.CriteriRicercaDTO;
import it.unisa.thetourist.applicazione.eccezioni.DatiNonValidiException;
import it.unisa.thetourist.dominio.entita.Volo;
import it.unisa.thetourist.persistenza.dao.VoloDAO;

import java.time.LocalDate;
import java.util.List;

public class ServizioRicercaVoli {

    private final VoloDAO voloDAO;

    public ServizioRicercaVoli(VoloDAO voloDAO) {
        this.voloDAO = voloDAO;
    }

    public List<Volo> cercaVoli(CriteriRicercaDTO criteri) throws DatiNonValidiException {
        valida(criteri);
        return voloDAO.findByCriteria(criteri);
    }

    private void valida(CriteriRicercaDTO c) throws DatiNonValidiException {
        if (c == null) throw new DatiNonValidiException("Criteri null");
        if (c.getPartenza() == null || c.getArrivo() == null) throw new DatiNonValidiException("Aeroporti null");
        if (c.getPartenza().equals(c.getArrivo())) throw new DatiNonValidiException("Partenza uguale ad arrivo");
        if (c.getDataPartenza() == null) throw new DatiNonValidiException("Data partenza null");
        if (c.getDataPartenza().isBefore(LocalDate.now())) throw new DatiNonValidiException("Data nel passato");
        if (c.getNumeroPasseggeri() <= 0) throw new DatiNonValidiException("Passeggeri non validi");
    }
}
