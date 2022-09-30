package it.polito.wa2.login_service.services.impl

import it.polito.wa2.login_service.dtos.*
import it.polito.wa2.login_service.entities.Activation
import it.polito.wa2.login_service.entities.ERole
import it.polito.wa2.login_service.entities.Role
import it.polito.wa2.login_service.entities.User
import it.polito.wa2.login_service.exceptions.*
import it.polito.wa2.login_service.repositories.ActivationRepository
import it.polito.wa2.login_service.repositories.RoleRepository
import it.polito.wa2.login_service.repositories.UserRepository
import it.polito.wa2.login_service.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*


@Service
@Transactional
@Configuration
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(name = ["scheduler.enabled"], matchIfMissing = true)
class UserServiceImpl : UserDetailsService, UserService {

    @Autowired
    lateinit var mailService: EmailServiceImpl

    @Autowired
    lateinit var activationRepository: ActivationRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder


    override fun loadUserByUsername(username: String): UserDTO {
        val user = userRepository.findByNickname(username) ?: throw UnauthorizedException("Username not found")
        if (!user.active) throw UnauthorizedException("User not active")
        return user.toDTO()
    }


    override fun createUser(userDTO: RegistrationRequestDTO): ActivationDTO {

        try {
            if (userRepository.findByNickname(userDTO.nickname) != null)
                throw BadRequestException("Username already in use")
            if (userRepository.findByEmail(userDTO.email) != null)
                throw BadRequestException("Email already in use")

            //encode the password before to store it
            val user = User(userDTO.nickname, passwordEncoder.encode(userDTO.password as String), userDTO.email)

            val savedUser = userRepository.save(user)

            val act = Activation(user)

            activationRepository.save(act) //save activation in DB

            val ret = mailService.sendEmail(
                savedUser.email, "Email verification",
                "Hi ${savedUser.nickname},\n" +
                        "This is your Activation Code : \n" +
                        "${act.activationCode} \n" +
                        "Use it to activate your account.\n" +
                        "Pay attention, this activation code will remain active up to 24 hours"
            )

            if (ret == false)
                throw BadRequestException("Problem Occurs during mail sending")

            return act.toDTO()
        } catch (ex: Exception) {
            throw BadRequestException(ex.message.toString())
        }
    }


    override fun validateUser(activation: ActivationDTO): UserDTO {
        try {
            /*
             * retrieve from repository the Activation starting from the provisionalId String.
             * Verify if it is not expired and enable the corresponding nickname.
             * If the activation record is not found or it is expired, throw an exception.
             */
            val act = activationRepository.findByProvisionalUserId(activation.provisional_id as UUID)
            if (act == null)
                throw NotFoundException("Provisional Id Not Found")
            if (act.isExpired()) {
                userRepository.delete(act.user)
                activationRepository.delete(act)
                throw NotFoundException("Activation has expired")
            }


            //In case of existing a not expired activation, but mismatched act. code
            if (act.activationCode != activation.activation_code) {
                if (act.attemptCounter == 1) {
                    userRepository.delete(act.user)
                    activationRepository.delete(act)
                } else {
                    act.attemptCounter -= 1
                    activationRepository.save(act)
                }
                throw NotFoundException("Mismatching activation code")
            }

            activationRepository.delete(act)
            act.user.active = true
            userRepository.save(act.user)

            return act.user.toDTO()
        } catch (ex: Exception) {
            throw NotFoundException(ex.message.toString())
        }
    }

    override fun createEmbeddedSystem(userDTO: RegistrationRequestDTO) : UserDTO{

        try {
            if (userRepository.findByNickname(userDTO.nickname) != null)
                throw BadRequestException("Username already in use")
            if (userRepository.findByEmail(userDTO.email) != null)
                throw BadRequestException("Email already in use")

            //encode the password before to store it
            val embeddedSystemRole = roleRepository.findByName(ERole.EMBEDDED_SYSTEM)
            if (embeddedSystemRole == null)
                throw NotFoundException("Role not found")

            val user = User(
                userDTO.nickname,
                passwordEncoder.encode(userDTO.password as String),
                userDTO.email,
                roles = mutableSetOf(embeddedSystemRole.get()),
                active = true
            )

            val savedUser = userRepository.save(user)

            val ret = mailService.sendEmail(
                savedUser.email, "Email verification",
                "Embedded system with name: ${savedUser.nickname}\n"
                        + "CREATED\n"
            )

            if (ret == false)
                throw BadRequestException("Problem Occurs during mail sending")

            return user.toDTO()

        } catch (ex: Exception) {
            throw BadRequestException(ex.message.toString())
        }
    }


    override fun addRole(userRoleDTO: UserRoleDTO) {
        try {

            if (userRoleDTO.userId == null)
                throw BadRequestException("No user id inserted")

            if (userRoleDTO.role == ERole.EMBEDDED_SYSTEM)
                throw BadRequestException("${ERole.EMBEDDED_SYSTEM} cannot be added to a human users")

            val user = userRepository.findById(userRoleDTO.userId)
            val newRole = roleRepository.findByName(userRoleDTO.role)
            if (user.isEmpty || newRole!!.isEmpty)
                throw BadRequestException("Wrong json fields")


            /* Main logic in adding roles - except EMBEDDED_SYSTEM */
            /*
                - ADMIN_E is also ADMIN
                - CUSTOMER that becomes ADMIN, is still a CUSTOMER
                - CUSTOMER that becomes ADMIN_E, is also Customer and ADMIN
            */

            val userRoles = user.get().roles
            val existingRoles = roleRepository.findAll()

            /* new role already owned by the user */
            if (userRoles.contains<Role>(newRole.get()))
                throw BadRequestException("Wrong data fields")

            /* An EMBEDDED_SYSTEM cannot have other roles */
            if (userRoles.any { it.name == ERole.EMBEDDED_SYSTEM })
                throw BadRequestException("An ${ERole.EMBEDDED_SYSTEM} cannot have other roles")

            if (userRoleDTO.role == ERole.ADMIN) {
                user.get().addRole(newRole.get())
            } else if (userRoleDTO.role == ERole.ADMIN_E) {
                /* force an ADMIN_E to be also an ADMIN */
                if (!userRoles.any { it.name == ERole.ADMIN }) {
                    user.get().addRole((existingRoles.filter { it.name == ERole.ADMIN })[0])
                }
                /* adding ADMIN_E as role */
                user.get().addRole(existingRoles.filter { it.name == ERole.ADMIN_E }[0])
            }

        } catch (ex: Exception) {
            throw BadRequestException(ex.message.toString())
        }

    }


    @Scheduled(fixedRate = 86400000)// 24hours expressed in milliseconds
    @Async
    @Transactional
    fun pruningExpiredRegistrationData() {
        val expiredActivations =
            activationRepository.findActivationsByExpirationDateIsBefore(Date(System.currentTimeMillis()))

        if (expiredActivations.toList().isEmpty())
            return

        for (act in expiredActivations) {
            userRepository.delete(act.user)
            activationRepository.delete(act)
        }
    }


}
