package com.byeduck.ceneoscrapper

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class ScrapHandler(private val scrapService: ScrapService) {

    fun getCategories(request: ServerRequest): Mono<ServerResponse> =
        scrapService.getCategories().transform { ServerResponse.ok().body(it) }

    fun scrapCeneo(request: ServerRequest): Mono<ServerResponse> {
        val category =
            ProductCategory.parse(request.queryParam("category").orElseThrow { RuntimeException("Category required") })
        val query = request.queryParam("q").orElseThrow { RuntimeException("Query required") }
            .lowercase()
        return scrapService.scrap(category, query).transform { ServerResponse.ok().body(it) }
    }
}
