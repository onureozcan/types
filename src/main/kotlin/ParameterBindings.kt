class ParameterBindings(
    val bindings: MutableMap<String, TypeExpression> = mutableMapOf()
) {

    fun add(param: String, type: TypeExpression) {
        if (bindings[param] != null) {
            throw Exception("param $param is already bound!")
        } else {
            bindings[param] = type
        }
    }

    fun isAssignableFrom(
        other: ParameterBindings
    ):Boolean {
        if (bindings.size != other.bindings.size) return false
        return bindings.all { (param, binding) ->
            return@all other.bindings[param]?.let { binding == it } ?: false
        }
    }

    fun reMap(with: ParameterBindings): ParameterBindings {
            val newBindings = mutableMapOf<String, TypeExpression>()
            bindings.forEach { (name, type) ->
                if (type is TypeVariable) {
                    newBindings[name] = getType(type.name, with.bindings)
                }
                else newBindings[name] = type
            }
            return ParameterBindings(newBindings)
    }

    fun getType(parameterName: String): TypeExpression {
        return getType(parameterName, bindings)
    }

    private fun getType(parameterName: String, bindings: Map<String, TypeExpression>) =
        bindings[parameterName] ?: throw RuntimeException("unbound parameter: $parameterName, bindings: $bindings")

    override fun toString(): String {
        return if (bindings.isNotEmpty())
            "<" + bindings.map { (param, type) -> "$param=$type" }.joinToString(",") + ">"
        else ""
    }
}