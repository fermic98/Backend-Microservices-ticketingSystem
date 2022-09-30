package it.polito.wa2.login_service.controllers

import it.polito.wa2.login_service.dtos.ActivationDTO
import it.polito.wa2.login_service.dtos.LoginRequestDTO
import it.polito.wa2.login_service.dtos.RegistrationRequestDTO
import it.polito.wa2.login_service.dtos.UserRoleDTO
import it.polito.wa2.login_service.exceptions.BadRequestException
import it.polito.wa2.login_service.security.Jwt
import it.polito.wa2.login_service.services.impl.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid


@RestController
@RequestMapping("/login")
class RegistrationController() {

    @Value("\${application.jwt.jwtHeaderStart}")
    lateinit var prefix: String

    @Autowired
    lateinit var jwt: Jwt

    @Autowired
    lateinit var userService: UserServiceImpl

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @PostMapping("/user/register")
    @ResponseBody
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun userRegistration(
        @RequestBody @Valid userDTO: RegistrationRequestDTO,
        bindingResult: BindingResult
    ): UserRegistrationResponseBody {

        if (bindingResult.hasErrors())
            throw BadRequestException("Wrong json fields")

        //creating user
        val activationDTO = userService.createUser(userDTO)

        return UserRegistrationResponseBody(activationDTO.provisional_id, userDTO.email)

    }

    @PostMapping("/user/validate")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    fun userValidation(
        @RequestBody @Valid activationDTO: ActivationDTO,
        bindingResult: BindingResult
    ): UserValidationResponseBody {

        if (bindingResult.hasErrors())
            throw BadRequestException("Wrong json fields")

        val user = userService.validateUser(activationDTO)

        return UserValidationResponseBody(user.userId, user.nickname, user.email)
    }


    @PostMapping("/user/login")
    fun login(
        @RequestBody @Valid loginRequest: LoginRequestDTO, bindingResult: BindingResult,
        response: HttpServletResponse
    ) {
        if (bindingResult.hasErrors())
            throw BadRequestException(bindingResult.fieldErrors.joinToString())

        /**
         * La versione del metodo UsernamePasswordAuthenticationToken che riceve solo due parametri
         * (principal e password) crea un oggetto Authentication (che l'Authentication Manager e Provider
         * andranno a gestire) che ha un'implementazione del metodo isAuthenticated() che ritorna false :
         * da quanto si evince l'authentication manager  una volta iniettata l'Authentication nel Security Context sfrutta
         * questo metodo per capire se è una authentication
         * non valida (se è false) o è già autenticato (se è true).
         * Quindi sta ricevendo un Authentication che non è ancora stata processata, e ne fa l'autenticazione
         * utilizzando in questo caso uno userDetailsService (loadUserByUsername)
         * Se authenticate fallisce, non ritorna alcuna autenticazione ma lancia l'eccezione; se invece va in porto
         * , ritorna un Authentication popolata con i dettagli dell'utente autenticato, che sfrutterò per generare il JWT
         */
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.nickname, loginRequest.password)
        )

        response.setHeader("Authorization", "$prefix ${jwt.generateJwt(authentication)}")

        //return (authentication.principal as UserDetailsDTO).copy(password = null)*/
    }


    @PostMapping("/admin/embedded_systems/register")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.login_service.entities.ERole).ADMIN_E)")
    @ResponseBody
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun embeddedSystemRegistration(
        @RequestBody @Valid userDTO: RegistrationRequestDTO,
        bindingResult: BindingResult
    ): EmbeddedSystemRegistrationResponseBody {

        if (bindingResult.hasErrors())
            throw BadRequestException("Wrong json fields")

        //creating user
//        val activationDTO = userService.createEmbeddedSystem(userDTO)
        val userDTO = userService.createEmbeddedSystem(userDTO)

        return EmbeddedSystemRegistrationResponseBody(userDTO.nickname, userDTO.email)

    }

    @PutMapping("/admin/users/{userId}")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.login_service.entities.ERole).ADMIN_E)")
    fun addRole(
        @RequestBody @Valid userRoleDTO: UserRoleDTO,
        @PathVariable("userId") userId: Long, //<--- TODO: could be useless due to UserRoleDTO and binding result not null for userId
        bindingResult: BindingResult
    ) {

        if (bindingResult.hasErrors())
            throw BadRequestException("Wrong json fields")

        val userRole = UserRoleDTO(userId, userRoleDTO.role)
        println(userRole)
        userService.addRole(userRole)
    }
}

data class UserRegistrationResponseBody(val provisional_id: UUID?, val email: String)
data class EmbeddedSystemRegistrationResponseBody(val nickname: String, val email: String)

data class UserValidationResponseBody(val userId: Long?, val nickname: String, val email: String)