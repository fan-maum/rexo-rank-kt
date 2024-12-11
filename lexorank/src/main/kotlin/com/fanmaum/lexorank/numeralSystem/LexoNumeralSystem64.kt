package com.fanmaum.lexorank.numeralSystem

class LexoNumeralSystem64 : LexoNumeralSystem {
    private val digits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ^_abcdefghijklmnopqrstuvwxyz".toCharArray()

    override val base: Int
        get() = 64

    override val positiveChar: Char
        get() = '+'

    override val negativeChar: Char
        get() = '-'

    override val radixPointChar: Char
        get() = ':'

    override fun toDigit(ch: Char): Int {
        return when {
            ch.isDigit() -> ch.digitToInt()
            ch.isLowerCase() -> ch.code - 'a'.code + 38
            ch.isUpperCase() -> ch.code - 'A'.code + 10
            ch == '^' -> 36
            ch == '_' -> 37
            else -> throw IllegalArgumentException("Not a valid digit: $ch")
        }
    }

    override fun toChar(digit: Int): Char = digits[digit]
}
