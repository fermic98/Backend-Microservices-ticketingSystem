package it.polito.wa2.payment_service.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.wa2.payment_service.dtos.Role
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtUtils {

    //@Value("\${application.jwt.jwtSecret}")
    private var jwtSecret: String = "Loremipsumdolorsitamet,consecteturadipiscingelit.Aliquamestex,accumsansitametmollisid,posuerevitaeurna.Inhachabitasseplatea dictumst.Vestibulumelitvelit,condimentumnonsapiensed,eleifendauctoraugue."

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

        val roles = rolesList.map { Role.valueOf(it) }.toSet()

        return UserDetailsJwt(bodySub, roles = roles)

    }

    fun generateJwt(sub : String, iat : Date, exp : Date): String {
        return Jwts.builder()
            .setSubject(sub.toString())
            .setIssuedAt(iat)
            .setExpiration(exp)
            .claim("roles", listOf(Role.SERVICE.toString(),Role.ADMIN.toString(), Role.CUSTOMER.toString()))
            .signWith(key)
            .compact()
    }

    fun getJwtTokenFromHeader(header: String): String{
        return header.split(" ")[1]
    }


}

data class UserDetailsJwt( val username : String, val roles : Set<Role> )