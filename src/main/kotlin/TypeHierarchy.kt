object TypeHierarchy {
    fun isAssignableFrom(
        self: ConstructedType,
        other: ConstructedType
    ) = isAssignableFromInternal(self, other, other.parameterBindings)

    private fun isAssignableFromInternal(
        _this: ConstructedType,
        other: ConstructedType,
        withBindings: ParameterBindings,
    ): Boolean {
        if (_this.typeDefinition == other.typeDefinition) {
            return _this.parameterBindings.isAssignableFrom(withBindings)
        }
        return other.typeDefinition.parent?.let { parent ->
            isAssignableFromInternal(_this, parent, parent.parameterBindings.reMap(withBindings))
        } ?: false
    }
}