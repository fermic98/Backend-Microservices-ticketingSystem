package it.polito.wa2.traveler_service.services

import it.polito.wa2.traveler_service.dtos.TransitDTO

interface TransitService {

        fun postTransit(transitDTO: TransitDTO)

}