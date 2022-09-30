package it.polito.wa2.payment_service.unit_tests

import com.fasterxml.jackson.core.JsonParseException
import it.polito.wa2.payment_service.dtos.TransactionDTO
import it.polito.wa2.payment_service.dtos.deserializer.PaymentRequestDeserializer
import it.polito.wa2.payment_service.dtos.toDTO
import it.polito.wa2.payment_service.entities.Status
import it.polito.wa2.payment_service.entities.Transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.wa2.payment_service.dtos.PaymentInfoDTO
import org.apache.kafka.common.errors.SerializationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows


/**
 *
 * **/
@SpringBootTest
class DtosUnitTests {

    @Test
    fun matchingTransactionToDto() {
        val transactionEntity = Transaction(
            1,
            2f,
            "testCustomer",
            1,
            LocalDateTime.now(),
            Status.PENDING,
            "asbcdefghil",
            "04-04-2024",
            "789",
            "Test Customer"
        )

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

        val date = LocalDateTime.now().format(formatter)

        val expectedTransactionDto = TransactionDTO(
            1,
            2f,
            "testCustomer",
            1,
            date,
            Status.PENDING,
            "asbcdefghil",
            "04-04-2024",
            "789",
            "Test Customer"
        )

        Assertions.assertEquals(expectedTransactionDto, transactionEntity.toDTO())
    }

    private val mapper: ObjectMapper = ObjectMapper()
    private lateinit var paymentRequestDeserializer: PaymentRequestDeserializer

    @BeforeEach
    fun setup() {
        paymentRequestDeserializer = PaymentRequestDeserializer()
    }

    @Test
    fun validPaymentRequestTest() {
        val paymentInfoDTO = PaymentInfoDTO(5f, "abcdefghil", "02/2024", "798", "test Customer", "testCustomer", 1)

        val deserializedProduct: PaymentInfoDTO?  = paymentRequestDeserializer.deserialize(
            "",
            mapper.writeValueAsBytes(paymentInfoDTO)
        )

        assertEquals(paymentInfoDTO, deserializedProduct)
    }

    @Test
    fun invalidPaymentRequestTest() {
        val byteArray: ByteArray = "Teste".toByteArray()

        assertThrows<JsonParseException> {
            paymentRequestDeserializer.deserialize(
                "",
                byteArray
            )
        }
    }

    @Test
    fun nullPaymentRequestTest() {
        var exception = assertThrows<SerializationException> {
            paymentRequestDeserializer.deserialize(
                "product",
                null
            )
        }
    }

}