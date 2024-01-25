import com.chans.codexwr.ULID
import com.chans.codexwr.ULIDJava
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ULIDTest {
    @Test
    @DisplayName("Java로 된 원래 ULID와 Kotlin으로 개발된 ULID가 동일한 동작을 수행하는지 테스트")
    fun ulidCompare() {
        /// given
        val juid = ULIDJava()
        val kuid = ULID()
        println(StringBuilder(26).also { juid.appendULID(it) })
        println(StringBuilder(26).also { kuid.appendULID(it) })

        /// when
        val timestamp = System.currentTimeMillis()
        val jid = juid.nextULID(timestamp).also { println(String.format("%12s: $it", "java ulid")) }
        val kid = kuid.nextULID(timestamp).also { println(String.format("%12s: $it", "kotlin ulid")) }

        /// then
        // java에서 생성된 id를 파싱하여 Value 객체의 상태 값이 같은지 확인
        var jVal = ULIDJava.parseULID(jid)
        var kVal = ULID.parseULID(jid)

        assertEquals(jVal.toUUID(), kVal.toUUID())
        assertEquals(jVal.toString(), kVal.toString())
        assertContentEquals(jVal.toBytes(), kVal.toBytes())
        assertEquals(ULIDJava.fromBytes(jVal.toBytes()).toUUID(), ULID.fromBytes(kVal.toBytes()).toUUID())
        assertEquals(
            juid.nextMonotonicValue(jVal, timestamp).toUUID(),
            kuid.nextMonotonicValue(ULID.Value(jVal.mostSignificantBits, jVal.leastSignificantBits), timestamp).toUUID()
        )

        // kotlin에서 생성된 id를 파싱하여 Value 객체의 상태 값이 같은지 확인
        jVal = ULIDJava.parseULID(kid)
        kVal = ULID.parseULID(kid)
        assertEquals(jVal.toUUID(), kVal.toUUID())
        assertEquals(jVal.toString(), kVal.toString())
        assertContentEquals(jVal.toBytes(), kVal.toBytes())
        assertEquals(ULIDJava.fromBytes(jVal.toBytes()).toUUID(), ULID.fromBytes(kVal.toBytes()).toUUID())
        assertEquals(
            juid.nextMonotonicValue(jVal, timestamp).toUUID(),
            kuid.nextMonotonicValue(ULID.Value(jVal.mostSignificantBits, jVal.leastSignificantBits), timestamp).toUUID()
        )
    }
}