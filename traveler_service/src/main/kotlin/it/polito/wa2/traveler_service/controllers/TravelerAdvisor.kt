package it.polito.wa2.traveler_service.controllers

import it.polito.wa2.traveler_service.exceptions.BadRequestException
import it.polito.wa2.traveler_service.exceptions.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class TravelerAdvisor {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException::class)
    fun badRequestExceptionHandler(ex: BadRequestException){
        println(ex.msg)
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException::class)
    fun validationExceptionHandler(ex: NotFoundException){
        println(ex.msg)
    }
}