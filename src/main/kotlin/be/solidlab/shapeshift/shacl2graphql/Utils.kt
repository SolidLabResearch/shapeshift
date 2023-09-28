package be.solidlab.shapeshift.shacl2graphql

import graphql.schema.GraphQLType
import graphql.schema.GraphQLTypeUtil
import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.graph.Node_URI
import org.apache.jena.shacl.Shapes
import org.apache.jena.shacl.engine.TargetType
import org.apache.jena.shacl.parser.NodeShape
import java.net.URI
import java.util.*
import kotlin.jvm.optionals.getOrNull

internal fun decapitalize(str: String): String {
    return str.replaceFirstChar { it.lowercase(Locale.getDefault()) }
}

internal fun parseName(subject: Node, context: ParseContext): String {
    val uri = URI(subject.uri)
    // If the URI has a fragment, use fragment, otherwise use the last path segment
    val name = uri.fragment ?: uri.path.substringAfterLast("/")
    return name.capitalizeFirstLetter()
}

internal fun String.capitalizeFirstLetter(): String {
    return this.replaceFirstChar { c -> c.uppercase(Locale.getDefault()) }
}

@OptIn(ExperimentalStdlibApi::class)
internal fun Graph.getString(subject: Node, predicate: Node): String? {
    return this.find(subject, predicate, null)
        .nextOptional().getOrNull()?.`object`?.takeIf { it.isLiteral }?.literalValue?.toString()
}

@OptIn(ExperimentalStdlibApi::class)
internal fun Graph.getReference(subject: Node, predicate: Node): Node_URI? {
    return this.find(subject, predicate, null)
        .nextOptional().getOrNull()?.`object`?.takeIf { it.isURI }?.let { it as Node_URI }
}

internal fun Shapes.getMatchingShape(shClass: Node_URI): NodeShape? {
    return this.filter { it.isNodeShape }.find {
        it.targets.any { target ->
            target.targetType == TargetType.targetClass && target.`object` == shClass
        }
    }?.let { it as NodeShape }
}

internal fun propertyNameFromPath(path: String): String {
    return if (path.contains("#")) {
        path.substringAfterLast("#")
    } else {
        path.substringAfterLast("/")
    }
}

internal data class ParseContext(
    val allShapes: Shapes,
    val shapeImports: Map<Node, ShapeConfig>,
    val nodeShape: NodeShape? = null
) {

    fun getShapeConfig(subject: Node): ShapeConfig {
        val importNode = allShapes.graph.find(subject, importedFrom, Node.ANY).toList()
            .firstOrNull()?.`object`
        return shapeImports[importNode]!!
    }

}

internal enum class InputTypeConfiguration(val typePrefix: String) {
    CREATE_TYPE("Create"), UPDATE_TYPE("Update")
}

fun GraphQLType.rawType(): GraphQLType {
    return if (GraphQLTypeUtil.isNonNull(this)) {
        GraphQLTypeUtil.unwrapNonNull(this).rawType()
    } else if (GraphQLTypeUtil.isList(this)) {
        GraphQLTypeUtil.unwrapOne(this).rawType()
    } else {
        this
    }
}

fun GraphQLType.unwrapNonNull(): GraphQLType {
    return if (GraphQLTypeUtil.isNonNull(this)) {
        GraphQLTypeUtil.unwrapNonNull(this)
    } else {
        this
    }
}

fun GraphQLType.isScalar(): Boolean {
    return GraphQLTypeUtil.isScalar(this)
}

fun GraphQLType.isCollection(): Boolean {
    return GraphQLTypeUtil.isList(this)
}
