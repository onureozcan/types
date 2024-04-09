import AssertionsHelper.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FunctionTypeTest {

    private val superType = TypeDefinition("SuperT").construct()
    private val subType = TypeDefinition("SubT").extends(superType).construct()
    private val t1 = TypeDefinition("T1").construct()
    private val t2 = TypeDefinition("T2").construct()
    private val t3 = TypeDefinition("nothing").construct()

    @Test
    fun `functions can NOT be assigned to other types`() {
        val fnc1 = FunctionType(listOf(), t3)
        assertFalse(t1.isAssignableFrom(fnc1))
    }

    @Test
    fun `other types can NOT be assigned to functions`() {
        val fnc1 = FunctionType(listOf(), t3)
        assertFalse(fnc1.isAssignableFrom(t1))
    }

    @Test
    fun `function with un-assignable return types are NOT compatible`() {
        val fnc1 = FunctionType(listOf(), t1)
        val fnc2 = FunctionType(listOf(), t2)
        assertFalse(fnc1.isAssignableFrom(fnc2))
    }

    @Test
    fun `function with different number of parameters are NOT compatible`() {
        val fnc1 = FunctionType(listOf(), t3)
        val fnc2 = FunctionType(listOf("param1" to t1, "param2" to t2), t3)
        assertFalse(fnc1.isAssignableFrom(fnc2))
        assertFalse(fnc2.isAssignableFrom(fnc1))
    }


    @Test
    fun `function with un-assignable parameters are NOT compatible`() {
        val fnc1 = FunctionType(listOf("param1" to t2, "param2" to t1), t3)
        val fnc2 = FunctionType(listOf("param1" to t1, "param2" to t2), t3)
        assertFalse(fnc1.isAssignableFrom(fnc2))
        assertFalse(fnc2.isAssignableFrom(fnc1))
    }

    @Test
    fun `function with NOT exact but assignable parameters and return types are NOT compatible - AKA invariant`() {
        val fnc1 = FunctionType(listOf("param1" to superType, "param2" to subType), t3)
        val fnc2 = FunctionType(listOf("param1" to subType, "param2" to subType), t3)
        assertFalse(fnc1.isAssignableFrom(fnc2))
    }

    @Test
    fun `function with exact parameters and return types are compatible`() {
        val fnc1 = FunctionType(listOf("param1" to subType, "param2" to subType), t3)
        val fnc2 = FunctionType(listOf("param1" to subType, "param2" to subType), t3)
        assertTrue(fnc1.isAssignableFrom(fnc2))
    }

    @Test
    fun `function type variables can be overloaded if they have the same return type`() {
        val fnc1 = FunctionType(listOf("param1" to t2, "param2" to t1), t3)
        val fnc2 = FunctionType(listOf("param1" to t1, "param2" to t2), t3)
        TypeDefinition("A")
            .property("fnc1", fnc1)
            .property("fnc1", fnc2)
            .construct()
    }

    @Test
    fun `function type variables can NOT be overloaded if they have different return types`() {
        val fnc1 = FunctionType(listOf("param1" to t2, "param2" to t1), t3)
        val fnc2 = FunctionType(listOf("param1" to t1, "param2" to t2), t2)
        val exception = assertThrows<RuntimeException> {
            TypeDefinition("A")
                .property("fnc1", fnc1)
                .property("fnc1", fnc2)
                .construct()
        }

        assertEquals("property fnc1 is already defined", exception.message)
    }

    @Test
    fun `function type variables can NOT be overloaded if they have the same parameters`() {
        val fnc1 = FunctionType(listOf("param1" to t1), t3)
        val fnc2 = FunctionType(listOf("param1" to t1), t3)
        val exception = assertThrows<RuntimeException> {
            TypeDefinition("A")
                .property("fnc1", fnc1)
                .property("fnc1", fnc2)
                .construct()
        }

        assertEquals("property fnc1 is already defined", exception.message)
    }

    @Test
    fun `function type variables can be overloaded if given parameter sizes are different`() {
        val fnc1 = FunctionType(listOf("param1" to t1, "param2" to t2), t3)
        val fnc2 = FunctionType(listOf("param1" to t1), t3)
        TypeDefinition("A")
            .property("fnc1", fnc1)
            .property("fnc1", fnc2)
            .construct()
    }

    @Test
    fun `only function types can be overloaded`() {
        val fnc1 = FunctionType(listOf("param1" to subType, "param2" to t1), t3)
        val exception = assertThrows<RuntimeException> {
            TypeDefinition("A")
                .property("fnc1", fnc1)
                .property("fnc1", t2)
                .construct()
        }
        assertEquals("property fnc1 is already defined", exception.message)
    }
}