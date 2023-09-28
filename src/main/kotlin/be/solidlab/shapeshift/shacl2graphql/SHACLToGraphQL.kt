package be.solidlab.shapeshift.shacl2graphql


import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLSchema
import graphql.schema.idl.SchemaPrinter
import org.apache.jena.graph.NodeFactory
import org.apache.jena.graph.Triple
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.shacl.Shapes
import org.apache.jena.shacl.parser.NodeShape
import org.apache.jena.sparql.graph.GraphFactory
import org.apache.jena.vocabulary.RDF
import java.io.File

internal val importedFrom = NodeFactory.createURI("https://solidlab.be/vocab/importedFrom")

object SHACLToGraphQL {
    fun getSchema(config: ShiftConfig): String {
        println("Looking at Shape definitions in ${config.shaclDir.path}")
        val graph = GraphFactory.createDefaultGraph()
        config.shapeConfigs.forEach { (uri, shapeConfig) ->
            println("  --> Importing ${uri} into graph")
            val shapeSubGraph = GraphFactory.createDefaultGraph()
            RDFDataMgr.read(shapeSubGraph, File(config.shaclDir, shapeConfig.getTargetFileName(uri)).toURI().toString())
            shapeSubGraph.find()
                .forEach {
                    if (it.predicateMatches(RDF.type.asNode())) {
                        graph.add(Triple.create(it.subject, importedFrom, NodeFactory.createURI(uri)))
                    }
                    graph.add(it)
                }
        }
        val context =
            ParseContext(Shapes.parse(graph), config.shapeConfigs.mapKeys {entry -> NodeFactory.createURI(entry.key) })

        println("Building GraphQL schema from Graph")
        println("  --> Constructing GraphQL object types")
        val graphQLTypes = context.allShapes.iteratorAll().asSequence()
            .map { shape -> generateObjectType(shape as NodeShape, context) }.distinctBy { it.name }.toSet()

        println("  --> Constructing GraphQL input types")
        val graphQLInputTypes = context.allShapes.iteratorAll().asSequence()
            .filter { shape -> context.getShapeConfig(shape.shapeNode).mutation }
            .flatMap { shape ->
                InputTypeConfiguration.values().mapNotNull { generateInputType(shape as NodeShape, context, it) }
            }
            .distinctBy { it.name }.toSet()

        println("  --> Assembling the GraphQL schema")
        val schema = GraphQLSchema.newSchema()
            .query(generateEntryPoints(graphQLTypes))
            .mutation(generateMutations(context, graphQLTypes.associateBy { it.name }))
            .additionalTypes(graphQLTypes.plus(graphQLInputTypes))
            .additionalDirectives(setOf(isDirective, propertyDirective, identifierDirective))
            .build()
        val schemaPrinter = SchemaPrinter(SchemaPrinter.Options.defaultOptions().includeSchemaElement {
            it !is GraphQLDirective || setOf("property", "is", "identifier").contains(it.name)
        })
        return schemaPrinter.print(schema)
    }
}
