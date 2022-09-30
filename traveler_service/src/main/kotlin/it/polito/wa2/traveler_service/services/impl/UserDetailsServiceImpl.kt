package it.polito.wa2.traveler_service.services.impl

import it.polito.wa2.traveler_service.dtos.*
import it.polito.wa2.traveler_service.services.impl.entities.UserDetails
import it.polito.wa2.traveler_service.repositories.UserDetailsRepository
import it.polito.wa2.traveler_service.services.UserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import it.polito.wa2.traveler_service.exceptions.NotFoundException
import java.util.*
import it.polito.wa2.traveler_service.services.impl.entities.TicketAcquired
import it.polito.wa2.traveler_service.exceptions.BadRequestException
import it.polito.wa2.traveler_service.repositories.TicketPurchasedRepository
import it.polito.wa2.traveler_service.security.JwtUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


@Service
@Transactional
class UserDetailsServiceImpl : UserDetailsService {

    @Autowired
    lateinit var userDetailsRepository: UserDetailsRepository

    @Autowired
    lateinit var ticketPurchasedRepository: TicketPurchasedRepository


    @Value("\${application.jwt.jwtExpirationMs}")
    val ticketExpirationMs: Long = -1

    override fun getUserProfile(username: String): UserDetailsDTO {
        val userDetailsDTO = userDetailsRepository.findByUsername(username)?.toDTO()

        if (userDetailsDTO == null) {
            throw NotFoundException("Username not found")
        }

        return userDetailsDTO
    }

    override fun putUserProfile(userDetailsDTO: UserDetailsDTO): UserDetailsDTO {
        try {

            val formatter = SimpleDateFormat("dd-MM-yyyy")
            var date: Date? = null

            if (userDetailsDTO.date_of_birth != null && userDetailsDTO.date_of_birth != "")
                date = formatter.parse(userDetailsDTO.date_of_birth)

            val userDetailsEntity = UserDetails(
                userDetailsDTO.username,
                userDetailsDTO.name,
                userDetailsDTO.address,
                date,
                userDetailsDTO.telephone_number
            )

            return userDetailsRepository.save(userDetailsEntity).toDTO()
        } catch (ex: Exception) {
            throw BadRequestException("Problems during insertion of User Details")
        }

    }

    override fun getUserTickets(username: String): List<TicketAcquiredDTO> {
        val userDetails = userDetailsRepository.findByUsername(username)

        if (userDetails == null)
            throw NotFoundException("Username not found")

        return ticketPurchasedRepository.findAllByUserDetailsUsername(username).map { it -> it.toDTO() }

    }

    override fun getTicketById(ticketId: Long, username: String): TicketAcquiredDTO {
        val ticket = ticketPurchasedRepository.findTicketAcquiredById(ticketId).toDTO()
        println(ticket.sub)
        if (ticket == null || !ticket.sub.equals(username)) {
            throw NotFoundException("Ticket doesn't exist for this user")
        }
        return ticket
    }


    override fun getTravelers(): List<String> {
        return userDetailsRepository.findUsernames()
    }


}


