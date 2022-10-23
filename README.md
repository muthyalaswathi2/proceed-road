1. build jar: ```mvn clean install```
2. build docker-image: ```docker build -t road-to-cloud-native:1.0.0 . ``` 
3. run container: ```docker run --name test -p 8080:8080 road-to-cloud-native:1.0.0```
4. open browser: ```http://localhost:8080/v1/weather```