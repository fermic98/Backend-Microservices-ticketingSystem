package it.polito.wa2.login_service.integration_tests

import it.polito.wa2.login_service.dtos.ActivationDTO
import it.polito.wa2.login_service.dtos.RegistrationRequestDTO
import it.polito.wa2.login_service.entities.Activation
import it.polito.wa2.login_service.entities.ERole
import it.polito.wa2.login_service.entities.Role
import it.polito.wa2.login_service.entities.User
import it.polito.wa2.login_service.repositories.ActivationRepository
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
import java.util.*



@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ValidationIntegrationTests {

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
    private lateinit var activationRepository: ActivationRepository

    @Autowired
    lateinit var roleRepository: RoleRepository


    /*
    * Integration test for validation
    * ValReqTest = Validation Request Test
    * */
    @Test
    fun correctValReqTest() {
        roleRepository.save(Role(1, ERole.CUSTOMER))
        //creating user to validate his account
        val userDTO = RegistrationRequestDTO( null, "username_ValTest1", "Username@2021!", "email_ValTest1@gmail.com")
        val user = User(userDTO.nickname, userDTO.password as String, userDTO.email, null, false)
        userRepository.save(user)
        //creating provisionalID and activation code
        val activation = Activation(user)

        val activationSaved = activationRepository.save(activation)

        //Creating client validation request
        val activationRequest = ActivationDTO( activationSaved.provisionalUserId, activationSaved.activationCode)

        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(activationRequest)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/validate", request)

        Assertions.assertEquals(HttpStatus.CREATED, response.statusCode)
    }


    @Test
    fun wrongProvisionalIdValReqTest() {
        val uuid = UUID.randomUUID()
        val activationRequest = ActivationDTO( uuid, 123456)
        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(activationRequest)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/validate", request)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun codeNotMatchValReqTest() {
        roleRepository.save(Role(1, ERole.CUSTOMER))
        //creating user to validate his account
        val userDTO = RegistrationRequestDTO( null, "username_ValTest2", "Username@2021!", "email_ValTest2@gmail.com")
        val user = User(userDTO.nickname, userDTO.password as String, userDTO.email, null, false)
        userRepository.save(user)
        //creating provisionalID and activation code
        val activation = Activation(user)
        val activationSaved = activationRepository.save(activation)

        //Creating client validation request
        val activationRequest = ActivationDTO( activationSaved.provisionalUserId,123456)

        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(activationRequest)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/validate", request)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }


    @Test
    fun expiredCodeValReqTest() {
        roleRepository.save(Role(1, ERole.CUSTOMER))
        //creating user to validate his account
        val userDTO = RegistrationRequestDTO( null, "username_ValTest3", "Username@2021!", "email_ValTest3@gmail.com")
        val user = User(userDTO.nickname, userDTO.password as String, userDTO.email, null, false)
        userRepository.save(user)
        //creating provisionalID and activation code
        val activation = Activation(user)
        Activation::class.java.getDeclaredField("expirationDate").let {
            it.isAccessible = true
            it.set(activation, Date(System.currentTimeMillis() - 86400001))
        }
        val activationSaved = activationRepository.save(activation)


        //Creating client validation request
        val activationRequest = ActivationDTO( activationSaved.provisionalUserId,123456)


        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(activationRequest)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/validate", request)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}
