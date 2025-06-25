Serra Intelligente - Backend Java + PostgreSQL
Questo progetto rappresenta il backend di una serra intelligente, scritto in Java, che si connette a un database PostgreSQL per raccogliere, filtrare e visualizzare dati ambientali come temperatura e umidità.


Funzionalità principali
•	Gestione utenti con login per Admin e User
•	Visualizzazione dati sensori:
o	Ultime letture
o	Filtri per giorno, intervallo e temperatura
•	Esportazione dati (solo per Admin):
o	In formato CSV
o	In formato JSON
•	Ruoli:
o	User: accesso in sola lettura, con possibilità di applicare filtri
o	Admin: accesso completo e possibilità di esportare i dati


Struttura del progetto
serra/
├── autenticazione/
│   ├── MainLogin.java
│   ├── UserMenu.java
│   └── AdminMenu.java
├── publisher/
│   └── Publisher.java
├── subscriber/
│   └── Subscriber.java
├── db/
│   └── schema.sql
├── dati.csv
├── README.md


Database
Tabelle principali
•	utenti — per la gestione degli accessi
•	temperature_log — per la registrazione delle letture di temperatura e umidità
Esempio di connessione in Java
Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
Requisiti
•	Java 17 o superiore
•	PostgreSQL installato
•	Driver JDBC PostgreSQL


Credenziali di test
Admin:
  username: admin
  password: admin123

User:
  username: user
  password: user123
