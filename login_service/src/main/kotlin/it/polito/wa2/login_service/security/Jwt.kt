package it.polito.wa2.login_service.security

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Serializer
import io.jsonwebtoken.security.Keys
import it.polito.wa2.login_service.dtos.UserDTO
import it.polito.wa2.login_service.entities.ERole


import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class Jwt {

    @Value("\${application.jwt.jwtSecret}")
    private lateinit var jwtSecret: String

    @Value("\${application.jwt.jwtExpirationMs}")
    var jwtExpirationMs: Long = -1

    @Value("\${application.jwt.jwtExpirationTurnstileMs}")
    var jwtExpirationTurnstile: Long = -1

    private val key: Key by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun validateJwt(authToken: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(3) // Allows a skew of 3 seconds between the timestamp inside the JWT
                //and the current one on the local machine
                .setSigningKey(key)
                .build()
                .parseClaimsJws(authToken)

            //Now we can safely trust the JWT (its integrity and authenticity)
            return true
        } catch (ex: Exception) {
            // we *cannot* use the JWT as authentication
            println(ex.message)
            return false
        }
    }


    fun getDetailsJwt(authToken: String): UserDetailsJwt {

        val jwtBody = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken).body

        val bodySub = jwtBody.subject
        val bodyRoles = jwtBody["roles"]

        if(bodySub == null || bodySub=="")
            throw Exception("Inavlid sub ")

        if(bodyRoles== null || bodyRoles=="")
            throw Exception("Inavlid roles")

        val rolesList = bodyRoles as List<String>

        if(rolesList.isEmpty())
            throw Exception("Empty roles list")

        val roles = rolesList.map { ERole.valueOf(it) }.toSet()

        return UserDetailsJwt(bodySub, roles = roles)

    }

    fun generateJwt(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserDTO

        var expiration : Long = jwtExpirationMs
        for (role in authentication.authorities) {
            if(role.toString()=="EMBEDDED_SYSTEM") {
                expiration = jwtExpirationTurnstile
            }
        }


        return Jwts.builder()
                .setSubject(userPrincipal.username)
                .setIssuedAt(Date())
                .setExpiration(Date(Date().time + expiration))
                .claim("roles", userPrincipal.authorities.map { it.toString() })
                .signWith(key)
                .compact()
    }

}

data class UserDetailsJwt( val username : String, val roles : Set<ERole> )