package com.byeduck.ceneoscrapper.scrap

import com.byeduck.ceneoscrapper.cache.CacheMono
import com.byeduck.ceneoscrapper.model.Product
import com.byeduck.ceneoscrapper.model.ProductCategory
import com.byeduck.ceneoscrapper.rest.CeneoFilter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class ScrapService(
    @Qualifier("scrapWebClient") private val webClient: WebClient,
    private val scrapper: Scrapper,
    @Value("\${scrap.query-pages-count:1}") private val queryPagesCount: Long
) {

    fun getCategories(): Mono<List<ProductCategory>> = Mono.just(ProductCategory.values().toList())

    @CacheMono
    fun scrap(category: ProductCategory, filter: CeneoFilter): Mono<List<Product>> =
        generateSequence(0) { it + 1 }.toFlux().take(queryPagesCount)
            .flatMap { queryForPage(category, filter, it) }
            .flatMap { Flux.fromIterable(scrapper.scrapCeneoPage(it, scrapper.createCacheKey(category, filter))) }
            .collectList()

    private fun queryForPage(category: ProductCategory, filter: CeneoFilter, pageNum: Int): Mono<String> {
        return webClient.get().uri(
            "/${category.id};" + queryUrlPart(filter, pageNum)
        )
            .accept(MediaType.TEXT_HTML)
            .exchangeToMono { it.bodyToMono(String::class.java) }
    }

    private fun queryUrlPart(filter: CeneoFilter, pageNum: Int): String {
        val suffix = pageUrlPart(pageNum)
        val query = "szukaj-${URLEncoder.encode(filter.query, StandardCharsets.UTF_8)}"
        val minPrice = filter.minPrice.map { "m$it" }.orElse("")
        val maxPrice = filter.maxPrice.map { "n$it" }.orElse("")
        return listOf(query, minPrice, maxPrice, suffix)
            .filter { it.isNotBlank() }
            .joinToString(";") + ".htm"
    }


    private fun pageUrlPart(pageNum: Int) = if (pageNum == 0) "" else "0020-30-0-0-${pageNum}"
}
