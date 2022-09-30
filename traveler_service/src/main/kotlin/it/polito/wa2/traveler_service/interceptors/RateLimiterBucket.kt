package it.polito.wa2.traveler_service.interceptors

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RateLimiterBucket {
    private final var bucket: Bucket

    // initializing Bucket
    init {
        //defining the limit of 10 requests per second
        val limit: Bandwidth = Bandwidth.classic(10, Refill.greedy(10, Duration.ofSeconds(1)))
        bucket = Bucket.builder()
            .addLimit(limit)
            .build()
    }

    fun resolveBucket(): Bucket {
        return bucket
    }

}