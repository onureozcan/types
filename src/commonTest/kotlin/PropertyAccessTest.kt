import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropertyAccessTest {

    private val typeStr = TypeDefinition("String").construct()
    private val typeInt = TypeDefinition("Integer").construct()
    private val typeList = TypeDefinition("List").parameter("T")

    /**
     *  class A {
     *      x: String
     *      y: Int
     *  }
     *  A.x = String
     *  A.y = Int
     */
    @Test
    fun `simple non-parametric property access`() {
        val a = TypeDefinition("A").property("x", typeStr).property("y", typeInt).construct()
        val x = a.find("x")
        val y = a.find("y")

        assertEquals(x?.depth, 0)
        assertEquals(x?.position, 0)
        assertEquals(x?.type, typeStr)

        assertEquals(y?.depth, 0)
        assertEquals(y?.position, 1)
        assertEquals(y?.type, typeInt)
    }

    /**
     *  class A {
     *      x: String
     *      y: Int
     *  }
     *  class B: A
     *
     *  B.x = String
     *  B.y = Int
     */
    @Test
    fun `simple non-parametric property access in chain`() {
        val a = TypeDefinition("A").property("x", typeStr).property("y", typeInt)
        val b = TypeDefinition("B").extends(a.construct()).construct()

        val x = b.find("x")
        val y = b.find("y")

        assertEquals(x?.depth, 1)
        assertEquals(x?.position, 0)
        assertEquals(x?.type, typeStr)

        assertEquals(y?.depth, 1)
        assertEquals(y?.position, 1)
        assertEquals(y?.type, typeInt)
    }

    /**
     *  class B {
     *      y: Int
     *  }
     *
     *  class C extends B
     *
     *  class A<T:B> {
     *     x: T
     *  }
     *  A<C>.x = C
     *  A<C>.x.y = Int
     */
    @Test
    fun `property access on type parameter relies on upper bound`() {
        val b = TypeDefinition("B").property("y", typeInt)
        val c = TypeDefinition("C").extends(b.construct())

        val a = TypeDefinition("A").parameter("T", b.construct()).property("x", TypeVariable("T"))

        val aOfC = a.with().param("T", c.construct()).construct()

        val x = aOfC.find("x")

        assertEquals(x?.depth, 0)
        assertEquals(x?.position, 0)
        assertEquals(x?.type, c.construct())

        val y = aOfC.find("x")?.type?.find("y")

        assertEquals(y?.depth, 1)
        assertEquals(y?.position, 0)
        assertEquals(y?.type, typeInt)
    }

    /**
     * class B<T> {
     *      x: T
     * }
     * class C<T>: B<T>
     * class D<T>: C<T>
     *
     * D<String>.x is String
     */
    @Test
    fun `parametric property access in chain`() {

        val b = TypeDefinition("B").parameter("T").property("x", TypeVariable("T"))
        val c = TypeDefinition("C").extends(b.with().param("T", TypeVariable("T")).construct())
        val d = TypeDefinition("D").extends(c.with().param("T", TypeVariable("T")).construct())

        val property = d.with().param("T", typeStr).construct().find("x")

        assertEquals(property?.depth, 2)
        assertEquals(property?.position, 0)
        assertEquals(property?.type, typeStr)
    }

    /**
     * class B<T> {
     *      x: List<T>
     * }
     * class C<T>: B<T>
     * class D<T>: C<T>
     *
     * D<String>.x is List<String>
     */
    @Test
    fun `parametric property access in chain 2`() {
        val listOfT = typeList.with().param("T", TypeVariable("T")).construct()

        val b = TypeDefinition("B").parameter("T").property("x", listOfT)
        val c = TypeDefinition("C").extends(b.with().param("T", TypeVariable("T")).construct())
        val d = TypeDefinition("D").extends(c.with().param("T", TypeVariable("T")).construct())

        val property = d.with().param("T", typeStr).construct().find("x")

        assertEquals(property?.depth, 2)
        assertEquals(property?.position, 0)

        val propertyType = property?.type
        assertTrue(property?.type is ConstructedType)

        if (propertyType is ConstructedType) {
            assertEquals(propertyType.typeDefinition, typeList)
            assertEquals(propertyType.parameterBindings.getType("T"), typeStr)
        }
    }

    /**
     * class B<T,K> {
     *      x: List<K>
     * }
     * class C<T>: B<T, Int>
     *
     * C<String>.x is List<Int>
     */
    @Test
    fun `parametric property access in chain 3`() {
        val listOfK = typeList.with().param("T", TypeVariable("K")).construct()

        val b = TypeDefinition("B").parameter("T").parameter("K").property("x", listOfK)
        val c = TypeDefinition("C").extends(b.with().param("T", TypeVariable("T")).param("K", typeInt).construct())

        val property = c.with().param("T", typeStr).construct().find("x")

        assertEquals(property?.depth, 1)
        assertEquals(property?.position, 0)

        val propertyType = property?.type
        assertTrue(property?.type is ConstructedType)

        if (propertyType is ConstructedType) {
            assertEquals(propertyType.typeDefinition, typeList)
            assertEquals(propertyType.parameterBindings.getType("T"), typeInt)
        }
    }

    /**
     * class B<T,K> {
     *      x: List<K>
     * }
     * class C<P, G>: B<P, G>
     *
     * C<String,Int>.x is List<Int>
     */
    @Test
    fun `parametric property access in chain 4`() {
        val listOfK = typeList.with().param("T", TypeVariable("K")).construct()

        val b = TypeDefinition("B").parameter("T").parameter("K").property("x", listOfK)
        val c = TypeDefinition("C").extends(b.with().param("T", TypeVariable("P")).param("K", TypeVariable("G")).construct())

        val property = c.with().param("P", typeStr).param("G", typeInt).construct().find("x")

        assertEquals(property?.depth, 1)
        assertEquals(property?.position, 0)

        val propertyType = property?.type
        assertTrue(property?.type is ConstructedType)

        if (propertyType is ConstructedType) {
            assertEquals(propertyType.typeDefinition, typeList)
            assertEquals(propertyType.parameterBindings.getType("T"), typeInt)
        }
    }
}