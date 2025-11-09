package com.example.dopamindetox.data.structures

class RedBlackTree<K: Comparable<K>, V> {
    private enum class Color { RED, BLACK }
    private inner class Node(var k:K, var v:V, var c:Color, var l:Node?=null, var r:Node?=null, var p:Node?=null)
    private var root: Node? = null

    fun put(key:K, value:V) {
        var y:Node? = null
        var x = root
        while (x != null) { y = x; x = if (key < x.k) x.l else if (key > x.k) x.r else { x.v = value; return } }
        val z = Node(key, value, Color.RED, null, null, y)
        if (y == null) root = z else if (key < y.k) y.l = z else y.r = z
        insertFix(z)
    }

    fun get(key:K): V? {
        var x = root
        while (x != null) {
            x = when {
                key < x.k -> x.l
                key > x.k -> x.r
                else -> return x.v
            }
        }
        return null
    }

    fun inorder(): List<Pair<K,V>> {
        val res = mutableListOf<Pair<K,V>>()
        fun walk(n:Node?) {
            if (n==null) return
            walk(n.l); res += n.k to n.v; walk(n.r)
        }
        walk(root); return res
    }

    private fun insertFix(z0: Node) {
        var z = z0
        while (z.p?.c == Color.RED) {
            if (z.p == z.p!!.p?.l) {
                val y = z.p!!.p?.r
                if (y?.c == Color.RED) {
                    z.p!!.c = Color.BLACK; y.c = Color.BLACK; z.p!!.p!!.c = Color.RED; z = z.p!!.p!!
                } else {
                    if (z == z.p!!.r) { z = z.p!!; leftRotate(z) }
                    z.p!!.c = Color.BLACK; z.p!!.p!!.c = Color.RED; rightRotate(z.p!!.p!!)
                }
            } else {
                val y = z.p!!.p?.l
                if (y?.c == Color.RED) {
                    z.p!!.c = Color.BLACK; y.c = Color.BLACK; z.p!!.p!!.c = Color.RED; z = z.p!!.p!!
                } else {
                    if (z == z.p!!.l) { z = z.p!!; rightRotate(z) }
                    z.p!!.c = Color.BLACK; z.p!!.p!!.c = Color.RED; leftRotate(z.p!!.p!!)
                }
            }
        }
        root?.c = Color.BLACK
    }

    private fun leftRotate(x: Node) {
        val y = x.r ?: return
        x.r = y.l
        y.l?.p = x
        y.p = x.p
        if (x.p == null) root = y else if (x == x.p!!.l) x.p!!.l = y else x.p!!.r = y
        y.l = x
        x.p = y
    }

    private fun rightRotate(x: Node) {
        val y = x.l ?: return
        x.l = y.r
        y.r?.p = x
        y.p = x.p
        if (x.p == null) root = y else if (x == x.p!!.r) x.p!!.r = y else x.p!!.l = y
        y.r = x
        x.p = y
    }
}
