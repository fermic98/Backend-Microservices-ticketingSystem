package it.polito.wa2.traveler_service.services.impl.entities


import java.util.Date
import javax.persistence.*

@Entity
class UserDetails(
    @Id
    @Column(updatable = false, nullable = false, unique = true)
    val username: String? = null,

    @Column(nullable = true, updatable = true)
    var name: String? = null,

    @Column(nullable = true, updatable = true)
    var address: String? = null,

    @Column(nullable = true, updatable = true)
    var date_of_birth: Date? = null,

    @Column(nullable = true, updatable = true)
    var telephone_number: String? = null,

    @OneToMany(mappedBy = "userDetails", fetch = FetchType.LAZY)
    val tickets: List<TicketAcquired>? = null,

    @OneToMany(mappedBy = "userDetails", fetch = FetchType.LAZY)
    val transits: List<Transit>? = null
)