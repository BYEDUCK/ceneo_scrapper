package com.byeduck.ceneoscrapper

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UseCache(val key: String = "")
