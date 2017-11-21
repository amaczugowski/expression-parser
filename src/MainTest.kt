import org.testng.annotations.Test
import kotlin.test.*

@Test fun testParseInfixValid() {
    val tests = mapOf(
            "0" to 0,
            "1 + 2 + 3" to 6,
            "3 + 1 * 2" to 5,
            "(3 + 1) * 2" to 8,
            "2%2" to 0,
            "3% 2" to 1,
            "5 / 3" to 1,
            "2 * 2 * 2" to 8,
            "2 & 4" to 0,
            "  5 & 4    " to 4,
            "!(5 + 10)" to -15,
            "1 - !(20 + 5)" to 26
    )

    for ((expr, ans) in tests) {
        val tokens = tokensFromList(listFromInput(expr, 0))
        assertEquals(ans, parseInfix(tokens))
    }
}

@Test fun testParseInfixInvalid() {
    val tests = listOf(
            "0 0",
            "1 + 2 +",
            "1 ~",
            "1 2 +",
            "1 + 2()",
            "(1 + 2"
    )

    for (expr in tests) {
        var exception: Exception? = null
        try {
            val tokens = tokensFromList(listFromInput(expr, 0))
            parseInfix(tokens)
        } catch (e: Exception) {
            exception = e
        }
        assertNotNull(exception)
    }
}

@Test fun testParseInfixWithPrevResult() {
    val tests = listOf(
            Triple("_", 6, 6),
            Triple("_ + 1", 5, 6),
            Triple("2 * !(_ + 15)", 15, -60)
    )

    for ((expr, prevResult, ans) in tests) {
        val tokens = tokensFromList(listFromInput(expr, prevResult))
        assertEquals(ans, parseInfix(tokens))
    }
}

@Test fun testParsePostfixValid() {
    val tests = mapOf(
            "0" to 0,
            "1 2 + 3 +" to 6,
            "3 1 2 * +" to 5,
            "3 1 +  2*" to 8,
            "2 2%  " to 0,
            "3 2 %" to 1,
            "5  3 /" to 1,
            "2  2 * 2 *" to 8,
            "2  4 &" to 0,
            "  5  4  &  " to 4,
            "5  10 +!" to -15,
            "1  20  5 + ! -" to 26
    )

    for ((expr, ans) in tests) {
        val tokens = tokensFromList(listFromInput(expr, 0))
        assertEquals(ans, parsePostfix(tokens))
    }
}

@Test fun testParsePostfixInvalid() {
    val tests = listOf(
            "0 0",
            "1  2 + +",
            "~ 1",
            "1 + 2",
            "1 + 2()",
            "(1 2 +)"
    )

    for (expr in tests) {
        var exception: Exception? = null
        try {
            val tokens = tokensFromList(listFromInput(expr, 0))
            parsePostfix(tokens)
        } catch (e: Exception) {
            exception = e
        }
        assertNotNull(exception)
    }
}

@Test fun testTokensFromListInvalid() {
    val tests = listOf(
            "1 + a",
            "a"
    )

    for (expr in tests) {
        var exception: Exception? = null
        try {
            tokensFromList(listFromInput(expr, 0))
        } catch (e: Exception) {
            exception = e
        }
        assertNotNull(exception)
    }
}