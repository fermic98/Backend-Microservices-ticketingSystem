package it.polito.wa2.traveler_service.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**Let’s define a filter that executes once per request. So we create AuthTokenFilter class that extends
OncePerRequestFilter and override doFilterInternal() method.*/

@Component
class JwtAuthenticationTokenFilter : OncePerRequestFilter() {

    @Value( "\${application.jwt.jwtHeaderStart}" )
    lateinit var prefix: String

    @Value( "\${application.jwt.jwtHeader}" )
    lateinit var header: String

    @Autowired lateinit var jwtUtils : JwtUtils


    /**
     * What we do inside doFilterInternal():
    – get JWT from the Authorization header (by removing Bearer prefix)
    – if the request has JWT, validate it, parse username from it
    – from username, get UserDetails to create an Authentication object
    – set the current UserDetails in SecurityContext .
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authorizationHeader = request.getHeader(header)
            
            if (authorizationHeader == null || !authorizationHeader.startsWith(prefix)) {
                filterChain.doFilter(request, response)
                return
            }

            val jwtToken = authorizationHeader.replace(prefix, "").trim()

            if (!jwtUtils.validateJwt(jwtToken)) {
                filterChain.doFilter(request, response)
                return
            }


            /** So what is happening is that when a request is received , the JWT is extracted from the header, the signature
            is verified, and they fits correct, and so the content of the JWT body is trusted and used for populating
            the authentication*/

            //utente autenticato!
            //ora recupero le informazioni dell'utente da dentro al token,
            //dato che so che ora posso fidarmi perchè è un'autenticazione valida
            //e quindi contiene nel jwt info valide (affidabili e verificate)
            val userDetails = jwtUtils.getDetailsJwt(jwtToken)

            //creo un oggetto di tipo Authentication che popolo con i dati contenuti nel jwt sull'utente autenticato
            /** La versione del metodo UsernamePasswordAuthenticationToken che riceve invece 3 parametri
            * (principal e password e authorties) crea un oggetto Authentication (che l'Authentication Manager e Provider
             * andranno a gestire una volta iniettato nel SecurityContext) che ha un'implementazione del metodo
             * isAuthenticated() che ritorna true :
             * quindi sostanzialmente noi stiamo creando (e iniettando nel security context) una Authentication
             * che è già stata processata e validata (e sappiamo che è valida perchè il JWT è valido).
             * Se invece non lo è (JWT invalido) lancia l'eccezione, e nel Security Context non ci inietto nulla, e di
             * conseguenza l'autenticazione viene respinta dal filtro
             *
             */
            val authentication = UsernamePasswordAuthenticationToken(
                    userDetails.username,
                    null,
                    userDetails.roles
            )


            authentication.details = WebAuthenticationDetailsSource()
                    .buildDetails(request)
            //Una volta autenticato, inserisco nel SecurityContext l'autenticazione (l'identità dell'utente), che conterrà i dettagli
            //che saranno utili poi all'Authorization filter, che li leggerà e sulla base
            //delle Authotirties (permessi) dell'utente autenticato, gestirà le proprie decisioni
            SecurityContextHolder.getContext().authentication = authentication

            /**
             * Ora abbiamo settato l'Authentication dentro al Security Context, e tale
             * Authentication conterrà i dettagli sull'utente autenticato che saranno accessibili
             * dal prossimo filtro nella catena (che nel nostro caso sarà il filtro di Autenticazione),
             * utilizzando appunto il SecurityContext
             * */
        }catch (e: Exception){
            println(e.message)
        }

        //richiama il prossimo filtro nella catena/sequenza, o se è lui l'ultimo della catena
        //richiama la risorsa finale (l'endpoint)
        filterChain.doFilter(request, response)

    }

}