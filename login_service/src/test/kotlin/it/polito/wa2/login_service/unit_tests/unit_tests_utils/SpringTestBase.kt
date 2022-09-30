package it.polito.wa2.login_service.unit_tests

import it.polito.wa2.login_service.entities.Activation
import it.polito.wa2.login_service.entities.ERole
import it.polito.wa2.login_service.entities.Role
import it.polito.wa2.login_service.entities.User
import it.polito.wa2.login_service.repositories.ActivationRepository
import it.polito.wa2.login_service.repositories.RoleRepository
import it.polito.wa2.login_service.repositories.UserRepository

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

@SpringBootTest
class SpringTestBase {

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var  activationRepository: ActivationRepository
    @Autowired lateinit var roleRepository: RoleRepository


    protected lateinit var user1: User
    protected lateinit var activation1: Activation

    protected lateinit var user2: User
    protected lateinit var activation2: Activation

    protected lateinit var user3: User
    protected lateinit var activation3: Activation

    protected lateinit var user4: User
    protected lateinit var activation4: Activation

    protected lateinit var user5: User
    protected lateinit var activation5: Activation

    protected lateinit var user6: User

    protected lateinit var user7: User

    @BeforeAll
    fun setup(){
        activationRepository.deleteAll()
        userRepository.deleteAll()

        roleRepository.save(Role(1, ERole.CUSTOMER))

        user1 = User(
                "prova1",
                "password",
                "polito@gmail.com",
                null,
                false
        )
        user1 = userRepository.save(user1)

        activation1 = Activation(user1)
        activation1 = activationRepository.save(activation1)

        user2 = User(
                "prova2",
                "password2",
                "polito2@gmail.com",
                null,
                false
        )
        user2 = userRepository.save(user2)

        activation2 = Activation(user2)
        activation2 = activationRepository.save(activation2)

        user3 = User(
                "prova3",
                "password3",
                "polito3@gmail.com",
                null,
                false
        )
        user3 = userRepository.save(user3)

        activation3 = Activation(user3)
        Activation::class.java.getDeclaredField("expirationDate").let {
            it.isAccessible = true
            it.set(activation3, Date(System.currentTimeMillis() - 86400001))
        }
        activation3 = activationRepository.save(activation3)



        user4 = User(
                "prova4",
                "password4",
                "polito4@gmail.com",
                null,
                false
        )

        user4 = userRepository.save(user4)

        activation4 = Activation(user4)
        activation4 = activationRepository.save(activation4)


        user5 = User(
                "prova5",
                "password5",
                "polito5@gmail.com",
                null,
                false
        )

        user5 = userRepository.save(user5)

        activation5 = Activation(user5)
        activation5 = activationRepository.save(activation5)


        user6 = User(
                "prova6",
                "password6",
                "polito6@gmail.com",
                null,
                true
        )

        user6 = userRepository.save(user6)
    }

    @AfterAll
    fun clearDb(){
        activationRepository.deleteAll()
        userRepository.deleteAll()
    }

}