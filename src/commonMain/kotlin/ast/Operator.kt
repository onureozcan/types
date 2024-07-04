package ast

import PredefinedTypes
import PredefinedTypes.Companion.typeDouble
import PredefinedTypes.Companion.typeInt
import PredefinedTypes.Companion.typeNumber
import PredefinedTypes.Companion.typeString
import TypeExpression


interface UnaryOperator {
    fun getResultingType(operand: Expression): TypeExpression
}

interface BinaryOperator {
    fun getResultingType(left: Expression, right: Expression): TypeExpression
}

object AssignmentOperator: BinaryOperator {
    override fun getResultingType(left: Expression, right: Expression): TypeExpression {
        return PredefinedTypes.typeVoid
    }
}

object AdditionOperator: BinaryOperator {
    override fun getResultingType(left: Expression, right: Expression): TypeExpression {
        val leftType = left.resolveType()
        val rightType = right.resolveType()

        if (typeString.isAssignableFrom(leftType) && typeString.isAssignableFrom(rightType)) {
            return typeString
        }

        if (!typeNumber.isAssignableFrom(leftType) || !typeNumber.isAssignableFrom(rightType)) {
            throw RuntimeException("Addition operation is defined only between 2 numbers or 2 strings")
        }

        if (typeDouble.isAssignableFrom(leftType) || typeDouble.isAssignableFrom(rightType)) {
            return typeDouble
        }
        return typeInt
    }
}