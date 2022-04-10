package com.byeduck.ceneoscrapper

import org.jsoup.Jsoup
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class Scrapper {

    @Cacheable(cacheNames = ["ceneo_scrap"], key = "#cacheKey") // TODO: cache api call
    fun scrapCeneoPage(htmlPage: String, cacheKey: String): List<Product> =
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
        }.drop(1) // first product is not correlated with query

    fun createCacheKey(category: ProductCategory, query: String): String = "$category:$query"

    private fun String.sanitize(): String = this.replace("\\s".toRegex(), "").replace(",", ".")
}
