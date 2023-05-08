class ParameterBindings(
    private val bindings: MutableMap<String, TypeExpression> = mutableMapOf()
) {

    fun add(param: String, type: TypeExpression) {
        if (bindings[param] != null) {
            throw Exception("param $param is already bound!")
        } else {
            bindings[param] = type
        }
    }

    fun isAssignable(
        with: ParameterBindings
    ) = bindings.all { (param, variable) -> with.bindings[param]?.isAssignableFrom(variable) ?: false }

    fun reMap(with: ParameterBindings) = if (with.bindings.isNotEmpty())
        ParameterBindings(bindings.mapValues { (parameterName, _) -> getType(parameterName, with.bindings) }
            .toMutableMap())
    else this

    fun getType(parameterName: String): TypeExpression {
        return getType(parameterName, bindings)
    }

    private fun getType(parameterName: String, bindings: Map<String, TypeExpression>) =
        bindings[parameterName] ?: throw RuntimeException("unbound parameter: $parameterName")

    override fun toString(): String {
        return if (bindings.isNotEmpty())
            "<" + bindings.map { (param, type) -> "$param=$type" }.joinToString(",") + ">"
        else ""
    }
}