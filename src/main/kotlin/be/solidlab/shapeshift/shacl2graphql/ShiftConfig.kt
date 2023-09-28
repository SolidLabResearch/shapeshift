package be.solidlab.shapeshift.shacl2graphql

import java.io.File
import java.net.URL

data class ShiftConfig(
    val shaclDir : File,
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


