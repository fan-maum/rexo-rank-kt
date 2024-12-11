package com.fanmaum.lexorank.numeralSystem

class LexoNumeralSystem10 : LexoNumeralSystem {
    private val digits = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray()

    override val base: Int
        get() = 10

    override val positiveChar: Char
        get() = '+'

    override val negativeChar: Char
        get() = '-'

    override val radixPointChar: Char
        get() = '.'

    override fun toDigit(ch: Char): Int {
        return ch.digitToIntOrNull() ?: throw IllegalArgumentException("Not a valid digit: $ch")
    }

    override fun toChar(digit: Int): Char = digits[digit]
}
