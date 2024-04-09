data class PropertyLookupResult(val depth: Int, val position: Int, val type: TypeExpression)

interface TypeExpression {
    fun find(name: String): PropertyLookupResult?
    fun isAssignableFrom(other: TypeExpression): Boolean
}

class TypeDefinition(val name: String, val isInterface: Boolean = false) {

    var parent: ConstructedType? = null
        private set
    var interfaces: MutableList<ConstructedType> = mutableListOf()
        private set
    val parameters: MutableList<TypeVariable> = mutableListOf()
    val properties: MutableList<Pair<String, TypeExpression>> = mutableListOf()
    var packageName: String = "default"
        private set

    fun extends(parent: ConstructedType) = this.apply {
        if (this.isInterface != parent.typeDefinition.isInterface) {
            throw RuntimeException("cannot extend from $parent")
        }
        this.parent = parent
    }

    fun implements(_interface: ConstructedType) = this.apply {
        if (!_interface.typeDefinition.isInterface) {
            throw RuntimeException("only interfaces can be implemented")
        }
        if (interfaces.any { i -> i.typeDefinition == _interface.typeDefinition }) {
            throw RuntimeException("already implemented")
        }
        this.interfaces.add(_interface)
    }

    fun parameter(name: String, upperBound: TypeExpression? = null) =
        this.apply { this.parameters.add(TypeVariable(name, upperBound)) }

    fun find(name: String) = properties.find { it.first == name }?.second

    fun property(name: String, type: TypeExpression) = this.apply {
        val existingProperties = this.properties.filter { (_name, _) -> name == _name }.map { it.second }
        if (existingProperties.isNotEmpty() && !checkOverloadPossible(existingProperties, type)) {
            throw RuntimeException("property $name is already defined")
        }
        this.properties.add(name to type)
    }

    private fun checkOverloadPossible(existingTypes: List<TypeExpression>, newType: TypeExpression): Boolean {
        if (!(newType is FunctionType))  return false
        existingTypes.filterIsInstance<FunctionType>().forEach { type ->
            if (type.returnType != newType.returnType) {
                return false
            }
            if (type.parameters.size == newType.parameters.size) {
                val allParamsAreAmbiguous = type.parameters.zip(newType.parameters).all { (t1, t2) ->
                    t1.second.isAssignableFrom(t2.second) || t2.second.isAssignableFrom(t1.second)
                }
                if (allParamsAreAmbiguous) {
                    return false
                }
            }
        }
        return true
    }

    fun with() = TypeInitiator(this)
    fun construct() = with().construct()

    fun getQualifyingName() = "$packageName.$name"

    fun getAllPropertyNames(): List<String> {
        val thisLevelProperties = properties.map { it -> it.first }
        if (parent != null) {
            return thisLevelProperties + (parent?.typeDefinition?.getAllPropertyNames() ?: listOf())
        }
        return thisLevelProperties
    }

    fun validateInterfaces() {
        val requiredPropertiesMap = mutableMapOf<String, TypeExpression>()
        for (_interface in interfaces) {
            val propertiesOfInterface = _interface.getAllProperties()
            for ((name, type) in propertiesOfInterface) {
                if (requiredPropertiesMap.containsKey(name)) {
                    val prevType = requiredPropertiesMap[name]
                    if (prevType != type) {
                        throw RuntimeException("type clash: types $type and $prevType are not compatible for property $name")
                    }
                }
                requiredPropertiesMap[name] = type
            }
        }
        // check all properties are present in this
        for ((name, type) in requiredPropertiesMap) {
            val foundProperty = find(name)
            if (foundProperty == null || foundProperty != type) {
                throw RuntimeException("interface error: property wasn't implemented $name")
            }
        }
    }

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
    private var isNullable = false

    fun param(param: String, to: TypeExpression) = this.apply { parameterBindings.add(param, to) }
    fun nullable() = this.apply { isNullable = true }

    fun construct(): ConstructedType {
        definition.parameters.all { typeParam ->
            val binding = parameterBindings.bindings[typeParam.name] ?: throw RuntimeException("unbound: $typeParam")
            return@all typeParam.upperBound?.isAssignableFrom(binding) ?: true
        }
        return ConstructedType(typeDefinition = definition, parameterBindings = parameterBindings, isNullable = isNullable).also {
            definition.validateInterfaces()
        }
    }
}

data class ConstructedType(
    val typeDefinition: TypeDefinition,
    val parameterBindings: ParameterBindings,
    val isNullable: Boolean
) : TypeExpression {

    fun getAllProperties(): List<Pair<String, TypeExpression>> {
        return typeDefinition.getAllPropertyNames().mapNotNull { name -> find(name)?.let { name to it.type } }
    }

    override fun find(name: String): PropertyLookupResult? {
        return PropertyAccess.find(name, on = this, withBindings = parameterBindings, 0)
    }

    override fun isAssignableFrom(other: TypeExpression): Boolean {
        if (other is ConstructedType) {
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
        return other is ConstructedType && other.toString() == this.toString()
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
        return other is TypeVariable && other.name == name
    }

    override fun toString(): String {
        return "$name extends ${upperBound ?: "Any"}"
    }
}

class FunctionType(
    val parameters: List<Pair<String, TypeExpression>>,
    val returnType: TypeExpression
) : TypeExpression {
    override fun find(name: String): PropertyLookupResult? {
        return null
    }

    override fun isAssignableFrom(other: TypeExpression): Boolean {
        if (other is FunctionType) {
            if (other.parameters.size != parameters.size) return false
            if (returnType != other.returnType) return false
            return parameters.zip(other.parameters).all { (p1, p2) ->
                p1.second == p2.second
            }
        }
        return false
    }
}