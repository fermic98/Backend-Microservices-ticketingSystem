package it.polito.wa2.ticket_catalogue_service.dtos

import java.time.LocalDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class PurchaseTicketsRequestDTO(
        @field:NotNull
        @field:Min(1)
        val quantity: Long,

        @field:NotNull
        @field:Min(1)
        val ticketId: Long,

        @field:NotNull
        @field:Size(min=1,max = 15, message = "wrong size of zone id")
        val zoneId: String,

        val notBefore: LocalDateTime,

        /** Payment info*/
        @field:NotBlank(message = "credit card number cannot be null or empty")
        @field:Size(min=13,max = 16, message = "credit card number has wrong number of digits")
        val creditCardNumber: String,

        //constraints for date
        @field:Pattern(regexp = "([0-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-[0-9]{4}")
        @field:NotNull
        val expirationDate: String,

        @field:NotBlank(message = "cvv cannot be null or empty")
        @field:Size(min=3,max = 3, message = "cvv has wrong number of digits")
        val cvv : String,

        @field:NotBlank(message = "card holder cannot be null or empty")
        val cardHolder: String
)
