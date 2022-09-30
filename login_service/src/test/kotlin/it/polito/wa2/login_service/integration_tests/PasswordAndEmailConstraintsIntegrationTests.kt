package it.polito.wa2.login_service.integration_tests

import it.polito.wa2.login_service.dtos.RegistrationRequestDTO
import it.polito.wa2.login_service.repositories.RoleRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PasswordAndEmailConstraintsIntegrationTests {

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:latest").apply {
            withDatabaseName("postgres")
            withUsername("postgres")
            withPassword("postgres")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }

    @LocalServerPort
    protected var port: Int = 8081

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var roleRepository: RoleRepository

    /*
    * Integration test for registration
    * RegReqTest = Registration Request Test
    *
    */

    /*
        password must be reasonably strong (
        it must not contain any whitespace,
        it must be at least 8 characters long,
        it must contain at least one digit,
        one uppercase letter,
        one lowercase letter,
        one non alphanumeric character)
    */
    @Test
    fun wrongPasswordWithWhitespaceRegReqTest() {
        val newUser = RegistrationRequestDTO(null, "username_RegTest10", "Username @2022!", "username_RegTest10@gmail.com")
        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun wrongPasswordWithLessThan8CharsLongRegReqTest() {
        val newUser = RegistrationRequestDTO(null, "username_RegTest11", "Us@22!", "username_RegTest11@gmail.com")
        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun wrongPasswordWithNoUpperCaseLetterRegReqTest() {
        val newUser = RegistrationRequestDTO(null, "username_RegTest12", "username@2022!", "username_RegTest12@gmail.com")
        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun wrongPasswordWithNoLowerCaseLetterRegReqTest() {
        val newUser = RegistrationRequestDTO(null, "username_RegTest13", "USERNAME@2022!", "username_RegTest13@gmail.com")
        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun wrongPasswordWithNoSpecialCharacterRegReqTest() {
        val newUser = RegistrationRequestDTO(null, "username_RegTest14", "Username2022", "username_RegTest14@gmail.com")
        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    // email address must be valid
    @Test
    fun invalidEmailRegReqTest() {
        val newUser = RegistrationRequestDTO(null, "username_RegTest15", "Username@2022!", "username_RegTest15gmail.com")
        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

}
