object TypeHierarchy {
    fun isAssignableFrom(
        self: TypeInstance,
        other: TypeInstance
    ) = isAssignableFromInternal(self, other, other.parameterBindings)

    private fun isAssignableFromInternal(
        _this: TypeInstance,
        other: TypeInstance,
        withBindings: Map<String, TypeExpression>,
    ): Boolean {
        if (_this.typeDefinition == other.typeDefinition) {
            return isParameterBindingsAssignable(_this.parameterBindings, withBindings)
        }
        return other.typeDefinition.parent?.let { parent ->
            isAssignableFromInternal(_this, parent, reMap(parent.parameterBindings, with = withBindings))
        } ?: false
    }

    private fun isParameterBindingsAssignable(
        bindings: Map<String, TypeExpression>, other: Map<String, TypeExpression>
    ): Boolean {
        return bindings.all { (param, variable) -> other[param]?.isAssignableFrom(variable) ?: false }
    }

    private fun reMap(bindings: Map<String, TypeExpression>, with: Map<String, TypeExpression>) =
        bindings.mapValues { (parameterName, _) -> getParameterType(parameterName, with) }

    private fun getParameterType(parameterName: String, bindings: Map<String, TypeExpression>) =
        bindings[parameterName] ?: throw RuntimeException("unbound parameter: $parameterName")
}