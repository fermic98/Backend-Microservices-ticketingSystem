package it.polito.wa2.traveler_service.security

import org.springframework.stereotype.Component
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import java.io.IOException


/**
 * Now we create AuthenticationEntryPointImpl class that implements AuthenticationEntryPoint interface.
 * Then we override the commence() method. This method will be triggerd anytime unauthenticated User requests
 * a secured HTTP resource and an AuthenticationException is thrown.
 * **/
@Component
class AuthenticationEntryPointImpl : AuthenticationEntryPoint {

    private val logger: Logger = LoggerFactory.getLogger(AuthenticationEntryPointImpl::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun commence(request: HttpServletRequest?, response: HttpServletResponse,
                          authException: AuthenticationException) {
        logger.error("Unauthorized error: {}", authException.message)
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized")
    }
}