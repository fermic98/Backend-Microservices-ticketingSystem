package it.polito.wa2.login_service.unit_tests

import it.polito.wa2.login_service.dtos.ActivationDTO
import it.polito.wa2.login_service.dtos.RegistrationRequestDTO
import it.polito.wa2.login_service.dtos.UserDTO
import it.polito.wa2.login_service.dtos.toDTO
import it.polito.wa2.login_service.entities.Activation
import it.polito.wa2.login_service.entities.ERole
import it.polito.wa2.login_service.entities.Role
import it.polito.wa2.login_service.entities.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class DtosUnitTests {

    @Test
    fun matchingUserToDto() {
        val userEntity = User("jack", "passW0rD", "email@gmail.com")

        val expectedUserDto = UserDTO(null, "jack", "passW0rD", "email@gmail.com",userEntity.roles,false)

        Assertions.assertEquals(expectedUserDto, userEntity.toDTO())
    }

    @Test
    fun matchingActivationToDto() {
        val userEntity = User("jack", "passW0rD", "email@gmail.com")
        val activationEntity = Activation(userEntity)
        activationEntity.provisionalUserId = UUID.randomUUID()

        val expectedActivationDto = ActivationDTO(activationEntity.provisionalUserId, activationEntity.activationCode)

        Assertions.assertEquals(expectedActivationDto, activationEntity.toDTO())
    }


    @Test
    fun matchingActivationWithNullProvisionalUserIdToDto() {
        val userEntity = User()
        val activationEntity = Activation(userEntity)

        val expectedActivationDto = ActivationDTO(null, activationEntity.activationCode)

        Assertions.assertEquals(expectedActivationDto, activationEntity.toDTO())
    }


    @Test
    fun notMatchingActivationToDto() {
        val userEntity = User()
        val activationEntity = Activation(userEntity)

        val expectedActivationDto = ActivationDTO(null, null)

        Assertions.assertNotEquals(expectedActivationDto, activationEntity.toDTO())

    }

}