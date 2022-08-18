package com.byeduck.ceneoscrapper.rest

import com.byeduck.ceneoscrapper.model.ProductCategory
import com.byeduck.ceneoscrapper.scrap.ScrapService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import java.util.*

@Component
class ScrapHandler(private val scrapService: ScrapService) {

    fun getCategories(request: ServerRequest): Mono<ServerResponse> =
        scrapService.getCategories().transform { ServerResponse.ok().body(it) }

    fun scrapCeneo(request: ServerRequest): Mono<ServerResponse> {
        val category = ProductCategory.parse(request.queryParam("category")
            .orElseThrow { RuntimeException("Category required") })
        val minPrice = request.queryParam("minPrice").flatMap { it.parseToInt() }
        val maxPrice = request.queryParam("maxPrice").flatMap { it.parseToInt() }
        val query = request.queryParam("q")
            .filter { it.isNotBlank() }
            .orElseThrow { RuntimeException("Query required") }
            .lowercase()
        val filter = CeneoFilter(query, minPrice, maxPrice)
        return scrapService.scrap(category, filter).transform { ServerResponse.ok().body(it) }
    }

    fun String.parseToInt(): Optional<Int> = Optional.ofNullable(this.toIntOrNull())
}
