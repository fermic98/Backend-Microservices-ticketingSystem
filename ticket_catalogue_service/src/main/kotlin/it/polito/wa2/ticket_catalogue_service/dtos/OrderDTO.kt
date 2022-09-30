package it.polito.wa2.ticket_catalogue_service.dtos


import it.polito.wa2.ticket_catalogue_service.entities.Order
import it.polito.wa2.ticket_catalogue_service.entities.Status

data class OrderDTO(
    val orderID : Long?,
    val status : Status,
    val userID : String,
    val quantity : Long,
    val totalPrice : Float,
    val ticketType : Long,
)

fun Order.toDTO() : OrderDTO {
    return OrderDTO(orderId, status , userId, quantity, totalPrice, ticketType)
}