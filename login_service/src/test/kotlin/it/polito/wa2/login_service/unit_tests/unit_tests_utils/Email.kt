package it.polito.wa2.login_service.unit_tests.unit_tests_utils

import java.util.*
import javax.mail.Address
import javax.mail.Message
import javax.mail.internet.MimeMultipart

class Email(message: Message){
    val from: String = getAddressString(message.from[0])
    val date: Date = message.sentDate
    val subject: String = message.subject
    val body: String =getBodyString(message.content)

    private fun getAddressString(addr: Address): String{
        val addressString = addr.toString()

        if(!addressString.contains('<')){
            return addressString
        }

        val startIndex = addressString.indexOf('<')
        val endIndex = addressString.indexOf('>')
        return addressString.substring(startIndex + 1, endIndex)
    }

    private fun getBodyString(bodyObj: Any): String{
        if (bodyObj is String){
            return bodyObj
        }

        val bodyMultipart = bodyObj as MimeMultipart
        return bodyMultipart.getBodyPart(0).content.toString()
    }
}