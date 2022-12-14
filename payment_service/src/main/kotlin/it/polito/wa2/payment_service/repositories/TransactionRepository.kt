package it.polito.wa2.payment_service.repositories

import it.polito.wa2.payment_service.entities.Transaction
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository: CoroutineCrudRepository<Transaction,Long> {

    fun findTransactionsByCustomer(customer: String): Flow<Transaction>

    suspend fun findTransactionsByOrderId(orderId: Long): Transaction?
}