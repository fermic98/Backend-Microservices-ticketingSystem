package it.polito.wa2.ticket_catalogue_service.entities

import org.springframework.data.relational.core.mapping.Table
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Table("payment_infos")
data class PaymentInfo(
    val orderId: Long,

    val creditCardNumber: String,

    val expirationDate: String,

    val cvv : String,

    val cardHolder: String
)
