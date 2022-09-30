package it.polito.wa2.payment_service.dtos.deserializer

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.wa2.payment_service.dtos.PaymentInfoDTO
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.LoggerFactory

class PaymentRequestDeserializer : Deserializer<PaymentInfoDTO> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun deserialize(topic: String?, data: ByteArray?): PaymentInfoDTO? {
        log.info("Deserializing...")
        return objectMapper.readValue(
            String(
                data ?: throw SerializationException("Error when deserializing byte[] to Product"), Charsets.UTF_8
            ), PaymentInfoDTO::class.java
        )
    }

    override fun close() {}

}