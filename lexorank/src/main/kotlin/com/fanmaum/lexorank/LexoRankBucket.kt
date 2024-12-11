package com.fanmaum.lexorank

class LexoRankBucket private constructor(strFull: String) {
    private val value = LexoInteger.parse(strFull, LexoRank.NUMERAL_SYSTEM)

    fun format(): String {
        return value.format()
    }

    fun next(): LexoRankBucket {
        if (this === BUCKET_0) return BUCKET_1

        if (this === BUCKET_1) return BUCKET_2

        return if (this === BUCKET_2) BUCKET_0 else BUCKET_2
    }

    fun prev(): LexoRankBucket {
        if (this === BUCKET_0) return BUCKET_2

        if (this === BUCKET_1) return BUCKET_0

        return if (this === BUCKET_2) BUCKET_1 else BUCKET_0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as LexoRankBucket
        return value == that.value
    }

    override fun toString(): String {
        return format()
    }

    override fun hashCode(): Int = value.hashCode()

    companion object {
        val BUCKET_0: LexoRankBucket = LexoRankBucket("0")
        val BUCKET_1: LexoRankBucket = LexoRankBucket("1")
        val BUCKET_2: LexoRankBucket = LexoRankBucket("2")

        private val VALUES = arrayOf(BUCKET_0, BUCKET_1, BUCKET_2)

        fun resolve(bucketId: Int): LexoRankBucket {
            for (bucket in VALUES) {
                if (bucket == from(bucketId.toString())) return bucket
            }

            throw IllegalArgumentException("No bucket found with id $bucketId")
        }

        fun from(str: String): LexoRankBucket {
            val parsedInteger = LexoInteger.parse(str, LexoRank.NUMERAL_SYSTEM)

            for (bucket in VALUES) {
                if (bucket.value == parsedInteger) return bucket
            }

            throw IllegalArgumentException("Unknown bucket: $str")
        }

        fun min(): LexoRankBucket {
            return VALUES[0]
        }

        fun max(): LexoRankBucket {
            return VALUES[VALUES.size - 1]
        }
    }
}
