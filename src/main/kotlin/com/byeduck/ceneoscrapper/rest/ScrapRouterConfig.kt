package com.byeduck.ceneoscrapper.rest

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class ScrapRouterConfig {

    @Bean
    fun apiRouter(scrapHandler: ScrapHandler) = router {
        "/api".nest {
            "/scrap".nest {
                GET("", scrapHandler::scrapCeneo)
                GET("/categories", scrapHandler::getCategories)
            }
        }
    }
}
