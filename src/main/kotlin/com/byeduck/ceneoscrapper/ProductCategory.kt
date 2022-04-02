package com.byeduck.ceneoscrapper

enum class ProductCategory(val id: String) {
    SMARTPHONES("Smartfony"),
    LAPTOPS("Laptopy"),
    TVS("Telewizory"),
    HEADPHONES("Sluchawki"),
    MONITORS("Monitory"),
    GRAPHICS_CARDS("Karty_graficzne");

    companion object {
        fun parse(name: String): ProductCategory =
            values().find { it.name == name.uppercase() } ?: throw RuntimeException("Unknown category $name")
    }
}
