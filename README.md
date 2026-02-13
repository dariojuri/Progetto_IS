# TheTourist – Progetto Ingegneria del Software

Repository del progetto **TheTourist** (UNISA – Ingegneria del Software).  
Il progetto è organizzato come **applicazione Java (Maven)** con test automatici e report di coverage tramite **JaCoCo**.

---

## Requisiti

- **Java 17**
- **IntelliJ IDEA** (consigliato) con supporto Maven
- **Maven**
  - Opzione A: **Maven integrato in IntelliJ**
  - Opzione B: **Maven installato** e disponibile in PATH (`mvn -v`)

---

## Struttura (alto livello)

- `src/main/java` → codice applicativo
- `src/test/java` → test (JUnit 5 + Mockito)
- `pom.xml` → configurazione Maven
- `target/` → output build (generato da Maven)

---

## Importare il progetto in IntelliJ

1. Apri la cartella che contiene **`pom.xml`**
2. Se richiesto: tasto destro su `pom.xml` → **Add as Maven Project**
3. Attendi il download delle dipendenze (Maven Sync)

---

## Build & Test

### Da IntelliJ (consigliato)
- Apri la finestra **Maven**
- `Lifecycle` → **test**

### Da terminale (solo se Maven è installato e in PATH)
```bash
mvn test
