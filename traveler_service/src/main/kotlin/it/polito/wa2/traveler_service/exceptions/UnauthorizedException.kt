package it.polito.wa2.traveler_service.exceptions

open class UnauthorizedException(val msg: String): Exception(msg) {
}