package it.polito.wa2.traveler_service.dtos


import java.time.LocalDateTime
import java.util.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class DateRangeDTO (
    @field:NotNull(message = "From cannot be empty or null")
    val from: Date,

    @field:NotNull(message = "To cannot be empty or null")
    val to: Date
) {}


