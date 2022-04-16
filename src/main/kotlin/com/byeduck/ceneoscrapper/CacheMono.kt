package com.byeduck.ceneoscrapper

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheMono(val key: String = "")
