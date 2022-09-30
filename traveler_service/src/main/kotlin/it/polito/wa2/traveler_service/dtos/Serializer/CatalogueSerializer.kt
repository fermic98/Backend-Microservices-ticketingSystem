package it.polito.wa2.traveler_service.dtos.Serializer

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.LoggerFactory

class CatalogueSerializer : Serializer<UserDetailsDTO2> {
    private val objectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun serialize(topic: String?, data: UserDetailsDTO2?): ByteArray? {
        log.info("Serializing...")
        return objectMapper.writeValueAsBytes(
            data ?: throw SerializationException("Error when serializing Product to ByteArray[]")
        )
    }

    override fun close(){}
}



data class UserDetailsDTO2(
    @JsonProperty("orderId")
    var orderId: Long,
    @JsonProperty("existing")
    val existing: Boolean,
    @JsonProperty("username")
    var username: String,
    @JsonProperty("name")
    var name: String?,
    @JsonProperty("address")
    var address: String?,
    @JsonProperty("telephone_number")
    var telephone_number: String?,
    @JsonProperty("date_of_bith")
    var date_of_birth: String?
)