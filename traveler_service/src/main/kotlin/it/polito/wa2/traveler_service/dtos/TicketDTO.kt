package it.polito.wa2.traveler_service.dtos

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
