package be.ugent.solidlab.shapeshift.shacl2graphql

/**
 * Configuration class for a transformation
 *
 * @constructor Creates a Context for the given parameters
 * @param catalogURL The URL for the Shape Catalog to be used. If an URL is provided, the Shape Catalog will be checked first.
 * @param strict Only allows SHACL Core constructs
 * @param shapeConfigs a map of [ShapeConfig] objects. The key to this map shoud be a URI of the SHACL file to be used, with the value containing the corresponding config object.
 */
data class Context(
    val catalogURL : String? = null,
    val strict: Boolean = false,
    val shapeConfigs : Map<String, ShapeConfig>
)
/**
 *
 * Configuration class for a Shape
 *
 * @constructor Creates a ShapeConfig for the given parameters
 * @param mutation Indicates wether mutations should be generated for the shapes defined in the corresponding URI
 * @param filterParams which properties should be used as filter parameters in the resulting GraphQL queries. (Not implemented yet)
 *
 */
data class ShapeConfig(
    val mutation : Boolean = false,
    val filterParams : List<String>? = null
)


