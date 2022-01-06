### Project Index 

1. Exceptions:
   1. Global
   2. Custom
   3. Importation/validation of properties:
      1. @PropertySource
      2. @ConfigurationProperties:
         1. Automatic importation from PropertiesFile to Class-Instances-variables
            1. Do not need "@Value" annotation
      3. javax.validation.constraints
   

2. ModelMapper (DTOs)
   

3. Tests:
   1. Reactive RestAssured (resources)
   2. Utilities Classes:
      1. TestDbUtils
      2. TestUtils:
         1. globalBeforeAll
         2. globalBeforeEach
         3. Test-Messages-Banners
      3. RestAssured-Specs-Utils
      4. Blockhound-Utils
   3. Databuilders
   4. Annotations:
      1. Custom:
         1. Repo + Mongo
         2. Global
         3. Resource
      2. @Import
   5. Contracts:
      1. JsonSchemaValidator
   6. StepVerifier (repos + services):
      2. Deep check:
         1. ExpectNextMatches
      3. Simple check:
         1. ExpectNextCount
   7. Test Strategy:
      1. applicationLog.properties:
         1. ColorFul TestBanner
      2. enabledTest
      3. State:
         1. Isolated:
            1. BeforeEach
         2. Shared:
            1. BeforeAll
   8. MultiThread/Parallel Test
      1. Aborted: Because server-costs in CI/CD   

4. Docker:
   1. Compose
   2. Dockerfile
   3. Bat Scripts
   

5. Testcontainers
   * Containers
   * compose
   

6. Maven
   * POM Banners Organization


7. MongoDB Strategy:
   1. Services:
      1. Embed Objects 
      2. Referencing
      3. "Assemble" full objects


8. Custom Spring Logging
   1. Pattern.console


8. Reactive Queries
   1. Repository Layer
      1. Spring Data Queries
         1. Method-Named:
            1. Simple
            2. Relationships
         2. Ex:
            1. findPostsByAuthor_Id
   2. Service Layer
      1. Chained Filter:
         1. Ex:
            1. ... findAll().filter(comment -> ...
      

9. Architectural Strategy:
   * SOLID
   * Screaming Architecture
   * CDC: Contract driven development
   * Testability:
      * TDD/CDC: Controllers
      * Confirmation: Repo + Service


10. Bean Validation + Exceptions:
    1. Annotations - javax.validation.constraints:
       1. @NotEmpty
       2. @NotNull
       3. @NotBlank
       4. @Min