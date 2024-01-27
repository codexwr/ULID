package com.chans.codexwr

import java.io.Serializable
import java.security.SecureRandom
import java.util.*


class ULID(private val random: Random = SecureRandom()) {

    class Value(val mostSignificantBits: Long, val leastSignificantBits: Long) : Comparable<Value>, Serializable {
        companion object {
            @Suppress("ConstPropertyName")
            private const val serialVersionUID: Long = -9047506782422892070L
        }

        val timestamp get() = mostSignificantBits ushr 16

        fun toUUID() = UUID(mostSignificantBits, leastSignificantBits)

        fun toBytes(): ByteArray {
            val result = ByteArray(16)
            for (i in 0 until 8) {
                result[i] = ((mostSignificantBits shr ((7 - i) * 8)) and 0xFF).toByte()
            }
            for (i in 8 until 16) {
                result[i] = ((leastSignificantBits shr ((15 - i) * 8)) and 0xFF).toByte()
            }

            return result
        }

        fun increment(): Value {
            val lsb = leastSignificantBits
            if (lsb != -1L) return Value(mostSignificantBits, lsb + 1)

            val msb = mostSignificantBits
            if ((msb and RANDOM_MSB_MASK) != RANDOM_MSB_MASK) return Value(msb + 1, 0)

            return Value(msb and TIMESTAMP_MSB_MASK, 0)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Value

            if (mostSignificantBits != other.mostSignificantBits) return false
            if (leastSignificantBits != other.leastSignificantBits) return false

            return true
        }

        override fun hashCode(): Int {
            val hilo = mostSignificantBits xor leastSignificantBits
            return (hilo shr 32).toInt() xor hilo.toInt()
        }

        override fun compareTo(other: Value): Int {
            // The ordering is intentionally set up so that the ULIDs
            // can simply be numerically compared as two numbers
            return if (mostSignificantBits < other.mostSignificantBits) -1
            else if (mostSignificantBits > other.mostSignificantBits) 1
            else if (leastSignificantBits < other.leastSignificantBits) -1
            else if (leastSignificantBits > other.leastSignificantBits) 1
            else 0
        }

        override fun toString(): String {
            val buffer = CharArray(26)

            internalWriteCrockford(buffer, timestamp, 10, 0)
            val value = ((mostSignificantBits and 0xFFFFL) shl 24) or (leastSignificantBits ushr 40)
            internalWriteCrockford(buffer, value, 8, 10)
            internalWriteCrockford(buffer, leastSignificantBits, 8, 18)

            return String(buffer)
        }
    }

    companion object {
        private val ENCODING_CHARS = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
            'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X',
            'Y', 'Z',
        )
        private val DECODING_CHARS = byteArrayOf(
            // 0
            -1, -1, -1, -1, -1, -1, -1, -1,
            // 8
            -1, -1, -1, -1, -1, -1, -1, -1,
            // 16
            -1, -1, -1, -1, -1, -1, -1, -1,
            // 24
            -1, -1, -1, -1, -1, -1, -1, -1,
            // 32
            -1, -1, -1, -1, -1, -1, -1, -1,
            // 40
            -1, -1, -1, -1, -1, -1, -1, -1,
            // 48
            0, 1, 2, 3, 4, 5, 6, 7,
            // 56
            8, 9, -1, -1, -1, -1, -1, -1,
            // 64
            -1, 10, 11, 12, 13, 14, 15, 16,
            // 72
            17, 1, 18, 19, 1, 20, 21, 0,
            // 80
            22, 23, 24, 25, 26, -1, 27, 28,
            // 88
            29, 30, 31, -1, -1, -1, -1, -1,
            // 96
            -1, 10, 11, 12, 13, 14, 15, 16,
            // 104
            17, 1, 18, 19, 1, 20, 21, 0,
            // 112
            22, 23, 24, 25, 26, -1, 27, 28,
            // 120
            29, 30, 31,
        )

        private const val MASK = 0x1FL
        private const val MASK_BITS = 5
        private const val TIMESTAMP_OVERFLOW_MASK: Long = -0x0001_0000_0000_0000
        private const val TIMESTAMP_MSB_MASK: Long = -0x0001_0000
        private const val RANDOM_MSB_MASK: Long = 0xFFFF

        private fun checkTimestamp(timestamp: Long) {
            if ((timestamp and TIMESTAMP_OVERFLOW_MASK) != 0L)
                throw IllegalArgumentException("ULID does not support timestamps after +10889-08-02T05:31:50.655Z!")
        }

        /*
         * http://crockford.com/wrmg/base32.html
         */
        private fun internalAppendCrockford(builder: StringBuilder, value: Long, count: Int) {
            for (i in (count - 1) downTo 0) {
                val index = ((value ushr (i * MASK_BITS)) and MASK).toInt()
                builder.append(ENCODING_CHARS[index])
            }
        }

        private fun internalWriteCrockford(buffer: CharArray, value: Long, count: Int, offset: Int) {
            for (i in 0 until count) {
                val index = ((value ushr ((count - i - 1) * MASK_BITS)) and MASK).toInt()
                buffer[offset + i] = ENCODING_CHARS[index]
            }
        }

        private fun internalParseCrockford(input: String): Long {
            if (input.length > 12)
                throw IllegalArgumentException("input length must not exceed 12 but was ${input.length}!")

            var result = 0L
            input.forEachIndexed { index, c ->
                var value: Byte = -1
                if (c.code < DECODING_CHARS.count())
                    value = DECODING_CHARS[c.code]

                if (value < 0)
                    throw IllegalArgumentException("Illegal character '$c'!")

                result = result or ((value.toLong()) shl ((input.length - 1 - index) * MASK_BITS))
            }

            return result
        }

        private fun internalUIDString(timestamp: Long, random: Random): String {
            checkTimestamp(timestamp)

            val buffer = CharArray(26)

            internalWriteCrockford(buffer, timestamp, 10, 0)
            internalWriteCrockford(buffer, random.nextLong(), 8, 10)
            internalWriteCrockford(buffer, random.nextLong(), 8, 18)

            return String(buffer)
        }

        private fun internalNextValue(timestamp: Long, random: Random): Value {
            checkTimestamp(timestamp)
            // could use nextBytes(byte[] bytes) instead
            val msb = (random.nextLong() and 0xFFFF) or (timestamp shl 16)

            return Value(msb, random.nextLong())
        }

        private fun internalAppendULID(builder: StringBuilder, timestamp: Long, random: Random) {
            checkTimestamp(timestamp)

            internalAppendCrockford(builder, timestamp, 10)
            // could use nextBytes(byte[] bytes) instead
            internalAppendCrockford(builder, random.nextLong(), 8)
            internalAppendCrockford(builder, random.nextLong(), 8)
        }

        fun fromUUID(uuid: UUID) = Value(uuid.mostSignificantBits, uuid.leastSignificantBits)

        fun fromBytes(data: ByteArray): Value {
            if (data.size != 16) throw IllegalArgumentException("data must be 16 bytes in length!")

            var msb = 0L
            var lsb = 0L
            for (i in 0 until 8) {
                msb = (msb shl 8) or (data[i].toInt() and 0xFF).toLong()
            }
            for (i in 8 until 16) {
                lsb = (lsb shl 8) or (data[i].toInt() and 0xFF).toLong()
            }

            return Value(msb, lsb)
        }

        fun parseULID(ulidStirng: String): Value {
            if (ulidStirng.length != 26) throw IllegalArgumentException("ulidString must be exactly 26 chars long.")

            val timeString = ulidStirng.substring(0, 10)
            val time = internalParseCrockford(timeString)
            if ((time and TIMESTAMP_OVERFLOW_MASK) != 0L) throw IllegalArgumentException("ulidString must not exceed '7ZZZZZZZZZZZZZZZZZZZZZZZZZ'!")

            val part1String = ulidStirng.substring(10, 18)
            val part2String = ulidStirng.substring(18)
            val part1 = internalParseCrockford(part1String)
            val part2 = internalParseCrockford(part2String)

            val msb = (time shl 16) or (part1 ushr 24)
            val lsb = part2 or (part1 shl 40)

            return Value(msb, lsb)
        }


    }

    fun appendULID(stringBuilder: StringBuilder) {
        internalAppendULID(stringBuilder, System.currentTimeMillis(), random)
    }

    fun nextULID(timestamp: Long = System.currentTimeMillis()) = internalUIDString(timestamp, random)

    fun nextValue(timestamp: Long = System.currentTimeMillis()) = internalNextValue(timestamp, random)

    /**
     * Returns the next monotonic value. If an overflow happened while incrementing
     * the random part of the given previous ULID value then the returned value will
     * have a zero random part.
     *
     * @param previousUlid the previous ULID value.
     * @param timestamp the timestamp of the next ULID value.
     * @return the next monotonic value.
     */
    fun nextMonotonicValue(previousUlid: Value, timestamp: Long = System.currentTimeMillis()): Value {
        if (previousUlid.timestamp == timestamp) return previousUlid.increment()

        return nextValue(timestamp)
    }

    /**
     * Returns the next monotonic value or empty if an overflow happened while incrementing
     * the random part of the given previous ULID value.
     *
     * @param previousUlid the previous ULID value.
     * @param timestamp the timestamp of the next ULID value.
     * @return the next monotonic value or empty if an overflow happened.
     */
    fun nextStrictlyMonotonicValue(previousUlid: Value, timestamp: Long): Value? {
        val result = nextMonotonicValue(previousUlid, timestamp)
        if (result.compareTo(previousUlid) < 1) return null

        return result
    }
}
