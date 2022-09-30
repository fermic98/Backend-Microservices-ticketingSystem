package it.polito.wa2.login_service.dtos

import it.polito.wa2.login_service.entities.Activation
import org.jetbrains.annotations.NotNull
import java.util.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min


data class ActivationDTO(

    @field:NotNull
    val provisional_id: UUID?,

    @field:NotNull
    @field:Min(value = 0)
    @field:Max(value = 99999999)
    val activation_code: Long?

    //User is an Entity field (inside Activation), but not inside the ActivationDTO
    /*
    @field:Null
    val userDTO: UserDTO?
    */

) {}

fun Activation.toDTO(): ActivationDTO {
    return ActivationDTO(provisionalUserId, activationCode)
}
