data class PropertyLookupResult(val depth: Int, val position: Int, val type: TypeExpression)

interface TypeExpression {
    fun find(name: String): PropertyLookupResult?
    fun isAssignableFrom(other: TypeExpression): Boolean
}

class TypeDefinition(val name: String) {

    var parent: TypeInstance? = null
        private set
    val parameters: MutableList<TypeVariable> = mutableListOf()
    val properties: MutableList<Pair<String, TypeExpression>> = mutableListOf()
    var packageName: String = "default"
        private set

    fun extends(parent: TypeInstance) = this.apply { this.parent = parent }
    fun parameter(name: String, upperBound: TypeExpression? = null) =
        this.apply { this.parameters.add(TypeVariable(name, upperBound)) }

    fun property(name: String, type: TypeExpression) = this.apply { this.properties.add(name to type) }

    fun with() = TypeInitiator(this)
    fun init() = with().init()  // TODO: validation

    fun getQualifyingName() = "$packageName.$name"

    override fun equals(other: Any?): Boolean {
        return other is TypeDefinition && other.getQualifyingName() == this.getQualifyingName()
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        result = 31 * result + parameters.hashCode()
        result = 31 * result + properties.hashCode()
        result = 31 * result + packageName.hashCode()
        return result
    }
}

class TypeInitiator(private val definition: TypeDefinition) {

    private val parameterBindings = ParameterBindings()

    fun param(param: String, to: TypeExpression) = this.apply { parameterBindings.add(param,to) }

    fun init() = TypeInstance(typeDefinition = definition, parameterBindings = parameterBindings)
}

data class TypeInstance(
    val typeDefinition: TypeDefinition,
    val parameterBindings: ParameterBindings
) : TypeExpression {

    override fun find(name: String): PropertyLookupResult? {
        return PropertyAccess.find(name, on = this, withBindings = parameterBindings, 0)
    }

    override fun isAssignableFrom(other: TypeExpression): Boolean {
        if (other is TypeInstance) {
            return TypeHierarchy.isAssignableFrom(self = this, other = other)
        } else if (other is TypeVariable) {
            return other.upperBound?.let { isAssignableFrom(it) } ?: false
        }
        return false
    }

    override fun toString(): String {
        return typeDefinition.name + parameterBindings.toString()
    }

    override fun equals(other: Any?): Boolean {
        return other is TypeInstance && other.toString() == this.toString()
    }

    override fun hashCode(): Int {
        var result = typeDefinition.hashCode()
        result = 31 * result + parameterBindings.hashCode()
        return result
    }
}

class TypeVariable(
    val name: String,
    val upperBound: TypeExpression? = null
) : TypeExpression {

    override fun find(name: String): PropertyLookupResult? {
        return upperBound?.find(name)
    }

    override fun isAssignableFrom(other: TypeExpression): Boolean {
        return upperBound?.isAssignableFrom(other) ?: true
    }

    override fun toString(): String {
        return "$name extends ${upperBound ?: "Any"}"
    }
}