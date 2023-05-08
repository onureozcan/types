import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TypeHierarchyTest {

    private val typeAny = TypeDefinition("Any").init()
    private val typeString = TypeDefinition("String").extends(typeAny)
    private val typeNumber = TypeDefinition("Number").extends(typeAny)
    private val typeInt = TypeDefinition("Int").extends(typeNumber.init())

    @Test
    fun `simple same types are assignable`() {
        val typeA = TypeDefinition("A").init()

        assertTrue(typeA.isAssignableFrom(typeA))
    }

    /**
    fun test() {
    open class A
    class B: A()
    val a: A = B()
    val b: B = A() // Error
    }
     */
    @Test
    fun `subtypes can be assigned to supertypes`() {
        val typeA = TypeDefinition("A").init()
        val typeB = TypeDefinition("B").extends(typeA).init()

        assertTrue(typeA.isAssignableFrom(typeB))
        assertFalse(typeB.isAssignableFrom(typeA))
    }

    /**
    fun test() {
    open class A
    class B<T>: A()
    val a: A = B<String>()
    val b: B<String> = A() // Error
    }
     */
    @Test
    fun `generic subtypes can be assigned to supertypes`() {
        val typeA = TypeDefinition("A").init()
        val typeB = TypeDefinition("B").extends(typeA).parameter("T") //, parameters = listOf(TypeVariableV2("T")), extends = setOf(typeA))

        val typeBofString = typeB.with().param("T", typeString.init()).init()

        assertTrue(typeA.isAssignableFrom(typeBofString))
        assertFalse(typeBofString.isAssignableFrom(typeA))
    }

    /**
    fun test() {
    open class A<T>
    class B: A<String>()
    val a: A<String> = A<String>()
    val a2: A<Any> = A<String>() // Error
    val a3: A<Int> = A<String>() // Error
    }
     */
    @Test
    fun `subtype of generic supertype can be assigned to generic supertype`() {
        val typeA = TypeDefinition("A").parameter("T")
        val typeB = TypeDefinition("B").extends(typeA.with().param("T", typeString.init()).init()).init()

        val typeAofString = typeA.with().param("T", typeString.init()).init()

        assertTrue(typeAofString.isAssignableFrom(typeB))
        assertFalse(typeB.isAssignableFrom(typeAofString))
    }

    /**
    fun test() {
    class A<T>
    val a: A<String> = A<String>()
    val a2: A<Any> = A<String>() // Error
    val a3: A<Int> = A<String>() // Error
    }
     */
    @Test
    fun `construction of a generic type with substitutable types are assignable`() {
        val typeA = TypeDefinition("A").parameter("T")

        val typeAofString = typeA.with().param("T", typeString.init()).init()
        val typeAofAny = typeA.with().param("T", typeAny).init()
        val typeAofInt = typeA.with().param("T", typeInt.init()).init()

        assertTrue(typeAofString.isAssignableFrom(typeAofString))
        assertFalse(typeAofAny.isAssignableFrom(typeAofString))
        assertFalse(typeAofInt.isAssignableFrom(typeAofString))
    }

    /**
    fun test() {
    open class A<T>
    class B<T>: A<T>()
    val a: A<String> = B<String>()
    val a2: A<Any> = B<String>() // Error
    val b: B<String> = A<String>() // Error
    val a3: A<Int> = B<String>() // Error
    }
     */
    @Test
    fun `generic subtype of generic supertype can be assigned to generic supertype `() {
        val typeA = TypeDefinition("A").parameter("T")
        val typeB = TypeDefinition("B").parameter("T").extends(typeA.with().param("T", TypeVariable("T")).init())

        val typeAofString = typeA.with().param("T", typeString.init()).init()
        val typeBofString = typeB.with().param("T", typeString.init()).init()
        val typeAofInt = typeA.with().param("T", typeInt.init()).init()
        val typeAofAny = typeA.with().param("T", typeAny).init()

        assertTrue(typeAofString.isAssignableFrom(typeBofString))
        assertFalse(typeAofAny.isAssignableFrom(typeBofString))
        assertFalse(typeBofString.isAssignableFrom(typeAofString))
        assertFalse(typeAofInt.isAssignableFrom(typeBofString))
    }
}