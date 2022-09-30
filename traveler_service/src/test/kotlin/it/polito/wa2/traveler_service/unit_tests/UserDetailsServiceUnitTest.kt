package it.polito.wa2.traveler_service.unit_tests

import it.polito.wa2.traveler_service.dtos.PurchaseTicketDTO
import it.polito.wa2.traveler_service.dtos.UserDetailsDTO
import it.polito.wa2.traveler_service.exceptions.BadRequestException
import it.polito.wa2.traveler_service.exceptions.NotFoundException
import it.polito.wa2.traveler_service.services.UserDetailsService
import it.polito.wa2.traveler_service.unit_tests.unit_tests_utils.SpringTestBase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest





@SpringBootTest
class UserDetailsServiceUnitTest : SpringTestBase() {
    @Autowired
    lateinit var userDetailsService: UserDetailsService


    @Test
    fun validGetUserProfile() {
        Assertions.assertInstanceOf(UserDetailsDTO::class.java,
            userDetails.username?.let { userDetailsService.getUserProfile(it) })
    }

    @Test
    fun nullUserDetailsDTOGetUserProfile() {
        Assertions.assertThrows(NotFoundException::class.java) {
            userDetailsService.getUserProfile("nickname_not_exist")
        }
    }


    @Test
    fun validPutUserProfile() {
        val userDetailsDTO = UserDetailsDTO(
            "username2",
            "name2",
            "via test 2",
            "456",
            "02-08-2005"
        )
        Assertions.assertInstanceOf(
            UserDetailsDTO::class.java,
            userDetailsService.putUserProfile(userDetailsDTO)
        )
    }

    @Test
    fun validEmptyDatePutUserProfile() {
        val userDetailsDTO = UserDetailsDTO(
            "username2",
            "name2",
            "via test 2",
            "456",
            ""
        )
        Assertions.assertInstanceOf(
            UserDetailsDTO::class.java,
            userDetailsService.putUserProfile(userDetailsDTO)
        )
    }

    @Test
    fun validNullDatePutUserProfile() {
        val userDetailsDTO = UserDetailsDTO(
            "username2",
            "name2",
            "via test 2",
            "456"
        )
        Assertions.assertInstanceOf(
            UserDetailsDTO::class.java,
            userDetailsService.putUserProfile(userDetailsDTO)
        )
    }


    @Test
    fun validGetUserTickets() {
        Assertions.assertInstanceOf(
            List::class.java,
            userDetails.username?.let { userDetailsService.getUserTickets(it) }
        )
    }

    @Test
    fun nullUserDetailsGetUserTickets() {
        Assertions.assertThrows(NotFoundException::class.java) {
            userDetailsService.getUserTickets("nullUser")
        }
    }


    @Test
    fun nullUserDetailsPostUserTickets() {
        val purchasedTicketDTO = PurchaseTicketDTO("buy_tickets", "ordinal", "daily", "24-08-2022", 3, 360000, "ABC")
        Assertions.assertThrows(NotFoundException::class.java) {
            userDetailsService.postUserTickets("null username", purchasedTicketDTO)
        }
    }


    @Test
    fun wrongCMDPostUserTickets() {
        val purchasedTicketDTO = PurchaseTicketDTO("buy_tickets", "ordinal", "daily", "24-08-2022", 3, 360000, "ABC")
        Assertions.assertThrows(NotFoundException::class.java) {
            userDetailsService.postUserTickets("null username", purchasedTicketDTO)
        }
    }

    //admin services
    @Test
    fun validGetTravelers() {
        Assertions.assertInstanceOf(List::class.java, userDetailsService.getTravelers())
    }
}