package it.polito.wa2.traveler_service.services

import it.polito.wa2.traveler_service.dtos.TicketAcquiredDTO
import it.polito.wa2.traveler_service.dtos.UserDetailsDTO
import it.polito.wa2.traveler_service.dtos.PurchaseTicketDTO


interface UserDetailsService {
    fun getUserProfile(username : String) : UserDetailsDTO
    fun putUserProfile(userDetailsDTO: UserDetailsDTO) : UserDetailsDTO
    fun getUserTickets(username: String) : List<TicketAcquiredDTO>
    fun getTicketById(ticketId : Long, username: String) : TicketAcquiredDTO
    //fun postUserTickets(username: String, purchaseTicketDTO: PurchaseTicketDTO)/* : List<TicketAcquiredDTO>*/

    //admin services
    fun getTravelers(): List<String>

}