package it.polito.wa2.traveler_service.unit_tests.unit_tests_utils

import it.polito.wa2.traveler_service.services.impl.entities.TicketAcquired
import it.polito.wa2.traveler_service.services.impl.entities.UserDetails
import it.polito.wa2.traveler_service.repositories.TicketPurchasedRepository
import it.polito.wa2.traveler_service.repositories.UserDetailsRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*


@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class SpringTestBase {

    @Autowired lateinit var userDetailsRepository: UserDetailsRepository
    @Autowired lateinit var  ticketPurchasedRepository: TicketPurchasedRepository

    protected lateinit var userDetails: UserDetails
    protected lateinit var ticketPurchased: TicketAcquired

    @BeforeAll
    fun setup(){
        ticketPurchasedRepository.deleteAll()
        userDetailsRepository.deleteAll()


        userDetails = UserDetails(
            "username1",
            "name1",
            "via test 1",
            Date(950965440000), // 2000/02/19
            "3687000432"
        )
        userDetails = userDetailsRepository.save(userDetails)

        ticketPurchased = TicketAcquired(
            Date(System.currentTimeMillis()),
            Date(1708347840000), // 2024/02/19
            Date(System.currentTimeMillis()+4200000),
            "ABC",
            "ordinal",
            "eyJhbGciOiJIUzUxMiJ9." +
                    "eyJzdWIiOiJjdXN0b21lcjEiLCJpYXQiOjE2NTI4OTE4NzksImV4cCI6MTcxNjA1MTI2Miwicm9sZXMiOlsiQ1VTVE9NRVIiXX0." +
                    "W71JOUP-TSK_j__yDz3XlWJbtO7UD3_5ZVs7BVQXg2EqKwHeW9J7d9NHpVAOVDpHtTyuuJWoBmA26jQ9wyP78g",
            userDetails
        )

        ticketPurchased = ticketPurchasedRepository.save(ticketPurchased)


    }

    @AfterAll
    fun clearDb(){
        ticketPurchasedRepository.deleteAll()
        userDetailsRepository.deleteAll()
    }


}