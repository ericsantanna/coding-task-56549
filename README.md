# coding-task-56549

### Requirements:
- Java 11 or higher
- Maven

### Running test (both unit and integration tests):
`mvn test`

### Executing:
`mvn spring-boot:run`

### Manual checking:

Execute with `mvn spring-boot:run`

#### Endpoints:
POST http://localhost:8080/data-snapshots/upload  
GET http://localhost:8080/data-snapshots/<primary_key>  
DELETE http://localhost:8080/data-snapshots/<primary_key>  

#### Or navigate to src/test/resources and execute the scripts:
- `bash upload.sample.100.sh`
- `bash get.by-primaryKey.sh <primary_key>`
- `bash delete.by-primaryKey.sh <primary_key>`

#### You can check the database console by accessing on the browser: http://localhost:8080/h2-console  
User: sa  
Password: sa  

- Click on the table name "DATA_SNAPSHOP", it should complete the console with a query
- Click in "Run"

