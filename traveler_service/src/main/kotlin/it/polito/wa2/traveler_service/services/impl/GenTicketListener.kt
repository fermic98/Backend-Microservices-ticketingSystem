package it.polito.wa2.traveler_service.services.impl

import it.polito.wa2.traveler_service.dtos.PurchaseTicketDTO
import it.polito.wa2.traveler_service.dtos.Serializer.PaymentAnswer
import it.polito.wa2.traveler_service.dtos.Serializer.Status
import it.polito.wa2.traveler_service.exceptions.BadRequestException
import it.polito.wa2.traveler_service.exceptions.NotFoundException
import it.polito.wa2.traveler_service.repositories.TicketPurchasedRepository
import it.polito.wa2.traveler_service.repositories.UserDetailsRepository
import it.polito.wa2.traveler_service.security.JwtUtils
import it.polito.wa2.traveler_service.services.impl.entities.TicketAcquired
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*


@Component
class GenTicketListener(
    @Value("\${kafka.topics.generate_ticket_answer}")
    val topic: String,

    @Autowired
    private val kafkaTemplatePayment: KafkaTemplate<String, Any>
) {

    @Autowired
    lateinit var userDetailsRepository: UserDetailsRepository

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var ticketPurchasedRepository: TicketPurchasedRepository

    @Autowired
    lateinit var jwtUtils: JwtUtils

    @KafkaListener(
        containerFactory = "kafkaListenerContainerFactoryPayment",
        topics = ["\${kafka.topics.generate_ticket}"],
        groupId = "gt"
    )
    fun listenGroupFoo2(consumerRecord: ConsumerRecord<Any, Any>, ack: Acknowledgment) {

        var answer: PaymentAnswer
        val purchasedTicketDTO = consumerRecord.value() as PurchaseTicketDTO
        println(purchasedTicketDTO)

        try{
            logger.info("Message received {}", consumerRecord)
            ack.acknowledge()

            //throw Exception("ciao")



            var numberOfTickets = purchasedTicketDTO.quantity

            val userDetails = userDetailsRepository.findByUsername(purchasedTicketDTO.principal)

            if(userDetails==null)
                throw NotFoundException("Username not found")


            if(purchasedTicketDTO.validFrom==null)
                throw BadRequestException("Invalid NotBefore Date")

            //business logic for NotBefore And Expiry Time
            val jwtTimeInfo = computeNbfAndExp(purchasedTicketDTO)

            //ticket creation
            do {
                val ticketWithoutJws = TicketAcquired(
                    Date(jwtTimeInfo.iat),
                    Date(jwtTimeInfo.nbf),
                    Date(jwtTimeInfo.exp),
                    purchasedTicketDTO.zone,
                    purchasedTicketDTO.ticket.type,
                    "",
                    userDetails
                )

                ticketPurchasedRepository.save(ticketWithoutJws)

                ticketWithoutJws.jws = jwtUtils.generateJwt(ticketWithoutJws.getId() as Long, ticketWithoutJws.issuedAt, ticketWithoutJws.validFrom, ticketWithoutJws.expiry, ticketWithoutJws.zoneId, ticketWithoutJws.type)

                ticketPurchasedRepository.save(ticketWithoutJws)

                numberOfTickets--
            } while (numberOfTickets > 0)

             answer = PaymentAnswer(purchasedTicketDTO.orderId,Status.ACCEPTED)

             contactPayment(answer)

        }catch(ex: Exception){

            println(ex)

            answer = PaymentAnswer(purchasedTicketDTO.orderId,Status.DENIED)

            contactPayment(answer)
        }




    }


    private fun contactPayment(answer: PaymentAnswer) {
        try {
            logger.info("Receiving product request")
            logger.info("Sending message to Kafka {}", answer)
            val message: Message<PaymentAnswer> = MessageBuilder
                .withPayload(answer)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader("X-Custom-Header", "Custom header here")
                .build()
            kafkaTemplatePayment.send(message)
            logger.info("Message sent with success")
            //ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error("Exception: {}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error to send message")
        }
    }



    private fun computeNbfAndExp(purchasedTicketDTO : PurchaseTicketDTO): NbfExpInfo {

        val type=purchasedTicketDTO.ticket.type
        val name=purchasedTicketDTO.ticket.name
        val validFrom=purchasedTicketDTO.validFrom

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")

        val iat: Long
        var exp: Long
        val nbf: Long
        val nbfSeasonal: LocalDateTime
        var expSeasonal : LocalDateTime

        if(type!="ordinal" && type!="seasonal")
            throw BadRequestException("wrong type")

        if (type == "ordinal") {

            val ticketInfo: NbfExpInfo = when (name) {
                "70 minutes" -> {
                    iat = Date().time
                    nbf = iat
                    exp = iat + 4200000 //4200 * 1000 = 70 minutes in  milliseconds

                    NbfExpInfo(iat, nbf, exp)
                }
                "daily" -> {
                    val currentDate = Date()
                    iat = currentDate.time

                    val cal = Calendar.getInstance()
                    val validFromDate = formatter.parse(validFrom)//.toLocalDate().toString())
                    cal.setTime(validFromDate)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)

                    nbf = cal.time.time

                    //retrieving midnight of the following day
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    NbfExpInfo(iat, nbf, cal.time.time)
                }
                "weekly" -> {
                    val cal = Calendar.getInstance()

                    //check if validFrom is a Monday
                    val validFromDate = formatter.parse(validFrom)//.toLocalDate().toString())
                    cal.setTime(validFromDate)
                    if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
                        throw BadRequestException("Invalid ValidFrom field")

                    //valid from midnight of the selected Monday
                    nbf = validFromDate.time

                    //retrieving midnight of the next Monday (with respect to the actual Monday selected)
                    cal.add(Calendar.DAY_OF_YEAR, 7)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)

                    iat = Date().time
                    NbfExpInfo(iat, nbf, cal.time.time)
                }
                "monthly" -> {
                    val cal = Calendar.getInstance()

                    //check if validFrom is the first of Any Month
                    val validFromDate = formatter.parse(validFrom)//.toLocalDate().toString())
                    cal.setTime(validFromDate)
                    if (cal.get(Calendar.DAY_OF_MONTH) != cal.getActualMinimum(Calendar.DAY_OF_MONTH))
                        throw BadRequestException("Invalid ValidFrom field")

                    //valid from midnight of the selected first day of any month
                    nbf = validFromDate.time

                    //retrieving midnight of the first day of the next month (with respect to the next month selected)
                    cal.add(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)


                    iat = Date().time
                    NbfExpInfo(iat, nbf, cal.time.time)
                }
                "biannually" -> {
                    val cal = Calendar.getInstance()

                    //check if validFrom is the first of Any Month
                    val validFromDate = formatter.parse(validFrom)//.toLocalDate().toString())
                    cal.setTime(validFromDate)
                    if (cal.get(Calendar.DAY_OF_MONTH) != cal.getActualMinimum(Calendar.DAY_OF_MONTH))
                        throw BadRequestException("Invalid ValidFrom field")

                    //valid from midnight of the selected first day of any month
                    nbf = validFromDate.time

                    //retrieving midnight of the first day of the next 6th month (with respect to the month selected)
                    cal.add(Calendar.MONTH, 6)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)


                    iat = Date().time
                    NbfExpInfo(iat, nbf, cal.time.time)
                }
                "yearly" -> {
                    val cal = Calendar.getInstance()

                    //check if validFrom is the first of Any Month
                    val validFromDate = formatter.parse(validFrom)//.toLocalDate().toString())
                    cal.setTime(validFromDate)
                    if (cal.get(Calendar.DAY_OF_MONTH) != cal.getActualMinimum(Calendar.DAY_OF_MONTH))
                        throw BadRequestException("Invalid ValidFrom field")

                    //valid from midnight of the selected first day of any month
                    nbf = validFromDate.time

                    //retrieving midnight of the first day of the next 12th month (with respect to the month selected)
                    cal.add(Calendar.MONTH, 12)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)


                    iat = Date().time
                    NbfExpInfo(iat, nbf, cal.time.time)
                }
                "weekend_pass" -> {
                    val cal = Calendar.getInstance()

                    //check if validFrom is a Saturday or a Sunday
                    val validFromDate = formatter.parse(validFrom)//.toLocalDate().toString())
                    cal.setTime(validFromDate)
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY)
                        throw BadRequestException("Invalid ValidFrom field")


                    if (dayOfWeek == Calendar.SATURDAY)
                        cal.add(Calendar.DAY_OF_YEAR, 2)
                    else
                        cal.add(Calendar.DAY_OF_YEAR, 1)

                    //valid from Midnight of the selected week-end day
                    nbf = validFromDate.time

                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)

                    iat = Date().time
                    NbfExpInfo(iat, nbf, cal.time.time)
                }
                else -> throw BadRequestException("Invalid Ticket Type")
            }

            return ticketInfo
        }else{
            if(purchasedTicketDTO.ticket.duration==null)
                throw BadRequestException("Wrong duration")
            iat = Date().time


            val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

            val dateTime = LocalDateTime.parse(validFrom, format)

            nbfSeasonal = dateTime
            expSeasonal=dateTime.plusMinutes(purchasedTicketDTO.ticket.duration)

            //check if notBefore+duration exceeds the seasonal end_period
            if(dateTime.plusMinutes(purchasedTicketDTO.ticket.duration).isAfter(LocalDateTime.parse(purchasedTicketDTO.ticket.end_period!!, format))) {
                expSeasonal=LocalDateTime.parse(purchasedTicketDTO.ticket.end_period!!, format)
            }

            //converting localDateTime in milliseconds
            nbf = nbfSeasonal.toEpochSecond(ZoneOffset.of("+01:00"))*1000
            exp = expSeasonal.toEpochSecond(ZoneOffset.of("+01:00"))*1000

            return NbfExpInfo(iat, nbf, exp)
        }
    }
}

data class NbfExpInfo(
    val iat: Long,
    val nbf : Long,
    val exp: Long
)