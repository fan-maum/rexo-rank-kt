package com.fanmaum.lexorank

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldNotBe

class LexoRankTest : StringSpec({
    "should create LexoRank with value" {
        val rank = LexoRank("0|100000")

        rank.toString() shouldBe "0|100000"
        rank.bucket shouldBe LexoRankBucket.BUCKET_0
        rank.decimal shouldBe LexoDecimal.parse("100000", LexoRank.NUMERAL_SYSTEM)
    }

    "should create LexoRank with bucket and decimal" {
        val decimal = LexoDecimal.parse("012345", LexoRank.NUMERAL_SYSTEM)
        val rank = LexoRank(LexoRankBucket.BUCKET_1, decimal)

        rank.toString() shouldBe "1|123450:"
        rank.bucket shouldBe LexoRankBucket.BUCKET_1
        rank.decimal shouldBe decimal
    }

    "should generate next LexoRank" {
        val rank = LexoRank("0|100000")
        val nextRank = rank.genNext()

        nextRank.decimal shouldBeGreaterThan rank.decimal
        nextRank.bucket shouldBe rank.bucket
    }

    "should generate previous LexoRank" {
        val rank = LexoRank("0|200000")
        val prevRank = rank.genPrev()

        prevRank.decimal shouldBeLessThan rank.decimal
        prevRank.bucket shouldBe rank.bucket
    }

    "should generate between LexoRank" {
        val leftRank = LexoRank("0|100000")
        val rightRank = LexoRank("0|200000")
        val rank = leftRank.between(rightRank)

        rank.decimal shouldBeLessThan rightRank.decimal
        rank.decimal shouldBeGreaterThan leftRank.decimal
    }

    "should handle edge case with very close ranks" {
        val leftRank = LexoRank("0|100001")
        val rightRank = LexoRank("0|100002")
        val betweenRank = leftRank.between(rightRank)

        betweenRank.decimal shouldBeGreaterThan leftRank.decimal
        betweenRank.decimal shouldBeLessThan rightRank.decimal
    }

    "should create LexoRank with zero bucket" {
        val rank = LexoRank("0|000000")
        rank.bucket shouldBe LexoRankBucket.BUCKET_0
        rank.decimal shouldBe LexoDecimal.parse("000000", LexoRank.NUMERAL_SYSTEM)
    }

    "should throw error for invalid LexoRank value" {
        shouldThrow<IllegalArgumentException> {
            LexoRank("INVALID|VALUE")
        }
    }

    "should handle smallest and largest LexoRank" {
        val minRank = LexoRank.min()
        val maxRank = LexoRank.max(LexoRankBucket.BUCKET_0)

        minRank.bucket shouldBe LexoRankBucket.BUCKET_0
        maxRank.bucket shouldBe LexoRankBucket.BUCKET_0
        minRank.decimal shouldBeLessThan maxRank.decimal
    }

    "should throw error when calling between on identical ranks" {
        val rank = LexoRank("0|100000")
        val exception = shouldThrow<IllegalArgumentException> {
            rank.between(rank)
        }
        exception.message shouldBe "Ranks cannot be the same: $rank, $rank"
    }

    "should handle genNext at max decimal" {
        val maxRank = LexoRank.max(LexoRankBucket.BUCKET_0)
        val nextRank = maxRank.genNext()

        nextRank.decimal shouldBe  maxRank.decimal
    }

    "should handle genPrev at min decimal" {
        val minRank = LexoRank.min()
        val prevRank = minRank.genPrev()

        prevRank.decimal shouldBe minRank.decimal
    }

    "should not allow between ranks from different buckets" {
        val rankBucket0 = LexoRank("0|100000")
        val rankBucket1 = LexoRank("1|100000")

        val exception = shouldThrow<IllegalArgumentException> {
            rankBucket0.between(rankBucket1)
        }
        exception.message shouldBe "Between works only within the same bucket"
    }

    "should compare ranks from different buckets correctly" {
        val rankBucket0 = LexoRank("0|100000")
        val rankBucket1 = LexoRank("1|100000")

        rankBucket0.compareTo(rankBucket1) shouldBeLessThan 0
    }

    "should throw error for LexoRank with invalid bucket" {
        shouldThrow<IllegalArgumentException> {
            LexoRank("3|100000")
        }
    }

    "should throw error for LexoRank with invalid decimal" {
        shouldThrow<IllegalArgumentException> {
            LexoRank("0|INVALID")
        }
    }

    "should handle very large LexoDecimal values" {
        val decimalStr = "1" + "0".repeat(50)
        val decimal = LexoDecimal.parse(decimalStr, LexoRank.NUMERAL_SYSTEM)
        decimal.format() shouldBe decimalStr
    }

    "should handle very large LexoRank values" {
        val rankStr = "0|" + "1" + "0".repeat(50)
        val rank = LexoRank(rankStr)
        rank.toString() shouldBe rankStr
    }

    "should correctly compare LexoRanks with different decimals" {
        val rank1 = LexoRank("0|100000")
        val rank2 = LexoRank("0|200000")
        rank1.compareTo(rank2) shouldBeLessThan 0
        rank2.compareTo(rank1) shouldBeGreaterThan 0
    }

    "should correctly compare LexoRanks with same decimals but different buckets" {
        val rank1 = LexoRank("0|100000")
        val rank2 = LexoRank("1|100000")
        rank1.compareTo(rank2) shouldBeLessThan 0
        rank2.compareTo(rank1) shouldBeGreaterThan 0
    }

    "should correctly evaluate equality of LexoRanks" {
        val rank1 = LexoRank("0|100000")
        val rank2 = LexoRank("0|100000")
        val rank3 = LexoRank("0|100001")

        rank1 shouldBe rank2
        rank1.hashCode() shouldBe rank2.hashCode()
        rank1 shouldNotBe rank3
    }

    "should move LexoRank to next bucket" {
        val rank = LexoRank("0|100000")
        val nextBucketRank = rank.inNextBucket()

        nextBucketRank.bucket shouldBe LexoRankBucket.BUCKET_1
        nextBucketRank.decimal shouldBe rank.decimal
    }

    "should move LexoRank to previous bucket" {
        val rank = LexoRank("0|100000")
        val prevBucketRank = rank.inPrevBucket()

        prevBucketRank.bucket shouldBe LexoRankBucket.BUCKET_2
        prevBucketRank.decimal shouldBe rank.decimal
    }

    "should generate distinct ranks when calling between repeatedly" {
        var leftRank = LexoRank("0|100000")
        var rightRank = LexoRank("0|100001")
        val ranks = mutableSetOf<LexoRank>()

        for (i in 1..10) {
            val newRank = leftRank.between(rightRank)
            ranks.add(newRank)
            leftRank = newRank
        }

        ranks.size shouldBe 10
    }

    "should correctly shift LexoInteger to left" {
        val integer = LexoInteger.parse("12345", LexoRank.NUMERAL_SYSTEM)
        val shiftedInteger = integer.shiftLeft(2)

        shiftedInteger.format() shouldBe "1234500"
    }

    "should correctly shift LexoInteger to right" {
        val integer = LexoInteger.parse("12345", LexoRank.NUMERAL_SYSTEM)
        val shiftedInteger = integer.shiftRight(2)

        shiftedInteger.format() shouldBe "123"
    }
})
