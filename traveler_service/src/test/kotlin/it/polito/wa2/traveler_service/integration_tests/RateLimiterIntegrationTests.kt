package it.polito.wa2.traveler_service.integration_tests

import it.polito.wa2.traveler_service.dtos.UserDetailsDTO
import it.polito.wa2.traveler_service.services.impl.entities.UserDetails
import it.polito.wa2.traveler_service.repositories.UserDetailsRepository
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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
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
            withDatabaseName("db_traveler")
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
    lateinit var userDetailsRepository: UserDetailsRepository

    /*
   * Integration test for rateLimiter
   * */
    @Test
    fun tooManyRequestsRateLimiter(){

        for(i in 0..15){
            val userDetailsDTO = UserDetailsDTO("customer_$i", "name", "address")
            val userDetails = UserDetails(userDetailsDTO.username, userDetailsDTO.name, userDetailsDTO.address)
            userDetailsRepository.save(userDetails)
        }

        val baseUrl = "http://localhost:$port/my/profile"
        var tooManyRequest = false



        val headers = HttpHeaders()

        headers.setBearerAuth(
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm" +
                    "9sZXMiOlsiQ1VTVE9NRVIiXX0.W71JOUP-TSK_j__yDz3XlWJbtO7UD3_5ZVs7BVQXg2EqKwHeW9J7d9NHpVAOVDpHtTyuuJWoBmA26jQ9wyP78g"
        )

        val entity = HttpEntity("", headers)


        runBlocking {

            val requests = mutableListOf<Job>()

            for (i in 0..15) {
                requests.add(
                    launch(Dispatchers.IO) {
                        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)
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
