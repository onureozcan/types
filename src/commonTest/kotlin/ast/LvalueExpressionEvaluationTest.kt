package ast

import AssertionsHelper.assertDoesNotThrow
import AssertionsHelper.assertThrows
import PredefinedTypes.Companion.typeInt
import PredefinedTypes.Companion.typeString
import TypeDefinition
import kotlin.test.Test
import kotlin.test.assertEquals

class LvalueExpressionEvaluationTest {

    /**
     *  class A {
     *      a: Int
     *
     *      fun test() {
     *          this.a is Int
     *      }
     *  }
     */
    @Test
    fun `simple property access on this object`() {
        val context = TypeDefinition("A").property("a", typeInt)
        assertEquals(typeInt, ThisExpression(context, "a").resolveType())
    }

    /**
     *  class A {
     *      a: Int
     *
     *      fun test() {
     *          this.b is compilation error
     *      }
     *  }
     */
    @Test
    fun `simple property access on this object should error if property is not defined`() {
        val context = TypeDefinition("A").property("a", typeInt)
        val err = assertThrows<RuntimeException> { ThisExpression(context, "b").resolveType() }
        assertEquals("cannot find b on type A", err.message)
    }

    /**
     *  class A {
     *      a: Int
     *
     *      fun test() {
     *          this.a = 4
     *      }
     *  }
     */
    @Test
    fun `simple property assignment on this object`() {
        val context = TypeDefinition("A").property("a", typeInt)

       assertDoesNotThrow<RuntimeException> { Assignment(ThisExpression(context, "a"), ValueExpression("4", ValueExpression.Companion.ValueType.VALUE_TYPE_INTEGER)) }
    }

    /**
     *  class A {
     *      a: Int
     *
     *      fun test() {
     *          this.a = "test" fails
     *      }
     *  }
     */
    @Test
    fun `simple property assignment on this object fails if types are not assignable`() {
        val context = TypeDefinition("A").property("a", typeInt)

        val ex = assertThrows<RuntimeException> { Assignment(ThisExpression(context, "a"), ValueExpression("4", ValueExpression.Companion.ValueType.VALUE_TYPE_STRING)) }
        assertEquals("cannot assign Int to String", ex.message)
    }

    /**
     *  class B {
     *      b: String
     *  }
     *
     *  class A {
     *      a: B
     *
     *      fun test() {
     *          this.a is B
     *          this.a.b is String
     *      }
     *  }
     */
    @Test
    fun `dot chain property access on this object`() {
        val typeB = TypeDefinition("B").property("b", typeString).construct()
        val context = TypeDefinition("A").property("a", typeB)

        val thisExpression = ThisExpression(context, "a")
        val dotAccessExpression = DotAccessExpression(thisExpression, "b")

        assertEquals(typeB, thisExpression.resolveType())
        assertEquals(typeString, dotAccessExpression.resolveType())
    }
}