package com.spotifiejpcastor.config

import io.lettuce.core.ClientOptions
import io.lettuce.core.SocketOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * Configuración Redis Reactiva usando Lettuce
 * Basada en la documentación oficial de Spring Data Redis
 */
@Configuration
class RedisReactiveConfig(
    private val redisProperties: RedisProperties
) {

    /**
     * Factory de conexiones Redis reactivas con Lettuce
     * Configuración optimizada para el servidor Redis en Kubernetes
     */
    @Bean
    @Primary
    fun reactiveRedisConnectionFactory(): ReactiveRedisConnectionFactory {
        // 🔧 Configuración del servidor Redis (de tu K8s)
        val redisConfig = RedisStandaloneConfiguration().apply {
            hostName = redisProperties.host
            port = redisProperties.port
            database = redisProperties.database

            // ✅ SOLUCION ERROR: Convertir String a RedisPassword
            redisProperties.password.let { passwordString ->
                password = RedisPassword.of(passwordString)
            }

            // 🔐 Username si existe
            redisProperties.username.let { user ->
                username = user
            }
        }

        // ⚙️ Configuración avanzada de Lettuce (según documentación oficial)
        val clientConfig = LettuceClientConfiguration.builder()
            .clientOptions(
                ClientOptions.builder()
                    .socketOptions(
                        SocketOptions.builder()
                            .connectTimeout(Duration.ofSeconds(10))
                            .keepAlive(true)
                            .build()
                    )
                    .build()
            )
            .commandTimeout(Duration.ofSeconds(2))
            .shutdownTimeout(Duration.ofMillis(100))
            .build()

        return LettuceConnectionFactory(redisConfig, clientConfig)
    }

    /**
     * ReactiveRedisTemplate para operaciones reactivas con tipos JSON
     * Configurado con serializadores Jackson para objetos complejos
     */
    @Bean
    fun reactiveRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, Any> {

        val serializer = GenericJackson2JsonRedisSerializer()

        val context = RedisSerializationContext
            .newSerializationContext<String, Any>()
            .key(StringRedisSerializer())
            .value(serializer)
            .hashKey(StringRedisSerializer())
            .hashValue(serializer)
            .build()

        return ReactiveRedisTemplate(connectionFactory, context)
    }

    /**
     * ReactiveRedisTemplate optimizado para operaciones String simples
     * Mejor rendimiento para keys/values de texto plano
     */
    @Bean("reactiveStringRedisTemplate")
    fun reactiveStringRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, String> {

        val context = RedisSerializationContext
            .newSerializationContext<String, String>()
            .key(StringRedisSerializer())
            .value(StringRedisSerializer())
            .hashKey(StringRedisSerializer())
            .hashValue(StringRedisSerializer())
            .build()

        return ReactiveRedisTemplate(connectionFactory, context)
    }
}