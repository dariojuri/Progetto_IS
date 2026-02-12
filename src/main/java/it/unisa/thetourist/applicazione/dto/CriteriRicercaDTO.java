package it.unisa.thetourist.applicazione.dto;
import java.time.LocalDate;
public class CriteriRicercaDTO {
    private String partenza;
    private String arrivo;
    private LocalDate dataPartenza;
    private int numeroPasseggeri;

    public String getPartenza() { return partenza; }
    public void setPartenza(String partenza) { this.partenza = partenza; }

    public String getArrivo() { return arrivo; }
    public void setArrivo(String arrivo) { this.arrivo = arrivo; }

    public LocalDate getDataPartenza() { return dataPartenza; }
    public void setDataPartenza(LocalDate dataPartenza) { this.dataPartenza = dataPartenza; }

    public int getNumeroPasseggeri() { return numeroPasseggeri; }
    public void setNumeroPasseggeri(int numeroPasseggeri) { this.numeroPasseggeri = numeroPasseggeri; }
}
