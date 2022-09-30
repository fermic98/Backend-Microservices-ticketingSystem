package it.polito.wa2.traveler_service.services.impl.entities

import java.util.Date
import javax.persistence.*

@Entity
class TicketAcquired(
    @Column(nullable = false, updatable = false)
    var issuedAt: Date,

    @Column(nullable = false, updatable = false)
    var validFrom: Date,

    @Column(nullable = false, updatable = false)
    var expiry: Date,

    @Column(nullable = false, updatable = false)
    var zoneId: String,

    @Column(nullable = false, updatable = false)
    var type: String,

    @Column(nullable = false, updatable = true)
    var jws: String = "",

    @ManyToOne(fetch=FetchType.LAZY)
    val userDetails: UserDetails

) : EntityBase<Long>() {}