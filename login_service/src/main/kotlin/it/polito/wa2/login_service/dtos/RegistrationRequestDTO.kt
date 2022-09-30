package it.polito.wa2.login_service.dtos

import it.polito.wa2.login_service.annotations.ValidPassword
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class RegistrationRequestDTO(
        val userId : Long?,

        @field:Size(max = 30, message = "username is too long")
        @field:NotEmpty(message = "nickname must not be empty")
        @field:NotNull
        var nickname : String,

        @field:NotEmpty(message = "password must not be empty")
        @field:NotNull
        @ValidPassword
        val password : String?,

        @field:Email(message = "invalid email")
        @field:NotEmpty(message = "email must not be empty")
        @field:NotNull
        var email : String
        ){}
