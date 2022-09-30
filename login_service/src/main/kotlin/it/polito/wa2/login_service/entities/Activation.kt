package it.polito.wa2.login_service.entities


import org.hibernate.annotations.GenericGenerator
import org.springframework.data.util.ProxyUtils
import java.util.*
import javax.persistence.*
import kotlin.random.Random

@Entity
class Activation(@OneToOne var user : User){
    private val expirationTime: Long = 86400000 // 1440 * 60 * 1000 = 1440 minutes (1 day, 24 hours) expressed in milliseconds

    @Id
    @Column(updatable = false, nullable = false, unique = true)
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    var provisionalUserId : UUID? = null

    @Column(nullable = false, updatable = false)
    val activationCode: Long? = getRandomActivationCode().toLong()

    @Column(nullable = false)
    var attemptCounter : Int = 5

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    val expirationDate: Date = Date(System.currentTimeMillis() +  expirationTime)



    fun isExpired(): Boolean {
        val timeLeft = this.expirationDate.time - Date().time
        return timeLeft <= 0
    }

    override fun toString(): String {
        return "Entity ${this.javaClass.name} with ID [${provisionalUserId}]"
    }

    override fun equals(other: Any?): Boolean {
        if(other == null) return false
        if(other === this) return true
        if(javaClass != ProxyUtils.getUserClass(other)) return false

        other as Activation

        return  if(provisionalUserId == null) false
        else this.provisionalUserId == other.provisionalUserId
    }

    override fun hashCode(): Int {
        return 31
    }

    //create an 8 digits activation code
    private fun getRandomActivationCode(): String {
        // It will generate 8 digit random Number.
        // from 0 to 99999999
        val rnd = Random
        val number: Int = rnd.nextInt(99999999)

        // this will convert any number sequence into 6 character.
        return String.format("%08d", number)
    }


}

