package it.polito.wa2.traveler_service.integration_tests

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
class AuthenticationIntegrationTests {

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


    @Test
    fun wrongKeyJwtTest() {
        val baseUrl = "http://localhost:$port/my/profile"

        val headers = HttpHeaders()

        // JWT key = "wrong"
        headers.setBearerAuth(
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm9sZXMiOlsiQ1VTVE9N" +
                    "RVIiXX0.2acWHnDKsstQpwAY5GBPkGuy5Vu0eJN8nun1vAdeWKUQvrCB1GLJy6UwItz3A2drVeQAlE6x9KXjhE0_3C5sPw"
        )


        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)

    }

    @Test
    fun timeExpiredJwtTest() {
        val baseUrl = "http://localhost:$port/my/profile"

        val headers = HttpHeaders()

        /*
                {
          "sub": "customer1",
          "iat": 1621356862,
          "exp": 1716051262,
          "roles": [
            "CUSTOMER"
          ]
        }
         */
        headers.setBearerAuth(
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2MjEzNTY4NjIsImV4cCI6MTcxNjA1MTI2Miwicm9sZXMiOlsiQ1VTVE9NRVIiXX0.E" +
                    "X4wlAWdJuu7KV-FtuSq5PkzvZSdY3pyEoeMGrxJ4_mVcterbw4KQbR2a5CuilailQyqRYtQe_XCTem3b6ZP_A"
        )


        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)

    }

    @Test
    fun wrongSubJwtTest() {
        val baseUrl = "http://localhost:$port/my/profile"

        val headers = HttpHeaders()

        /*
                {
          "sub": "",
          "iat": 1621356862,
          "exp": 1716051262,
          "roles": [
            "CUSTOMER"
          ]
        }
         */
        headers.setBearerAuth(
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIiLCJpYXQiOjE2MjEzNTY4NjIsImV4cCI6MTcxNjA1MTI2Miwicm9sZXMiOlsiQ1VTVE9NRVIiXX0" +
                    ".UrHUX6DRD6o3kCV2Us5eqUPghF2Nf27fi9IXyh-0mc7rlaNwVu1RfRYzyah8_uVFSULDdJNAJoewUlyxzVMVFg"
        )


        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)

    }

    @Test
    fun emptyRolesJwtTest() {
        val baseUrl = "http://localhost:$port/my/profile"

        val headers = HttpHeaders()

        /*
                {
          "sub": "customer1",
          "iat": 1621356862,
          "exp": 1716051262,
          "roles": [

          ]
        }
         */
        headers.setBearerAuth(
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2MjEzNTY4NjIsImV4cCI6MTcxNjA1MTI2Miwicm9sZXMiOltdf" +
                    "Q.vtrsIzMvJx7XG9PoSEQHyFrUF0YS_hkjDYY24ZymbVmEGduJGMNk8L6_6gCJJCiPreCPOFk0_EfjDpMZ20ro8Q"
        )


        val entity = HttpEntity("", headers)
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)

    }

    @Test
    fun absentHeaderJwtTest() {
        val baseUrl = "http://localhost:$port/my/profile"
        val entity = HttpEntity("")
        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }


    @Test
    fun noBearerHeaderJwtTest() {
        val baseUrl = "http://localhost:$port/my/profile"

        val headers = HttpHeaders()

        val entity = HttpEntity("", headers)

        headers.setBasicAuth("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm" +
                "9sZXMiOlsiQ1VTVE9NRVIiXX0.W71JOUP-TSK_j__yDz3XlWJbtO7UD3_5ZVs7BVQXg2EqKwHeW9J7d9NHpVAOVDpHtTyuuJWoBmA26jQ9wyP78g")

        val response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }
}