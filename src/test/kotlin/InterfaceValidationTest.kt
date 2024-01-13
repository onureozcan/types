import kotlin.test.Test
import kotlin.test.assertTrue

class InterfaceValidationTest {

    private val typeStr = TypeDefinition("String").init()
    private val typeInt = TypeDefinition("Integer").init()
    private val typeList = TypeDefinition("List").parameter("T")

    @Test
    fun `should detect interface type clash`() {
        val interface1 = TypeDefinition("I1", true).property("a", typeStr).init()
        val interface2 = TypeDefinition("I2", true).property("a", typeInt).init()

        var exceptionThrown = false
        try {
            TypeDefinition("Type").property("a", typeStr).implements(interface1).implements(interface2).init()
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
        val interface1 = TypeDefinition("I1", true).property("a", typeList.with().param("T", typeInt).init()).init()
        val interface2 = TypeDefinition("I2", true).property("a", typeList.with().param("T", typeStr).init()).init()

        var exceptionThrown = false
        try {
            TypeDefinition("Type").property("a", typeStr).implements(interface1).implements(interface2).init()
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
        val listOfInt = typeList.with().param("T", typeInt).init()
        val interface1 = TypeDefinition("I1", true).property("a", listOfInt).init()
        val interface2 = TypeDefinition("I2", true).property("a", listOfInt).init()

        TypeDefinition("Type").property("a", listOfInt).implements(interface1).implements(interface2).init()
    }

    @Test
    fun `should detect interface not implemented`() {
        val listOfInt = typeList.with().param("T", typeInt).init()
        val interface1 = TypeDefinition("I1", true).property("a", listOfInt).init()
        val interface2 = TypeDefinition("I2", true).property("a", listOfInt).init()

        var exceptionThrown = false
        try {
            TypeDefinition("Type").property("a", typeStr).implements(interface1).implements(interface2).init()
        } catch (e: RuntimeException) {
            if (e.message?.contains("interface error: property wasn't implemented a") != true) {
                throw RuntimeException("expected interface error")
            }
            exceptionThrown = true
        }

        assertTrue(exceptionThrown)
    }
}
