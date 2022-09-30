package it.polito.wa2.ticket_catalogue_service.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono


@Component
class JwtReactiveAuthenticationFilter : WebFilter {

    @Autowired
    lateinit var jwtUtils : JwtUtils



    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {

        //this endpoint doesn't require any jwt authentication
        if(exchange.request.path.pathWithinApplication().toString()=="/tickets")
            return chain.filter(exchange)

        val jwt = extractJwt(exchange.request)
        if (jwt!=null && jwtUtils.validateJwt(jwt)) {
            val user = jwtUtils.getDetailsJwt(jwt)
            val authentication: Authentication = UsernamePasswordAuthenticationToken(user.username, null, user.roles)
            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
        }
        return chain.filter(exchange)
    }

    private fun extractJwt(request: ServerHttpRequest): String? {
        val authorizationHeader = request.headers.getFirst("Authorization")
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer")) {
            return null
        }
        else
            return return jwtUtils.getJwtTokenFromHeader(authorizationHeader)

    }

}


//BEFORE WAS :
/*
@Component
class JwtAuthenticationManager : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
    }
}
@Component
class JwtServerAuthenticationConverter(private val jwtUtils: JwtUtils) : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange?): Mono<Authentication> {



        val result = exchange?.request?.headers?.get("Authorization")
            ?.firstOrNull {
                it.startsWith("Bearer")
            }
            ?.let {
                val jwt = jwtUtils.getJwtTokenFromHeader(it)
                if (jwtUtils.validateJwt(jwt))
                    jwtUtils.getDetailsJwt(jwt)
                else
                    null
            }
        return Mono.justOrEmpty(result)
            .map { UsernamePasswordAuthenticationToken(it.username, null, it.roles) }
    }
}
 */

