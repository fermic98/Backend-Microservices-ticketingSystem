package it.polito.wa2.login_service.integration_tests

import it.polito.wa2.login_service.dtos.RegistrationRequestDTO
import it.polito.wa2.login_service.entities.ERole
import it.polito.wa2.login_service.entities.Role
import it.polito.wa2.login_service.entities.User
import it.polito.wa2.login_service.repositories.RoleRepository
import it.polito.wa2.login_service.repositories.UserRepository
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
class UniqueSystemWideFieldsIntegrationTests {

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
    protected var port: Int = 8080

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var roleRepository: RoleRepository


    /*
    * Integration test for registration
    * RegReqTest = Registration Request Test
    *
    */

    /*
        username and email address must be unique system-wide
    */
    @Test
    fun notUniqueUsernameRegReqTest() {
        roleRepository.save(Role(1, ERole.CUSTOMER))
        val baseUrl = "http://localhost:$port/user"
        //Inserting a new user in DB
        val user = User("username_RegTest8", "Username@2022!", "username_RegTest8@gmail.com", null, false)
        userRepository.save(user)

        //Inserting a new user in DB with same username of the previous inserted user
        val newUser = RegistrationRequestDTO(null, "username_RegTest8", "Username@2022!", "username_RegTest5B@gmail.com")


        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun notUniqueEmailRegReqTest() {
        roleRepository.save(Role(1, ERole.CUSTOMER))
        //Inserting a new user in DB
        val user = User("username_RegTest9", "Username@2022!", "username_RegTest9@gmail.com", null, false)
        userRepository.save(user)

        //Inserting a new user in DB with same email of the previous inserted user
        val newUser = RegistrationRequestDTO(null, "username_RegTest6B", "Username@2022!", "username_RegTest9@gmail.com")

        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(newUser)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

}
