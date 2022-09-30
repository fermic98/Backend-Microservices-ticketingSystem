package it.polito.wa2.ticket_catalogue_service.repositories

import it.polito.wa2.ticket_catalogue_service.entities.PaymentInfo
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TemporaryPaymentRepository: CoroutineCrudRepository<PaymentInfo,Long> {
    suspend fun deleteByOrderId(orderId: Long)
    suspend fun findByOrderId(orderId: Long): PaymentInfo
}