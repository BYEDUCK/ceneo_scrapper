package com.byeduck.ceneoscrapper.rest

import com.byeduck.ceneoscrapper.model.ProductCategory
import com.byeduck.ceneoscrapper.scrap.ScrapService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import java.util.*
import kotlin.random.Random
import kotlin.random.asJavaRandom

@Component
class ScrapHandler(private val scrapService: ScrapService) {

    fun getCategories(request: ServerRequest): Mono<ServerResponse> =
        scrapService.getCategories().transform { ServerResponse.ok().body(it) }

    fun scrapCeneo(request: ServerRequest): Mono<ServerResponse> {
        val category = parseCategory(request)
        val filter = parseFilter(request)
        return scrapService.scrap(category, filter).transform { ServerResponse.ok().body(it) }
    }

    fun scrapCeneoSample(request: ServerRequest): Mono<ServerResponse> {
        val category = parseCategory(request)
        val filter = parseFilter(request)
        val sampleSize = request.queryParam("sample")
            .orElseThrow { RuntimeException("Sample size required") }
            .toIntOrNull() ?: throw RuntimeException("Sample should be a valid number")
        val random = Random.asJavaRandom()
        return scrapService.scrap(category, filter)
            .map {
                val adjustedSampleSize = if (sampleSize > it.size) it.size else sampleSize
                if (adjustedSampleSize == 0) {
                    return@map emptyList()
                }
                random.ints(adjustedSampleSize.toLong(), 0, it.size)
                    .mapToObj { idx -> it[idx] }
                    .toList()
            }.transform { ServerResponse.ok().body(it) }
    }

    fun parseCategory(request: ServerRequest): ProductCategory = ProductCategory
        .parse(request.queryParam("category")
            .orElseThrow { RuntimeException("Category required") })

    fun parseFilter(request: ServerRequest): CeneoFilter {
        val minPrice = request.queryParam("minPrice").flatMap { it.parseToInt() }
        val maxPrice = request.queryParam("maxPrice").flatMap { it.parseToInt() }
        val query = request.queryParam("q")
            .filter { it.isNotBlank() }
            .orElseThrow { RuntimeException("Query required") }
            .lowercase()
        return CeneoFilter(query, minPrice, maxPrice)
    }

    fun String.parseToInt(): Optional<Int> = Optional.ofNullable(this.toIntOrNull())
}
