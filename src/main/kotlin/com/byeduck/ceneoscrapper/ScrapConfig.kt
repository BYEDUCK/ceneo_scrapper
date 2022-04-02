package com.byeduck.ceneoscrapper

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(ScrapProperties::class)
class ScrapConfig {

    @Bean
    fun scrapWebClient(webClientBuilder: WebClient.Builder, scrapProperties: ScrapProperties) = webClientBuilder
        .codecs { it.defaultCodecs().maxInMemorySize(1 * 1024 * 1024) }
//        .clientConnector(
//            ReactorClientHttpConnector(
//                HttpClient.create().wiretap(true)
//            )
//        )
        .baseUrl(scrapProperties.ceneoBaseUrl)
        .build()
}
