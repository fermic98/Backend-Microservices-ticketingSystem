package it.polito.wa2.traveler_service.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class UsernameDTO(
    @JsonProperty("principal")
    val principal: String,
    @JsonProperty("orderId")
    val orderId: Long
){}