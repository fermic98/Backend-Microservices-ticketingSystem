package it.polito.wa2.ticket_catalogue_service.dtos.deserializer

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.wa2.ticket_catalogue_service.dtos.PaymentInfoAnswerDTO
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.LoggerFactory

class PaymentAnswerDeserializer : Deserializer<PaymentInfoAnswerDTO> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun deserialize(topic: String?, data: ByteArray?): PaymentInfoAnswerDTO? {
        log.info("Deserializing payment...")
        return objectMapper.readValue(
            String(
                data ?: throw SerializationException("Error when deserializing byte[] to Product"), Charsets.UTF_8
            ), PaymentInfoAnswerDTO::class.java
        )
    }

    override fun close() {}

}