package com.byeduck.ceneoscrapper

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.reactive.function.client.WebClient
import org.zalando.logbook.Logbook


@Configuration
@EnableConfigurationProperties(ScrapProperties::class)
@EnableAspectJAutoProxy
@EnableRedisRepositories
class ScrapConfig {

    @Bean
    fun scrapWebClient(webClientBuilder: WebClient.Builder, scrapProperties: ScrapProperties) = webClientBuilder
        .codecs { it.defaultCodecs().maxInMemorySize(1 * 1024 * 1024) }
        .clientConnector(ReactorClientHttpConnector())
        .baseUrl(scrapProperties.ceneoBaseUrl)
        .build()

    @Bean
    fun objectMapperBuilder(): Jackson2ObjectMapperBuilder = Jackson2ObjectMapperBuilder()
        .modules(ParameterNamesModule(), JavaTimeModule())
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Bean
    fun redisOperations(redisConnectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisOperations<String, Any> =
        ReactiveRedisTemplate(
            redisConnectionFactory,
            RedisSerializationContext.newSerializationContext<String, Any>(StringRedisSerializer())
                .value(RedisSerializer.java())
                .hashKey(StringRedisSerializer())
                .build()
        )

    @Bean
    @Primary
    fun logbook(): Logbook = Logbook.builder().build()
}
