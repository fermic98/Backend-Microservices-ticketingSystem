package it.polito.wa2.traveler_service.integration_tests

import it.polito.wa2.traveler_service.dtos.UserDetailsDTO
import it.polito.wa2.traveler_service.services.impl.entities.UserDetails
import it.polito.wa2.traveler_service.repositories.UserDetailsRepository
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
class AdminIntegrationTests {

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




    /**   5) GET /admin/travelers*/
    @Test
    fun validGetMyAdminTravelersTest() {
        val userDetailsDTO = UserDetailsDTO("customer21", "name", "address")
        val userDetails = UserDetails(userDetailsDTO.username, userDetailsDTO.name, userDetailsDTO.address)

        val userDetailsDTO2 = UserDetailsDTO("customer22", "name2", "address2")
        val userDetails2 = UserDetails(userDetailsDTO2.username, userDetailsDTO2.name, userDetailsDTO2.address)

        userDetailsRepository.save(userDetails)
        userDetailsRepository.save(userDetails2)

        val baseUrl = "http://localhost:$port/admin/travelers"

        val headers = HttpHeaders()

        headers.setBearerAuth(
                "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjIwIiwiaWF0IjoxNjUyODkxODc5LCJleHAiOjE3MTYwNTEyNjIsInJvbGVzI" +
                        "jpbIkNVU1RPTUVSIiwiQURNSU4iXX0.cJ9OjS-ojAz46YUbWQw5vrj4mQ_QQ1kv7UAuG3JZ7e5tahtm0z39ruFfnmKUmUmHn4d11NrHPYso0AKsuPPY_Q"
        )

        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(true, (response.body?.toString()!!.contains("customer22") && response.body?.toString()!!.contains("customer21")))

    }


    @Test
    fun unauthorizedGetMyAdminTravelersTest() {

        val baseUrl = "http://localhost:$port/admin/travelers"

        val headers = HttpHeaders()

        /*doesn't have role ADMIN*/
        headers.setBearerAuth(
                "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjIwIiwiaWF0IjoxNjUyODkxODc5LCJleHAiOjE3MTYwNTEyNjIsInJvbGVzI" +
                        "jpbIkNVU1RPTUVSIl19.wFmHAikBsfAxXas3Cnn_D0-2FRU629vhVXPdEpmBIj_EmE_EsJKcpNXk0ikXyELoI7zeK8eqrgJSOLq_6p1vkw"
        )

        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN,response.statusCode)

    }




    /**   6) GET /admin/traveler/{userID}/profile **/
    @Test
    fun validGetTravelerProfileTest() {
        val userDetailsDTO = UserDetailsDTO("customer1", "name", "address")
        val userDetails = UserDetails(userDetailsDTO.username, userDetailsDTO.name, userDetailsDTO.address)
        userDetailsRepository.save(userDetails)

        val userId = userDetails.username

        val baseUrl = "http://localhost:$port/admin/traveler/$userId/profile"
        println(baseUrl)
        val headers = HttpHeaders()

        headers.setBearerAuth(
                "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm9sZXMiOlsiQURNSU4iXX0" +
                        ".JTOH6UvT5w5pkeI-Z5QABgOKldYyPHE84ydyc0BRFc0rq4SGo-cE4_yULmIVgUdxU33TO1Dgl5olmfeW6Kn1Eg"
        )


        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)

    }

    @Test
    fun invalidPermissionsGetTravelerProfileTest() {
        val userDetailsDTO = UserDetailsDTO("customer1", "name", "address")
        val userDetails = UserDetails(userDetailsDTO.username, userDetailsDTO.name, userDetailsDTO.address)
        userDetailsRepository.save(userDetails)

        val userId = userDetails.username

        val baseUrl = "http://localhost:$port/admin/traveler/$userId/profile"
        println(baseUrl)
        val headers = HttpHeaders()

        headers.setBearerAuth(
                "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm" +
                        "9sZXMiOlsiQ1VTVE9NRVIiXX0.W71JOUP-TSK_j__yDz3XlWJbtO7UD3_5ZVs7BVQXg2EqKwHeW9J7d9NHpVAOVDpHtTyuuJWoBmA26jQ9wyP78g"
        )

        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)

    }

    @Test
    fun noUserDetailsGetTravelerProfileTest() {
        val userId = "notFound"

        val baseUrl = "http://localhost:$port/admin/traveler/$userId/profile"
        println(baseUrl)
        val headers = HttpHeaders()

        headers.setBearerAuth(
                "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm9sZXMiOlsiQURNSU4iXX0" +
                        ".JTOH6UvT5w5pkeI-Z5QABgOKldYyPHE84ydyc0BRFc0rq4SGo-cE4_yULmIVgUdxU33TO1Dgl5olmfeW6Kn1Eg"
        )


        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)

    }





    /**   7) GET /admin/traveler/{userID}/tickets
         */
    @Test
    fun unauthorizedAdminGetUserTicketsTest() {

        val baseUrl = "http://localhost:$port/admin/traveler/customer21/tickets"

        val headers = HttpHeaders()

        /*doesn't have role ADMIN*/
        headers.setBearerAuth(
                "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjIwIiwiaWF0IjoxNjUyODkxODc5LCJleHAiOjE3MTYwNTEyNjIsInJvbGVzI" +
                        "jpbIkNVU1RPTUVSIl19.wFmHAikBsfAxXas3Cnn_D0-2FRU629vhVXPdEpmBIj_EmE_EsJKcpNXk0ikXyELoI7zeK8eqrgJSOLq_6p1vkw"
        )

        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN,response.statusCode)

    }


    @Test
    fun validAdminGetUserTicketsTest() {
        val userDetailsDTO = UserDetailsDTO("customer23", "name23", "address")
        val userDetails = UserDetails(userDetailsDTO.username, userDetailsDTO.name, userDetailsDTO.address)
        userDetailsRepository.save(userDetails)

        val baseUrl = "http://localhost:$port/admin/traveler/customer23/tickets"

        val headers = HttpHeaders()

        headers.setBearerAuth(
                "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjIwIiwiaWF0IjoxNjUyODkxODc5LCJleHAiOjE3MTYwNTEyNjIsInJvbGVzI" +
                        "jpbIkNVU1RPTUVSIiwiQURNSU4iXX0.cJ9OjS-ojAz46YUbWQw5vrj4mQ_QQ1kv7UAuG3JZ7e5tahtm0z39ruFfnmKUmUmHn4d11NrHPYso0AKsuPPY_Q"
        )

        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals("[]", response.body.toString())

    }


    @Test
    fun adminGetUserTicketsWithNoUserTest() {

        val baseUrl = "http://localhost:$port/admin/traveler/customer24/tickets"

        val headers = HttpHeaders()

        headers.setBearerAuth(
                "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjIwIiwiaWF0IjoxNjUyODkxODc5LCJleHAiOjE3MTYwNTEyNjIsInJvbGVzI" +
                        "jpbIkNVU1RPTUVSIiwiQURNSU4iXX0.cJ9OjS-ojAz46YUbWQw5vrj4mQ_QQ1kv7UAuG3JZ7e5tahtm0z39ruFfnmKUmUmHn4d11NrHPYso0AKsuPPY_Q"
        )

        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)

    }

}