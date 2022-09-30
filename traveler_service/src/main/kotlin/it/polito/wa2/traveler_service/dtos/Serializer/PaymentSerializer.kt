package it.polito.wa2.traveler_service.dtos.Serializer

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.LoggerFactory

class PaymentSerializer : Serializer<PaymentAnswer> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun serialize(topic: String?, data: PaymentAnswer?): ByteArray? {
        log.info("Serializing...")
        return objectMapper.writeValueAsBytes(
            data ?: throw SerializationException("Error when serializing Product to ByteArray[]")
        )
    }

    override fun close(){}
}



data class PaymentAnswer(
    @JsonProperty("orderId")
    var orderId: Long,
    @JsonProperty("status")
    val status: Status
)

enum class Status {
    PENDING,
    ACCEPTED,
    DENIED;
}