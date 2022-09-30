package it.polito.wa2.traveler_service.dtos.Deserializer

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.wa2.traveler_service.dtos.UsernameDTO

import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.LoggerFactory

class CatalogueDeserializer : Deserializer<UsernameDTO> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun deserialize(topic: String?, data: ByteArray?): UsernameDTO? {
        log.info("Deserializing...")
        return objectMapper.readValue(
            String(
                data ?: throw SerializationException("Error when deserializing byte[] to Product"), Charsets.UTF_8
            ), UsernameDTO::class.java
        )
    }

    override fun close() {}

}