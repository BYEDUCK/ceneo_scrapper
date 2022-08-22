package com.byeduck.ceneoscrapper.model

data class ProductReviewScore(val score: Double, val maxScore: Int) : java.io.Serializable {

    companion object {
        fun parse(scoreStr: String): ProductReviewScore = if (scoreStr.contains("/")) scoreStr.split("/").let {
            ProductReviewScore(it[0].toDouble(), it[1].toInt())
        } else empty()

        private fun empty(): ProductReviewScore = ProductReviewScore(0.0, 0)
    }
}
