package com.byeduck.ceneoscrapper

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@Aspect
@Component
class CachingAspect(
    @Value("\${scrap.cache-ttl}") private val ttl: Duration,
    private val redisOperations: ReactiveRedisOperations<String, Any>
) {

    @Around("@annotation(CacheMono)")
    fun useCache(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = proceedingJoinPoint.signature as MethodSignature
        val annotation = methodSignature.method.getDeclaredAnnotation(CacheMono::class.java)
        val cacheKey =
            annotation.key.ifBlank { computeCacheKeyFromParameters(proceedingJoinPoint.args) }
        return redisOperations.opsForValue().get(cacheKey)
            .filter(Objects::nonNull)
            .switchIfEmpty(cacheAndReturn(cacheKey, proceedingJoinPoint))
    }

    fun cacheAndReturn(cacheKey: String, proceedingJoinPoint: ProceedingJoinPoint): Mono<*> {
        return when (val returned = proceedingJoinPoint.proceed()) {
            is Mono<*> -> returned.flatMap { r ->
                redisOperations.opsForValue().set(cacheKey, r, ttl).map { r }
            }
            else -> throw IllegalArgumentException("Returned type must be Mono")
        }
    }

    fun computeCacheKeyFromParameters(parameters: Array<Any>): String =
        parameters.joinToString(":", transform = Any::toString)
}
