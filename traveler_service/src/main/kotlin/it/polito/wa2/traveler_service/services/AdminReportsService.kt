package it.polito.wa2.traveler_service.services

import it.polito.wa2.traveler_service.dtos.DateRangeDTO
import it.polito.wa2.traveler_service.dtos.DateTimeRangeDTO
import it.polito.wa2.traveler_service.dtos.TicketAcquiredDTO
import it.polito.wa2.traveler_service.dtos.TransitDTO

interface AdminReportsService {

    fun getTicketsAcquiredByUser(userID : String, dateRangeDTO: DateRangeDTO) :  List<TicketAcquiredDTO>
    fun getTicketsAcquired(dateRangeDTO: DateRangeDTO) :  List<TicketAcquiredDTO>
    fun getTransitsByUser(userID : String, dateTimeRangeDTO: DateTimeRangeDTO) : List<TransitDTO>
    fun getTransits(dateTimeRangeDTO: DateTimeRangeDTO) : List<TransitDTO>

}