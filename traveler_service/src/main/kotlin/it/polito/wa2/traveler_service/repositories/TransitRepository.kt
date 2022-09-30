package it.polito.wa2.traveler_service.repositories

import it.polito.wa2.traveler_service.dtos.DateRangeDTO
import it.polito.wa2.traveler_service.dtos.DateTimeRangeDTO
import it.polito.wa2.traveler_service.services.impl.entities.TicketAcquired
import it.polito.wa2.traveler_service.services.impl.entities.Transit
import it.polito.wa2.traveler_service.services.impl.entities.UserDetails
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface TransitRepository : CrudRepository<Transit, Long> {

    @Query("select t from Transit t where t.userDetails.username = :#{#username} " +
            "and t.timestamp >= :#{#dateRange.from} and t.timestamp <= :#{#dateRange.to}")
    @Transactional(readOnly = true)
    fun getTransitsByUser(@Param("username") username : String, @Param("dateRange") dateTimeRangeDTO: DateTimeRangeDTO): List<Transit>

    @Query("select t from Transit t where " +
            "t.timestamp >= :#{#dateRange.from} and t.timestamp <= :#{#dateRange.to}")
    @Transactional(readOnly = true)
    fun getTransits(@Param("dateRange") dateTimeRangeDTO: DateTimeRangeDTO ): List<Transit>
}