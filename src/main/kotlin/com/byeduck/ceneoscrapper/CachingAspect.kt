package com.byeduck.ceneoscrapper

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Aspect
@Component
class CachingAspect(private val redisOperations: ReactiveRedisOperations<String, Any>) {

    private val logger: Logger = LoggerFactory.getLogger(CachingAspect::class.java)

    @Around("@annotation(UseCache)")
    fun useCache(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = proceedingJoinPoint.signature as MethodSignature
        val annotation = methodSignature.method.getDeclaredAnnotation(UseCache::class.java)
        val cacheKey =
            annotation.key.ifBlank { computeCacheKeyFromParameters(proceedingJoinPoint.args) }
        return redisOperations.opsForValue().get(cacheKey)
            .switchIfEmpty(cacheAndReturn(cacheKey, proceedingJoinPoint))
    }

    fun cacheAndReturn(cacheKey: String, proceedingJoinPoint: ProceedingJoinPoint): Mono<*> {
        return when (val returned = proceedingJoinPoint.proceed()) {
            is Mono<*> -> returned.flatMap { r -> redisOperations.opsForValue().set(cacheKey, r).map { r } }
            else -> throw IllegalArgumentException("Returned type must be Mono")
        }
    }

    fun computeCacheKeyFromParameters(parameters: Array<Any>): String =
        parameters.joinToString(":", transform = Any::toString)
}
