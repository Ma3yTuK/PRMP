package com.example.rpmp

import java.math.BigDecimal
import java.math.RoundingMode


val NUMBER_REGEX = "[0-9]+(\\.[0-9]*)?|\\.[0-9]+".toRegex()


interface CalcObject {
    val value: String
}

enum class BinaryOperation(override val value: String, val priority: Int) : CalcObject {
    ADD("+", 0),
    SUB("-", 0),
    DIV("/", 1),
    MUL("*", 1),
    POW("^", 2),
    LOG("log", 3),
}

enum class UnaryOperation(override val value: String) : CalcObject {
    SIN("sin("),
    COS("cos("),
    TAN("tan("),
    ASIN("asi("),
    ACOS("aco("),
    ATAN("ata(")
}

enum class NonBinaryOperation(override val value: String) : CalcObject {
    PI("Pi"),
    E("e"),
    FACT("!"),
    PAR_O("("),
    PAR_C(")")
}

data class Operand(override var value: String): CalcObject


class Calculator(
    private val precision: Int,
    wholePartLength: Int = 0
) {
    private val error = BigDecimal(5).divide(BigDecimal(10).pow(precision+2))
    private val maxValue = BigDecimal(10).pow(wholePartLength)
    private val ln2 = multiply(BigDecimal(2), calculateSum { divide(BigDecimal(1), multiply(add(multiply(it, BigDecimal(2)), BigDecimal(1)), integerPow(BigDecimal(3), add(multiply(it, BigDecimal(2)), BigDecimal(1)).setScale(0, RoundingMode.HALF_UP)))) })
    private val e = exp(BigDecimal(1))
    private val pi = multiply(subtract(multiply(atan(divide(BigDecimal(2), BigDecimal(10))), BigDecimal(4)), atan(divide(BigDecimal(1), BigDecimal(239)))), BigDecimal(4))

    private val binaryOperationFunctions = mapOf(
        BinaryOperation.ADD to fun(value1: BigDecimal, value2: BigDecimal): BigDecimal { return add(value1, value2) },
        BinaryOperation.SUB to fun(value1: BigDecimal, value2: BigDecimal): BigDecimal { return subtract(value1, value2) },
        BinaryOperation.MUL to fun(value1: BigDecimal, value2: BigDecimal): BigDecimal { return multiply(value1, value2) },
        BinaryOperation.DIV to fun(value1: BigDecimal, value2: BigDecimal): BigDecimal { return divide(value1, value2) },
        BinaryOperation.POW to fun(value1: BigDecimal, value2: BigDecimal): BigDecimal { return pow(value1, value2) },
        BinaryOperation.LOG to fun(value1: BigDecimal, value2: BigDecimal): BigDecimal { return log(value1, value2) }
    )

    private val unaryOperationFunctions = mapOf(
        UnaryOperation.SIN to fun(value: BigDecimal): BigDecimal { return sin(value) },
        UnaryOperation.COS to fun(value: BigDecimal): BigDecimal { return cos(value) },
        UnaryOperation.TAN to fun(value: BigDecimal): BigDecimal { return tan(value) },
        UnaryOperation.ASIN to fun(value: BigDecimal): BigDecimal { return asin(value) },
        UnaryOperation.ACOS to fun(value: BigDecimal): BigDecimal { return acos(value) },
        UnaryOperation.ATAN to fun(value: BigDecimal): BigDecimal { return atan(value) },
    )

    private fun checkValue(value: BigDecimal) {
        if (maxValue != BigDecimal(10) && maxValue <= value.abs() && pi != null)
            throw Exception("Result is too big")
    }

    private fun integerPow(value: BigDecimal, n: BigDecimal): BigDecimal {
        if (n.scale() != 0) {
            throw Exception("Value should be integer")
        }

        var result = BigDecimal(1)
        var currentValue = value
        var n = n

        if (n < BigDecimal(0)) {
            currentValue = divide(BigDecimal(1), value)
            n = -n
        }

        if (n == BigDecimal(0))
            return BigDecimal(1)

        var currentIndex = n
        while (currentIndex != BigDecimal(1)) {

            if (currentIndex.remainder(BigDecimal(2)) == BigDecimal(0)) {
                currentValue = multiply(currentValue, currentValue)
                currentIndex = divide(currentIndex, (BigDecimal(2))).setScale(0, RoundingMode.HALF_UP)
            } else {
                result = multiply(result, currentValue)
                currentIndex = subtract(currentIndex, BigDecimal(1)).setScale(0, RoundingMode.HALF_UP)
            }

            if (Thread.currentThread().isInterrupted)
                throw InterruptedException("Interrupted")
        }

        return multiply(result, currentValue)
    }

    private fun calculateSum(calculateIteration: (BigDecimal) -> BigDecimal): BigDecimal {
        var result = BigDecimal(0)
        var currentIteration = BigDecimal(0)
        var currentIterationValue = calculateIteration(currentIteration)

        while (currentIterationValue.abs() > BigDecimal(0)) {
            result = add(result, currentIterationValue)
            currentIteration = add(currentIteration, BigDecimal(1))
            currentIterationValue = calculateIteration(currentIteration)

            if (Thread.currentThread().isInterrupted)
                throw InterruptedException("Interrupted")
        }

        return result
    }

    private fun exp(value: BigDecimal): BigDecimal {

        val calculateIteration = { i: BigDecimal ->
            divide(integerPow(value, i), integerFactorial(i))
        }

        return calculateSum(calculateIteration)
    }

    private fun ln(value: BigDecimal): BigDecimal {

        if (value <= BigDecimal(0))
            throw Exception("Invalid ln parameter")

        var coeff = BigDecimal(0)
        var divisorResult = value
        while (subtract(divisorResult, divide(BigDecimal(4), BigDecimal(3))) > error || subtract(divisorResult, divide(BigDecimal(2), BigDecimal(3))) < -error) {

            if (subtract(divisorResult, divide(BigDecimal(4), BigDecimal(3))) > error) {
                var coeffCorrection = BigDecimal(1)
                var divisorCorrection = BigDecimal(2)
                divisorResult = divide(divisorResult, divisorCorrection)
                while (subtract(divisorResult, divide(BigDecimal(4), BigDecimal(3))) > error) {
                    coeffCorrection = multiply(coeffCorrection, (BigDecimal(2)))
                    divisorResult = divide(divisorResult, divisorCorrection)
                    divisorCorrection = multiply(divisorCorrection, divisorCorrection)

                    if (Thread.currentThread().isInterrupted)
                        throw InterruptedException("Interrupted")
                }
                coeff = add(coeff, coeffCorrection)
            }

            if (subtract(divisorResult, divide(BigDecimal(2), BigDecimal(3))) < -error) {
                var coeffCorrection = BigDecimal(1)
                var divisorCorrection = BigDecimal(2)
                divisorResult = multiply(divisorResult, divisorCorrection)
                while (subtract(divisorResult, divide(BigDecimal(2), BigDecimal(3))) < -error) {
                    coeffCorrection = multiply(coeffCorrection, BigDecimal(2))
                    divisorResult = multiply(divisorResult, divisorCorrection)
                    divisorCorrection = multiply(divisorCorrection, divisorCorrection)

                    if (Thread.currentThread().isInterrupted)
                        throw InterruptedException("Interrupted")
                }
                coeff = subtract(coeff, coeffCorrection)
            }
        }

        val calculateIteration = { i: BigDecimal ->
            divide(multiply(integerPow(BigDecimal(-1), add(i, BigDecimal(2))), integerPow(subtract(divisorResult, BigDecimal(1)), add(i, BigDecimal(1)))), add(i, BigDecimal(1)))
        }

        return add(multiply(ln2, coeff), calculateSum(calculateIteration))
    }

    private fun atanRestricted(value: BigDecimal): BigDecimal {

        if (value < BigDecimal(0))
            return -atanRestricted(-value)

        if (subtract(value, BigDecimal(1)) > BigDecimal(0))
            throw Exception("Invalid atan parameter")

        if (subtract(value.abs(), BigDecimal(0.5)) > error)
            return divide(pi, BigDecimal(4)) + atan(divide(value.subtract(BigDecimal(1)), add(value, BigDecimal(1))))

        val calculateIteration = { i: BigDecimal ->
            val tmp = add(multiply(BigDecimal(2), i), BigDecimal(1)).setScale(0, RoundingMode.HALF_UP)
            divide(multiply(integerPow(BigDecimal(-1), i), integerPow(value, tmp)), tmp)
        }

        return calculateSum(calculateIteration)
    }

    private fun integerFactorial(value: BigDecimal): BigDecimal {
        if (value.scale() != 0) {
            throw Exception("Value should be integer")
        }

        var result = BigDecimal(1)
        var currentValue = value

        while (currentValue != BigDecimal(0)) {
            result = multiply(result, currentValue)
            currentValue = subtract(currentValue, BigDecimal(1))

            if (Thread.currentThread().isInterrupted)
                throw InterruptedException("Interrupted")
        }

        return result
    }

    private fun multiply(value1: BigDecimal, value2: BigDecimal): BigDecimal {
        val result = value1.multiply(value2)
        checkValue(result)

        return result.setScale(precision * 4, RoundingMode.HALF_UP)
    }

    private fun divide(value1: BigDecimal, value2: BigDecimal): BigDecimal {
        val result = value1.divide(value2, precision * 4, RoundingMode.HALF_UP)
        checkValue(result)
        return result
    }

    private fun add(value1: BigDecimal, value2: BigDecimal): BigDecimal {
        val result = value1.add(value2)
        checkValue(result)
        return result
    }

    private fun subtract(value1: BigDecimal, value2: BigDecimal): BigDecimal {
        val result = value1.subtract(value2)
        checkValue(result)
        return result
    }

    private fun factorial(value: BigDecimal): BigDecimal {
        if (subtract(value, value.setScale(0, RoundingMode.HALF_UP)).abs() > error) {
            throw Exception("Invalid factorial parameter")
        }

        return integerFactorial(value.setScale(0, RoundingMode.HALF_UP))
    }

    private fun pow(value1: BigDecimal, value2: BigDecimal): BigDecimal {
        if (value1 < error) {
            if (value1 < BigDecimal(0)) {
                if (subtract(value2.setScale(0, RoundingMode.HALF_UP), value2).abs() > error) {
                    throw Exception("Invalid pow paramenter")
                }
                return integerPow(value1, value2.setScale(0, RoundingMode.HALF_UP))
            } else {
                if (value2 < BigDecimal(0)) {
                    throw Exception("Invalid pow parameter")
                }
                return BigDecimal(0)
            }
        }

        val tmp = multiply(value2, ln(value1))
        val wholePart = tmp.setScale(0, RoundingMode.FLOOR)
        val fractionPart = tmp.subtract(wholePart)

        return multiply(integerPow(e, wholePart), exp(fractionPart))
    }

    private fun log(value1: BigDecimal, value2: BigDecimal): BigDecimal {
        if (subtract(value2, BigDecimal(1)).abs() < error)
            throw Exception("Invalid log parameter")

        return divide(ln(value1), ln(value2))
    }

    private fun sin(value: BigDecimal): BigDecimal {
        if (value < BigDecimal(0)) {
            return -sin(-value)
        }

        if (subtract(value, multiply(BigDecimal(2), pi)) > BigDecimal(0)) {
            return sin(subtract(value, multiply(value.divideToIntegralValue(multiply(BigDecimal(2), pi)), multiply(BigDecimal(2), pi))))
        }

        if (subtract(value, pi) > BigDecimal(0))  {
            return -sin(subtract(value, pi))
        }

        if (subtract(value, divide(pi, BigDecimal(2))) > BigDecimal(0)) {
            return -cos(subtract(value, divide(pi, BigDecimal(2))))
        }

        val calculateIteration = { i: BigDecimal ->
            val tmp = add(multiply(BigDecimal(2), i), BigDecimal(1)).setScale(0, RoundingMode.HALF_UP)
            divide(multiply(integerPow(BigDecimal(-1), i), integerPow(value, tmp)), integerFactorial(tmp))
        }

        return calculateSum(calculateIteration)
    }

    private fun cos(value: BigDecimal): BigDecimal {
        if (value < BigDecimal(0)) {
            return cos(-value)
        }

        if (subtract(value, multiply(BigDecimal(2), pi)) > BigDecimal(0)) {
            return cos(subtract(value, multiply(value.divideToIntegralValue(multiply(BigDecimal(2), pi)), multiply(BigDecimal(2), pi))))
        }

        if (subtract(value, pi) > BigDecimal(0))  {
            return -cos(subtract(value, pi))
        }

        if (subtract(value, divide(pi, BigDecimal(2))) > BigDecimal(0)) {
            return sin(subtract(value, divide(pi, BigDecimal(2))))
        }

        val calculateIteration = { i: BigDecimal ->
            val tmp = multiply(BigDecimal(2), i).setScale(0, RoundingMode.HALF_UP)
            divide(multiply(integerPow(BigDecimal(-1), i), integerPow(value, tmp)), integerFactorial(tmp))
        }

        return calculateSum(calculateIteration)
    }

    private fun tan(value: BigDecimal): BigDecimal {
        val tmp = cos(value)

        if (tmp.abs() < error)
            throw Exception("Invalid tan parameter")

        return divide(sin(value), tmp)
    }

    private fun asin(value: BigDecimal): BigDecimal {
        return subtract(divide(pi, BigDecimal(2)), acos(value))
    }

    private fun acos(value: BigDecimal): BigDecimal {
        if (value.abs() < error)
            return divide(pi, BigDecimal(2))

        return atan(pow(subtract(divide(BigDecimal(1), multiply(value, value)), BigDecimal(1)), divide(BigDecimal(1), BigDecimal(2))))
    }

    private fun atan(value: BigDecimal): BigDecimal {

        if (subtract(value.abs(), BigDecimal(1)) > BigDecimal(0)) {
            return divide(pi, BigDecimal(2)) - atanRestricted(divide(BigDecimal(1), value))
        }

        return atanRestricted(value)
    }

    private fun calculate(calcObjects: List<CalcObject>, index: Int): Pair<BigDecimal?, Int> {
        val operands = mutableListOf<BigDecimal>()
        val operations = mutableListOf<BinaryOperation>()

        var index = index
        while (index != calcObjects.size) {
            when (val calcObject = calcObjects[index]) {
                is UnaryOperation -> {
                    val (operand, newIndex) = calculate(calcObjects, index + 1)
                    operands.add(unaryOperationFunctions[calcObject]!!.invoke(operand!!))
                    index = newIndex
                    continue
                }
                is BinaryOperation -> {
                    if (calcObject == BinaryOperation.SUB && operands.size == operations.size) {
                        val numberSubstring = NUMBER_REGEX.matchAt(calcObjects[++index].value, 0)?.value ?: throw Exception("Invalid string")
                        operands.add(-BigDecimal(numberSubstring))
                    } else {
                        val currentPriority = calcObject.priority

                        while (operations.isNotEmpty()) {
                            val operation = operations.last()

                            if (operation.priority < currentPriority)
                                break

                            if (operands.size < 2)
                                throw Exception("Invalid string")

                            if (Thread.currentThread().isInterrupted)
                                throw InterruptedException("Interrupted")

                            val value2 = operands.removeLast()
                            val value1 = operands.removeLast()
                            operands.add(binaryOperationFunctions[operations.removeLast()]!!.invoke(value1, value2))
                        }
                        operations.add(calcObject)
                    }
                }
                NonBinaryOperation.PI -> operands.add(pi)
                NonBinaryOperation.E -> operands.add(e)
                NonBinaryOperation.PAR_O -> {
                    val (operand, newIndex) = calculate(calcObjects, index + 1)
                    operands.add(operand!!)
                    index = newIndex
                    continue
                }
                NonBinaryOperation.PAR_C -> {
                    index++
                    break
                }
                NonBinaryOperation.FACT -> {
                    if (operands.size < 1)
                        throw Exception("Invalid string")

                    operands.add(factorial(operands.removeLast()))
                }
                is Operand -> {
                    if (!NUMBER_REGEX.matches(calcObject.value))
                        throw Exception("Invalid string")

                    operands.add(BigDecimal(calcObject.value))
                }
            }
            index++
        }

        while (operations.isNotEmpty()) {
            if (operands.size < 2)
                throw Exception("Invalid string")

            if (Thread.currentThread().isInterrupted)
                throw InterruptedException("Interrupted")

            val value2 = operands.removeLast()
            val value1 = operands.removeLast()
            operands.add(binaryOperationFunctions[operations.removeLast()]!!.invoke(value1, value2))
        }

        if (operands.isEmpty())
            return Pair(null, index)

        if (operands.size != 1)
            throw Exception("Invalid string")

        checkValue(operands.last())
        return Pair(operands.last(), index)
    }

    fun calc(value: List<CalcObject>): String {
        val (result, newIndex) = calculate(value, 0)

        if (newIndex != value.size)
            throw Exception("Invalid string")

        return result?.setScale(precision, RoundingMode.HALF_EVEN)?.stripTrailingZeros()?.toPlainString() ?: ""
    }
}