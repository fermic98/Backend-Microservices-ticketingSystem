package it.polito.wa2.ticket_catalogue_service.repositories

import it.polito.wa2.ticket_catalogue_service.entities.Order
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface OrderRepository: CoroutineCrudRepository<Order, Long> {

    fun findByUserId(userId: String): Flow<Order>

    //suspend fun findOrderByOrderId(userId: String, orderId: String): Mono<Order>

    suspend fun findOrderByOrderIdAndUserId(orderId: Long, userId: String): Mono<Order>

}