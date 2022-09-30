package it.polito.wa2.login_service.controllers

import it.polito.wa2.login_service.exceptions.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException::class)
    fun badRequestExceptionHandler(ex: BadRequestException){
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException::class)
    fun validationExceptionHandler(ex: NotFoundException){
        println(ex.msg)
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UnauthorizedException::class)
    fun unauthorizedExceptionHandler(ex: UnauthorizedException){
        println(ex.msg)
    }

}

