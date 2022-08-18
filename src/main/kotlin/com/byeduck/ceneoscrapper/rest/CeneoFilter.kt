package com.byeduck.ceneoscrapper.rest

import java.util.*

data class CeneoFilter(val query: String, val minPrice: Optional<Int>, val maxPrice: Optional<Int>)
