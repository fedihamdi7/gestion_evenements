# Service Avis — Microservice de Gestion des Avis

Microservice Spring Boot responsable de la collecte et de la gestion des avis et évaluations laissés par les utilisateurs sur les événements. Il fait partie de l'architecture microservices du projet **Gestion des Événements**.

---

## Table des matières

- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Lancer le projet complet](#lancer-le-projet-complet)
- [Lancer uniquement service-avis](#lancer-uniquement-service-avis)
- [Vérifications après démarrage](#vérifications-après-démarrage)
- [API Reference](#api-reference)
- [Tester avec Postman](#tester-avec-postman)
- [Tester avec curl](#tester-avec-curl)
- [Modèle de données](#modèle-de-données)
- [Problèmes connus](#problèmes-connus)

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   API Gateway :9090                     │
│          /api/avis/**  →  lb://service-avis             │
└────────────────────────┬────────────────────────────────┘
                         │  Eureka (lb://)
              ┌──────────▼──────────┐
              │    service-avis     │
              │       :8083         │
              │  Spring Boot 3.5    │
              └──────────┬──────────┘
                         │
              ┌──────────▼──────────┐
              │   MySQL :3306       │
              │   db_avis           │
              └─────────────────────┘
```

**Ports du projet complet :**

| Service              | Port  | Base de données        |
|----------------------|-------|------------------------|
| eureka-server        | 8761  | —                      |
| api-gateway          | 9090  | —                      |
| service-utilisateurs | 8081  | MySQL — db_utilisateurs |
| service-reservation  | 8082  | H2 in-memory           |
| **service-avis**     | **8083** | **MySQL — db_avis** |

> **Pourquoi le port 9090 pour le gateway ?**
> Oracle XE (installé sur cette machine) occupe le port 8080 par défaut.
> Le gateway a été déplacé sur 9090 pour éviter le conflit.

---

## Prérequis

| Outil          | Version  | Vérifier                  |
|----------------|----------|---------------------------|
| Java (JDK)     | 17+      | `java -version`           |
| MySQL          | 8.0+     | `mysql --version`         |
| eureka-server  | démarré  | http://localhost:8761     |

> Maven n'a **pas** besoin d'être installé globalement.
> Chaque service inclut `mvnw.cmd` (Windows) / `mvnw` (Linux/Mac) qui télécharge Maven automatiquement.

---

## Lancer le projet complet

### Étape 1 — Démarrer MySQL

Vérifier que MySQL tourne sur le port 3306.

```cmd
:: Démarrer le service MySQL (Windows)
net start MySQL80

:: Vérifier la connexion
mysql -u root -p -e "SHOW DATABASES;"
```

La base `db_avis` est créée **automatiquement** au premier démarrage du service (grâce à `createDatabaseIfNotExist=true`).

---

### Étape 2 — Démarrer Eureka Server (port 8761)

Ouvrir un **terminal dédié** dans le dossier `eureka-server` :

```cmd
cd eureka-server
mvnw.cmd spring-boot:run
```

Attendre le message :
```
Started EurekaServerApplication in X seconds
```

Vérifier : [http://localhost:8761](http://localhost:8761)

---

### Étape 3 — Démarrer API Gateway (port 9090)

Ouvrir un **nouveau terminal** dans le dossier `api-gateway` :

```cmd
cd api-gateway
mvnw.cmd spring-boot:run
```

Attendre le message :
```
Netty started on port 9090
```

---

### Étape 4 — Démarrer Service Avis (port 8083)

Ouvrir un **nouveau terminal** dans le dossier `service-avis` :

```cmd
cd service-avis
mvnw.cmd spring-boot:run
```

Attendre le message :
```
Started ServiceAvisApplication in X seconds
```

Avec un mot de passe MySQL différent de `root` :

```cmd
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=monMotDePasse
mvnw.cmd spring-boot:run
```

---

## Lancer uniquement service-avis

Si tu veux tester seulement ce service (sans gateway), les seuls prérequis sont :
1. MySQL démarré sur le port 3306
2. Eureka démarré sur le port 8761

```cmd
cd service-avis
mvnw.cmd spring-boot:run
```

---

## Vérifications après démarrage

### 1. Health checks

```cmd
curl http://localhost:8083/actuator/health
curl http://localhost:9090/actuator/health
```

Réponse attendue pour chaque :
```json
{"status":"UP"}
```

### 2. Eureka Dashboard

Ouvrir [http://localhost:8761](http://localhost:8761)

Les services enregistrés doivent apparaître :
```
API-GATEWAY      UP (1)   localhost:api-gateway:9090
SERVICE-AVIS     UP (1)   localhost:service-avis:8083
```

### 3. Base de données MySQL

```sql
-- Vérifier que la base et la table ont été créées
mysql -u root -p -e "USE db_avis; SHOW TABLES; DESCRIBE avis;"
```

Résultat attendu :
```
+-------------------+
| Tables_in_db_avis |
+-------------------+
| avis              |
+-------------------+

+------------------+--------------+------+-----+
| Field            | Type         | Null | Key |
+------------------+--------------+------+-----+
| id               | bigint       | NO   | PRI |
| utilisateur_id   | bigint       | NO   |     |
| evenement_id     | bigint       | NO   |     |
| note             | int          | NO   |     |
| commentaire      | varchar(255) | YES  |     |
| date_creation    | datetime(6)  | NO   |     |
+------------------+--------------+------+-----+
```

---

## API Reference

Base URL directe : `http://localhost:8083`
Base URL via gateway : `http://localhost:9090`

### Créer un avis

```
POST /api/avis
Content-Type: application/json
```

**Body :**
```json
{
  "utilisateurId": 1,
  "evenementId": 2,
  "note": 4,
  "commentaire": "Très bon événement, bien organisé !"
}
```

Règles de validation :
- `utilisateurId` — obligatoire
- `evenementId` — obligatoire
- `note` — obligatoire, entre **1 et 5**
- `commentaire` — optionnel

**Réponse 201 Created :**
```json
{
  "id": 1,
  "utilisateurId": 1,
  "evenementId": 2,
  "note": 4,
  "commentaire": "Très bon événement, bien organisé !",
  "dateCreation": "2026-06-12T18:04:14.444618"
}
```

---

### Lister tous les avis

```
GET /api/avis
```

---

### Obtenir un avis par ID

```
GET /api/avis/{id}
```

- **200 OK** — avis trouvé
- **404 Not Found** — id inexistant

---

### Avis d'un événement

```
GET /api/avis/evenement/{evenementId}
```

---

### Avis d'un utilisateur

```
GET /api/avis/utilisateur/{utilisateurId}
```

---

### Modifier un avis

```
PUT /api/avis/{id}
Content-Type: application/json
```

Seuls `note` et `commentaire` sont modifiables :
```json
{
  "note": 5,
  "commentaire": "Finalement excellent, je recommande !"
}
```

---

### Supprimer un avis

```
DELETE /api/avis/{id}
```

- **204 No Content** — suppression réussie
- **404 Not Found** — id inexistant

---

### Tableau récapitulatif

| Méthode | Endpoint                          | Description                | Code succès |
|---------|-----------------------------------|----------------------------|-------------|
| POST    | /api/avis                         | Créer un avis              | 201         |
| GET     | /api/avis                         | Lister tous les avis       | 200         |
| GET     | /api/avis/{id}                    | Avis par ID                | 200         |
| GET     | /api/avis/evenement/{evenementId} | Avis d'un événement        | 200         |
| GET     | /api/avis/utilisateur/{userId}    | Avis d'un utilisateur      | 200         |
| PUT     | /api/avis/{id}                    | Modifier note/commentaire  | 200         |
| DELETE  | /api/avis/{id}                    | Supprimer un avis          | 204         |

---

## Tester avec Postman

### Séquence de test recommandée

#### 1. Créer deux avis
- **Method :** POST
- **URL :** `http://localhost:8083/api/avis`
- **Headers :** `Content-Type: application/json`
- **Body :**
```json
{
  "utilisateurId": 1,
  "evenementId": 1,
  "note": 5,
  "commentaire": "Événement fantastique, à refaire !"
}
```

Créer un second avis avec `utilisateurId: 2, note: 3`.

#### 2. Lister tous les avis
- **Method :** GET — `http://localhost:8083/api/avis`

#### 3. Avis par événement
- **Method :** GET — `http://localhost:8083/api/avis/evenement/1`

#### 4. Avis par utilisateur
- **Method :** GET — `http://localhost:8083/api/avis/utilisateur/1`

#### 5. Modifier un avis
- **Method :** PUT — `http://localhost:8083/api/avis/1`
```json
{
  "note": 4,
  "commentaire": "Très bien finalement."
}
```

#### 6. Tester une erreur de validation
- **Method :** POST avec `"note": 6` → doit retourner **400 Bad Request**

#### 7. Tester un ID inexistant
- **Method :** GET — `http://localhost:8083/api/avis/999` → doit retourner **404 Not Found**

#### 8. Supprimer un avis
- **Method :** DELETE — `http://localhost:8083/api/avis/1` → **204 No Content**

#### 9. Tester via le Gateway
- Remplacer `localhost:8083` par `localhost:9090` dans toutes les URLs
- Le gateway route automatiquement via Eureka

---

## Tester avec curl

```bash
# 1. Créer un avis
curl -X POST http://localhost:8083/api/avis \
  -H "Content-Type: application/json" \
  -d '{"utilisateurId":1,"evenementId":1,"note":5,"commentaire":"Super evenement !"}'

# 2. Lister tous les avis
curl http://localhost:8083/api/avis

# 3. Avis par événement
curl http://localhost:8083/api/avis/evenement/1

# 4. Avis par utilisateur
curl http://localhost:8083/api/avis/utilisateur/1

# 5. Modifier un avis
curl -X PUT http://localhost:8083/api/avis/1 \
  -H "Content-Type: application/json" \
  -d '{"note":4,"commentaire":"Tres bien finalement."}'

# 6. Supprimer un avis
curl -X DELETE http://localhost:8083/api/avis/1

# 7. Via le Gateway (port 9090)
curl http://localhost:9090/api/avis
```

---

## Modèle de données

**Table `avis` (MySQL — db_avis) :**

| Colonne          | Type         | Contrainte           |
|------------------|--------------|----------------------|
| id               | BIGINT       | PK, auto-increment   |
| utilisateur_id   | BIGINT       | NOT NULL             |
| evenement_id     | BIGINT       | NOT NULL             |
| note             | INT          | NOT NULL, entre 1-5  |
| commentaire      | VARCHAR(255) | nullable             |
| date_creation    | DATETIME(6)  | NOT NULL             |

> La table est créée/mise à jour automatiquement par Hibernate au démarrage (`ddl-auto=update`).
> La base `db_avis` est créée automatiquement si elle n'existe pas.

---

## Problèmes connus

### Port 8080 occupé (Oracle XE)
Oracle Database XE utilise le port 8080 pour son interface HTTP.
**Solution appliquée :** le gateway est configuré sur le port **9090**.

### Première exécution lente
`mvnw.cmd` télécharge Maven (~8 Mo) puis les dépendances Spring Boot (~200 Mo).
Les démarrages suivants sont rapides car tout est mis en cache dans `~/.m2`.

### MySQL — accès refusé
Si tu vois `Access denied for user 'root'@'localhost'` :

**Mot de passe vide (configuration par défaut sur cette machine) :**
```properties
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
```
La valeur après `:` est vide — Spring Boot se connecte sans mot de passe.

**Mot de passe personnalisé :**
```cmd
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=tonMotDePasse
mvnw.cmd spring-boot:run
```
