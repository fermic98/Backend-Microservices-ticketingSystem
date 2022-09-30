package it.polito.wa2.ticket_catalogue_service.services.impl

import it.polito.wa2.ticket_catalogue_service.dtos.PaymentInfoAnswerDTO
import it.polito.wa2.ticket_catalogue_service.dtos.PurchaseTicketDTO
import it.polito.wa2.ticket_catalogue_service.dtos.UserDetailsDTO
import it.polito.wa2.ticket_catalogue_service.entities.Order
import it.polito.wa2.ticket_catalogue_service.entities.Status
import it.polito.wa2.ticket_catalogue_service.entities.Ticket
import it.polito.wa2.ticket_catalogue_service.exceptions.BadRequestException
import it.polito.wa2.ticket_catalogue_service.repositories.OrderRepository
import it.polito.wa2.ticket_catalogue_service.repositories.TicketRepository
import it.polito.wa2.ticket_catalogue_service.security.JwtUtils
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.*

@Component
class PaymentAnswerListener {

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var ticketRepository: TicketRepository

    private val logger = LoggerFactory.getLogger(javaClass)

    private val webClient = WebClient.create(("http://localhost:8080"))

    private val jwtUtils = JwtUtils()

    @Value("\${application.jwt.jwtExpirationMs}")
    var jwtExpirationMs: Long = -1




    @KafkaListener(containerFactory = "kafkaListenerContainerFactoryPayment",topics = ["\${kafka.topics.payment_answer}"], groupId = "pbca")
    fun listenGroupFoo(consumerRecord: ConsumerRecord<Any, Any>, ack: Acknowledgment) {
        logger.info("Message received {}", consumerRecord)
        ack.acknowledge()

        val order = consumerRecord.value() as PaymentInfoAnswerDTO

        val orderEntity: Order?

        runBlocking {
            orderEntity = orderRepository.findById(order.orderId)
        }

        if (orderEntity == null)
            throw BadRequestException("Not Existing Order")

        orderEntity.status = order.status

        runBlocking {
            orderRepository.save(orderEntity)
        }

        if(order.status==Status.ACCEPTED){
            var ticketEntity : Ticket?

            runBlocking {
                ticketEntity = ticketRepository.findById(orderEntity.ticketType)
            }

            val purchasingTickets = PurchaseTicketDTO("buy_tickets", ticketEntity!!, orderEntity.notBefore, orderEntity.quantity, orderEntity.zoneId)

            //generating jwt for the authentication with Traveler Service
            val jwt = jwtUtils.generateJwt(orderEntity.userId, Date(), Date(Date().time+jwtExpirationMs))




            /*
            runBlocking {
                val tickets = webClient.post().uri("/my/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + jwt)
                    .body(Mono.just(purchasingTickets as Any), PurchaseTicketDTO::class.java)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchangeToMono { response ->
                        println(response.statusCode())
                        if (response.statusCode() == HttpStatus.OK) {
                            response.bodyToMono(UserDetailsDTO::class.java)
                        } else {

                            println("Something Went Wrong :")
                            println(response.statusCode())
                            Mono.empty()
                        }
                    }.awaitSingleOrNull()*/
           // }

        }

    }

}
