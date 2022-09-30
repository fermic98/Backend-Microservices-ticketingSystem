package it.polito.wa2.login_service.integration_tests

import it.polito.wa2.login_service.dtos.RegistrationRequestDTO
import it.polito.wa2.login_service.entities.ERole
import it.polito.wa2.login_service.entities.Role
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
class EmptyFieldsIntegrationTests {

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
    @Test
    fun correctRegReqTest() {
        roleRepository.save(Role(1, ERole.CUSTOMER))
        val newUser = RegistrationRequestDTO(null, "username_RegTest1", "Username@2022!", "username_RegTest1@gmail.com")
        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.ACCEPTED, response.statusCode)
    }

    /*
        username, password and email address cannot be empty
    */

    @Test
    fun emptyNicknameRegReqTest() {
        val newUser = RegistrationRequestDTO(null, "", "Username@2022!", "username_RegTest2@gmail.com")
        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun nullNicknameRegReqTest() {

        data class UserDTOTest(var nickname : String?,
                               var password : String?,
                               var email : String)

        val newUser = UserDTOTest(null, "Username@2022!", "username_RegTest3@gmail.com")
        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun emptyPasswordRegReqTest() {
        val newUser = RegistrationRequestDTO(null, "username_RegTest4", "", "username_RegTest4@gmail.com")

        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun nullPasswordRegReqTest() {

        val newUser = RegistrationRequestDTO(null, "username_RegTest5", null, "username_RegTest5@gmail.com")

        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    @Test
    fun emptyEmailRegReqTest() {
        val newUser = RegistrationRequestDTO(null, "username_RegTest6", "Username@2022!", "")

        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun nullEmailRegReqTest() {
        data class UserDTOTest(var nickname : String,
                               var password : String?,
                               var email : String?)

        val newUser = UserDTOTest("username_RegTest7", "Username@2022!", null)

        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }


}
