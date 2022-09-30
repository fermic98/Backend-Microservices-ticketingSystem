package it.polito.wa2.login_service.services

interface EmailService {
    fun sendEmail(toMail: String, subject: String, body: String): Boolean
}