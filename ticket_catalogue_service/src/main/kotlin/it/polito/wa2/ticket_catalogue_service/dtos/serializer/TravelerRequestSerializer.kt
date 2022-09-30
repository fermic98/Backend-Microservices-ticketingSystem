package it.polito.wa2.ticket_catalogue_service.dtos.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.wa2.ticket_catalogue_service.dtos.PaymentInfoDTO
import it.polito.wa2.ticket_catalogue_service.dtos.UsernameDTO
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.LoggerFactory

class TravelerRequestSerializer : Serializer<UsernameDTO> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun serialize(topic: String?, data: UsernameDTO?): ByteArray? {
        log.info("Serializing...")
        return objectMapper.writeValueAsBytes(
            data ?: throw SerializationException("Error when serializing Product to ByteArray[]")
        )
    }

    override fun close(){}
}