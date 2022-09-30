package it.polito.wa2.login_service.repositories

import it.polito.wa2.login_service.entities.ERole
import it.polito.wa2.login_service.entities.Role
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface RoleRepository : CrudRepository<Role, Long> {

    @Transactional(readOnly = true)
    fun findByName(name: ERole?): Optional<Role?>?

}