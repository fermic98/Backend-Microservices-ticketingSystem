package it.polito.wa2.traveler_service.repositories

import it.polito.wa2.traveler_service.services.impl.entities.UserDetails
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserDetailsRepository : CrudRepository<UserDetails, Long> {

    @Transactional(readOnly = true)
    fun findByUsername(nickname: String): UserDetails?

    @Query("select ud.username from UserDetails ud")
    @Transactional(readOnly = true)
    fun findUsernames(): List<String>

}