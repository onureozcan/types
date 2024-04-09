data class PropertyLookupResult(val depth: Int, val position: Int, val type: TypeExpression)

interface TypeExpression {
    fun find(name: String): PropertyLookupResult?
    fun isAssignableFrom(other: TypeExpression): Boolean
}

class TypeVariable(
    val name: String,
    val upperBound: TypeExpression? = null,
    val isNullable: Boolean = false
) : TypeExpression {

    override fun find(name: String): PropertyLookupResult? {
        return upperBound?.find(name)
    }

    override fun isAssignableFrom(other: TypeExpression): Boolean {
        if (other is TypeVariable) {
            if (!this.isNullable && other.isNullable) {
                return false
            }
            return other.name == this.name
        }
        return false
    }

    override fun toString(): String {
        return "($name extends ${upperBound ?: "Any"})${if (isNullable) "?" else ""}"
    }
}

class FunctionType(
    val parameters: List<Pair<String, TypeExpression>>,
    val returnType: TypeExpression
) : TypeExpression {
    override fun find(name: String): PropertyLookupResult? {
        return null
    }

    override fun isAssignableFrom(other: TypeExpression): Boolean {
        if (other is FunctionType) {
            if (other.parameters.size != parameters.size) return false
            if (returnType != other.returnType) return false
            return parameters.zip(other.parameters).all { (p1, p2) ->
                p1.second == p2.second
            }
        }
        return false
    }
}