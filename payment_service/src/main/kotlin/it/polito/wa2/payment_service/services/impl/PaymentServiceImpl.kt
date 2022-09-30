package it.polito.wa2.payment_service.services.impl

import it.polito.wa2.payment_service.dtos.TransactionDTO
import it.polito.wa2.payment_service.dtos.toDTO
import it.polito.wa2.payment_service.repositories.TransactionRepository
import it.polito.wa2.payment_service.services.PaymentService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional



@Service
@Transactional
class PaymentServiceImpl(): PaymentService {
    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    override fun getAllTransaction(): Flow<TransactionDTO> {
        return transactionRepository.findAll().map { it.toDTO() }
    }

    override fun getTransactionsByUser(userId: String): Flow<TransactionDTO> {
        return transactionRepository.findTransactionsByCustomer(userId).map { it.toDTO() }
    }
}



