package it.polito.wa2.payment_service.config


import it.polito.wa2.payment_service.dtos.serializer.GenTicketSerializer
import it.polito.wa2.payment_service.dtos.serializer.PaymentAnswerSerializer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaProducerConfig(
    @Value("\${kafka.bootstrapAddress}")
    private val servers: String
) {

    /** ---------------- Catalogue Kafka Producer ------------------- **/

    @Bean
    fun producerFactoryCatalogue(): ProducerFactory<String, Any> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = PaymentAnswerSerializer::class.java
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplateCatalogue(): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactoryCatalogue())
    }


    /** ---------------- Traveler Kafka Producer -------------------**/
    @Bean
    fun producerFactoryGenTicket(): ProducerFactory<String, Any> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = GenTicketSerializer::class.java
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplateGenTicket(): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactoryGenTicket())
    }
}