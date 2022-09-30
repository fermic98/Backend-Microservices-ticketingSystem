package it.polito.wa2.payment_service.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import javax.validation.constraints.*

data class GenTicketRequestDTO (
    @field:NotBlank(message = "Principal cannot be empty or null")
    @JsonProperty("principal")
    val principal: String,

    @JsonProperty("orderId")
    val orderId: Long,

    //val cmd: String,

    @field:NotNull
    @JsonProperty("ticket")
    val ticket : Ticket,

    @field:NotNull
    @JsonProperty("validFrom")
    val validFrom: String,//LocalDateTime,

    @field:NotNull
    @field:Min(1)
    @JsonProperty("quantity")
    val quantity: Long,

    @field:NotBlank(message = "Type cannot be empty or null")
    @JsonProperty("zone")
    val zone: String
)




