package com.fanmaum.lexorank

import com.fanmaum.lexorank.numeralSystem.LexoNumeralSystem
import kotlin.math.abs

class LexoInteger private constructor(
    val sys: LexoNumeralSystem,
    val mag: IntArray,
    val sign: Int
) : Comparable<LexoInteger> {
    companion object {
        private val ZERO_MAG: IntArray = intArrayOf(0)
        private val ONE_MAG: IntArray = intArrayOf(1)

        fun zero(sys: LexoNumeralSystem): LexoInteger = LexoInteger(sys = sys, sign = 0, mag = ZERO_MAG)

        fun one(sys: LexoNumeralSystem): LexoInteger = make(sys, 1, ONE_MAG)

        fun parse(strFull: String, system: LexoNumeralSystem): LexoInteger {
            var str = strFull
            var sign = 1

            when {
                strFull.startsWith(system.positiveChar) -> str = strFull.drop(1)
                strFull.startsWith(system.negativeChar) -> {
                    str = strFull.drop(1)
                    sign = -1
                }
            }

            val mag = str.reversed().map { system.toDigit(it) }.toIntArray()

            return make(system, sign, mag)
        }

        fun make(sys: LexoNumeralSystem, sign: Int, mag: IntArray): LexoInteger {
            var actualLength = mag.size
            while (actualLength > 0 && mag[actualLength - 1] == 0) {
                actualLength--
            }
            if (actualLength == 0) return zero(sys)
            if (actualLength == mag.size) return LexoInteger(sys = sys, sign = sign, mag = mag)
            val nMag = IntArray(actualLength)
            System.arraycopy(mag, 0, nMag, 0, actualLength)

            return LexoInteger(sys = sys, sign = sign, mag = nMag)
        }

        private fun add(sys: LexoNumeralSystem, left: IntArray, right: IntArray): IntArray {
            val estimatedSize = maxOf(left.size, right.size)
            val result = IntArray(estimatedSize)
            var carry = 0

            for (i in 0 until estimatedSize) {
                val leftNum = if (i < left.size) left[i] else 0
                val rightNum = if (i < right.size) right[i] else 0
                val sum = leftNum + rightNum + carry
                result[i] = sum % sys.base
                carry = sum / sys.base
            }

            return extendWithCarry(result, carry)
        }

        private fun extendWithCarry(result: IntArray, carry: Int): IntArray {
            if (carry <= 0) return result
            val extended = IntArray(result.size + 1) { 0 }
            extended[extended.size - 1] = carry
            System.arraycopy(result, 0, extended, 0, result.size)
            return extended
        }

        private fun subtract(sys: LexoNumeralSystem, l: IntArray, r: IntArray): IntArray {
            val result = IntArray(maxOf(l.size, r.size))
            var borrow = 0
            for (i in result.indices) {
                val left = if (i < l.size) l[i] else 0
                val right = if (i < r.size) r[i] else 0
                var diff = left - right - borrow
                if (diff < 0) {
                    diff += sys.base
                    borrow = 1
                } else {
                    borrow = 0
                }
                result[i] = diff
            }

            return result
        }

        private fun multiply(sys: LexoNumeralSystem, l: IntArray, r: IntArray): IntArray {
            val result = IntArray(l.size + r.size)
            for (i in l.indices) {
                var carry = 0
                for (j in r.indices) {
                    val resultIdx = i + j
                    val product = l[i] * r[j] + result[resultIdx] + carry
                    result[resultIdx] = product % sys.base
                    carry = product / sys.base
                }
                if (carry > 0) {
                    result[i + r.size] += carry
                }
            }

            return result
        }

        private fun complement(sys: LexoNumeralSystem, mag: IntArray, digits: Int): IntArray {
            if (digits <= 0) throw IllegalArgumentException("Expected at least 1 digit")

            val nMag = IntArray(digits) { sys.base - 1 }

            for (i in mag.indices) {
                nMag[i] = sys.base - 1 - mag[i]
            }

            return nMag
        }
    }

    fun add(other: LexoInteger): LexoInteger {
        checkSystem(other)
        if (isZero()) return other
        if (other.isZero()) return this

        if (sign != other.sign) {
            val pos: LexoInteger
            if (sign == -1) {
                pos = negate()
                return pos.subtract(other).negate()
            }
            pos = other.negate()
            return subtract(pos)
        }

        val result = add(sys, mag, other.mag)
        return make(sys, sign, result)
    }

    fun subtract(other: LexoInteger): LexoInteger {
        checkSystem(other)
        if (isZero()) return other.negate()
        if (other.isZero()) return this

        if (sign != other.sign) {
            val negate: LexoInteger
            if (sign == -1) {
                negate = negate()
                val sum = negate.add(other)
                return sum.negate()
            }

            negate = other.negate()
            return add(negate)
        }

        val cmp = compare(mag, other.mag)
        if (cmp == 0) return zero(sys)

        return if (cmp < 0) make(sys, if (sign == -1) 1 else -1, subtract(sys, other.mag, mag))
        else make(sys, if (sign == -1) -1 else 1, subtract(sys, mag, other.mag))
    }

    fun multiply(other: LexoInteger): LexoInteger {
        checkSystem(other)
        if (isZero()) return this
        if (other.isZero()) return other

        if (isOneish()) {
            return if (sign == other.sign) make(sys, 1, other.mag) else make(sys, -1, other.mag)
        }
        if (other.isOneish()) {
            return if (sign == other.sign) make(sys, 1, mag) else make(sys, -1, mag)
        }

        val newMag = multiply(sys, mag, other.mag)
        return if (sign == other.sign) make(sys, 1, newMag) else make(sys, -1, newMag)
    }

    fun format(): String {
        if (isZero()) return sys.toChar(0).toString()
        val sb = StringBuilder()
        mag.forEach { digit ->
            sb.insert(0, sys.toChar(digit))
        }

        if (sign == -1) sb.setCharAt(0, sys.negativeChar)
        return sb.toString()
    }

    fun shiftLeft(times: Int = 1): LexoInteger {
        if (times == 0) return this
        if (times < 0) return shiftRight(abs(times))

        val nMag = IntArray(mag.size + times).apply {
            mag.copyInto(this, destinationOffset = times)
        }
        return make(sys, sign, nMag)
    }

    fun shiftRight(times: Int = 1): LexoInteger {
        if (mag.size <= times) return zero(sys)

        val nMag = mag.copyOfRange(times, mag.size)
        return make(sys, sign, nMag)
    }

    fun magAt(index: Int) = mag[index]
    fun complement(digits: Int = mag.size) = make(sys, sign, complement(sys, mag, digits))

    fun isZero() = sign == 0 && mag.size == 1 && mag[0] == 0
    fun isOne() = sign == 1 && mag.size == 1 && mag[0] == 1
    private fun isOneish() = mag.size == 1 && mag[0] == 1

    private fun negate() = if (isZero()) this else make(sys, if (sign == 1) -1 else 1, mag)

    private fun checkSystem(other: LexoInteger) {
        require(sys.base == other.sys.base) { "Expected numbers of same numeral sys" }
    }

    private fun compare(l: IntArray, r: IntArray): Int {
        if (l.size < r.size) return -1
        if (l.size > r.size) return 1
        for (i in l.indices.reversed()) {
            if (l[i] < r[i]) return -1
            if (l[i] > r[i]) return 1
        }
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LexoInteger) return false

        return sys.base == other.sys.base && compareTo(other) == 0
    }

    override fun hashCode(): Int {
        var result = sign
        result = 31 * result + sys.hashCode()
        result = 31 * result + mag.contentHashCode()
        return result
    }

    override fun compareTo(other: LexoInteger): Int {
        if (this === other) return 0

        if (sign == -1) {
            if (other.sign == 1) {
                val cmp = compare(mag, other.mag)
                if (cmp == -1) return 1
                return if (cmp == 1) -1 else 0
            }

            return -1
        }

        if (sign == 1) return if (other.sign == 1) compare(mag, other.mag) else 1
        if (other.sign == -1) return 1
        return if (other.sign == 1) -1 else 0
    }
}
