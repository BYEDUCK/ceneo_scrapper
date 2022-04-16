package com.byeduck.ceneoscrapper

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

    @CacheMono("ceneo_categories")
    fun getCategories(): Mono<List<ProductCategory>> = Mono.just(ProductCategory.values().toList())

    @CacheMono
    fun scrap(category: ProductCategory, query: String): Mono<List<Product>> =
        generateSequence(0) { it + 1 }.toFlux().take(queryPagesCount)
            .flatMap { queryForPage(category, query, it) }
            .flatMap { Flux.fromIterable(scrapper.scrapCeneoPage(it, scrapper.createCacheKey(category, query))) }
            .collectList()

    private fun queryForPage(category: ProductCategory, query: String, pageNum: Int): Mono<String> {
        return webClient.get().uri(
            "/${category.id};szukaj-${URLEncoder.encode(query, StandardCharsets.UTF_8)}${pageUrlPartTemplate(pageNum)}"
        )
            .accept(MediaType.TEXT_HTML)
            .exchangeToMono { it.bodyToMono(String::class.java) }
    }

    private fun pageUrlPartTemplate(pageNum: Int) = if (pageNum == 0) "" else ";0020-30-0-0-${pageNum}.htm"
}
