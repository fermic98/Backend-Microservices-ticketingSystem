package it.polito.wa2.ticket_catalogue_service.controllers

import it.polito.wa2.ticket_catalogue_service.exceptions.BadRequestException
import it.polito.wa2.ticket_catalogue_service.exceptions.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException
import javax.validation.ConstraintViolationException

@RestControllerAdvice
class CatalogueAdvisor {

    @ResponseStatus(value= HttpStatus.BAD_REQUEST)
    @ExceptionHandler( value=[BadRequestException::class, ConstraintViolationException::class,
        WebExchangeBindException::class, ServerWebInputException::class])
    fun badRequestExceptionHandler( e: Exception){
        //return Mono.just(ErrorMessageDTO(e, HttpStatus.NOT_FOUND, serverWebExchange.request.path.toString()))
        println(e.message)
    }

    @ResponseStatus(value= HttpStatus.NOT_FOUND)
    @ExceptionHandler( value=[NotFoundException::class])
    fun notFoundExceptionHandler( e: Exception){
        //return Mono.just(ErrorMessageDTO(e, HttpStatus.NOT_FOUND, serverWebExchange.request.path.toString()))
        println(e.message)
    }
}