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
Move `io.kx.loanapp.Main` to `io.kx` package. <br>
In `pom.xml`:
1. In `<mainClass>io.kx.loanapp.Main</mainClass>` replace `io.kx.loanapp.Main` with `io.kx.Main`
2. In `<dockerImage>my-docker-repo/${project.artifactId}</dockerImage>` replace `my-docker-repo` with the right `dockerId`


# Loan application service

## Define persistence (domain) data structure  (GRPC)
1. Create package `io.kx.loanapp.doman`<br>
2. Create Java interface `LoanAppDomainEvent` and add Java records for events `Submitted`, `Approved`, `Declined`
3. Create `LoanAppDomainStatus` enum
4. Create file `LoanAppDomainState` with `empty`, `onSubmitted`, `onApproved` and `onDeclined` methods
<i><b>Tip</b></i>: Check content in `step-1` git branch

## Define API data structure and endpoints (GRPC)
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
mvn compile exec:exec
```

## Test service locally
Submit loan application:
```
curl -XPOST -d '{
  "client_id": "12345",
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' http://localhost:9000/loanapp/1 -H "Content-Type: application/json"
```

Get loan application:
```
curl -XGET http://localhost:9000/loanapp/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPUT http://localhost:9000/loanapp/1/approve -H "Content-Type: application/json"
```

## Package

<i><b>Note</b></i>: Make sure you have updated `dockerImage` in your `pom.xml` and that your local docker is authenticated with your docker container registry

```
mvn package
```

<br><br>

Push docker image to docker repository:
```
mvn docker:push
```

## Register for Kalix account or Login with existing account
[Register](https://console.kalix.io/register)

## kalix CLI
Validate version:
```
kalix version
```
Login (need to be logged in the Kalix Console in web browser):
```
kalix auth login
```
Create new project:
```
kalix projects new loan-application --region <REGION>
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
## Deploy service
```
kalix service deploy loan-application my-docker-repo/loan-application:1.0-SNAPSHOT
```
<i><b>Note</b></i>: Replace `my-docker-repo` with your docker repository

List services:
```
kalix services list
```
```
NAME               AGE    REPLICAS   STATUS   DESCRIPTION   
loan-application   102s   1          Ready  
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
  "client_id": "12345",
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' https://<somehost>.kalix.app/loanapp/1 -H "Content-Type: application/json"
```
Get loan application:
```
curl -XGET https://<somehost>.kalix.app/loanapp/1 -H "Content-Type: application/json"
```
Approve:
```
curl -XPUT https://<somehost>.kalix.app/loanapp/1/approve -H "Content-Type: application/json"
```

# Loan application processing service

## Increment version
In `pom.xml` in `<version>1.0-SNAPSHOT</version>` replace `1.0-SNAPSHOT` with `1.1-SNAPSHOT`

## Define API data structure and endpoints (GRPC)
Create `io/kx/loanproc/api` folder in `src/main/proto` folder. <br>
Create `loan_proc_api.proto` in `src/main/proto/io/kx/loanproc/api` folder. <br>
Create: <br>
- state
- commands
- service

<i><b>Tip</b></i>: Check content in `step-2` git branch

## Define persistence (domain) data structure  (GRPC)
Create `io/kx/loanproc/domain` folder in `src/main/proto` folder. <br>
Create `loan_proc_domain.proto` in `src/main/proto/io/kx/loanproc/domain` folder. <br>
Create: <br>
- state
- events

<i><b>Tip</b></i>: Check content in `step-2` git branch
## Add codegen annotations in API data structure and endpoints (GRPC)
In `src/main/proto/io/kx/loanproc/api/loan_proc_api.proto` add AkkaServerless codegen annotations to GRPC service
```
service LoanProcService {
```
```
option (kalix.codegen) = {
    event_sourced_entity: {
      name: "io.kx.loanproc.domain.LoanProcEntity"
      entity_type: "loanproc"
      state: "io.kx.loanproc.domain.LoanProcDomainState"
      events: [
        "io.kx.loanproc.domain.ReadyForReview",
        "io.kx.loanproc.domain.Approved",
        "io.kx.loanproc.domain.Declined"
      ]
    }
  };
```
```
...
```
<i><b>Note</b></i>: `event_sourced_entity.name` has to be a unique name
## Compile maven project to trigger codegen
```
mvn compile
```

Compile will generate help classes (`target/generated-*` folders) and skeleton classes<br><br>
Business logic:<br>
`src/main/java/io/kx/loanproc/domain/LoanProcEntity`<br>
<br>
Unit tests:<br>
`src/test/java/io/kx/loanproc/domain/LoanProcEntityTest`<br>
Integration tests:<br>
`src/it/java/io/kx/loanproc/api/LoanProcEntityIntegrationTest`<br>

<i><b>Tip</b></i>: If required reimport project in your IDE

## Update Main class
In `src/main/java/io/kx/Main` you need to add new entity component (`LoanProcEntity`):
```
 return AkkaServerlessFactory.withComponents(LoanAppEntity::new, LoanProcEntity::new);
```
## Implement entity skeleton class
Implement `src/main/java/io/kx/loanproc/domain/LoanProcEntity` class<br>
<i><b>Tip</b></i>: Check content in `step-2` git branch

## Implement unit test
Implement `src/test/java/io/kx/loanproc/domain/LoanProcEntityTest` class<br>
<i><b>Tip</b></i>: Check content in `step-2` git branch

## Run unit test
```
mvn test
```
## Implement integration test
Implement `src/it/java/io/kx/loanproc/api/LoanProcEntityIntegrationTest` class<br>
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
mvn compile exec:exec
```

## Test service locally
Start processing:
```
curl -XPOST -d '{
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' http://localhost:9000/loanproc/1 -H "Content-Type: application/json"
```

Get loan processing:
```
curl -XGET http://localhost:9000/loanproc/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPUT http://localhost:9000/loanproc/1/approve -H "Content-Type: application/json"
```

## Package

```
mvn package
```
<br><br>

Push docker image to docker repository:
```
mvn docker:push
```
## Deploy service
```
kalix service deploy loan-application my-docker-repo/loan-application:1.1-SNAPSHOT
```
## Test service in production
Start processing:
```
curl -XPOST -d '{
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' https://<somehost>.kalix.app/loanproc/1 -H "Content-Type: application/json"
```

Get loan processing:
```
curl -XGET https://<somehost>.kalix.app/loanproc/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPUT https://<somehost>.kalix.app/loanproc/1/approve -H "Content-Type: application/json"
```
## Increment version
In `pom.xml` in `<version>1.1-SNAPSHOT</version>` replace `1.1-SNAPSHOT` with `1.2-SNAPSHOT`

## Create a view
Create `io/kx/loanproc/view` folder in `src/main/proto` folder. <br>
Create `loan_proc_by_status_view.proto` in `src/main/proto/io/kx/loanproc/view` folder. <br>
Create: <br>
- state
- request/response
- service

<i><b>Note</b></i>: `SELECT` result alias `AS results` needs to correspond with `GetLoanProcByStatusResponse` parameter name `repeated LoanProcViewState results`<br>
<i><b>Note</b></i>: Currently `enums` are not supported as query parameters ([issue 1141](https://github.com/lightbend/kalix-proxy/issues/1141)) so enum `number` value is used for query<br>
<i><b>Tip</b></i>: Check content in `step-3` git branch

## Compile maven project to trigger codegen for views
```
mvn compile
```

Compile will generate help classes (`target/generated-*` folders) and skeleton classes<br><br>

`src/main/java/io/kx/loanproc/view/LoanProcByStatusView`<br>

In `src/main/java/io/kx/Main` you need to add view (`LoanProcByStatusView`) initialization:
```
 return AkkaServerlessFactory.withComponents(LoanAppEntity::new, LoanProcEntity::new, LoanProcByStatusView::new);
```

## Implement view LoanProcByStatusView skeleton class
Implement `src/main/java/io/kx/loanproc/view/LoanProcByStatusView` class<br>
<i><b>Tip</b></i>: Check content in `step-3` git branch

##Unit test

Because of the nature of views only Integration tests are done.

## Create integration tests for view
1. Copy `io/kx/loanproc/view/LoanProcEntityIntegrationTest` class to `io/kx/loanproc/view/LoanProcViewIntegrationTest`
2. Remove all methods annotated with `@Test`
3. Add test case
```
@Test
public void viewTest() throws Exception {
...  
```
<i><b>Tip</b></i>: Check content in `step-3` git branch

## Run integration test
```
mvn verify -Pit
```

<i><b>Note</b></i>: Integration tests uses [TestContainers](https://www.testcontainers.org/) to span integration environment so it could require some time to download required containers.
Also make sure docker is running.


## Package

```
mvn package
```
<br><br>

Push docker image to docker repository:
```
mvn docker:push
```
## Deploy service
```
kalix service deploy loan-application my-docker-repo/loan-application:1.2-SNAPSHOT
```

## Test service in production
Get loan processing by status:
```
curl -XPOST -d {"status_id":2} https://<somehost>.kalix.app/loanproc/views/by-status -H "Content-Type: application/json"
```

# Event driven communication

## Increment version
In `pom.xml` in `<version>1.2-SNAPSHOT</version>` replace `1.3-SNAPSHOT` with `1.3-SNAPSHOT`

## Action for submitted event (Loan application service -> Loan application processing service)
Create `io/kx/loanapp/action` folder in `src/main/proto` folder. <br>
Create `loan_app_eventing_to_proc_action.proto` in `src/main/proto/io/kx/loanapp/action` folder. <br>
Create: <br>
- service

<i><b>Tip</b></i>: Check content in `step-4` git branch

## Action for approved & declined processing event (Loan application processing service -> Loan application service)
Create `io/kx/loanproc/action` folder in `src/main/proto` folder. <br>
Create `loan_proc_eventing_to_app_action.proto` in `src/main/proto/io/kx/loanproc/action` folder. <br>
Create: <br>
- service

<i><b>Tip</b></i>: Check content in `step-4` git branch

## Compile maven project to trigger codegen for action
```
mvn compile
```
Compile will generate help classes (`target/generated-*` folders) and skeleton classes<br><br>

`src/main/java/io/kx/loanapp/action/LoanAppEventingToProcAction`<br>
`src/main/java/io/kx/loanproc/action/LoanProcEventingToAppAction`<br>

In `src/main/java/io/kx/Main` you need to add view (`LoanAppEventingToProcAction` & `LoanProcEventingToAppAction`) initialization:
```
 return AkkaServerlessFactory.withComponents(LoanAppEntity::new, LoanProcEntity::new, LoanAppEventingToProcAction::new, LoanProcByStatusView::new, LoanProcEventingToAppAction::new);
```
## Implement view LoanAppEventingToProcAction skeleton class
Implement `src/main/java/io/kx/loanapp/action/LoanAppEventingToProcAction` class<br>
<i><b>Tip</b></i>: Check content in `step-4` git branch

## Implement view LoanProcEventingToAppAction skeleton class
Implement `src/main/java/io/kx/loanproc/action/LoanProcEventingToAppAction` class<br>
<i><b>Tip</b></i>: Check content in `step-4` git branch

## System integration tests (multiple services)
In `src/it/java/io/kx` folder create new class `SystemIntegrationTest`.
<i><b>Tip</b></i>: Check content in `step-4` git branch

## Run integration test
```
mvn verify -Pit
```

<i><b>Note</b></i>: Integration tests uses [TestContainers](https://www.testcontainers.org/) to span integration environment so it could require some time to download required containers.
Also make sure docker is running.

## Package

```
mvn package
```

<br><br>

Push docker image to docker repository:
```
mvn docker:push
```
## Deploy service
```
kalix service deploy loan-application my-docker-repo/loan-application:1.3-SNAPSHOT
```
<i><b>Note</b></i>: Replace `my-docker-repo` with your docker repository

## Test service in production
Submit loan application:
```
curl -XPOST -d '{
  "client_id": "123456",
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' https://<somehost>.kalix.app/loanapp/2 -H "Content-Type: application/json"
```
Approve loan processing:
```
curl -XPUT -d '{
"reviewer_id": "9999"
}' https://<somehost>.kalix.app/loanproc/2/approve -H "Content-Type: application/json"
```
Get loan application :
```
curl -XGET https://<somehost>.kalix.app/loanapp/2 -H "Content-Type: application/json"
```