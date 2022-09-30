package it.polito.wa2.ticket_catalogue_service.repositories

import it.polito.wa2.ticket_catalogue_service.entities.Order
import it.polito.wa2.ticket_catalogue_service.entities.Ticket
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TicketRepository: CoroutineCrudRepository<Ticket,Long> {
    suspend fun findByName(name: String): Ticket?
}