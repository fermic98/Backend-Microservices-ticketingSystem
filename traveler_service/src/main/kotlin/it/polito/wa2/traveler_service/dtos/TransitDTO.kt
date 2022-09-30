package it.polito.wa2.traveler_service.dtos

import it.polito.wa2.traveler_service.services.impl.entities.TicketAcquired
import it.polito.wa2.traveler_service.services.impl.entities.Transit
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class TransitDTO(
    @field:NotNull
    val timestamp: LocalDateTime?,

    @field:NotBlank(message = "Name cannot be empty or null")
    val username: String,
) {}

fun Transit.toDTO(): TransitDTO {
    return TransitDTO(timestamp, userDetails.username!!)
}
