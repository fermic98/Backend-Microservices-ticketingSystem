package it.polito.wa2.traveler_service.repositories

import it.polito.wa2.traveler_service.dtos.DateRangeDTO
import it.polito.wa2.traveler_service.services.impl.entities.TicketAcquired
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface TicketPurchasedRepository : CrudRepository<TicketAcquired, Long> {

    @Transactional(readOnly = true)
    fun findAllByUserDetailsUsername(userDetailsUsername : String) : List<TicketAcquired>

    @Transactional(readOnly = true)
    fun findTicketAcquiredById(ticketId : Long) : TicketAcquired

    @Query("select ad from TicketAcquired ad where ad.userDetails.username = :#{#username} " +
            "and ad.issuedAt >= :#{#dateRange.from} and ad.issuedAt <= :#{#dateRange.to}")
    @Transactional(readOnly = true)
    fun getTicketsAcquiredByUser(@Param("username") username : String, @Param("dateRange") dateRangeDTO: DateRangeDTO ): List<TicketAcquired>


    @Query("select ad from TicketAcquired ad where" +
            " ad.issuedAt >= :#{#dateRange.from} and ad.issuedAt <= :#{#dateRange.to}")
    @Transactional(readOnly = true)
    fun getTicketsAcquired(@Param("dateRange") dateRangeDTO: DateRangeDTO ): List<TicketAcquired>

}