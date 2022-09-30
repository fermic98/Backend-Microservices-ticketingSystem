package it.polito.wa2.ticket_catalogue_service.exceptions

open class NotFoundException(val msg: String): Exception(msg)  {
}