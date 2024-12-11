package com.fanmaum.lexorank

import com.fanmaum.lexorank.numeralSystem.LexoNumeralSystem

class LexoDecimal private constructor(
    private val lexoInteger: LexoInteger,
    private val sig: Int,
) : Comparable<LexoDecimal> {
    companion object {
        fun half(sys: LexoNumeralSystem): LexoDecimal {
            val mid = sys.base / 2
            return make(LexoInteger.make(sys, 1, intArrayOf(mid)), 1)
        }

        fun parse(str: String, system: LexoNumeralSystem): LexoDecimal {
            val partialIndex = str.indexOf(system.radixPointChar)

            if (str.lastIndexOf(system.radixPointChar) != partialIndex) {
                throw IllegalArgumentException("More than one " + system.radixPointChar)
            }

            if (partialIndex < 0) {
                return make(LexoInteger.parse(str, system), 0)
            }

            val intStr = str.substring(0, partialIndex) + str.substring(partialIndex + 1)

            return make(LexoInteger.parse(intStr, system), str.length - 1 - partialIndex)
        }

        fun from(integer: LexoInteger): LexoDecimal {
            return make(integer, 0)
        }

        fun make(integer: LexoInteger, sig: Int): LexoDecimal {
            if (integer.isZero()) return LexoDecimal(integer, 0)

            val zeroCount = integer.mag.take(sig).takeWhile { it == 0 }.size
            val newInteger: LexoInteger = integer.shiftRight(zeroCount)
            val newSig = sig - zeroCount

            return LexoDecimal(newInteger, newSig)
        }
    }

    fun getSystem(): LexoNumeralSystem {
        return lexoInteger.sys
    }

    fun add(other: LexoDecimal): LexoDecimal {
        var thisMag = lexoInteger
        var thisSig = sig
        var otherMag = other.lexoInteger
        var otherSig = other.sig

        while (thisSig < otherSig) {
            thisMag = thisMag.shiftLeft()
            ++thisSig
        }

        while (thisSig > otherSig) {
            otherMag = otherMag.shiftLeft()
            ++otherSig
        }

        return make(thisMag.add(otherMag), thisSig)
    }

    fun subtract(other: LexoDecimal): LexoDecimal {
        var thisMag = lexoInteger
        var thisSig = sig
        var otherMag = other.lexoInteger
        var otherSig = other.sig

        while (thisSig < otherSig) {
            thisMag = thisMag.shiftLeft()
            ++thisSig
        }

        while (thisSig > otherSig) {
            otherMag = otherMag.shiftLeft()
            ++otherSig
        }

        return make(thisMag.subtract(otherMag), thisSig)
    }

    fun multiply(other: LexoDecimal): LexoDecimal {
        return make(lexoInteger.multiply(other.lexoInteger), sig + other.sig)
    }

    fun floor(): LexoInteger {
        return lexoInteger.shiftRight(sig)
    }

    fun ceil(): LexoInteger {
        if (isExact()) return lexoInteger

        val floor = floor()
        return floor.add(LexoInteger.one(floor.sys))
    }

    fun isExact(): Boolean = sig == 0 || (0 until sig).all { lexoInteger.magAt(it) == 0 }

    fun getScale(): Int = sig

    fun setScale(nSig: Int, ceiling: Boolean = false): LexoDecimal {
        if (nSig >= sig) return this

        var mutableSig = nSig
        if (mutableSig < 0) mutableSig = 0

        val diff = sig - mutableSig
        var nMag: LexoInteger = lexoInteger.shiftRight(diff)

        if (ceiling) nMag = nMag.add(LexoInteger.one(nMag.sys))

        return make(nMag, mutableSig)
    }

    fun format(): String {
        val intStr = lexoInteger.format()
        if (sig == 0) return intStr

        val sb = StringBuilder(intStr)
        val head = sb[0]
        val specialHead =
            head == lexoInteger.sys.positiveChar || head == lexoInteger.sys.negativeChar

        if (specialHead) sb.deleteAt(0)
        while (sb.length < sig + 1) sb.insert(0, lexoInteger.sys.toChar(0))

        sb.insert(sb.length - sig, lexoInteger.sys.radixPointChar)

        if (sb.length - sig == 0) sb.insert(0, lexoInteger.sys.toChar(0))
        if (specialHead) sb.insert(0, head)

        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as LexoDecimal
        return sig == that.sig && lexoInteger == that.lexoInteger
    }

    override fun hashCode(): Int = 31 * sig + lexoInteger.hashCode()

    override fun toString(): String = format()

    override fun compareTo(other: LexoDecimal): Int {
        if (this === other) return 0

        var thisMag = lexoInteger
        var otherMag = other.lexoInteger

        when {
            sig > other.sig -> otherMag = otherMag.shiftLeft(sig - other.sig)
            sig < other.sig -> thisMag = thisMag.shiftLeft(other.sig - sig)
        }

        return thisMag.compareTo(otherMag)
    }
}
