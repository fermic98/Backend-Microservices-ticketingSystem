package it.polito.wa2.payment_service.dtos

import it.polito.wa2.payment_service.entities.Status
import it.polito.wa2.payment_service.entities.Transaction
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

data class TransactionDTO(
    val transactionId: Long?,
    val amount: Float,
    val customer: String,
    val orderId: Long?=null,
    val date: String?,
    var status: Status = Status.PENDING,
    val creditCardNumber: String,
    val expirationDate: String,
    val cvv : String,
    var cardHolder: String,
)




fun Transaction.toDTO(): TransactionDTO {

    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

    val date = transactionDate.format(formatter)

    return TransactionDTO(transactionId, amount, customer, orderId, date , status, creditCardNumber, expirationDate, cvv, cardHolder)
}
