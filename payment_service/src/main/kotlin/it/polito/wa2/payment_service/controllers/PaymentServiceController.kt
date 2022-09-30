package it.polito.wa2.payment_service.controllers

import it.polito.wa2.payment_service.dtos.TransactionDTO
import it.polito.wa2.payment_service.services.PaymentService
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/payment")
class PaymentServiceController {

    @Autowired
    lateinit var paymentService: PaymentService

    @GetMapping("/admin/transactions", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    @PreAuthorize("hasAuthority(T(it.polito.wa2.payment_service.dtos.Role).ADMIN)")
    fun getAllTransaction(): Flow<TransactionDTO> {
        return paymentService.getAllTransaction()
    }

    @GetMapping("/transactions", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    @PreAuthorize("hasAuthority(T(it.polito.wa2.payment_service.dtos.Role).CUSTOMER)")
    fun getTransactionsByUser(principal: Principal): Flow<TransactionDTO> {
        return paymentService.getTransactionsByUser(principal.name)
    }

}