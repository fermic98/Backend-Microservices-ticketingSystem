package it.polito.wa2.traveler_service.services.impl.entities

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "transits")
class Transit(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false, unique = true)
    private var id: Integer? = null,

    @Column(nullable = false)
    var timestamp: LocalDateTime,

    @ManyToOne(fetch=FetchType.LAZY)
    val userDetails: UserDetails

){}

