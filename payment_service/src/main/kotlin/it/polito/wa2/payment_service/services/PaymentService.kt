package it.polito.wa2.payment_service.services

import it.polito.wa2.payment_service.dtos.TransactionDTO
import kotlinx.coroutines.flow.Flow

interface PaymentService {
    fun getAllTransaction(): Flow<TransactionDTO>
    fun getTransactionsByUser(userId: String): Flow<TransactionDTO>
}