package it.polito.wa2.ticket_catalogue_service.dtos

import it.polito.wa2.ticket_catalogue_service.entities.Ticket
import org.springframework.beans.factory.annotation.Value
import java.time.LocalDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class TicketDTO(
    @field:NotNull
    @field:Min(1)
    val price : Float,

    val ticketID : Long?,

    @field:NotBlank
    val type: String,//it will be ordinal or seasonal


    @field:NotBlank
    val name : String,

    @field:Min(0)
    val minAge: Int?,

    @field:Min(1)
    val maxAge: Int?,

    val start_period: LocalDateTime?,

    val end_period: LocalDateTime?,

    @field:Min(1)
    @field:NotNull
    val duration: Long?
)

fun Ticket.toDTO() : TicketDTO {
    return TicketDTO(price,ticketId,type, name, minAge, maxAge, start_period, end_period, duration)
}
