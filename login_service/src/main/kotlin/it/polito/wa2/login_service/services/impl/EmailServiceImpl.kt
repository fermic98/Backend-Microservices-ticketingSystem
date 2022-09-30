package it.polito.wa2.login_service.services.impl

import it.polito.wa2.login_service.services.EmailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailServiceImpl : EmailService {

    @Autowired
    lateinit var mailSender: JavaMailSender

    @Value("politolab03@gmail.com")
    lateinit var fromMail : String

    override fun sendEmail(toMail: String, subject: String, body: String): Boolean {
        try {
            val mailMessage = SimpleMailMessage()
            mailMessage.setSubject(subject)
            mailMessage.setText(body)
            mailMessage.setTo(toMail)
            mailMessage.setFrom(fromMail)
            mailSender.send(mailMessage)
            return true
        }catch(ex: Exception){
            return false
        }
    }
}