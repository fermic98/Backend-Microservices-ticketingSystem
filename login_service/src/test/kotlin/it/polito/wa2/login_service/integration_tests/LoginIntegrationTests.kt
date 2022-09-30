package it.polito.wa2.login_service.integration_tests

import it.polito.wa2.login_service.dtos.LoginRequestDTO
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
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LoginIntegrationTests {
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
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun wrongPasswordLogin(){
        roleRepository.save(Role(1, ERole.CUSTOMER))
        //creating user to validate his account
        val userDTO = RegistrationRequestDTO( null, "username_ValTestLogin1", "Username@2021!", "email_ValTestLogin1@gmail.com")
        val user = User(userDTO.nickname, userDTO.password as String, userDTO.email, null, true)

        //password is not encrypted before to be stored, so it's wrong
        userRepository.save(user)

        val loginRequestDTO = LoginRequestDTO(user.nickname,user.password)


        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(loginRequestDTO)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/login", request)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun rightCredentialsButAccountNotActiveLogin(){
        roleRepository.save(Role(1, ERole.CUSTOMER))
        //creating user to validate his account
        val userDTO = RegistrationRequestDTO( null, "username_ValTestLogin2", "Username@2021!", "email_ValTestLogin2@gmail.com")
        val user = User(userDTO.nickname,passwordEncoder.encode( userDTO.password as String ), userDTO.email, null, false)

        //password is not encrypted before to be stored, so it's wrong
        userRepository.save(user)

        val loginRequestDTO = LoginRequestDTO(user.nickname,userDTO.password as String)


        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(loginRequestDTO)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/login", request)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun correctLogin(){
        roleRepository.save(Role(1, ERole.CUSTOMER))
        //creating user to validate his account
        val userDTO = RegistrationRequestDTO( null, "username_ValTestLogin3", "Username@2021!", "email_ValTestLogin3@gmail.com")
        val user = User(userDTO.nickname,passwordEncoder.encode( userDTO.password as String ), userDTO.email, null, true)

        //password is not encrypted before to be stored, so it's wrong
        userRepository.save(user)

        val loginRequestDTO = LoginRequestDTO(user.nickname,userDTO.password as String)


        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(loginRequestDTO)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/login", request)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
    }


    @Test
    fun correctLogin2(){
        roleRepository.save(Role(1, ERole.CUSTOMER))
        //creating user to validate his account
        val userDTO = RegistrationRequestDTO( null, "username_ValTestLogin4", "Username@2021!", "email_ValTestLogin4@gmail.com")
        val user = User(userDTO.nickname,passwordEncoder.encode( userDTO.password as String ), userDTO.email, null, true)

        //password is not encrypted before to be stored, so it's wrong
        userRepository.save(user)

        val loginRequestDTO = LoginRequestDTO(user.nickname,userDTO.password as String)


        val baseUrl = "http://localhost:$port/user"
        val request = HttpEntity(loginRequestDTO)
        val response = restTemplate.postForEntity<Unit>("$baseUrl/login", request)

        val x = response.headers.toList().map { it.first }


        Assertions.assertEquals(x.contains("Authorization"),true)
    }

}