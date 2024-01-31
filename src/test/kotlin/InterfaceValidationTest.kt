import kotlin.test.Test
import kotlin.test.assertTrue

class InterfaceValidationTest {

    private val typeStr = TypeDefinition("String").construct()
    private val typeInt = TypeDefinition("Integer").construct()
    private val typeList = TypeDefinition("List").parameter("T")

    @Test
    fun `should detect interface type clash`() {
        val interface1 = TypeDefinition("I1", true).property("a", typeStr).construct()
        val interface2 = TypeDefinition("I2", true).property("a", typeInt).construct()

        var exceptionThrown = false
        try {
            TypeDefinition("Type").property("a", typeStr).implements(interface1).implements(interface2).construct()
        } catch (e: RuntimeException) {
            if (e.message?.contains("type clash") != true) {
                throw RuntimeException("expected type clash")
            }
            exceptionThrown = true
        }

        assertTrue(exceptionThrown)
    }

    @Test
    fun `should detect interface type clash with generic types`() {
        val interface1 = TypeDefinition("I1", true).property("a", typeList.with().param("T", typeInt).construct()).construct()
        val interface2 = TypeDefinition("I2", true).property("a", typeList.with().param("T", typeStr).construct()).construct()

        var exceptionThrown = false
        try {
            TypeDefinition("Type").property("a", typeStr).implements(interface1).implements(interface2).construct()
        } catch (e: RuntimeException) {
            if (e.message?.contains("type clash") != true) {
                throw RuntimeException("expected type clash")
            }
            exceptionThrown = true
        }

        assertTrue(exceptionThrown)
    }

    @Test
    fun `should NOT detect interface type clash with generic types with the same bounding`() {
        val listOfInt = typeList.with().param("T", typeInt).construct()
        val interface1 = TypeDefinition("I1", true).property("a", listOfInt).construct()
        val interface2 = TypeDefinition("I2", true).property("a", listOfInt).construct()

        TypeDefinition("Type").property("a", listOfInt).implements(interface1).implements(interface2).construct()
    }

    @Test
    fun `should detect interface not implemented`() {
        val listOfInt = typeList.with().param("T", typeInt).construct()
        val interface1 = TypeDefinition("I1", true).property("a", listOfInt).construct()
        val interface2 = TypeDefinition("I2", true).property("a", listOfInt).construct()

        var exceptionThrown = false
        try {
            TypeDefinition("Type").property("a", typeStr).implements(interface1).implements(interface2).construct()
        } catch (e: RuntimeException) {
            if (e.message?.contains("interface error: property wasn't implemented a") != true) {
                throw RuntimeException("expected interface error")
            }
            exceptionThrown = true
        }

        assertTrue(exceptionThrown)
    }

    /**
     *  interface I {
     *      i:A
     *  }
     *
     *  class A
     *  class B extends A
     *
     *  class C implements I {
     *      i: B // error
     *  }
     */
    @Test
    fun `interface implementation is invariant`() {
        val typeA = TypeDefinition("A").construct()
        val typeB = TypeDefinition("B").extends(typeA).construct()
        val interfaceI = TypeDefinition("I", true).property("i", typeA).construct()

        var exceptionThrown = false
        try {
            TypeDefinition("C").property("i", typeB).implements(interfaceI).construct()
        } catch (e: RuntimeException) {
            if (e.message?.contains("interface error: property wasn't implemented i") != true) {
                throw RuntimeException("expected interface error")
            }
            exceptionThrown = true
        }

        assertTrue(exceptionThrown)
    }

    /**
     *  interface I<T> {
     *      i:T
     *  }
     *
     *  class A
     *  class B extends A
     *
     *  class C implements I<A> {
     *      i: B // error
     *  }
     */
    @Test
    fun `interface implementation is invariant on generic types`() {
        val typeA = TypeDefinition("A").construct()
        val typeB = TypeDefinition("B").extends(typeA).construct()
        val interfaceI = TypeDefinition("I", true).parameter("T").property("i", typeA)

        var exceptionThrown = false
        try {
            TypeDefinition("C").property("i", typeB).implements(interfaceI.with().param("T", typeB).construct()).construct()
        } catch (e: RuntimeException) {
            if (e.message?.contains("interface error: property wasn't implemented i") != true) {
                throw RuntimeException("expected interface error")
            }
            exceptionThrown = true
        }

        assertTrue(exceptionThrown)
    }
}
