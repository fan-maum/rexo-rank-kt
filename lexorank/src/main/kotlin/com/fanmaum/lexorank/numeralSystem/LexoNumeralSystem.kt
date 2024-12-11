package com.fanmaum.lexorank.numeralSystem

interface LexoNumeralSystem {
    val base: Int
    val positiveChar: Char
    val negativeChar: Char
    val radixPointChar: Char

    fun toDigit(ch: Char): Int
    fun toChar(digit: Int): Char
}
