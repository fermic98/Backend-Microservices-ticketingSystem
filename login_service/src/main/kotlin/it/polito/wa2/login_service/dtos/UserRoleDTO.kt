package it.polito.wa2.login_service.dtos

import it.polito.wa2.login_service.entities.ERole
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull


data class UserRoleDTO(
    @field:NotNull
//    @field:NotEmpty(message = "Used id must not be empty")
    val userId: Long?,

    @field:NotNull
//    @field:NotEmpty(message = "Role must not be empty")
    val role: ERole
) {}
