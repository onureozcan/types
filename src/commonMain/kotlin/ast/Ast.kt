package ast

import PredefinedTypes
import TypeDefinition
import TypeExpression

interface Statement

interface Expression: Statement {
    fun resolveType(): TypeExpression
}

open class BinaryExpression(
    private val left: Expression,
    private val operator: BinaryOperator,
    private val right: Expression
): Expression {
    override fun resolveType(): TypeExpression {
        return operator.getResultingType(left, right)
    }
}

class ValueExpression(
    val data: String,
    private val valueType: ValueType
): Expression {

    companion object {
        enum class ValueType {
            VALUE_TYPE_STRING, VALUE_TYPE_DECIMAL, VALUE_TYPE_INTEGRAL, VALUE_TYPE_BOOL
        }
    }

    override fun resolveType(): TypeExpression {
        return when(valueType) {
            ValueType.VALUE_TYPE_BOOL -> PredefinedTypes.typeBool
            ValueType.VALUE_TYPE_STRING -> PredefinedTypes.typeString
            ValueType.VALUE_TYPE_INTEGRAL ->  PredefinedTypes.typeInt
            ValueType.VALUE_TYPE_DECIMAL -> PredefinedTypes.typeDouble
        }
    }
}

interface LvalueExpression: Expression

class ThisExpression(
    private val typeOfThis: TypeDefinition,
    private val symbolName: String
): LvalueExpression {
    override fun resolveType(): TypeExpression {
        return typeOfThis.find(symbolName) ?: throw RuntimeException("cannot find $symbolName on type ${typeOfThis.name}")
    }
}

class DotAccessExpression(
    private val left: LvalueExpression,
    private val symbolName: String
): LvalueExpression {
    override fun resolveType(): TypeExpression {
        return left.resolveType().find(symbolName)?.type ?: throw RuntimeException("cannot find $symbolName on chain access")
    }
}

class Assignment(
    left: LvalueExpression,
    right: Expression): BinaryExpression(left, AssignmentOperator ,right) {

}

class Loop {

}

class Branch {

}

class Function {

}