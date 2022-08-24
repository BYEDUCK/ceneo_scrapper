package com.byeduck.ceneoscrapper.scrap

import com.byeduck.ceneoscrapper.model.Product
import com.byeduck.ceneoscrapper.model.ProductCategory
import com.byeduck.ceneoscrapper.model.ProductReviewScore
import com.byeduck.ceneoscrapper.rest.CeneoFilter
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class Scrapper(
    @Value("\${scrap.ceneo-base-url}") private val ceneoBaseUrl: String
) {

    fun scrapCeneoPage(htmlPage: String, cacheKey: String): List<Product> =
        Jsoup.parseBodyFragment(htmlPage).body().getElementsByClass(PRODUCT_ROW_CLASS).toList().map {
            val brandName = it.attr(PRODUCT_ROW_BRAND_ATTRIBUTE_SELECTOR).sanitize().ifBlank { "N/A" }
            val productNameElement = it.selectFirst(PRODUCT_ROW_NAME_SELECTOR)?.selectFirst("a")
            val productRelativeUrl = productNameElement?.attr("href")
            val productName = productNameElement?.selectFirst("span")?.text() ?: "N/A"
            val productPriceSpan = it.selectFirst(PRODUCT_PRICE_SPAN_SELECTOR)
            val productScore = it.selectFirst(PRODUCT_SCORE_SPAN_SELECTOR)?.text()?.sanitize() ?: "0/0"
            val productPriceInt = productPriceSpan?.selectFirst(PRODUCT_PRICE_INTEGER_SPAN_SELECTOR)?.text() ?: "0"
            val productPriceDec = productPriceSpan?.selectFirst(PRODUCT_PRICE_DECIMAL_SPAN_SELECTOR)?.text() ?: ",0"
            val productPrice = BigDecimal.valueOf(
                (productPriceInt.sanitize() + productPriceDec.replace(",", ".").sanitize()).toDouble()
            )
            return@map Product(
                productName,
                brandName,
                getProductUrl(productRelativeUrl),
                productPrice,
                ProductReviewScore.parse(productScore)
            )
        }.drop(1) // first product is not correlated with query

    fun createCacheKey(category: ProductCategory, filter: CeneoFilter): String =
        "$category:${filter.query}:${filter.minPrice}:${filter.maxPrice}"

    private fun String.sanitize(): String = this.replace("\\s".toRegex(), "").replace(",", ".")

    private fun getProductUrl(relativeUrl: String?): String? {
        return if (relativeUrl == null) null else ceneoBaseUrl + relativeUrl
    }
}
