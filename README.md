# ilivalidator-web-service

## Beschreibung

## Anleitungen

## Komponenten

## Konfigurieren und Starten

Die Anwendung kann wie folgt gestartet werden:

```
java -jar ilivalidator-web-service-server/target/ilivalidator-web-service.jar --spring.mail.username=stefan.ziegler.de@gmail.com --spring.mail.password=my_secret_key
```
Anstelle der spring.xxx.xxx Properties kann man auch die entsprechenden Env Vars setzen (siehe application.properties).



## Entwicklung

### Run 

First Terminal:
```
./mvnw spring-boot:run -pl *-server -am -Penv-dev -Dspring-boot.run.arguments="'spring.mail.username=foo@bar.ch' 'spring.mail.password=my_secret_key'"
```
Anstelle der spring.xxx.xxx Properties kann man (lokal zum Entwickeln) auch die entsprechenden Env Vars setzen (siehe application.properties).

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