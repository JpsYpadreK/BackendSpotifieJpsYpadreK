package com.spotifiejpcastor.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.properties.Delegates


@Component
@ConfigurationProperties(prefix = "spring.data.redis")
class RedisProperties {
    lateinit var host: String
    var port by Delegates.notNull<Int>()
    var database by Delegates.notNull<Int>()
    lateinit var username: String
    lateinit var password: String
}