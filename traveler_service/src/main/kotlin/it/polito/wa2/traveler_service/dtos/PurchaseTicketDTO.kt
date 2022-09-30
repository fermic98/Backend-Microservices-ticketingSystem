package it.polito.wa2.traveler_service.dtos


import com.fasterxml.jackson.annotation.JsonProperty
import it.polito.wa2.traveler_service.services.impl.entities.Ticket
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class PurchaseTicketDTO(
        @field:NotBlank(message = "Principal cannot be empty or null")
        @JsonProperty("principal")
        val principal: String,

        @JsonProperty("orderId")
        val orderId: Long,

        //val cmd: String,

        @field:NotNull
        @JsonProperty("ticket")
        val ticket : Ticket2,

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
) {}


data class Ticket2(
        @JsonProperty("ticketId")
        val ticketId: Long?,
        @JsonProperty("price")
        val price : Float,
        @JsonProperty("type")
        val type : String,
        @JsonProperty("name")
        val name : String,
        @JsonProperty("minAge")
        val minAge: Int?,
        @JsonProperty("maxAge")
        val maxAge: Int?,
        @JsonProperty("start_period")
        val start_period: String?,//LocalDateTime?,
        @JsonProperty("end_period")
        val end_period: String?,//LocalDateTime?,
        @JsonProperty("duration")
        val duration: Long? = null
)



