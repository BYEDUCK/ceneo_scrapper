package com.byeduck.ceneoscrapper.model

import java.math.BigDecimal

data class Product(val name: String, val price: BigDecimal, val score: ProductScore) : java.io.Serializable
