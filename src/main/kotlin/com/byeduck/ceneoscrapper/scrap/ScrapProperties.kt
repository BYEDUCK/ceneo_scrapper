package com.byeduck.ceneoscrapper.scrap

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "scrap")
@ConstructorBinding
data class ScrapProperties(val ceneoBaseUrl: String)
