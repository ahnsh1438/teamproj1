package com.example.dopamindetox.data.structures

class StackList<T> {
    private val data = mutableListOf<T>()
    fun push(t:T) = data.add(t)
    fun pop(): T? = if (data.isNotEmpty()) data.removeAt(data.lastIndex) else null
    fun peek(): T? = data.lastOrNull()
    fun asList(): List<T> = data.toList()
}
