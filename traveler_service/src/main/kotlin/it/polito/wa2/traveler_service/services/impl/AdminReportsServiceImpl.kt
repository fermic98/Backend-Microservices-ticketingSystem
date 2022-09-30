package it.polito.wa2.traveler_service.services.impl

import it.polito.wa2.traveler_service.dtos.*
import it.polito.wa2.traveler_service.exceptions.NotFoundException
import it.polito.wa2.traveler_service.repositories.TicketPurchasedRepository
import it.polito.wa2.traveler_service.repositories.TransitRepository
import it.polito.wa2.traveler_service.repositories.UserDetailsRepository
import it.polito.wa2.traveler_service.services.AdminReportsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AdminReportsServiceImpl : AdminReportsService {

    @Autowired
    lateinit var transitRepository: TransitRepository

    @Autowired
    lateinit var ticketPurchasedRepository: TicketPurchasedRepository

    @Autowired
    lateinit var userDetailsRepository: UserDetailsRepository

    override fun getTicketsAcquiredByUser(userID: String, dateRangeDTO: DateRangeDTO): List<TicketAcquiredDTO> {
        val userDetails = userDetailsRepository.findByUsername(userID)

        if (userDetails == null)
            throw NotFoundException("Username not found")

        return ticketPurchasedRepository.getTicketsAcquiredByUser(userID, dateRangeDTO)
            .map { ticketAcquired -> ticketAcquired.toDTO() }
    }

    override fun getTicketsAcquired(dateRangeDTO: DateRangeDTO): List<TicketAcquiredDTO> {
        return ticketPurchasedRepository.getTicketsAcquired(dateRangeDTO)
            .map { ticketAcquired -> ticketAcquired.toDTO() }
    }

    override fun getTransitsByUser(userID: String, dateTimeRangeDTO: DateTimeRangeDTO): List<TransitDTO> {
        val userDetails = userDetailsRepository.findByUsername(userID)

        if (userDetails == null)
            throw NotFoundException("Username not found")

        return transitRepository.getTransitsByUser(userID, dateTimeRangeDTO).map { transit -> transit.toDTO() }
    }

    override fun getTransits(dateTimeRangeDTO: DateTimeRangeDTO): List<TransitDTO> {
        return transitRepository.getTransits(dateTimeRangeDTO).map { transit -> transit.toDTO() }
    }
}