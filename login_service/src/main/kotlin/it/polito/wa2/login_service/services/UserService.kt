package it.polito.wa2.login_service.services

import it.polito.wa2.login_service.dtos.ActivationDTO
import it.polito.wa2.login_service.dtos.RegistrationRequestDTO
import it.polito.wa2.login_service.dtos.UserRoleDTO
import it.polito.wa2.login_service.dtos.UserDTO

interface UserService{
    fun createUser(userDTO: RegistrationRequestDTO) : ActivationDTO
    fun validateUser(activation: ActivationDTO): UserDTO
    fun addRole(userRoleDTO: UserRoleDTO)
    fun createEmbeddedSystem(userDTO: RegistrationRequestDTO) : UserDTO
}