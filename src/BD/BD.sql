CREATE TABLE Classe (
                        id_classe SERIAL PRIMARY KEY,
                        niveau VARCHAR(100) NOT NULL UNIQUE,
                        nombre_eleves INTEGER DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Enseignant (
                            id_enseignant SERIAL PRIMARY KEY,
                            nom VARCHAR(100) NOT NULL,
                            prenom VARCHAR(100) NOT NULL,
                            email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE etudiant (
                          id SERIAL PRIMARY KEY,
                          nom VARCHAR(100) NOT NULL,
                          prenom VARCHAR(100) NOT NULL,
                          classe_id INTEGER REFERENCES Classe(id_classe) ON DELETE SET NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE matiere (
                         nom VARCHAR(100) PRIMARY KEY,
                         id_enseignant INTEGER UNIQUE NOT NULL REFERENCES Enseignant(id_enseignant) ON DELETE CASCADE
);

CREATE TABLE note (
                      id SERIAL PRIMARY KEY,
                      id_etudiant INTEGER NOT NULL REFERENCES etudiant(id) ON DELETE CASCADE,
                      periode VARCHAR(50) NOT NULL,
                      matiere VARCHAR(100) REFERENCES Matiere(nom) ON DELETE CASCADE,
                      valeur NUMERIC(5,2) NOT NULL CHECK (valeur >= 0 AND valeur <= 20),
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE bulletin (
                          id SERIAL PRIMARY KEY,
                          id_etudiant INTEGER NOT NULL REFERENCES etudiant(id) ON DELETE CASCADE,
                          periode VARCHAR(50) NOT NULL,
                          moyenne NUMERIC(5,2),
                          moyenne_de_la_classe NUMERIC(5,2),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          UNIQUE(id_etudiant, periode)
);


INSERT INTO Classe (niveau, nombre_eleves) VALUES
                                               ('Terminale S', 0),
                                               ('Terminale ES', 0),
                                               ('Terminale L', 0),
                                               ('Première S', 0),
                                               ('Première ES', 0),
                                               ('Première L', 0),
                                               ('Seconde', 0),
                                               ('6ème A', 0),
                                               ('6ème B', 0),
                                               ('5ème A', 0),
                                               ('5ème B', 0),
                                               ('4ème A', 0),
                                               ('3ème A', 0)
    ON CONFLICT (niveau) DO NOTHING;

INSERT INTO etudiant (nom, prenom, classe_id) VALUES
                                                  ('Dupont', 'Jean', 1),
                                                  ('Martin', 'Marie', 1),
                                                  ('Durand', 'Paul', 1),
                                                  ('Lefebvre', 'Sophie', 2),
                                                  ('Bernard', 'Lucas', 2);

INSERT INTO note (id_etudiant, periode, matiere, valeur) VALUES
                                                             (1, '2025-T1', 'Mathématiques', 15.5),
                                                             (1, '2025-T1', 'Français', 14.0),
                                                             (1, '2025-T1', 'Histoire-Géographie', 16.5),
                                                             (1, '2025-T1', 'Anglais', 13.0);



INSERT INTO note (id_etudiant, periode, matiere, valeur) VALUES
                                                             (1, '2025-S1', 'Mathématiques', 16.0),
                                                             (1, '2025-S1', 'Français', 15.5),
                                                             (1, '2025-S1', 'Histoire-Géographie', 17.0),
                                                             (1, '2025-S1', 'Anglais', 14.5);


INSERT INTO note (id_etudiant, periode, matiere, valeur) VALUES
                                                             (2, '2025-S1', 'Mathématiques', 13.0),
                                                             (2, '2025-S1', 'Français', 14.5),
                                                             (2, '2025-S1', 'Histoire-Géographie', 15.0),
                                                             (2, '2025-S1', 'Anglais', 12.5);

INSERT INTO Enseignant (nom, prenom, email) VALUES
                                                ('Durand', 'Pierre', 'pierre.durand@ecole.fr'),
                                                ('Martin', 'Sophie', 'sophie.martin@ecole.fr'),
                                                ('Lefebvre', 'Marc', 'marc.lefebvre@ecole.fr'),
                                                ('Bernard', 'Julie', 'julie.bernard@ecole.fr');

INSERT INTO Matiere (nom, id_enseignant) VALUES
                                             ('Mathématiques', 1),
                                             ('Français', 2),
                                             ('Histoire-Géographie', 3),
                                             ('Anglais', 4);
DROP TABLE IF EXISTS bulletin CASCADE;
DROP TABLE IF EXISTS note CASCADE;
DROP TABLE IF EXISTS Matiere CASCADE;
DROP TABLE IF EXISTS Enseignant CASCADE;
DROP TABLE IF EXISTS etudiant CASCADE;
DROP TABLE IF EXISTS Classe CASCADE;

ALTER TABLE Enseignant ADD COLUMN login VARCHAR(50) UNIQUE;
ALTER TABLE Enseignant ADD COLUMN password_hash VARCHAR(255);
ALTER TABLE Enseignant ADD COLUMN actif BOOLEAN DEFAULT true;
ALTER TABLE Enseignant ADD COLUMN derniere_connexion TIMESTAMP;

ALTER TABLE etudiant ADD COLUMN login VARCHAR(50) UNIQUE;
ALTER TABLE etudiant ADD COLUMN password_hash VARCHAR(255);
ALTER TABLE etudiant ADD COLUMN actif BOOLEAN DEFAULT true;
ALTER TABLE etudiant ADD COLUMN derniere_connexion TIMESTAMP;

UPDATE Enseignant SET
                      login = 'prof.durand',
                      password_hash = '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', -- "password"
                      actif = true
WHERE id_enseignant = 1;

UPDATE Enseignant SET
                      login = 'prof.martin',
                      password_hash = '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
                      actif = true
WHERE id_enseignant = 2;

UPDATE etudiant SET
                    login = 'jean.dupont',
                    password_hash = '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', -- "password"
                    actif = true
WHERE id = 1;

UPDATE etudiant SET
                    login = 'marie.martin',
                    password_hash = '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
                    actif = true
WHERE id = 2;

ALTER TABLE etudiant ADD COLUMN IF NOT EXISTS email VARCHAR(255);

ALTER TABLE etudiant ADD CONSTRAINT etudiant_email_unique UNIQUE (email);



ALTER TABLE Enseignant ALTER COLUMN login SET NOT NULL;
ALTER TABLE Enseignant ALTER COLUMN password_hash SET NOT NULL;
ALTER TABLE etudiant ALTER COLUMN login SET NOT NULL;
ALTER TABLE etudiant ALTER COLUMN password_hash SET NOT NULL;

ALTER TABLE Classe ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;


UPDATE Classe c SET nombre_eleves = (
    SELECT COUNT(*)
    FROM etudiant e
    WHERE e.classe_id = c.id_classe
);

CREATE TABLE IF NOT EXISTS Admin (
                                     id_admin SERIAL PRIMARY KEY,
                                     nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    login VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    actif BOOLEAN DEFAULT true,
    derniere_connexion TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

INSERT INTO Admin (nom, prenom, email, login, password_hash, actif)
VALUES (
           'Administrateur',
           'Système',
           'admin@ecole.fr',
           'admin',
           'e10adc3949ba59abbe56e057f20f883e', -- MD5 de "admin123" (à remplacer par SHA-256)
           true
       );

UPDATE Admin SET password_hash = '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'
WHERE login = 'admin';