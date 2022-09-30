package it.polito.wa2.ticket_catalogue_service.entities

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

/**
 * Note that in JPA we would not be able to have a data class, because JPA pretend objects to be extended
 * and replaced dynamically by code. Instead here we have the opportunity to create a data class which is immutable.
 */
@Table("tickets")
data class Ticket(
        @Id
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
        val start_period: LocalDateTime?,
        @JsonProperty("end_period")
        val end_period: LocalDateTime?,
        @JsonProperty("duration")
        val duration: Long? = null
)


