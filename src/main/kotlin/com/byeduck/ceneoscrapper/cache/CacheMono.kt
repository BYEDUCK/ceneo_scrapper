package com.byeduck.ceneoscrapper.cache

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheMono(val key: String = "")
