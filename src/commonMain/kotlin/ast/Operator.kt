package ast

import PredefinedTypes
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

        if (!PredefinedTypes.typeNumber.isAssignableFrom(leftType) || !PredefinedTypes.typeNumber.isAssignableFrom(rightType)) {
            throw RuntimeException("Addition operation is defined only between numbers")
        }

        if (PredefinedTypes.typeDouble.isAssignableFrom(leftType) || PredefinedTypes.typeDouble.isAssignableFrom(rightType)) {
            return PredefinedTypes.typeDouble
        }
        return PredefinedTypes.typeInt
    }
}