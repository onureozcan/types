import AssertionsHelper.assertThrows
import ast.AdditionOperator
import ast.BinaryExpression
import ast.ValueExpression
import kotlin.test.Test
import kotlin.test.assertEquals


class ExpressionEvaluationTest {

    @Test
    fun `simple addition between integers yield to int`() {
        val left = ValueExpression("3", ValueExpression.Companion.ValueType.VALUE_TYPE_INTEGER)
        val right = ValueExpression("5", ValueExpression.Companion.ValueType.VALUE_TYPE_INTEGER)

        val binary = BinaryExpression(left, AdditionOperator, right)

        assertEquals(PredefinedTypes.typeInt, binary.resolveType())
    }

    @Test
    fun `simple addition between integer and double yield to double`() {
        val left = ValueExpression("3", ValueExpression.Companion.ValueType.VALUE_TYPE_INTEGER)
        val right = ValueExpression("5", ValueExpression.Companion.ValueType.VALUE_TYPE_DECIMAL)

        val binary = BinaryExpression(left, AdditionOperator, right)

        assertEquals(PredefinedTypes.typeDouble, binary.resolveType())
    }

    @Test
    fun `simple addition between strings yield to string`() {
        val left = ValueExpression("a", ValueExpression.Companion.ValueType.VALUE_TYPE_STRING)
        val right = ValueExpression("b", ValueExpression.Companion.ValueType.VALUE_TYPE_STRING)

        val binary = BinaryExpression(left, AdditionOperator, right)

        assertEquals(PredefinedTypes.typeString, binary.resolveType())
    }

    @Test
    fun `simple addition between strings and numbers error`() {
        val left = ValueExpression("9", ValueExpression.Companion.ValueType.VALUE_TYPE_INTEGER)
        val right = ValueExpression("b", ValueExpression.Companion.ValueType.VALUE_TYPE_STRING)

        val binary = BinaryExpression(left, AdditionOperator, right)

        val thrown = assertThrows<RuntimeException> { binary.resolveType() }
        assertEquals("Addition operation is defined only between 2 numbers or 2 strings", thrown.message)
    }
}