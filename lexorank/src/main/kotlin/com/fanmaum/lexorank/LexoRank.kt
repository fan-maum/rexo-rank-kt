package com.fanmaum.lexorank

import com.fanmaum.lexorank.numeralSystem.LexoNumeralSystem36
import kotlin.math.max

class LexoRank private constructor(
    val value: String,
    val bucket: LexoRankBucket,
    val decimal: LexoDecimal
) : Comparable<LexoRank> {

    constructor(value: String) : this(
        value,
        LexoRankBucket.from(value.split("|")[0]),
        LexoDecimal.parse(value.split("|")[1], NUMERAL_SYSTEM)
    )

    constructor(bucket: LexoRankBucket, decimal: LexoDecimal) : this(
        bucket.format() + "|" + formatDecimal(decimal),
        bucket,
        decimal
    )

    companion object {
        val NUMERAL_SYSTEM = LexoNumeralSystem36()
        private val ZERO_DECIMAL = LexoDecimal.parse("0", NUMERAL_SYSTEM)
        private val ONE_DECIMAL = LexoDecimal.parse("1", NUMERAL_SYSTEM)
        private val EIGHT_DECIMAL = LexoDecimal.parse("8", NUMERAL_SYSTEM)
        private val MIN_DECIMAL = ZERO_DECIMAL
        private val INITIAL_MIN_DECIMAL = LexoDecimal.parse("100000", NUMERAL_SYSTEM)
        private val INITIAL_MAX_DECIMAL = LexoDecimal.parse(
            NUMERAL_SYSTEM.toChar(NUMERAL_SYSTEM.base - 2) + "00000",
            NUMERAL_SYSTEM
        )
        private val MAX_DECIMAL = LexoDecimal.parse("1000000", NUMERAL_SYSTEM).subtract(ONE_DECIMAL)

        fun min(): LexoRank = from(LexoRankBucket.BUCKET_0, MIN_DECIMAL)

        fun middle(): LexoRank {
            val minLexoRank: LexoRank = min()
            return minLexoRank.between(max(minLexoRank.bucket))
        }

        fun max(bucket: LexoRankBucket): LexoRank = from(bucket, MAX_DECIMAL)

        fun initial(bucket: LexoRankBucket): LexoRank {
            return if (bucket === LexoRankBucket.BUCKET_0) {
                from(bucket, INITIAL_MIN_DECIMAL)
            } else {
                from(bucket, INITIAL_MAX_DECIMAL)
            }
        }

        private fun between(oLeft: LexoDecimal, oRight: LexoDecimal): LexoDecimal {
            require(oLeft.getSystem() == oRight.getSystem()) { "Expected same system" }

            var left = oLeft
            var right = oRight
            var nLeft: LexoDecimal
            if (oLeft.getScale() < oRight.getScale()) {
                nLeft = oRight.setScale(oLeft.getScale(), false)
                if (oLeft >= nLeft) return middle(oLeft, oRight)

                right = nLeft
            }

            if (oLeft.getScale() > right.getScale()) {
                nLeft = oLeft.setScale(right.getScale(), true)
                if (nLeft >= right) return middle(oLeft, oRight)

                left = nLeft
            }

            var nRight: LexoDecimal
            var scale = left.getScale()
            while (scale > 0) {
                val nScale1 = scale - 1
                val nLeft1 = left.setScale(nScale1, true)
                nRight = right.setScale(nScale1, false)
                if (nLeft1 == nRight) return checkMid(oLeft, oRight, nLeft1)
                if (nLeft1 > nRight) break

                scale = nScale1
                left = nLeft1
                right = nRight
            }

            var mid = middle(oLeft, oRight, left, right)

            var nScale: Int
            var mScale = mid.getScale()
            while (mScale > 0) {
                nScale = mScale - 1
                val nMid = mid.setScale(nScale)
                if (oLeft >= nMid || nMid >= oRight) break

                mid = nMid
                mScale = nScale
            }

            return mid
        }

        private fun middle(
            lBound: LexoDecimal,
            rBound: LexoDecimal,
            left: LexoDecimal,
            right: LexoDecimal
        ): LexoDecimal {
            val mid = middle(left, right)

            return checkMid(lBound, rBound, mid)
        }

        private fun checkMid(
            lBound: LexoDecimal,
            rBound: LexoDecimal,
            mid: LexoDecimal
        ): LexoDecimal {
            if (lBound >= mid) return middle(lBound, rBound)

            return if (mid >= rBound) middle(lBound, rBound) else mid
        }

        private fun middle(left: LexoDecimal, right: LexoDecimal): LexoDecimal {
            val sum = left.add(right)
            val mid = sum.multiply(LexoDecimal.half(left.getSystem()))
            val scale = max(left.getScale(), right.getScale())

            if (mid.getScale() > scale) {
                val roundDown = mid.setScale(scale, false)
                if (roundDown > left) return roundDown

                val roundUp = mid.setScale(scale, true)
                if (roundUp < right) return roundUp
            }

            return mid
        }

        private fun formatDecimal(dec: LexoDecimal): String {
            val radixPoint = NUMERAL_SYSTEM.radixPointChar
            val zero = NUMERAL_SYSTEM.toChar(0)

            return buildString {
                append(dec.format())
                while (length < 6) append(zero)
                if (radixPoint !in this) append(radixPoint)
                while (isNotEmpty() && last() == zero) deleteAt(lastIndex)
            }
        }

        fun from(bucket: LexoRankBucket, dec: LexoDecimal): LexoRank {
            require(dec.getSystem().base == NUMERAL_SYSTEM.base) { "Expected same system" }

            return LexoRank(bucket, dec)
        }
    }

    fun inNextBucket(): LexoRank = from(bucket.next(), decimal)

    fun inPrevBucket(): LexoRank = from(bucket.prev(), decimal)

    private fun isMin(): Boolean = decimal == MIN_DECIMAL

    private fun isMax(): Boolean = decimal == MAX_DECIMAL

    private fun format(): String = value

    fun genPrev(): LexoRank {
        if (isMax()) return LexoRank(bucket, INITIAL_MAX_DECIMAL)

        val floorInteger = decimal.floor()
        val floorDecimal = LexoDecimal.from(floorInteger)
        var nextDecimal = floorDecimal.subtract(EIGHT_DECIMAL)

        if (nextDecimal <= MIN_DECIMAL) nextDecimal = between(MIN_DECIMAL, decimal)

        return LexoRank(bucket, nextDecimal)
    }

    fun genNext(): LexoRank {
        if (isMin()) return LexoRank(bucket, INITIAL_MIN_DECIMAL)

        val ceilInteger = decimal.ceil()
        val ceilDecimal = LexoDecimal.from(ceilInteger)
        var nextDecimal = ceilDecimal.add(EIGHT_DECIMAL)

        if (nextDecimal >= MAX_DECIMAL) nextDecimal = between(decimal, MAX_DECIMAL)

        return LexoRank(bucket, nextDecimal)
    }

    fun between(lexoRank: LexoRank): LexoRank {
        require(bucket == lexoRank.bucket) { "Between works only within the same bucket" }

        return when {
            decimal > lexoRank.decimal -> LexoRank(bucket, between(lexoRank.decimal, decimal))
            decimal == lexoRank.decimal -> throw IllegalArgumentException("Ranks cannot be the same: $this, $lexoRank")
            else -> LexoRank(bucket, between(decimal, lexoRank.decimal))
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is LexoRank
            && value == other.value
            && bucket == other.bucket
            && decimal == other.decimal
    }

    override fun hashCode(): Int {
        return 31 * value.hashCode() +
            31 * bucket.hashCode() +
            decimal.hashCode()
    }

    override fun toString(): String = value

    override fun compareTo(other: LexoRank): Int {
        return value.compareTo(other.value)
    }
}
