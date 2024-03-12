import com.github.codexwr.ULID
import com.github.codexwr.ULIDJava
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

class ULIDTest {
    private val ulidFormatRegex = """[0123456789ABCDEFGHJKMNPQRSTVWXYZ]{26}""".toRegex()

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

    @RepeatedTest(value = 1000)
    @DisplayName("UUID로부터 ULID 생성")
    fun generateULIDFromUUID() {
        val id = ULID.fromUUID(UUID.randomUUID())
        assertNotNull(id, "생성된 id는 null일 수 없다.")
        assertTrue(ulidFormatRegex.matches(id.toString()), "생성된 id의 형식이 잘못되었다.")
    }

    @Test
    @DisplayName("static ULID 생성")
    fun generateStaticULID() {
        val idStr = ULID.nextULID()
        assertNotNull(idStr, "생성된 id는 null일 수 없다.")
        assertTrue(ulidFormatRegex.matches(idStr), "생성된 id의 형식이 잘못되었다.")
    }

    @Test
    @DisplayName("static Value 생성")
    fun generateStaticValue() {
        val id = ULID.nextValue()
        assertNotNull(id, "생성된 id는 null일 수 없다.")
        assertTrue(ulidFormatRegex.matches(id.toString()), "생성된 id의 형식이 잘못되었다.")
    }
}