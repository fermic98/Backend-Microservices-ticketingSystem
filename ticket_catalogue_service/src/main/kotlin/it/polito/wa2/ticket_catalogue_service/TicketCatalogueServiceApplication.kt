package it.polito.wa2.ticket_catalogue_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@EnableEurekaClient
@SpringBootApplication
class TicketCatalogueServiceApplication

fun main(args: Array<String>) {
    runApplication<TicketCatalogueServiceApplication>(*args)
}

