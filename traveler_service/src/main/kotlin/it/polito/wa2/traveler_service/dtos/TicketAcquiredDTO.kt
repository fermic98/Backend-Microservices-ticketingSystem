package it.polito.wa2.traveler_service.dtos

import it.polito.wa2.traveler_service.services.impl.entities.TicketAcquired


data class TicketAcquiredDTO(
        val sub: String?,
        var iat: String,
        var nbf: String,//NotBefore, stands for validFrom
        var exp: String,
        var zid: String,
        var type: String,
        var jws: String
) {}

fun TicketAcquired.toDTO(): TicketAcquiredDTO {
    return TicketAcquiredDTO(userDetails.username, issuedAt.toString(), validFrom.toString(), expiry.toString(), zoneId, type,jws)
}
