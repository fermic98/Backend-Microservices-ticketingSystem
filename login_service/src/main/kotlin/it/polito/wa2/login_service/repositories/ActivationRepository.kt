package it.polito.wa2.login_service.repositories

import it.polito.wa2.login_service.entities.Activation
import java.util.*
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface ActivationRepository : CrudRepository<Activation, UUID>{

    @Transactional(readOnly = true)
    fun findByProvisionalUserId(provisionalUserId: UUID) : Activation?

    @Transactional(readOnly = true)
    fun findActivationsByExpirationDateIsBefore(date: Date): Iterable<Activation>

}