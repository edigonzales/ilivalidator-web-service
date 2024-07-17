[![CI/CD](https://github.com/edigonzales/ilivalidator-web-service/actions/workflows/main.yml/badge.svg)](https://github.com/edigonzales/ilivalidator-web-service/actions/workflows/main.yml)

# ilivalidator-web-service

The ilivalidator web service is a [Spring Boot](https://projects.spring.io/spring-boot/) application and uses [ilivalidator](https://github.com/claeis/ilivalidator) for the INTERLIS transfer file validation.

## Beschreibung

* checks INTERLIS 1+2 transfer files: see [https://github.com/claeis/ilivalidator](https://github.com/claeis/ilivalidator) for all the INTERLIS validation magic of ilivalidator
* uses remote config files for validation tailoring
* user can upload multiple transfer files at once
* REST-API
* simple clustering for horizontal scaling

## Anleitungen

Der Benutzer wählt eine oder mehrere INTERLIS-Transferdateien aus und lädt sie hoch. Im Browser erscheint nach erfolgter Prüfung das Resultat und Links zu den Logdateien. Es stehen verschiedene Prüfprofile zur Verfügung, welche die Validierung zusätzlich konfigurieren (z.B. Warnung statt Error, zusätzliche Constraints, etc.).

Weitere Informationen:

- GUI: [docs/user-manual-de.md](docs/user-manual-de.md)
- Nutzungsplanung: [docs/user-manual-de-nplso.md](docs/user-manual-de-nplso.md)
- REST-API: [docs/rest-api-de.md](docs/rest-api-de.md)

## Komponenten

Die Anwendung besteht aus einer Komponente. Wird ein Datenbankserver für das Speichern der Jobqueue verwendet, gehört das Schema etc. auch als Komponente zur Anwendung. Standardmässig wird eine SQlite-Datenbank verwendet, welche automatisch erstellt wird, falls sie nicht vorhanden ist.

Die Prüfprofile (aka Zusatzkonfiguration) sind nicht Bestandteil der Komponente, sondern sie liegen in eigenen oder fremden ilidata-Repositories.

## Konfigurieren und Starten

Die Anwendung kann wie folgt gestartet werden:

```
java -jar ilivalidator-web-service-server/target/ilivalidator-web-service.jar 
```

respektive mit Docker:

```
docker run -p 8080:8080 sogis/ilivalidator-web-service
```

Die Konfiguration erfolgt entweder mit Spring Properties oder mit Env Vars.

### Optionen (Umgebungsvariablen)

Mit Docker wird die Anwendung mit einem docker-Profil gestartet (siehe Dockerfile). Standardwerte gemäss diesem application-docker.properties:



### Clean up

### Additional models

### Ilivalidator custom functions

### Clustering

## Externe Abhängigkeiten

## Interne Struktur

TODO:
- spring boot / maven multimodule 
- GWT
- Jobrunr
- rest api
- Max file size: Stand heute an zwei Orten...
- Test und Dockertests
- Wie wird metaConfig etc. getestet? (Weiss ich noch nicht zu 100%: Idee neu wäre wohl mit kleinem lokalen Dockerimage mit ilidata.xml etc. Dann müsste preferred Ili Repo im Test noch anders gestetzt werden.)
- Registrierung Zusatzfunktionen
- --spring.profiles.active=docker
- ./mvnw versions:set -DnewVersion=3.0.1-SNAPSHOT -DprocessAllModules
- git-commit-id-plugin -> inkl. Link: http://localhost:8080/actuator/info

## Entwicklung

### Run 

First Terminal:
```
./mvnw spring-boot:run -pl *-server -am -Penv-dev 
```

Second Terminal:
```
./mvnw gwt:codeserver -pl *-client -am
```

Or without downloading all the snapshots again:
```
./mvnw gwt:codeserver -pl *-client -am -nsu 
```

### Build

```
./mvnw -Penv-prod clean package -DexcludedGroups="docker"
```

In der Package-Phase werden die "Spring pur"-Tests durchgeführt und es wird am Ende ein Dockerimage für die Dockertests erstellt. Die definitiven Dockerimages werden wegen des Multi-Arch-Builds in der Pipeline erstellt und publiziert. Die Tests können auch separat ausgeführt werden:

Maven kennt Integrationtests in der Verify-Phase (nach Package). Wir verwenden jedoch nochmals eine separate Testphase, um die Dockertests durchzuführen (siehe nachfolgendes Kapitel).

### Tests

```
./mvnw -Penv-test clean test -DexcludedGroups="docker"
```

```
./mvnw -Penv-test clean test -DexcludedGroups="docker"
```

Einzelner Test ausführen:

```
./mvnw -Penv-test test -Dtest=SpringJobControllerTests#validate_File_Interlis2_Ok -Dsurefire.failIfNoSpecifiedTests=false
```


### Lokales Repository

Für die Durchführung der Tests wird ein INTERLIS-Modellrepository benötigt. Um zur Laufzeit der Tests nicht von fremden (dazu gehört auch unser eigenes) Repositories abhängig zu sein und Veränderungen in solchen (z.B. replaced Modelle, Änderungen in den ini-Konfigs), wird ein Dockerimage mit den für die Tests benötigten Modellen hergestellt. Die Modelle liegen im _*-server/src/test/docker/models_-Ordner. Die ilimodels.xml-Datei wird mit ilimanager hergestellt und sie muss im gleichen Ordner wie die Modelle zu liegen kommen (siehe Befehl unten). Das Dockerimage wird im Maven-Build erzeugt und in den Tests mit Testcontainers hochgefahren.

In Units, Time und CoordSys musste das Metaattribut "precursorVersion" gelöscht werden, weil es im Repo keine geben wird und ili2c solche Modell anschliessend ignoriert.

```
java -jar ilimanager-0.9.1.jar --createIliModels --repos models --out models/ilimodels.xml
```

Zusätzlich ist auch ein ilidata.xml notwendig für die ilivalidator-Konfigurationen (ini-Files). Die Datei wurde manuell angelegt und nachgeführt.

Docker image builden:

```
docker build -t sogis/interlis-repository-test .
```