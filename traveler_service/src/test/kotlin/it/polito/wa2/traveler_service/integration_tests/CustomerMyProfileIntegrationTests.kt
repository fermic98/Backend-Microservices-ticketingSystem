package it.polito.wa2.traveler_service.integration_tests

import it.polito.wa2.traveler_service.dtos.UserDetailsDTO
import it.polito.wa2.traveler_service.services.impl.entities.UserDetails
import it.polito.wa2.traveler_service.repositories.TicketPurchasedRepository
import it.polito.wa2.traveler_service.repositories.UserDetailsRepository
import it.polito.wa2.traveler_service.security.JwtUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
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


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerMyProfileIntegrationTests {

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
    lateinit var jwtUtils: JwtUtils

    @Autowired
    lateinit var userDetailsRepository: UserDetailsRepository

    @Autowired
    lateinit var ticketPurchasedRepository: TicketPurchasedRepository


    /** GET /my/profile  **/

    @Test
    fun validGetMyProfileTest() {
        val userDetailsDTO = UserDetailsDTO("customer1", "name", "address")
        val userDetails = UserDetails(userDetailsDTO.username, userDetailsDTO.name, userDetailsDTO.address)
        userDetailsRepository.save(userDetails)

        val baseUrl = "http://localhost:$port/my/profile"

        val headers = HttpHeaders()

        headers.setBearerAuth(
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm" +
                    "9sZXMiOlsiQ1VTVE9NRVIiXX0.W71JOUP-TSK_j__yDz3XlWJbtO7UD3_5ZVs7BVQXg2EqKwHeW9J7d9NHpVAOVDpHtTyuuJWoBmA26jQ9wyP78g"
        )

        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)

    }

    @Test
    fun noDataForUserdetailsGetMyProfileTest() {
        val baseUrl = "http://localhost:$port/my/profile"

        val headers = HttpHeaders()

        headers.setBearerAuth(
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjIiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm9sZXMiO" +
                    "lsiQ1VTVE9NRVIiXX0.oBF7ct8jLBfpMfe6oev6utrcborJL8RCwxJLt_GbpwMyvad_o-qdy_3UuilCWqyTU_gO-HKyzL9DstpU0eT3Fg"
        )

        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)

    }


    /** 2) PUT /my/profile **/

    @Test
    fun validPutMyProfileTest() {

        val userDetailsDTO = UserDetailsDTO(null, "name", "address", "3774632969", "29-02-2020")

        val baseUrl = "http://localhost:$port/my/profile"

        val headers = HttpHeaders()

        headers.setBearerAuth(
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm" +
                    "9sZXMiOlsiQ1VTVE9NRVIiXX0.W71JOUP-TSK_j__yDz3XlWJbtO7UD3_5ZVs7BVQXg2EqKwHeW9J7d9NHpVAOVDpHtTyuuJWoBmA26jQ9wyP78g"
        )
        val entity = HttpEntity(userDetailsDTO, headers)

        val response = restTemplate.exchange(baseUrl, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)

    }


    @Test
    fun invalidLeapYearPutMyProfileTest() {

        val userDetailsDTO = UserDetailsDTO(null, "name", "address", "3774632969", "29-02-1998")

        val baseUrl = "http://localhost:$port/my/profile"

        val headers = HttpHeaders()

        headers.setBearerAuth(
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm" +
                    "9sZXMiOlsiQ1VTVE9NRVIiXX0.W71JOUP-TSK_j__yDz3XlWJbtO7UD3_5ZVs7BVQXg2EqKwHeW9J7d9NHpVAOVDpHtTyuuJWoBmA26jQ9wyP78g"
        )
        val entity = HttpEntity(userDetailsDTO, headers)

        val response = restTemplate.exchange(baseUrl, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

    }

    @Test
    fun validEmptyDtoPutMyProfileTest() {

        val userDetailsDTO = UserDetailsDTO(null, null, null, null, null)

        val baseUrl = "http://localhost:$port/my/profile"

        val headers = HttpHeaders()

        headers.setBearerAuth(
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm" +
                    "9sZXMiOlsiQ1VTVE9NRVIiXX0.W71JOUP-TSK_j__yDz3XlWJbtO7UD3_5ZVs7BVQXg2EqKwHeW9J7d9NHpVAOVDpHtTyuuJWoBmA26jQ9wyP78g"
        )
        val entity = HttpEntity(userDetailsDTO, headers)

        val response = restTemplate.exchange(baseUrl, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)

    }

    @Test
    fun invalidDataFormatPutMyProfileTest() {

        val userDetailsDTO = UserDetailsDTO(null, "name", "address", "3774632969", "04/04/1998")

        val baseUrl = "http://localhost:$port/my/profile"

        val headers = HttpHeaders()

        headers.setBearerAuth(
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm" +
                    "9sZXMiOlsiQ1VTVE9NRVIiXX0.W71JOUP-TSK_j__yDz3XlWJbtO7UD3_5ZVs7BVQXg2EqKwHeW9J7d9NHpVAOVDpHtTyuuJWoBmA26jQ9wyP78g"
        )
        val entity = HttpEntity(userDetailsDTO, headers)

        val response = restTemplate.exchange(baseUrl, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

    }

}