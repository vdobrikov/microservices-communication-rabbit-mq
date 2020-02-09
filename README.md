# microservices-communication-rabbit-mq

Education project to learn microservices communications via RabbitMQ

## Build
```shell script
$ mvn clean package
```

## Run
```shell script
$ cd docker
$ docker-compose up 
```

## Use
```shell script
$ curl -X POST \
  http://localhost:8080/publish \
  -H 'Content-Type: application/json' \
  -d '{
	"topic": "hello",
	"forwardToTopic": "hello_fw",
	"message": "Test message 42"
}'
```