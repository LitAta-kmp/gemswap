package org.example.gemswap.domain

class TileIdGenerator {
    private var nextId = 0L

    fun next(): Long = nextId++
}