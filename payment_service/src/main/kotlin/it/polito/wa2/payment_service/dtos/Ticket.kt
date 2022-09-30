package it.polito.wa2.payment_service.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class Ticket(
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