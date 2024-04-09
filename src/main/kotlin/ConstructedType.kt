
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
            return isAssignableFrom(other = other)
        } else if (other is TypeVariable) {
            return other.upperBound?.let { isAssignableFrom(it) } ?: false
        }
        return false
    }

    private fun isAssignableFrom(
        other: ConstructedType
    ) = isAssignableFromInternal(other, other.parameterBindings)

    private fun isAssignableFromInternal(
        other: ConstructedType,
        withBindings: ParameterBindings,
    ): Boolean {
        if (other.isNullable && !this.isNullable) {
            return false
        }
        if (this.typeDefinition == other.typeDefinition) {
            return this.parameterBindings.isAssignableFrom(withBindings)
        }
        val allParents = mutableListOf<ConstructedType>()
        other.typeDefinition.parent?.let { allParents.add(it) }

        if (this.typeDefinition.isInterface) {
            allParents.addAll(other.typeDefinition.interfaces)
        }
        if (allParents.isEmpty()) {
            return false
        }
        return allParents.any { parent ->
            isAssignableFromInternal(parent, parent.parameterBindings.reMap(withBindings))
        }
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