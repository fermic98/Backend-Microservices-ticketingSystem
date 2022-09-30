package it.polito.wa2.payment_service.config

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin

@Configuration
class KafkaConfig(
    @Value("\${kafka.bootstrapAddress}")
    private val servers: String,

    @Value("\${kafka.topics.payment_answer}")
    private val topicBankCheckAnswer: String,

    @Value("\${kafka.topics.generate_ticket}")
    private val topicGenerateTicket: String,
) {

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs: MutableMap<String, Any?> = HashMap()
        configs[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
        return KafkaAdmin(configs)
    }

    @Bean
    fun newtopic(): NewTopic {
        return NewTopic(topicBankCheckAnswer, 1, 1.toShort())
    }

    @Bean
    fun newtopicGenerateTicket(): NewTopic {
        return NewTopic(topicGenerateTicket, 1, 1.toShort())
    }
}