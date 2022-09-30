package it.polito.wa2.login_service.repositories
import it.polito.wa2.login_service.entities.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface UserRepository : CrudRepository<User, Long>{

    @Transactional(readOnly = true)
    fun findByNickname(nickname: String): User?

    @Transactional(readOnly = true)
    fun findByEmail(email: String): User?

}