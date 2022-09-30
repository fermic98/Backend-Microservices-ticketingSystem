package it.polito.wa2.traveler_service.dtos


import it.polito.wa2.traveler_service.services.impl.entities.UserDetails
import java.text.SimpleDateFormat
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class UserDetailsDTO(
    @field:Size(max = 30, message = "name is too long")
    var username: String? = null,

    @field:Size(max = 30, message = "name is too long")
    var name: String? = null,

    @field:Size(max = 30, message = "address is too long")
    var address: String? = null,

    @field:Size(max = 13, message = "telephon_number is too long")
    var telephone_number: String? = null,

    //contraints for date
    @field:Pattern(regexp = "([0-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-[0-9]{4}")
    var date_of_birth: String? = null,
) {}

fun UserDetails.toDTO(): UserDetailsDTO {

    val formatter = SimpleDateFormat("dd-MM-yyyy")
    var date : String? = null
    if(date_of_birth!=null)
        date = formatter.format(date_of_birth)
    return UserDetailsDTO(username, name, address, telephone_number, date)
}