package it.polito.wa2.payment_service.services.impl

import it.polito.wa2.payment_service.dtos.PaymentInfoAnswerDTO
import it.polito.wa2.payment_service.entities.Status
import it.polito.wa2.payment_service.entities.Transaction

import it.polito.wa2.payment_service.repositories.TransactionRepository
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import java.time.LocalDateTime


@Component
class GenTicketListener(

    @Value("\${kafka.topics.payment_answer}")
    val topic: String,

    @Autowired
    private val kafkaTemplateCatalogue: KafkaTemplate<String, Any>
) {

    @Autowired
    private lateinit var transactionRepository: TransactionRepository


    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(containerFactory = "kafkaListenerContainerFactoryGenTicket" ,topics = ["\${kafka.topics.generate_ticket_answer}"], groupId = "gta")
    fun listenGroupFoo(consumerRecord: ConsumerRecord<Any, Any>, ack: Acknowledgment) {
        logger.info("Message received {}", consumerRecord)
        ack.acknowledge()

        val message = consumerRecord.value() as PaymentInfoAnswerDTO
        println(message)

        val transaction: Transaction?

        catalgueResponse(message)

        runBlocking {
            transaction = transactionRepository.findTransactionsByOrderId(message.orderId)
        }

        if(transaction==null)
            throw Exception("Not existing transaction");



    //rollback compensating action (reverse transaction)
        if(message.status==Status.DENIED){

            var newTrans = Transaction(
                null,
                - transaction.amount,
                transaction.customer,
                message.orderId,
                LocalDateTime.now(),
                Status.ACCEPTED,
                transaction.creditCardNumber,
                transaction.expirationDate,
                transaction.cvv,
                transaction.cardHolder
            )

            runBlocking {
                transactionRepository.save(newTrans)
            }
        }

    }



    private fun catalgueResponse(request: PaymentInfoAnswerDTO) {
        try {
            logger.info("Receiving product request")
            logger.info("Sending message to Kafka {}", request)
            val message: Message<PaymentInfoAnswerDTO> = MessageBuilder
                .withPayload(request)
                .setHeader(KafkaHeaders.TOPIC, topic)
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


