package it.polito.wa2.ticket_catalogue_service.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("orders")
data class Order(
    @Id
    var orderId: Long?=null,
    var status: Status = Status.CREATED,
    val ticketType: Long, //references Tickets table
    val notBefore: LocalDateTime,
    val quantity: Long,
    val totalPrice: Float,
    val userId: String,
    val zoneId: String

)