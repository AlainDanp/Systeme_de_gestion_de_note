# Système de gestion de notes

Application console Java de gestion des notes et bulletins scolaires
(architecture en couches, PostgreSQL, JDBC).

## Prérequis
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Installation
```bash
createdb gestion_etudiants
psql -d gestion_etudiants -f src/BD/BD.sql