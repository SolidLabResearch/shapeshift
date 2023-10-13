package be.ugent.solidlab.shapeshift.shacl2graphql

import graphql.Scalars
import graphql.schema.*
import org.apache.jena.graph.Node_URI
import org.apache.jena.shacl.engine.TargetType
import org.apache.jena.shacl.engine.constraint.MaxCount
import org.apache.jena.shacl.engine.constraint.MinCount
import org.apache.jena.shacl.parser.NodeShape
import org.apache.jena.shacl.parser.PropertyShape
import org.apache.jena.shacl.vocabulary.SHACL
import org.apache.jena.vocabulary.XSD

internal fun generateEntryPoints(types: Set<GraphQLObjectType>): GraphQLObjectType {
    return GraphQLObjectType.newObject()
        .name("Query")
        .fields(types.flatMap { type ->
            listOf(
                GraphQLFieldDefinition.newFieldDefinition()
                    .name(decapitalize(type.name))
                    .argument(GraphQLArgument.newArgument().name("id").type(Scalars.GraphQLString).build())
                    .type(type)
                    .build(),
                GraphQLFieldDefinition.newFieldDefinition()
                    .name("${decapitalize(type.name)}Collection")
                    .type(GraphQLList.list(type))
                    .build()
            )
        })
        .build()
}

internal fun generateObjectType(shape: NodeShape, context: ParseContext): GraphQLObjectType {
    val shapeName = parseName(shape.shapeNode, context)
    val childContext = context.copy(nodeShape = shape)

    return GraphQLObjectType.newObject()
        .name(shapeName)
        .withAppliedDirective(
            isDirective.toAppliedDirective().transform {
                it.argument(
                    GraphQLAppliedDirectiveArgument.newArgument().name("class").type(Scalars.GraphQLString)
                        .valueProgrammatic(shape.targets.find { it.targetType == TargetType.targetClass }?.`object`?.uri)
                )
            }
        )
        .fields(listOf(idField).plus(shape.propertyShapes.mapNotNull { generateFieldDefinition(it, childContext) }))
        .build()
}

internal fun generateFieldDefinition(property: PropertyShape, context: ParseContext): GraphQLFieldDefinition? {
    val propName =
        property.shapeGraph.getString(property.shapeNode, SHACL.name) ?: propertyNameFromPath(
            property.path.toString().removeSurrounding("<", ">")
        )
    val description = property.shapeGraph.getString(property.shapeNode, SHACL.description)
    val collection =
        property.constraints.any { it is MaxCount && it.maxCount > 1 } || property.constraints.none { it is MaxCount }
    val minCount = property.constraints.find { it is MinCount }?.let { (it as MinCount).minCount } ?: 0
    val dataType = property.shapeGraph.getReference(property.shapeNode, SHACL.datatype)
    val classRef = property.shapeGraph.getReference(property.shapeNode, SHACL.class_)
    val rawGraphQLType = if (dataType != null) {
        generateScalar(dataType)
    } else if (classRef != null) {
        val matchingShape = context.allShapes.getMatchingShape(classRef)
        if (matchingShape != null) {
            GraphQLTypeReference(parseName(matchingShape.shapeNode, context))
        } else {
            println("  --> Property '$propName' for Shape <${context.nodeShape?.shapeNode?.uri}> has no matching Shape definition (for sh:class <${classRef.uri}>) and will be ignored!")
            null
        }
    } else {
        println("  --> Property '$propName' for Shape ${context.nodeShape?.shapeNode?.uri} does not provide a dataType or class and will be ignored!")
        null
    }

    return if (rawGraphQLType != null) {
        val graphQLType = if (collection) {
            GraphQLNonNull.nonNull(GraphQLList.list(GraphQLNonNull.nonNull(rawGraphQLType)))
        } else {
            if (minCount > 0) GraphQLNonNull(rawGraphQLType) else rawGraphQLType
        }
        GraphQLFieldDefinition.newFieldDefinition()
            .name(propName)
            .description(description)
            .type(graphQLType)
            .withAppliedDirective(
                propertyDirective.toAppliedDirective().transform {
                    it.argument(
                        GraphQLAppliedDirectiveArgument.newArgument().name("iri").type(Scalars.GraphQLString)
                            .valueProgrammatic(property.path)
                    )
                }
            )
            .build()
    } else {
        null
    }
}

internal fun generateScalar(dataType: Node_URI): GraphQLScalarType {
    return when (dataType) {
        XSD.decimal, XSD.xfloat, XSD.xdouble -> Scalars.GraphQLFloat
        XSD.integer, XSD.positiveInteger, XSD.nonPositiveInteger, XSD.negativeInteger, XSD.nonNegativeInteger, XSD.unsignedInt, XSD.xint -> Scalars.GraphQLInt
        XSD.xboolean -> Scalars.GraphQLBoolean
        else -> Scalars.GraphQLString
    }
}
