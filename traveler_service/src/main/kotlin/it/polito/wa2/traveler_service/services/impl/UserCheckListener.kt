package it.polito.wa2.traveler_service.services.impl

import it.polito.wa2.traveler_service.dtos.Serializer.UserDetailsDTO2
import it.polito.wa2.traveler_service.dtos.UsernameDTO
import it.polito.wa2.traveler_service.exceptions.NotFoundException
import it.polito.wa2.traveler_service.repositories.UserDetailsRepository
import kotlinx.coroutines.runBlocking
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
import java.time.LocalDateTime
import javax.validation.Valid


@Component
class UserCheckListener(
    @Value("\${kafka.topics.customer_check_answer}")
    val answerTopic: String,
    @Autowired
    private val kafkaTemplateCatalogue: KafkaTemplate<String, Any>
) {

    @Autowired
    lateinit var userDetailsRepository: UserDetailsRepository

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(containerFactory = "kafkaListenerContainerFactoryCatalogue" ,topics = ["\${kafka.topics.customer_check}"], groupId = "tc")
    fun listenGroupFoo(consumerRecord: ConsumerRecord<Any, Any>, ack: Acknowledgment) {
        logger.info("Message received {}", consumerRecord)
        ack.acknowledge()


        val message = consumerRecord.value() as UsernameDTO
        println(message)

        val user = userDetailsRepository.findByUsername(message.principal)

        val answer: UserDetailsDTO2

        if (user == null)
            answer = UserDetailsDTO2(message.orderId,false,"null","null","null","null","null")
        else
            answer = UserDetailsDTO2(message.orderId,true,user.username!!,user.name!!,"","",user.date_of_birth.toString().split(" ")[0])

        contactCatalogueService(answer)
    }



    private fun contactCatalogueService(answer: UserDetailsDTO2) {
        try {
            logger.info("Receiving product request")
            logger.info("Sending message to Kafka {}", answer)
            val message: Message<UserDetailsDTO2> = MessageBuilder
                .withPayload(answer)
                .setHeader(KafkaHeaders.TOPIC, answerTopic)
                .setHeader("X-Custom-Header", "Custom header here")
                .build()
            kafkaTemplateCatalogue.send(message)
            logger.info("Message sent with success")
            //ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error("Exception: {}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error to send message")
        }
    }

}