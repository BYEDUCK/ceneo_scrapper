package com.byeduck.ceneoscrapper

import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class ScrapService(@Qualifier("scrapWebClient") private val webClient: WebClient) {

    fun getCategories(): Mono<List<ProductCategory>> = Mono.just(ProductCategory.values().toList())

    fun scrap(category: ProductCategory, query: String): Mono<List<Product>> =
        generateSequence(0) { it + 1 }.toFlux().take(5L)
            .flatMap { queryForPage(category, query, it) }
            .flatMap { Flux.fromIterable(scrapCeneoPage(it)) }
            .collectList()

    private fun queryForPage(category: ProductCategory, query: String, pageNum: Int): Mono<String> {
        return webClient.get().uri(
            "/${category.id};szukaj-${URLEncoder.encode(query, StandardCharsets.UTF_8)}${pageUrlPartTemplate(pageNum)}"
        )
            .accept(MediaType.TEXT_HTML)
            .exchangeToMono { it.bodyToMono(String::class.java) }
    }

    private fun scrapCeneoPage(htmlPage: String): List<Product> =
        Jsoup.parseBodyFragment(htmlPage).body().getElementsByClass(PRODUCT_ROW_CLASS).toList().map {
            val productName = it.selectFirst(PRODUCT_ROW_NAME_SELECTOR)?.selectFirst("span")?.text() ?: "N/A"
            val productPriceSpan = it.selectFirst(PRODUCT_PRICE_SPAN_SELECTOR)
            val productScore = it.selectFirst(PRODUCT_SCORE_SPAN_SELECTOR)?.text()?.sanitize() ?: "0/0"
            val productPriceInt = productPriceSpan?.selectFirst(PRODUCT_PRICE_INTEGER_SPAN_SELECTOR)?.text() ?: "0"
            val productPriceDec = productPriceSpan?.selectFirst(PRODUCT_PRICE_DECIMAL_SPAN_SELECTOR)?.text() ?: ",0"
            val productPrice = BigDecimal.valueOf(
                (productPriceInt.sanitize() + productPriceDec.replace(",", ".").sanitize()).toDouble()
            )
            return@map Product(productName, productPrice, ProductScore.parse(productScore))
        }

    fun String.sanitize(): String = this.replace("\\s".toRegex(), "").replace(",", ".")

    private fun pageUrlPartTemplate(pageNum: Int) = if (pageNum == 0) "" else ";0020-30-0-0-${pageNum}.htm"
}
