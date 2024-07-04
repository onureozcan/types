class PredefinedTypes {

    companion object {
        val typeVoid = TypeDefinition("Void").construct()
        val typeAny = TypeDefinition("Any").construct()
        val typeString = TypeDefinition("String").extends(typeAny).construct()
        val typeNumber = TypeDefinition("Number").extends(typeAny).construct()
        val typeInt = TypeDefinition("Int").extends(typeNumber).construct()
        val typeBool = TypeDefinition("Bool").extends(typeNumber).construct()
        val typeDouble = TypeDefinition("Double").extends(typeNumber).construct()
    }
}