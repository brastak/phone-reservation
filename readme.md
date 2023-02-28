# Phone reservation app

## How to use
### Run with docker
1. Run application: \
$ docker-compose up -d
2. Get real port number: \
$ docker-compose port app 8080 \
0.0.0.0:**65150**
3. Get list of phones: \
$ curl localhost:65150/phones/all \
[{"phoneId":1,"modelId":**1**,"vendor":"Samsung"...}]
4. Send request to reserve phone of specific model: \
$ curl -XPOST -d '{"modelId": 1, "reservedBy": "brastak"}' -H "Content-Type: application/json" localhost:65150/reservations \
{"phoneId":1,"modelId":1, ... ,"reservation":{"id":**1**,"reservedBy":"brastak",...}}
5. Finish reservation: \
$ curl -XPOST localhost:65150/reservations/1/finish

## Assumptions and Limitations
### Several phones of one model
Phone Models and Phone devices are different objects in the  application. User wants to reserve any 
Phone of specific Model. But application reserves specific Phone device of this Model. Reservation is 
related with concrete Phone. And device Specification is related with phone Model.

### Scheduled reservation
Not implemented. User can not say that he wants to reserve the Phone tomorrow for 2 days. He can just
book Phone right now and return Phone right now.

### Authentication and authorization
Not supported. The user who wants to reserve device is passed as reservation parameter.

### CRUD operations for Phones and Models
Not supported.

### Pagination
Not supported.

### Validation of input
Partly supported. Application validates the required structure of the request, but it does not limit username 
length, for example.

### Retrieving phone specs for external service
Not implemented. There is a requirement that phone Spec has to be loaded from fonoapi service. This service is down:
[https://fonoapi.freshpixl.com](https://fonoapi.freshpixl.com).
Looks like that this service is down for last 2,5 years: [https://github.com/shakee93/fonoapi](https://github.com/shakee93/fonoapi).

I have tried to use some other service. I have found [https://developer.techspecs.io](https://developer.techspecs.io) site.
Looks like it should provide the similar functionality. But it is down too. So there is no
any client to remote service right now.

I have implemented failover mechanism for spec retrieving: app tries to load spec from remote service
and if it failed, then it uses local CSV-file. Right now, remote call always fails.

Also, there is an update mechanism for the spec. Local database are used as spec cache with soft time to live.
If there is no any spec in database for phone model, it will try to load it from remote service and failover with
local CSV-file. If spec already presents in the local database, last_updated_at field is checked. If it is outdated,
application will try to refresh data from remote service only. This refresh request executes asynchronously so 
immediate result will contain an old data.

### Swagger
I have added swagger endpoint to simplify testing. Just go to
[http://localhost:65150/swagger-ui.html](http://localhost:65150/swagger-ui.html) (use actual exposed port)

## Non-docker run
You need Gradle, JDK and Postgres service to run app without docker. I have tried it with Gradle 7 and JDK 18.
Application need JDK 17+ (it uses Java Records). There is Spring **local** profile described in application-local.yaml.
Also Gradle build file override standard bootRun task to use this **local** Spring profile. 