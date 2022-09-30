package it.polito.wa2.login_service.interceptors

import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.Any
import kotlin.Boolean
import kotlin.Exception
import kotlin.Long
import kotlin.Throws

class RateLimitInterceptor : HandlerInterceptor {

    private val ratelimiterBucket : RateLimiterBucket = RateLimiterBucket()

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        //Try to get one token from the Bucket if available
        val tokenBucket : Bucket = ratelimiterBucket.resolveBucket()
        val probe: ConsumptionProbe = tokenBucket.tryConsumeAndReturnRemaining(1)

        //return true if the token was available, false otherwise
        return if (probe.isConsumed) {
            response.addHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())
            true
        } else {
            val waitForRefill: Long = probe.nanosToWaitForRefill / 1000000000
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", waitForRefill.toString())
            response.sendError(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "You have exhausted your API Request Quota"
            )
            false
        }
    }
}