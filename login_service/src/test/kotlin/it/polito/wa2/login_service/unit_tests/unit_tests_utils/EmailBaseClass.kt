package it.polito.wa2.login_service.unit_tests.unit_tests_utils

import com.sun.mail.util.MailSSLSocketFactory
import it.polito.wa2.login_service.services.impl.EmailServiceImpl
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import java.util.*
import javax.mail.Folder
import javax.mail.Session
import javax.mail.Store

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class EmailBaseClass {
    @Autowired
    lateinit var mailService: EmailServiceImpl

    // Email session info
    @Value("\${spring.mail.username}" )
    lateinit var email: String
    @Value("\${spring.mail.password}")
    lateinit var password: String

    lateinit var store: Store
    lateinit var inbox: Folder


    @BeforeAll
    fun mailInit(){
        val sf = MailSSLSocketFactory()
        sf.isTrustAllHosts = true

        val props = Properties()
        props["mail.pop3.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.pop3.socketFactory.fallback"] = "false"
        props["mail.pop3.socketFactory.port"] = "995"
        props["mail.pop3.port"] = "995"
        props["mail.pop3.host"] = "pop.gmail.com"
        props["mail.pop3.user"] = email
        props["mail.pop3.ssl.trust"] = "*"
        props["mail.pop3.ssl.socketFactory"] = sf
        props["mail.store.protocol"] = "pop3"

        val auth = PWDAuthenticator()
        val session = Session.getDefaultInstance(props, auth)
        store = session.getStore("pop3")
        store.connect("pop.gmail.com", 995, email, password)

        inbox = store.getFolder("INBOX")

    }

    @AfterAll
    fun mailCloseConnection(){
        store.close()
    }

    // This function is needed because, in order to know if the email reached the server,
    // the old number of emails must be compared to the current one.
    // In order to read the current state of the inbox, it must be opened before reading and closed right after.
    fun sendAndReceiveEmail(sendEmail: () -> Boolean): Email {
        val oldMessageNum = getCurrentEmailNum()

        sendEmail()

        return getLastEmail(oldMessageNum)
    }

    fun getLastEmail(oldMessageNum: Int) : Email {
        var newMessageNum = oldMessageNum
        while (newMessageNum == oldMessageNum) {
            Thread.sleep(1000)
            newMessageNum = getCurrentEmailNum()
        }

        inbox.open(Folder.READ_ONLY)
        val email = Email(inbox.messages.last())
        inbox.close()
        return email
    }

    // In order to read the current state of the inbox, it must be opened before reading and closed right after.
    fun getCurrentEmailNum(): Int{
        inbox.open(Folder.READ_ONLY)
        val tmp = inbox.messageCount
        inbox.close()

        return tmp
    }
}