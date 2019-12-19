# microservices-communication-event-sourcing

Education project to learn microservices communications via RabbitMQ / Kafka

## Build
```shell script
$ mvn clean package
```

## Run
```shell script
$ cd docker
# Rabbit backed
$ docker-compose -f docker-compose-rabbitmq.yml up
# Kafka backed
$ docker-compose -f docker-compose-kafka.yml up 
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