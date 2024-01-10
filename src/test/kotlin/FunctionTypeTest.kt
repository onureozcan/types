import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FunctionTypeTest {

    private val superType = TypeDefinition("SuperT").init()
    private val subType = TypeDefinition("SubT").extends(superType).init()
    private val t1 = TypeDefinition("T1").init()
    private val t2 = TypeDefinition("T2").init()
    private val t3 = TypeDefinition("nothing").init()

    @Test
    fun `functions can NOT be assigned to other types`() {
        val fnc1 = FunctionType("fnc1", listOf(), t3)
        assertFalse(t1.isAssignableFrom(fnc1))
    }

    @Test
    fun `other types can NOT be assigned to functions`() {
        val fnc1 = FunctionType("fnc1", listOf(), t3)
        assertFalse(fnc1.isAssignableFrom(t1))
    }

    @Test
    fun `function with un-assignable return types are NOT compatible`() {
        val fnc1 = FunctionType("fnc1", listOf(), t1)
        val fnc2 = FunctionType("fnc1", listOf(), t2)
        assertFalse(fnc1.isAssignableFrom(fnc2))
    }

    @Test
    fun `function with different number of parameters are NOT compatible`() {
        val fnc1 = FunctionType("fnc1", listOf(), t3)
        val fnc2 = FunctionType("fnc1", listOf("param1" to t1, "param2" to t2), t3)
        assertFalse(fnc1.isAssignableFrom(fnc2))
        assertFalse(fnc2.isAssignableFrom(fnc1))
    }


    @Test
    fun `function with un-assignable parameters are NOT compatible`() {
        val fnc1 = FunctionType("fnc1", listOf("param1" to t2, "param2" to t1), t3)
        val fnc2 = FunctionType("fnc1", listOf("param1" to t1, "param2" to t2), t3)
        assertFalse(fnc1.isAssignableFrom(fnc2))
        assertFalse(fnc2.isAssignableFrom(fnc1))
    }

    @Test
    fun `function with assignable parameters and return types are compatible`() {
        val fnc1 = FunctionType("fnc1", listOf("param1" to superType, "param2" to subType), t3)
        val fnc2 = FunctionType("fnc1", listOf("param1" to subType, "param2" to subType), t3)
        assertTrue(fnc1.isAssignableFrom(fnc2))
    }
}