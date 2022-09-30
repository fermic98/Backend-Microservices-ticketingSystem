package it.polito.wa2.payment_service.entities

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("transactions")
data class Transaction (
    @Id
    val transactionId: Long?=null,
    val amount: Float,
    val customer: String,
    val orderId: Long?=null,
    val transactionDate: LocalDateTime,
    var status: Status = Status.PENDING,
    val creditCardNumber: String,
    val expirationDate: String,
    val cvv : String,
    val cardHolder: String

        )

