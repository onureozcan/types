object TypeHierarchy {
    fun isAssignableFrom(
        self: TypeInstance,
        other: TypeInstance
    ) = isAssignableFromInternal(self, other, other.parameterBindings)

    private fun isAssignableFromInternal(
        _this: TypeInstance,
        other: TypeInstance,
        withBindings: ParameterBindings,
    ): Boolean {
        if (_this.typeDefinition == other.typeDefinition) {
            return _this.parameterBindings.isAssignable(withBindings)
        }
        return other.typeDefinition.parent?.let { parent ->
            isAssignableFromInternal(_this, parent, parent.parameterBindings.reMap(withBindings))
        } ?: false
    }
}