DROP TABLE IF EXISTS bulletin CASCADE;
DROP TABLE IF EXISTS note CASCADE;
DROP TABLE IF EXISTS matiere CASCADE;
DROP TABLE IF EXISTS enseignant CASCADE;
DROP TABLE IF EXISTS etudiant CASCADE;
DROP TABLE IF EXISTS classe CASCADE;
DROP TABLE IF EXISTS admin CASCADE;


CREATE TABLE classe(
    id_classe SERIAL PRIMARY KEY,
    niveau VARCHAR(100) NOT NULL UNIQUE,
    nombre_eleves INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admin(
    id_admin SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(250) NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    derniere_connexion TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE enseignant (
    id_enseignant      SERIAL PRIMARY KEY,
    nom                VARCHAR(100) NOT NULL,
    prenom             VARCHAR(100) NOT NULL,
    email              VARCHAR(255) NOT NULL UNIQUE,
    login              VARCHAR(50)  NOT NULL UNIQUE,
    password_hash      VARCHAR(255) NOT NULL,
    actif              BOOLEAN   DEFAULT TRUE,
    derniere_connexion TIMESTAMP,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE etudiant(
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(250) NOT NULL,
    classe_id INT REFERENCES classe(id_classe) ON DELETE SET NULL,
    actif BOOLEAN DEFAULT TRUE,
    derniere_connexion TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE matiere(
    nom VARCHAR(100) PRIMARY KEY,
    coefficient INT NOT NULL,
    id_enseignant INT REFERENCES enseignant(id_enseignant) ON DELETE SET NULL
);


CREATE TABLE note(
    id SERIAL PRIMARY KEY,
    id_etudiant INT REFERENCES etudiant(id) ON DELETE CASCADE,
    nom_matiere VARCHAR(100) REFERENCES matiere(nom) ON DELETE CASCADE,
    periode VARCHAR(50) NOT NULL,
    valeur NUMERIC(5, 2) NOT NULL CHECK ( valeur >= 0 AND valeur <= 20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT note_unique_par_periode UNIQUE (id_etudiant, nom_matiere, periode)
);

CREATE TABLE bulletin(
    id SERIAL PRIMARY KEY,
    id_etudiant INTEGER NOT NULL REFERENCES etudiant(id) ON DELETE CASCADE,
    periode VARCHAR(50) NOT NULL,
    moyenne NUMERIC(5, 2) NOT NULL,
    moyenne_de_la_classe NUMERIC(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_etudiant, periode)
);

CREATE INDEX idx_note_etudiant_periode ON note(id_etudiant, periode);
CREATE INDEX idx_note_matiere ON note(nom_matiere);
CREATE INDEX idx_etudiant_classe ON etudiant(classe_id);

INSERT INTO classe (niveau) VALUES
('6ème'),
('5ème'),
('4ème'),
('3ème'),
('2nde'),
('1ère'),
('Terminale');

INSERT INTO admin (nom, prenom, email, login, password_hash) VALUES
    ('Administrateur', 'Système', 'admin@ecole.fr', 'admin',
     '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9');

INSERT INTO enseignant (nom, prenom, email, login, password_hash) VALUES
            ('Durand',   'Pierre', 'pierre.durand@ecole.fr',  'prof.durand',
            '937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244'),
            ('Martin',   'Sophie', 'sophie.martin@ecole.fr',  'prof.martin',
            '937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244'),
            ('Lefebvre', 'Marc',   'marc.lefebvre@ecole.fr',  'prof.lefebvre',
            '937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244'),
            ('Bernard',  'Julie',  'julie.bernard@ecole.fr',  'prof.bernard',
            '937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244'
);

INSERT INTO matiere (nom, coefficient,id_enseignant) VALUES
    ('Mathématiques',       6,1),
    ('Français',            4,2),
    ('Histoire-Géographie', 2,3),
    ('Anglais',             2,4);

INSERT INTO etudiant (nom, prenom, email, login, password_hash, classe_id) VALUES
   ('Dupont',   'Jean',   'jean.dupont@ecole.fr',   'jean.dupont',
    '937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244', 1),
   ('Martin',   'Marie',  'marie.martin@ecole.fr',  'marie.martin',
   '937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244', 1),
   ('Durand',   'Paul',   'paul.durand@ecole.fr',   'paul.durand',
   '937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244', 1),
   ('Lefebvre', 'Sophie', 'sophie.lefebvre@ecole.fr','sophie.lefebvre',
    '937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244', 2),
   ('Bernard',  'Lucas',  'lucas.bernard@ecole.fr', 'lucas.bernard',
    '937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244', 2);


INSERT INTO note (id_etudiant, periode, nom_matiere, valeur) VALUES
   (1, '2025-T1', 'Mathématiques',       15.5),
   (1, '2025-T1', 'Français',            14.0),
   (1, '2025-T1', 'Histoire-Géographie', 16.5),
   (1, '2025-T1', 'Anglais',             13.0),
   (2, '2025-T1', 'Mathématiques',       13.0),
   (2, '2025-T1', 'Français',            14.5),
   (2, '2025-T1', 'Histoire-Géographie', 15.0),
   (2, '2025-T1', 'Anglais',             12.5),
   (3, '2025-T1', 'Mathématiques',       11.0),
   (3, '2025-T1', 'Français',            12.0);


-- Tous les hash ci-dessous correspondent au mot de passe : Test1234