object PropertyAccess {
    fun find(
        name: String,
        on: ConstructedType,
        withBindings: ParameterBindings,
        atDepth: Int = 0
    ): PropertyLookupResult? {
        val property = find(on.typeDefinition, name)
        if (property != null) {
            return property.copy(type = resolve(property.type, withBindings), depth = atDepth)
        }
        val parent = on.typeDefinition.parent ?: return null
        return find(name, parent, parent.parameterBindings.reMap(with = withBindings), atDepth = atDepth + 1)
    }

    private fun resolve(type: TypeExpression, bindings: ParameterBindings): TypeExpression {
        if (type is TypeVariable) {
            return bindings.getType(type.name)
        } else if (type is ConstructedType) {
            return type.copy(parameterBindings = type.parameterBindings.reMap(with = bindings))
        }
        return type
    }

    private fun find(typeDefinition: TypeDefinition, name: String): PropertyLookupResult? {
        return typeDefinition.properties.find { (propertyName, _) -> propertyName == name }?.let { property ->
            PropertyLookupResult(
                position = typeDefinition.properties.indexOf(property),
                depth = 0,
                type = property.second
            )
        }
    }
}