package com.byeduck.ceneoscrapper

data class ProductScore(val score: Double, val maxScore: Int) {

    companion object {
        fun parse(scoreStr: String): ProductScore = if (scoreStr.contains("/")) scoreStr.split("/").let {
            ProductScore(it[0].toDouble(), it[1].toInt())
        } else empty()

        private fun empty(): ProductScore = ProductScore(0.0, 0)
    }
}
