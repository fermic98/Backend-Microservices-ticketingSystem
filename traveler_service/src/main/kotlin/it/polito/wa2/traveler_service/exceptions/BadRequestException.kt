package it.polito.wa2.traveler_service.exceptions

open class BadRequestException(val msg: String): Exception(msg) {
}