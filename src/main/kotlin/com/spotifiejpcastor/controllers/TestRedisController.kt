package com.spotifiejpcastor.controllers

import com.spotifiejpcastor.config.RedisProperties
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.Duration


@RestController
@RequestMapping("api/test/redis")
@CrossOrigin(origins = ["*"])
class TestRedisController(
    private val redisProperties: RedisProperties,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>
) {


    /**
     * Prueba básica de conectividad
     */
    @GetMapping("/ping")
    fun ping(): Mono<ResponseEntity<Map<String, Any>>> {
        return reactiveRedisTemplate.execute { connection ->
            connection.ping()
        }
            .single()
            .map { pingResult ->
                ResponseEntity.ok(
                    mapOf<String, Any>(
                        "status" to "✅ CONNECTED",
                        "ping_result" to pingResult,
                        "host" to redisProperties.host,
                        "port" to redisProperties.port,
                        "database" to redisProperties.database,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
            .onErrorReturn(
                ResponseEntity.status(500).body(
                    mapOf(
                        "status" to "❌ ERROR",
                        "message" to "No se pudo conectar a Redis",
                        "host" to redisProperties.host,
                        "port" to redisProperties.port
                    )
                )
            )
    }

    /**
     * Prueba escribir y leer un valor
     */
    @PostMapping("/test-write-read")
    fun testWriteRead(): Mono<ResponseEntity<Map<String, Any>>> {
        val testKey = "test:${System.currentTimeMillis()}"
        val testValue = "Test value from ${redisProperties.host}:${redisProperties.port}"

        return reactiveRedisTemplate.opsForValue()
            .set(testKey, testValue, Duration.ofMinutes(1))
            .flatMap {
                reactiveRedisTemplate.opsForValue().get(testKey)
            }
            .map { retrievedValue ->
                ResponseEntity.ok(
                    mapOf(
                        "status" to "✅ SUCCESS",
                        "operation" to "write-read",
                        "key" to testKey,
                        "written_value" to testValue,
                        "retrieved_value" to retrievedValue,
                        "values_match" to (testValue == retrievedValue),
                        "message" to "Escritura y lectura exitosa"
                    )
                )
            }
            .onErrorReturn(
                ResponseEntity.status(500).body(
                    mapOf(
                        "status" to "❌ ERROR",
                        "operation" to "write-read",
                        "message" to "Error en operación Redis"
                    )
                )
            )
    }

    /**
     * Mostrar configuración actual
     */
    @GetMapping("/config")
    fun getConfig(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "redis_config" to mapOf(
                    "host" to redisProperties.host,
                    "port" to redisProperties.port,
                    "database" to redisProperties.database,
                    "username" to (redisProperties.username ?: "not-set"),
                    "password_configured" to true,
                ),
                "status" to "Configuration loaded successfully"
            )
        )
    }

    /**
     * Prueba completa: ping + write + read + delete
     */
    @PostMapping("/full-test")
    fun fullTest(): Mono<ResponseEntity<Map<String, Any>>> {
        val testKey = "fulltest:${System.currentTimeMillis()}"
        val testValue = "Full test from Redis reactive config"

        return reactiveRedisTemplate.execute { connection -> connection.ping() }
            .single()
            .flatMap {
                reactiveRedisTemplate.opsForValue().set(testKey, testValue, Duration.ofMinutes(1))
            }
            .flatMap {
                reactiveRedisTemplate.opsForValue().get(testKey)
            }
            .flatMap { retrievedValue ->
                reactiveRedisTemplate.delete(testKey)
                    .map { deletedCount ->
                        mapOf(
                            "status" to "✅ FULL SUCCESS",
                            "tests" to mapOf(
                                "ping" to "✅ OK",
                                "write" to "✅ OK",
                                "read" to "✅ OK",
                                "delete" to "✅ OK ($deletedCount keys deleted)"
                            ),
                            "test_key" to testKey,
                            "test_value" to testValue,
                            "retrieved_value" to retrievedValue,
                            "values_matched" to (testValue == retrievedValue),
                        )
                    }
            }
            .map { result -> ResponseEntity.ok(result) }
            .onErrorReturn(
                ResponseEntity.status(500).body(
                    mapOf(
                        "status" to "❌ FULL TEST FAILED",
                        "message" to "Error durante el test completo",
                    )
                )
            )
    }

}