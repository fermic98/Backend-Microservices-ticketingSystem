package it.polito.wa2.payment_service.dtos.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.wa2.payment_service.dtos.GenTicketRequestDTO

import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.LoggerFactory

class GenTicketSerializer : Serializer<GenTicketRequestDTO> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun serialize(topic: String?, data: GenTicketRequestDTO?): ByteArray? {
        log.info("Serializing...")
        return objectMapper.writeValueAsBytes(
            data ?: throw SerializationException("Error when serializing Product to ByteArray[]")
        )
    }

    override fun close() {}
}