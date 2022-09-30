package it.polito.wa2.traveler_service.services.impl

import it.polito.wa2.traveler_service.dtos.TransitDTO
import it.polito.wa2.traveler_service.dtos.UserDetailsDTO
import it.polito.wa2.traveler_service.services.impl.entities.Transit
import it.polito.wa2.traveler_service.repositories.TransitRepository
import it.polito.wa2.traveler_service.services.TransitService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import it.polito.wa2.traveler_service.services.impl.entities.UserDetails
import java.text.SimpleDateFormat
import java.util.*

@Service
@Transactional
class TransitServiceImpl : TransitService{
    @Autowired
    lateinit var transitRepository: TransitRepository

    @Autowired
    lateinit var userDetailsService: UserDetailsServiceImpl



    override fun postTransit(transitDTO: TransitDTO) {

        val formatter = SimpleDateFormat("dd-MM-yyyy")
        var date : Date? = null

        val userDetailsDTO : UserDetailsDTO = userDetailsService.getUserProfile(transitDTO.username)

        if(userDetailsDTO.date_of_birth!=null && userDetailsDTO.date_of_birth!="")
            date = formatter.parse(userDetailsDTO.date_of_birth)

        val userDetailsEntity = UserDetails(
            userDetailsDTO.username,
            userDetailsDTO.name,
            userDetailsDTO.address,
            date,
            userDetailsDTO.telephone_number
        )

        transitRepository.save(Transit(null, transitDTO.timestamp!!, userDetailsEntity))

    }
}