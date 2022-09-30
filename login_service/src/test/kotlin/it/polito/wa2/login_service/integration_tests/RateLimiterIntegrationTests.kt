package it.polito.wa2.login_service.integration_tests

import it.polito.wa2.login_service.dtos.ActivationDTO
import it.polito.wa2.login_service.dtos.RegistrationRequestDTO
import kotlinx.coroutines.*
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
class RateLimiterIntegrationTests {

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

    /*
   * Integration test for rateLimiter
   * */
    @Test
    fun tooManyRequestsRateLimiter(){

        val listOfUsers : MutableList<RegistrationRequestDTO> = mutableListOf()
        for(i in 0..15){
            listOfUsers.add(RegistrationRequestDTO(null,"user"+i,"Username@2022!","email"+i+"@mailfittizia.com"))
        }
        val baseUrl = "http://localhost:$port/user"
        var tooManyRequest = false

        runBlocking {

            val requests = mutableListOf<Job>()

            for (i in 0..15) {
                requests.add(
                    launch(Dispatchers.IO) {
                        val request = HttpEntity(listOfUsers.get(i))
                        val response = restTemplate.postForEntity<Unit>("$baseUrl/register", request)
                        if(response.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                            tooManyRequest = true
                        }
                    }
                )
            }

            requests.joinAll()
            Assertions.assertEquals(true, tooManyRequest)
        }
    }


    @Test
    fun tooManyValidationRequestTest() {

        val listOfUsers : MutableList<ActivationDTO> = mutableListOf()
        for(i in 0..15){
            listOfUsers.add(ActivationDTO(UUID.randomUUID(),i.toLong()))
        }
        val baseUrl = "http://localhost:$port/user"
        var tooManyRequest = false

        runBlocking {

            val requests = mutableListOf<Job>()

            for (i in 0..15) {
                requests.add(
                    launch(Dispatchers.IO) {
                        val request = HttpEntity(listOfUsers.get(i))
                        val response = restTemplate.postForEntity<Unit>("$baseUrl/validate", request)
                        if(response.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                            tooManyRequest = true
                        }
                    }
                )
            }

            requests.joinAll()
            Assertions.assertEquals(true, tooManyRequest)
        }
    }


}
