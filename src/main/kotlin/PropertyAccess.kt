object PropertyAccess {
    fun find(
        name: String,
        on: TypeInstance,
        withBindings: Map<String, TypeExpression>,
        atDepth: Int = 0
    ): PropertyLookupResult? {
        val property = find(on.typeDefinition, name)
        if (property != null) {
            return property.copy(type = resolve(property.type, withBindings), depth = atDepth)
        }
        val parent = on.typeDefinition.parent ?: return null
        return find(name, parent, reMap(parent.parameterBindings, with = withBindings), atDepth = atDepth + 1)
    }

    private fun reMap(bindings: Map<String, TypeExpression>, with: Map<String, TypeExpression>) =
        bindings.mapValues { (parameterName, _) -> getParameterType(parameterName, with) }

    private fun resolve(type: TypeExpression, bindings: Map<String, TypeExpression>): TypeExpression {
        if (type is TypeVariable) {
            return getParameterType(type.name, bindings)
        } else if (type is TypeInstance) {
            return type.copy(parameterBindings = reMap(type.parameterBindings, with = bindings))
        }
        return type
    }

    private fun getParameterType(parameterName: String, bindings: Map<String, TypeExpression>) =
        bindings[parameterName] ?: throw RuntimeException("unbound parameter: ${parameterName}")

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