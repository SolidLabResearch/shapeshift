package be.solidlab.shapeshift.shacl2graphql

data class ShiftConfig(
    val catalogURL : String?,
    val strict: Boolean,
    val shapeConfigs : Map<String, ShapeConfig>
) {
}

data class ShapeConfig(
    val mutation : Boolean,
    val filterParams : List<String>
) {
    fun getTargetFileName(uri : String): String {
        return uri.substringAfterLast("/")
    }
}


