CREATE TABLE IF NOT EXISTS temperature_log (
    id SERIAL PRIMARY KEY,
    temperatura DOUBLE PRECISION NOT NULL,
    umidita DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- Utenti base per login
CREATE TABLE IF NOT EXISTS utenti (
    username TEXT PRIMARY KEY,
    password TEXT NOT NULL,
    ruolo TEXT NOT NULL CHECK (ruolo IN ('admin', 'user'))
);

-- Inserimento utenti di esempio
INSERT INTO utenti (username, password, ruolo) VALUES
('admin', 'admin123', 'admin'),
('user', 'user123', 'user')
ON CONFLICT DO NOTHING;