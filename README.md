# ilivalidator-web-service

## Entwicklung

### Run 

First Terminal:
```
./mvnw spring-boot:run -pl *-server -am -Penv-dev
```
**ACHTUNG:** `<skip>true</skip>` auf `false` setzen, sonst beendet sich "run" gleich wieder.


Second Terminal:
```
./mvnw gwt:codeserver -pl *-client -am
```

Or without downloading all the snapshots again:
```
./mvnw gwt:codeserver -pl *-client -am -nsu 
```
