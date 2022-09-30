package it.polito.wa2.payment_service.dtos

import com.fasterxml.jackson.annotation.JsonProperty

import java.time.LocalDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size



data class PaymentInfoDTO(
    @JsonProperty("totalAmount")
    val totalAmount: Float,

    @JsonProperty("creditCardNumber")
    @field:Size(min=13,max = 16, message = "credit card number has wrong number of digits")
    val creditCardNumber: String,

    @JsonProperty("expirationDate")
    @field:Pattern(regexp = "([0-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-[0-9]{4}")
    val expirationDate: String,

    @JsonProperty("cvv")
    @field:NotBlank(message = "cvv cannot be null or empty")
    @field:Size(min=3,max = 3, message = "cvv has wrong number of digits")
    val cvv : String,

    @JsonProperty("cardHolder")
    @field:NotBlank(message = "card holder cannot be null or empty")
    val cardHolder: String,

    @JsonProperty("username")
    val username: String,
    @JsonProperty("orderId")
    val orderId: Long,

    @JsonProperty("ticket")
    val ticket : Ticket,

    @JsonProperty("validFrom")
    val validFrom: String,//LocalDateTime,

    @JsonProperty("quantity")
    val quantity: Long,

    @JsonProperty("zone")
    val zone: String
)