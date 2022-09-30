package it.polito.wa2.login_service.exceptions

open class BadRequestException(val msg: String): Exception(msg) {
}