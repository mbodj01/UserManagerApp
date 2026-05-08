# UserManagerApp

> Application client Java — Gestion des utilisateurs via Web Service SOAP  
> Projet Architecture Logicielle · ESP/UCAD · DIT2 · 2025

---

## Description

Application console Java qui se connecte au **service SOAP** du site d'actualités afin de permettre la gestion complète des utilisateurs. Elle implémente la **Partie 3** du projet (Application Client).

Au démarrage, l'application :
1. Demande l'URL du service SOAP et le **jeton d'authentification** (généré par un admin depuis l'interface web)
2. Demande le **login et mot de passe** de l'utilisateur
3. Appelle `authentifier()` via SOAP pour vérifier les droits
4. Si le rôle est `ADMIN` → accès au menu complet de gestion des utilisateurs

---

## Architecture

```
src/main/java/sn/esp/
├── Main.java                # Point d'entrée & injection des dépendances
├── client/
│   └── SoapClient.java      # Appels SOAP via HTTP brut (sans JAX-WS)
├── model/
│   └── User.java            # Modèle utilisateur
├── service/
│   └── UserService.java     # Couche service / logique métier
└── ui/
    └── ConsoleUI.java       # Interface console interactive
```

## Prérequis

- Java 17+
- Maven 3.6+
- Serveur Tomcat démarré sur `localhost:8080`
- Application `ProjetArchi` déployée
- Jeton d'authentification généré par un administrateur

---

## Configuration

| Paramètre | Valeur |
|-----------|--------|
| URL SOAP | `http://localhost:8080/ProjetArchi/ws/utilisateur` |
| WSDL | `http://localhost:8080/ProjetArchi/ws/utilisateur?wsdl` |
| Namespace | `http://webservices.archi.com/utilisateur` |

Pour modifier l'URL par défaut, éditer la constante dans `Main.java` :

```java
private static final String DEFAULT_URL =
    "http://localhost:8080/ProjetArchi/ws/utilisateur";
```

---

## Compilation & Exécution

```bash
# Compiler et packager
mvn clean package

# Lancer (mode interactif — recommandé)
java -jar target/UserManagerApp-1.0.0-jar-with-dependencies.jar

# Ou avec arguments directs
java -jar target/UserManagerApp-1.0.0-jar-with-dependencies.jar \
     http://localhost:8080/ProjetArchi/ws/utilisateur MON_JETON
```

> **Sans Maven** (compilation manuelle) :
> ```bash
> javac -d out $(find src -name "*.java")
> jar --create --file UserManagerApp.jar --main-class sn.esp.Main -C out .
> java -jar UserManagerApp.jar
> ```

---

## Fonctionnalités

| Opération SOAP | Description | Jeton requis |
|----------------|-------------|:------------:|
| `authentifier` | Vérifie login/motDePasse, retourne le rôle | Non |
| `listerUtilisateurs` | Liste tous les utilisateurs | Oui |
| `ajouterUtilisateur` | Crée un nouvel utilisateur | Oui |
| `modifierUtilisateur` | Met à jour un utilisateur existant | Oui |
| `supprimerUtilisateur` | Supprime un utilisateur par ID | Oui |

---

## Rôles utilisateurs

| Rôle | Accès |
|------|-------|
| `ADMIN` | Accès complet à l'application cliente |
| `USER` | Accès refusé (droits insuffisants) |
| `MODERATEUR` | Accès refusé (droits insuffisants) |

> L'application refuse l'accès à tout rôle autre qu'`ADMIN`, même si les identifiants sont corrects.

---

## Données de test

| Login | Mot de passe | Rôle |
|-------|-------------|------|
| `admin` | `admin123` | ADMIN |

---

## Structure d'une requête SOAP (exemple)

Authentification :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope
  xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
  xmlns:tns="http://webservices.archi.com/utilisateur">
  <soapenv:Header/>
  <soapenv:Body>
    <tns:authentifier>
      <login>admin</login>
      <motDePasse>admin123</motDePasse>
    </tns:authentifier>
  </soapenv:Body>
</soapenv:Envelope>
```

---

## Notes importantes

- Le **jeton** doit être généré au préalable depuis la page d'administration du site web.
- Le **mot de passe** est masqué à la saisie si l'application est lancée depuis un vrai terminal.
- Maximum **3 tentatives** d'authentification avant fermeture automatique.
- Le parsing XML est adaptatif : plusieurs noms de balises sont testés pour s'adapter à l'implémentation JAX-WS du serveur.

---

## Auteurs


| Abdoul Hamidou MBODJ|

---

*ESP/UCAD · École Supérieure Polytechnique · Dakar, Sénégal*
