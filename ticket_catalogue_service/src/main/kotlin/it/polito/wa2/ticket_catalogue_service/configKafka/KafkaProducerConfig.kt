package it.polito.wa2.ticket_catalogue_service.configKafka


import it.polito.wa2.ticket_catalogue_service.dtos.serializer.PaymentRequestSerializer
import it.polito.wa2.ticket_catalogue_service.dtos.serializer.TravelerRequestSerializer
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

    /** ---------------- Payment Kafka Producer ------------------- **/

    @Bean
    fun producerFactoryPayment(): ProducerFactory<String, Any> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = PaymentRequestSerializer::class.java
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplatePayment(): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactoryPayment())
    }


    /** ---------------- Traveler Kafka Producer -------------------**/
    @Bean
    fun producerFactoryTraveler(): ProducerFactory<String, Any> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = TravelerRequestSerializer::class.java
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplateTraveler(): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactoryTraveler())
    }
}
