# TheTourist - Progetto Ingegneria del Software

## Requisiti
- Java 17
- IntelliJ IDEA (consigliato)
- Maven (integrato in IntelliJ oppure installato)

## Come importare il progetto
1. Apri la cartella che contiene `pom.xml` con IntelliJ
2. Tasto destro su `pom.xml` → Add as Maven Project (se richiesto)

## Eseguire i test
- Da IntelliJ: Maven → Lifecycle → test
- Da terminale (se Maven è installato): `mvn test`

## Coverage (JaCoCo)
Dopo `mvn test` apri:
`target/site/jacoco/index.html`
