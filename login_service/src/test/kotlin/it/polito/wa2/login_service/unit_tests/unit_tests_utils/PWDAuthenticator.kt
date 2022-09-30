package it.polito.wa2.login_service.unit_tests.unit_tests_utils

import org.springframework.beans.factory.annotation.Value
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication

class PWDAuthenticator: Authenticator(){

    @Value( "\${spring.mail.username}" )
    lateinit var email: String
    @Value( "\${spring.mail.password}" )
    lateinit var password: String

    @Override
    override fun getPasswordAuthentication(): PasswordAuthentication {
        return PasswordAuthentication(email, password)
    }
}