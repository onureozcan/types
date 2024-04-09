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
        if (!(newType is FunctionType)) return false
        existingTypes.filterIsInstance<FunctionType>().forEach { type ->
            if (type.returnType != newType.returnType) {
                return false
            }
            if (type.parameters.size == newType.parameters.size) {
                val allParamsAreOfTheSameType = type.parameters.zip(newType.parameters).all { (t1, t2) ->
                    t1.second == t2.second
                }
                if (allParamsAreOfTheSameType) {
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
        result = 31 * result + isInterface.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        result = 31 * result + interfaces.hashCode()
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
        return ConstructedType(
            typeDefinition = definition,
            parameterBindings = parameterBindings,
            isNullable = isNullable
        ).also {
            definition.validateInterfaces()
        }
    }
}