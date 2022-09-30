package it.polito.wa2.ticket_catalogue_service.services.impl


import it.polito.wa2.ticket_catalogue_service.dtos.*
import it.polito.wa2.ticket_catalogue_service.entities.Order
import it.polito.wa2.ticket_catalogue_service.entities.PaymentInfo
import it.polito.wa2.ticket_catalogue_service.entities.Status
import it.polito.wa2.ticket_catalogue_service.entities.Ticket
import it.polito.wa2.ticket_catalogue_service.exceptions.BadRequestException
import it.polito.wa2.ticket_catalogue_service.repositories.OrderRepository
import it.polito.wa2.ticket_catalogue_service.repositories.TemporaryPaymentRepository
import it.polito.wa2.ticket_catalogue_service.repositories.TicketRepository
import it.polito.wa2.ticket_catalogue_service.security.JwtUtils
import it.polito.wa2.ticket_catalogue_service.services.TicketCatalogueService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.text.SimpleDateFormat

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*


@Service
@Transactional
class TicketCatalogueServiceImpl(
    //@Value("\${kafka.topics.bank_check}") val topicPayment: String,
    @Value("\${kafka.topics.customer_check}") val topicTraveler: String,

    @Autowired
    private val kafkaTemplateTraveler: KafkaTemplate<String, Any>
) : TicketCatalogueService {


    val log = LoggerFactory.getLogger(javaClass)


    @Autowired
    private lateinit var ticketRepository: TicketRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var paymentRepository: TemporaryPaymentRepository

    @Value("\${application.jwt.jwtExpirationMs}")
    var jwtExpirationMs: Long = -1

    private val webClient = WebClient.create(("http://localhost:8080"))

    private val jwtUtils = JwtUtils()

    override fun getAllTickets(): Flow<TicketDTO> {
        return ticketRepository.findAll().map { it.toDTO() }
    }


    override suspend fun purchaseTickets(
        principal: String,
        purchaseTicketsRequestDTO: PurchaseTicketsRequestDTO
    ): Mono<Long> {

        val ticket = ticketRepository.findById(purchaseTicketsRequestDTO.ticketId)

        if (ticket == null)
            throw BadRequestException("Invalid ticketID")


        checkValidityOfValidFrom(ticket.type, ticket.name, purchaseTicketsRequestDTO.notBefore.toLocalDate().toString(), ticket)
        /*if (ticket.type == "seasonal" && (ticket.duration == null || ticket.duration < 1))
            throw BadRequestException("Invalid duration")*/




        val totalAmount = (ticket.price * purchaseTicketsRequestDTO.quantity)

        //Save Pending Order
        val order = Order(
            null,
            Status.CREATED,
            purchaseTicketsRequestDTO.ticketId,
            purchaseTicketsRequestDTO.notBefore,
            purchaseTicketsRequestDTO.quantity,
            totalAmount,
            principal,
            purchaseTicketsRequestDTO.zoneId
        )
        orderRepository.save(order)

        val paymentInfo = PaymentInfo(order.orderId!!,purchaseTicketsRequestDTO.creditCardNumber,purchaseTicketsRequestDTO.expirationDate,purchaseTicketsRequestDTO.cvv,purchaseTicketsRequestDTO.cardHolder)
        paymentRepository.save(paymentInfo)

        val username = UsernameDTO(principal,order.orderId!!);
        contactTravelerService(username)

        return Mono.just(order.orderId as Long)

    }

    override fun getOrdersByUserId(userId: String): Flow<OrderDTO> {
        return orderRepository.findByUserId(userId).map { it.toDTO() }
    }

    override suspend fun getOrderByOrderIdAndUserId(userId: String, orderId: Long): Mono<OrderDTO> {
        return orderRepository.findOrderByOrderIdAndUserId(orderId, userId).map {
            if (it == null)
                null
            else it.toDTO()
        }
    }

    override fun getAllOrdersByAllUsers(): Flow<OrderDTO> {
        return orderRepository.findAll().map { it.toDTO() }
    }

    override suspend fun addTicket(ticketDTO: TicketDTO) {
        val ticket = ticketRepository.findByName(ticketDTO.name)

        if (ticket != null)
            throw BadRequestException("Invalid ticket name")

        checkConstraintsBoundaries(ticketDTO)

        val ticketEntity = Ticket(
            null,
            ticketDTO.price,
            ticketDTO.type,
            ticketDTO.name,
            ticketDTO.minAge,
            ticketDTO.maxAge,
            ticketDTO.start_period,
            ticketDTO.end_period,
            ticketDTO.duration
        )
        ticketRepository.save(ticketEntity)
    }

    override suspend fun updateTicket(ticketDTO: TicketDTO) {
        val ticket = ticketRepository.findById(ticketDTO.ticketID!!)

        if (ticket == null)
            throw BadRequestException("Invalid ticket id")

        val ticketEntity: Ticket =
            if (ticketDTO.type == "ordinal") {
                // allowed modifications: price //name not allowed for example
                Ticket(
                    ticket.ticketId,
                    ticketDTO.price,
                    ticket.type,
                    ticket.name,
                    ticket.minAge,
                    ticket.maxAge,
                    ticket.start_period,
                    ticket.end_period,
                    ticket.duration
                )
            } else { //ticketDTO.type == "seasonal"
                checkConstraintsBoundaries(ticketDTO)

                // update (add/remove active columns: min_age, max_age, start_period, end_period)
                // or modify some values (price, name, min_age, max_age, start_period, end_period, duration)
                Ticket(
                    ticket.ticketId,
                    ticketDTO.price,
                    ticket.type,
                    ticketDTO.name,
                    ticketDTO.minAge,
                    ticketDTO.maxAge,
                    ticketDTO.start_period,
                    ticketDTO.end_period,
                    ticketDTO.duration
                )
            }
        ticketRepository.save(ticketEntity) //'save' performs an update if the row exists

        /* Alternative rough method */
        /*
        //It does not update, but delete the row and recreate a new one with a new ticket id
        ticketRepository.deleteById(ticket?.ticketId!!)
        ticketRepository.save(ticketEntity)
         */
    }


    //

    fun calculateAge(birthDate: LocalDate?, currentDate: LocalDate?): Int {
        return if (birthDate != null && currentDate != null) {
            Period.between(birthDate, currentDate).getYears()
        } else {
            0
        }
    }

    /*private fun checkAgeConstraints(userInfo: UserDetailsDTO?, ticket: Ticket) {


        if (userInfo!!.date_of_birth == null)
            throw BadRequestException("Date of Birth is not available")

        val date = (userInfo.date_of_birth as String).split("-")
        val userLocalDate = LocalDate.of(date[2].toInt(), date[1].toInt(), date[0].toInt())

        val currentDate = LocalDate.now()

        val currentNumberOfYears = calculateAge(userLocalDate, currentDate)

        if (currentNumberOfYears == 0)
            throw BadRequestException("Invalid Age for this ticket type")

        if (ticket.maxAge != null)
            if (currentNumberOfYears > ticket.maxAge)
                throw BadRequestException("Invalid Age for this ticket type")

        if (ticket.minAge != null)
            if (currentNumberOfYears < ticket.minAge)
                throw BadRequestException("Invalid Age for this ticket type")

    }

    private fun contactPaymentService(request: PaymentInfoDTO) {
        try {
            log.info("Receiving product request")
            log.info("Sending message to Kafka {}", request)
            val message: Message<PaymentInfoDTO> = MessageBuilder
                .withPayload(request)
                .setHeader(KafkaHeaders.TOPIC, topicPayment)
                .setHeader("X-Custom-Header", "Custom header here")
                .build()
            kafkaTemplate.send(message)
            log.info("Message sent with success")
            //ResponseEntity.ok().build()
        } catch (e: Exception) {
            log.error("Exception: {}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error to send message")
        }
    }*/

    private fun contactTravelerService(request: UsernameDTO) {
        try {
            log.info("Receiving product request")
            log.info("Sending message to Kafka {}", request)
            val message: Message<UsernameDTO> = MessageBuilder
                .withPayload(request)
                .setHeader(KafkaHeaders.TOPIC, topicTraveler)
                .setHeader("X-Custom-Header", "Custom header here")
                .build()
            kafkaTemplateTraveler.send(message)
            log.info("Message sent with success")
            //ResponseEntity.ok().build()
        } catch (e: Exception) {
            log.error("Exception: {}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error to send message")
        }
    }


    private fun checkValidityOfValidFrom(type: String, name: String, validFrom: String, ticket: Ticket) {

        val formatter = SimpleDateFormat("yyyy-MM-dd")

        if (type == "ordinal") {

            when (name) {
                "70 minutes" -> {

                }
                "daily" -> {

                }
                "weekly" -> {
                    val cal = Calendar.getInstance()

                    //check if validFrom is a Monday
                    val validFromDate = formatter.parse(validFrom)
                    cal.setTime(validFromDate)
                    if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
                        throw BadRequestException("Invalid ValidFrom field")

                }
                "monthly" -> {
                    val cal = Calendar.getInstance()

                    //check if validFrom is the first of Any Month
                    val validFromDate = formatter.parse(validFrom)
                    cal.setTime(validFromDate)
                    if (cal.get(Calendar.DAY_OF_MONTH) != cal.getActualMinimum(Calendar.DAY_OF_MONTH))
                        throw BadRequestException("Invalid ValidFrom field")
                }
                "biannually" -> {
                    val cal = Calendar.getInstance()

                    //check if validFrom is the first of Any Month
                    val validFromDate = formatter.parse(validFrom)
                    cal.setTime(validFromDate)
                    if (cal.get(Calendar.DAY_OF_MONTH) != cal.getActualMinimum(Calendar.DAY_OF_MONTH))
                        throw BadRequestException("Invalid ValidFrom field")

                }
                "yearly" -> {
                    val cal = Calendar.getInstance()

                    //check if validFrom is the first of Any Month
                    val validFromDate = formatter.parse(validFrom)
                    cal.setTime(validFromDate)
                    if (cal.get(Calendar.DAY_OF_MONTH) != cal.getActualMinimum(Calendar.DAY_OF_MONTH))
                        throw BadRequestException("Invalid ValidFrom field")

                }
                "weekend_pass" -> {
                    val cal = Calendar.getInstance()

                    //check if validFrom is a Saturday or a Sunday
                    val validFromDate = formatter.parse(validFrom)
                    cal.setTime(validFromDate)
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY)
                        throw BadRequestException("Invalid ValidFrom field")

                }
                else -> {
                    throw BadRequestException("Invalid ValidFrom field")
                }
            }

        } else { //type == "seasonal"
            if (validFrom < ticket.start_period!!.toLocalDate().toString()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                || validFrom > ticket.end_period!!.toLocalDate().toString()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            )
                throw BadRequestException("NotBefore must be in the validity period of seasonal types")

        }
    }

    private fun checkConstraintsBoundaries(ticketDTO: TicketDTO) {
        /* Check Age Boundaries */
        if (ticketDTO.minAge != null && ticketDTO.maxAge != null) {
            if (ticketDTO.minAge > ticketDTO.maxAge) throw BadRequestException("Wrong AGE boundaries inserted")
        }


        /* Check Period Boundaries */
        // Mandatory duration
        if (ticketDTO.duration == null)
            throw BadRequestException("Mandatory duration field")

        // Range boundary check
        if (ticketDTO.start_period != null && ticketDTO.end_period != null && (
                    ticketDTO.start_period.isAfter(ticketDTO.end_period)
//                            ||
//                            ((ticketDTO.end_period.minus(ticketDTO.start_period.)) <= ticketDTO.duration) //TODO: @alessiodongio
                    )
        ) {
            throw BadRequestException("Wrong Start and/or End period range fields or duration field")
        }
    }
}

