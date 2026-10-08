package org.example.gemswap

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform