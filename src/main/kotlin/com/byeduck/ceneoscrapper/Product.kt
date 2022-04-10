package com.byeduck.ceneoscrapper

import java.math.BigDecimal

data class Product(val name: String, val price: BigDecimal, val score: ProductScore) : java.io.Serializable
