package com.byeduck.ceneoscrapper.model

import java.math.BigDecimal

data class Product(
    val name: String,
    val brandName: String,
    val price: BigDecimal,
    val reviewScore: ProductReviewScore
) : java.io.Serializable
