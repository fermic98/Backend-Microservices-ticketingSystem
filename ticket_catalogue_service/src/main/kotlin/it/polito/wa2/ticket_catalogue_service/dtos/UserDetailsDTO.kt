package it.polito.wa2.ticket_catalogue_service.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class UserDetailsDTO(
    @JsonProperty("orderId")
    var orderId: Long,
    @JsonProperty("existing")
    val existing: Boolean,
    @JsonProperty("username")
    var username: String,
    @JsonProperty("name")
    var name: String?,
    @JsonProperty("address")
    var address: String?,
    @JsonProperty("telephone_number")
    var telephone_number: String?,
    @JsonProperty("date_of_bith")
    var date_of_birth: String?
)
