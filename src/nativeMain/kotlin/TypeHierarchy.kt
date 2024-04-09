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
        if (other.isNullable &&!_this.isNullable) {
            return false
        }
        if (_this.typeDefinition == other.typeDefinition) {
            return _this.parameterBindings.isAssignableFrom(withBindings)
        }
        val allParents = mutableListOf<ConstructedType>()
        other.typeDefinition.parent?.let { allParents.add(it) }

        if (_this.typeDefinition.isInterface) {
            allParents.addAll(other.typeDefinition.interfaces)
        }
        if (allParents.isEmpty()) {
            return false
        }
        return allParents.any { parent ->
            isAssignableFromInternal(_this, parent, parent.parameterBindings.reMap(withBindings))
        }
    }
}