import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

open class Token
open class BinaryOpToken: Token()
data class UnaryOpToken(val op: Char): Token()
data class AddOpToken(val op: Char): BinaryOpToken()
data class MulOpToken(val op: Char): BinaryOpToken()
data class BinOpToken(val op: Char): BinaryOpToken()
data class NumToken(val num: Int): Token()
data class ParenToken(val paren: Char): Token()

data class ParseResult(val num: Int, val op: UnaryOpToken? = null)

class TokenScanner(val tokenList: List<Token>) {
    var idx = 0

    fun next(): Token? = if (idx < tokenList.size) tokenList[idx] else null

    fun inc() = idx++

    override fun toString(): String = "($idx): $tokenList"
}

val unOpChars = setOf('~', '!')
val unOpStrings = unOpChars.map { it.toString() }.toSet()

val addOpChars = setOf('+', '-')
val addOpStrings = addOpChars.map { it.toString() }.toSet()

val mulOpChars = setOf('*', '/', '%')
val mulOpStrings = mulOpChars.map { it.toString() }.toSet()

val binOpChars = setOf('&', '|', '^')
val binOpStrings = binOpChars.map { it.toString() }.toSet()

val parenChars = setOf('(', ')')
val parenStrings = parenChars.map { it.toString() }.toSet()

val sc = BufferedReader(InputStreamReader(System.`in`))

fun listFromInput(input: String, prevResult: Int): List<String> {
    val sb = StringBuilder()
    input.forEach {
        when (it) {
            in unOpChars,
            in addOpChars,
            in mulOpChars,
            in binOpChars,
            in parenChars -> sb.append(" $it ")
            '_' -> sb.append("$prevResult")
            else -> sb.append("$it")
        }
    }
    return sb.toString().trim().split(Regex("\\s+"))
}

fun tokensFromList(li: List<String>): List<Token> {
    return li.map {
        when {
            it in unOpStrings -> {
                UnaryOpToken(it[0])
            }
            it in addOpStrings -> {
                AddOpToken(it[0])
            }
            it in mulOpStrings -> {
                MulOpToken(it[0])
            }
            it in binOpStrings -> {
                BinOpToken(it[0])
            }
            it in parenStrings -> {
                ParenToken(it[0])
            }
            it.isNotEmpty() && it.all { it.isDigit() } -> {
                NumToken(it.toInt())
            }
            else -> throw IllegalArgumentException()
        }
    }
}

fun unOpToFun(token: UnaryOpToken): (i: Int) -> Int = when (token.op) {
    '~' -> { i -> i.inv() }
    '!' -> { i -> -i }
    else -> {
        throw UnsupportedOperationException(
                "${token.op} is not a supported op"
        )
    }
}

fun addOpToFun(token: AddOpToken): (i: Int, j: Int) -> Int = when (token.op) {
    '+' -> { i, j -> i + j }
    '-' -> { i, j -> i - j }
    else -> {
        throw UnsupportedOperationException(
                "${token.op} is not a supported op"
        )
    }
}

fun mulOpToFun(token: MulOpToken): (i: Int, j: Int) -> Int = when (token.op) {
    '*' -> { i, j -> i * j }
    '/' -> { i, j -> i / j }
    '%' -> { i, j -> i % j }
    else -> {
        throw UnsupportedOperationException(
                "${token.op} is not a supported op"
        )
    }
}

fun binOpToFun(token: BinOpToken): (i: Int, j: Int) -> Int = when (token.op) {
    '&' -> { i, j -> i.and(j) }
    '|' -> { i, j -> i.or(j) }
    '^' -> { i, j -> i.xor(j) }
    else -> {
        throw UnsupportedOperationException(
                "${token.op} is not a supported op"
        )
    }
}

fun binaryOpToFun(token: BinaryOpToken): (i: Int, j: Int) -> Int = when (token) {
    is AddOpToken -> addOpToFun(token)
    is MulOpToken -> mulOpToFun(token)
    is BinOpToken -> binOpToFun(token)
    else -> throw UnsupportedOperationException()
}

fun parsePostfix(tokens: List<Token>): Int {
    val stack = Stack<Int>()
    tokens.forEach {
        when(it) {
            is NumToken -> stack.push(it.num)
            is UnaryOpToken -> {
                require(stack.size >= 1) {
                    "Not a valid postfix expression"
                }
                val f = unOpToFun(it)
                val n = stack.pop()
                stack.push(f(n))
            }
            is BinaryOpToken -> {
                require(stack.size >= 2) {
                    "Not a valid postfix expression"
                }
                val f = binaryOpToFun(it)
                val n2 = stack.pop()
                val n1 = stack.pop()
                stack.push(f(n1, n2))
            }
            is ParenToken -> {
                throw UnsupportedOperationException(
                        "${it.paren} is not a supported operation"
                )
            }
        }
    }
    require(stack.size == 1) { "Not a valid postfix expression" }
    return stack.pop()
}

fun expr(tsc: TokenScanner): ParseResult {
    val (num, _) = addExpr(tsc)
    return ParseResult(num)
}

fun addExpr(tsc: TokenScanner): ParseResult {
    val (num1, _) = mulExpr(tsc)
    return addExprPred(tsc, num1)
}

fun addExprPred(tsc: TokenScanner, num1: Int): ParseResult {
    val curr = tsc.next()
    return when (curr) {
        is AddOpToken -> {
            tsc.inc()
            val (num2, _) = mulExpr(tsc)
            val result = addOpToFun(curr)(num1, num2)

            val (num, _) = addExprPred(tsc, result)

            ParseResult(num)
        }
        else -> ParseResult(num1)
    }
}

fun mulExpr(tsc: TokenScanner): ParseResult {
    val (num1, _) = binExpr(tsc)
    return mulExprPred(tsc, num1)
}

fun mulExprPred(tsc: TokenScanner, num1: Int): ParseResult {
    val curr = tsc.next()
    return when (curr) {
        is MulOpToken -> {
            tsc.inc()
            val (num2, _) = binExpr(tsc)
            val result = mulOpToFun(curr)(num1, num2)

            val (num, _) = mulExprPred(tsc, result)

            ParseResult(num)
        }
        else -> ParseResult(num1)
    }
}

fun binExpr(tsc: TokenScanner): ParseResult {
    val (num1, op1) = primary(tsc)
    val newNum1 = when (op1) {
        is UnaryOpToken -> unOpToFun(op1)(num1)
        else -> num1
    }

    return binExprPred(tsc, newNum1)
}

fun binExprPred(tsc: TokenScanner, newNum1: Int): ParseResult {
    val curr = tsc.next()
    return when (curr) {
        is BinOpToken -> {
            tsc.inc()
            val (num2, op2) = primary(tsc)
            val newNum2 = when (op2) {
                is UnaryOpToken -> unOpToFun(op2)(num2)
                else -> num2
            }
            val result = binOpToFun(curr)(newNum1, newNum2)

            val (num, _) = binExprPred(tsc, result)

            ParseResult(num)
        }
        else -> ParseResult(newNum1)
    }
}

fun primary(tsc: TokenScanner): ParseResult {
    val curr = tsc.next()
    return when (curr) {
        is ParenToken -> {
            require(curr.paren == '(') {
                "Expected '(' but found ${curr.paren}"
            }
            tsc.inc()

            val (num, _) = expr(tsc)

            val nextToken = tsc.next()
            when (nextToken) {
                is ParenToken -> require(nextToken.paren == ')') {
                    "Expected ')' but found ${curr.paren}"
                }
                else -> throw IllegalArgumentException()
            }
            tsc.inc()

            ParseResult(num)
        }
        is UnaryOpToken -> {
            tsc.inc()
            val (num, op) = primary(tsc)
            val newNum = when (op) {
                is UnaryOpToken -> unOpToFun(op)(num)
                else -> num
            }

            ParseResult(newNum, curr)
        }
        is NumToken -> {
            tsc.inc()
            ParseResult(curr.num)
        }
        else -> throw IllegalArgumentException()
    }
}

fun parseInfix(tokens: List<Token>): Int {
    val tsc = TokenScanner(tokens)
    val num = expr(tsc).num
    require(tsc.idx == tokens.size) { "Not a valid infix expression" }
    return num
}

fun main(args: Array<String>) {
    println("Enter 0 for Infix, 1 for Postfix, 2 for Help: ")
    val choice = sc.readLine().toInt()
    require(choice in 0..2) { "Not a valid choice" }

    if (choice == 2) {
        println("Valid Operations: ")
        println("  Unary: ~ bitwise inverse, ! unary negation")
        println("  Binary: + add, - subtract, * multiply, / divide, % modulus")
        println("  Numbers: [0-9]+ 32-bit integer values, _ previous result")
        println("  Other (Infix only): ( left parenthesis, ) right parenthesis")
        System.exit(0)
    }

    var prevResult = 0

    println("Enter 'q' to exit")

    while (true) {
        if (choice == 0) {
            try {
                println("Enter an infix expression:")

                val input = sc.readLine()

                if (input == "q") {
                    System.exit(0)
                }

                val strings = listFromInput(input, prevResult)
                val tokens = tokensFromList(strings)

                prevResult = parseInfix(tokens)
                println(prevResult)
            }
            catch (e: Exception) {
                println("Not a valid infix expression")
            }
        } else {
            try {
                println("Enter a postfix expression:")

                val input = sc.readLine()

                if (input == "q") {
                    System.exit(0)
                }

                val strings = listFromInput(input, prevResult)
                val tokens = tokensFromList(strings)

                prevResult = parsePostfix(tokens)
                println(prevResult)
            }
            catch (e: Exception) {
                println("Not a valid postfix expression")
            }
        }
    }
}