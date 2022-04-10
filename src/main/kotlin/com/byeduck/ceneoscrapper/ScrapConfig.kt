package com.byeduck.ceneoscrapper

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.reactive.function.client.WebClient
import org.zalando.logbook.Logbook


@Configuration
@EnableConfigurationProperties(ScrapProperties::class)
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
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<Any, Any> =
        RedisTemplate<Any, Any>().apply {
            keySerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            setConnectionFactory(redisConnectionFactory)
        }

    @Bean
    @Primary
    fun logbook(): Logbook = Logbook.builder().build()
}
