# ilivalidator-web-service

TODO:
- git-commit-id-plugin -> inkl. Link 

## Beschreibung

## Anleitungen

## Komponenten

## Konfigurieren und Starten

Die Anwendung kann wie folgt gestartet werden:

```
java -jar ilivalidator-web-service-server/target/ilivalidator-web-service.jar 
```

### Optionen (Umgebungsvariablen)

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

In der Package-Phase werden die "Spring pur"-Tests durchgeführt und es wird am Ende ein Dockerimage für die Dockertests erstellt. Die definitiven Dockerimages werden wegen des Multi-Arch-Builds in der Pipeline erstellt und publiziert. 

Maven kennt Integrationtests in der Verify-Phase (nach Package). Wir verwenden jedoch nochmals eine separate Testphase, um die Dockertests durchzuführen:

```
./mvnw test -Dgroups="docker"

```

### Tests

```
./mvnw test -DexcludedGroups="docker"
```

```
./mvnw test -Dgroups="docker"
```

### Lokales Repository

Für die Durchführung der Tests wird ein INTERLIS-Modellrepository benötigt. Um zur Laufzeit nicht von fremden (dazu gehört auch unser eigenes) Repositories abhängig zu sein, wird ein Dockerimage mit den für die Tests benötigten Modellen hergestellt. Die Modell liegen im _*-server/src/test/data/models_-Ordner. Die ilimodels.xml-Datei wird mit ilimanager hergestellt. Das Dockerimage wird im Maven-Build erzeugt und in den Tests mit TestContainers hochgefahren.

TODO: Geometrie, Units etc.

```
java -jar ilimanager-0.9.1.jar --createIliModels --repos . --out ilimodels.xml
```