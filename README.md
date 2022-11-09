# Kalix Workshop - Loan application - Spring
Not supported by Lightbend in any conceivable way, not open for contributions.
## Prerequisite
Java 17<br>
Apache Maven 3.6 or higher<br>
[Kalix CLI](https://docs.kalix.io/kalix/install-kalix.html) <br>
Docker 20.10.8 or higher (client and daemon)<br>
Container registry with public access (like Docker Hub)<br>
Access to the `gcr.io/kalix-public` container registry<br>
cURL<br>
IDE / editor<br>

## Create kickstart maven project

```
mvn \
archetype:generate \
-DarchetypeGroupId=io.kalix \
-DarchetypeArtifactId=kalix-spring-boot-archetype \
-DarchetypeVersion=LATEST
```
Define value for property 'groupId': `io.kx`<br>
Define value for property 'artifactId': `loan-application`<br>
Define value for property 'version' 1.0-SNAPSHOT: :<br>
Define value for property 'package' io.kx: : `io.kx.loanapp`<br>

## Import generated project in your IDE/editor

## Update main class
Move `io.kx.Main` to `io.kx` package. <br>
In `pom.xml`:
1. In `<mainClass>io.kx.Main</mainClass>` replace `io.kx.Main` with `io.kx.Main`
2. In `<dockerImage>my-docker-repo/${project.artifactId}</dockerImage>` replace `my-docker-repo` with the right `dockerId`


# Loan application service

## Define persistence (domain) 
1. Create package `io.kx.loanapp.doman`<br>
2. Create Java interface `LoanAppDomainEvent` and add Java records for events `Submitted`, `Approved`, `Declined`
3. Create `LoanAppDomainStatus` enum
4. Create file `LoanAppDomainState` with `empty`, `onSubmitted`, `onApproved` and `onDeclined` methods
<i><b>Tip</b></i>: Check content in `step-1` git branch

## Define API data structure and endpoints
1. Create package `io.kx.loanapp.api`<br>
2. Create sealed interface `LoanAppApi` and add Java records for requests and responses 
3. Create class `LoanAppService` extending `EventSourcedEntity<LoanAppDomainState>`
   1. add class level annotations `@Entity(entityKey = "loanAppId", entityType = "loanapp")` (event sourcing entity configuration)  & `@RequestMapping("/loanapp/{loanAppId}")` (path prefix)
   2. Override `emptyState` and return `LoanAppDomainState.empty()`, set loanAppId via `EventSourcedEntityContext` injected through the constructor
   3. Implement each request method and event handlers
<i><b>Tip</b></i>: Check content in `step-1` git branch
   

## Implement unit test
1. Create  `src/test/java` <br>
2. Create  `io.kx.loanapp.LoanAppServiceTest` class<br>
3. Create `happyPath`
<i><b>Tip</b></i>: Check content in `step-1` git branch

## Run unit test
```
mvn test
```
## Implement integration test
1. Edit `io.kx.loanapp.IntegrationTest` class<br>
2. 
<i><b>Tip</b></i>: Check content in `step-1` git branch

## Run integration test
```
mvn -Pit verify
```

<i><b>Note</b></i>: Integration tests uses [TestContainers](https://www.testcontainers.org/) to span integration environment so it could require some time to download required containers.
Also make sure docker is running.

## Run locally

In project root folder there is `docker-compose.yaml` for running `kalix proxy` and (optionally) `google pubsub emulator`.
<i><b>Tip</b></i>: If you do not require google pubsub emulator then comment it out in `docker-compose.yaml`
```
docker-compose up
```

Start the service:

```
mvn exec:exec
```

## Test service locally
Submit loan application:
```
curl -XPOST -d '{
  "clientId": "12345",
  "clientMonthlyIncomeCents": 60000,
  "loanAmountCents": 20000,
  "loanDurationMonths": 12
}' http://localhost:9000/loanapp/1/submit -H "Content-Type: application/json"
```

Get loan application:
```
curl -XGET http://localhost:9000/loanapp/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPOST http://localhost:9000/loanapp/1/approve -H "Content-Type: application/json"
```

## Register for Kalix account or Login with existing account
[Register](https://console.kalix.io/register)

## kalix CLI
Login (need to be logged in the Kalix Console in web browser):
```
kalix auth login
```
Create new project:
```
kalix projects new loan-application --region gcp-us-east1
```
<i><b>Note</b></i>: Replace `<REGION>` with desired region

List projects:
```
kalix projects list
```
Set project:
```
kalix config set project loan-application
```

## Package & Deploy

<i><b>Note</b></i>: Make sure you have updated `dockerImage` in your `pom.xml` and that your local docker is authenticated with your docker container registry

```
mvn deploy
```


## Expose service
```
kalix services expose loan-application
```
Result:
`
Service 'loan-application' was successfully exposed at: <some_host>.us-east1.kalix.app
`
## Test service in production
Submit loan application:
```
curl -XPOST -d '{
  "clientId": "12345",
  "clientMonthlyIncomeCents": 60000,
  "loanAmountCents": 20000,
  "loanDurationMonths": 12
}' https://<somehost>.kalix.app/loanapp/1/submit -H "Content-Type: application/json"
```
Get loan application:
```
curl -XGET https://<somehost>.kalix.app/loanapp/1 -H "Content-Type: application/json"
```
Approve:
```
curl -XPOST https://<somehost>.kalix.app/loanapp/1/approve -H "Content-Type: application/json"
```


# Loan application processing service
## Create loan application processing packages
Create package `io.kx.loanproc` in `main`, `test` and `it`<br>

## Define persistence (domain) data structure  (GRPC)
1. Create package `io.kx.loanproc.doman`<br>
2. Create Java interface `LoanProcDomainEvent` and add Java records for events `ReadyForReview`, `Approved`, `Declined`
3. Create `LoanProcDomainStatus` enum
4. Create file `LoanProcDomainState` with `empty`, `onReadyForReview`, `onApproved` and `onDeclined` methods
   <i><b>Tip</b></i>: Check content in `step-2` git branch

## Define API data structure and endpoints (GRPC)
1. Create package `io.kx.loanproc.api`<br>
2. Create sealed interface `LoanProcApi` and add Java records for requests and responses
3. Create class `LoanProcService` extending `EventSourcedEntity<LoanProcDomainState>`
   1. add class level annotations `@Entity(entityKey = "loanAppId", entityType = "loanproc")` (event sourcing entity configuration)  & `@RequestMapping("/loanproc/{loanAppId}")` (path prefix)
   2. Override `emptyState` and return `LoanProcDomainState.empty()`, set loanAppId via `EventSourcedEntityContext` injected through the constructor
   3. Implement each request method and event handlers
      <i><b>Tip</b></i>: Check content in `step-2` git branch


## Implement unit test
1. Create  `src/test/java` <br>
2. Create  `io.kx.loanproc.LoanProcServiceTest` class<br>
3. Create `happyPath`
   <i><b>Tip</b></i>: Check content in `step-2` git branch

## Run unit test
```
mvn test
```
## Implement integration test
1. Edit `io.kx.loanproc.IntegrationTest` class<br>
2.
<i><b>Tip</b></i>: Check content in `step-2` git branch

## Run integration test
```
mvn -Pit verify
```

<i><b>Note</b></i>: Integration tests uses [TestContainers](https://www.testcontainers.org/) to span integration environment so it could require some time to download required containers.
Also make sure docker is running.

## Run locally

In project root folder there is `docker-compose.yaml` for running `kalix proxy` and (optionally) `google pubsub emulator`.
<i><b>Tip</b></i>: If you do not require google pubsub emulator then comment it out in `docker-compose.yaml`
```
docker-compose up
```

Start the service:

```
mvn exec:exec
```
## Test service locally
Start processing:
```
curl -XPOST http://localhost:9000/loanproc/1/process -H "Content-Type: application/json"
```

Get loan processing:
```
curl -XGET http://localhost:9000/loanproc/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPOST -d '{"reviewerId":"9999"}' http://localhost:9000/loanproc/1/approve -H "Content-Type: application/json"
```
## Package & Deploy
```
mvn deploy
```
## Test service in production
Start processing:
```
curl -XPOST https://<somehost>.kalix.app/loanproc/1/process -H "Content-Type: application/json"
```

Get loan processing:
```
curl -XGET https://<somehost>.kalix.app/loanproc/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPOST -d '{"reviewerId":"9999"}' https://<somehost>.kalix.app/loanproc/1/approve -H "Content-Type: application/json"
```

## Create a view
1. Create package `io.kx.loanproc.view`
2. Create `io.kx.loanproc.view.LoanProcViewModel` interface with Java records with `ViewRecord`
3. Create `io.kx.loanproc.viewLoanProcByStatusView` class extending `View`
   
<i><b>Tip</b></i>: Check content in `step-3` git branch

##Unit test
Because of the nature of views only Integration tests are done.

## Create integration tests for view
Update `io.kx.loanproc.IntegrationTest` class with `webclient` query of the view endpoint
<i><b>Tip</b></i>: Check content in `step-3` git branch

## Run integration test
```
mvn -Pit verify
```
## Package & Deploy
```
mvn deploy
```
## Test service in production
```
curl -XPOST -d {"statusId":"STATUS_READY_FOR_REVIEW"} https://<somehost>.kalix.app/loanproc/views/by-status -H "Content-Type: application/json"
```

# Event driven communication
## Action for submitted event (Loan application service -> Loan application processing service)
1. Create package `io.kx.loanapp.action`
2. Create `io.kx.loanapp.LoanAppToLoanProcEventingAction` class extending `Action`
<i><b>Tip</b></i>: Check content in `step-4` git branch
## Action for approved & declined processing event (Loan application processing service -> Loan application service)
1. Create package `io.kx.loanproc.action`
2. Create `io.kx.loanproc.LoanProcToLoanAppEventingAction` class extending `Action`
<i><b>Tip</b></i>: Check content in `step-4` git branch

## Create integration tests for eventing (end-to-end test)
Update `io.kx.IntegrationTest` and add `endToEndHappyPath` test
<i><b>Tip</b></i>: Check content in `step-4` git branch
## Run integration test
```
mvn -Pit verify
```
## Package & Deploy
```
mvn deploy
```
## Test service in production

Submit loan application:
```
curl -XPOST -d '{
  "clientId": "12345",
  "clientMonthlyIncomeCents": 60000,
  "loanAmountCents": 20000,
  "loanDurationMonths": 12
}' https://<somehost>.kalix.app/loanapp/3/submit -H "Content-Type: application/json"
```
Check loan processing status:
```
curl -XPOST -d {"statusId":"STATUS_READY_FOR_REVIEW"} https://<somehost>.kalix.app/loanproc/views/by-status -H "Content-Type: application/json"
```
Approve loan processing:
```
curl -XPOST -d '{"reviewerId":"9999"}' https://<somehost>.kalix.app/loanproc/3/approve -H "Content-Type: application/json"
```
Get loan application:
```
curl -XGET https://<somehost>.kalix.app/loanapp/3 -H "Content-Type: application/json"
```