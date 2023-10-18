package be.ugent.solidlab.shapeshift.shacl2graphql


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
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

internal val importedFrom = NodeFactory.createURI("https://solidlab.be/vocab/importedFrom")



object SHACLToGraphQL {

    /**
     * Main function to transform SHACL shapes into a GraphQL Schema
     *
     * @param config the Context object containing the necessary configuration for the transformation.
     * @return a GraphQL schema in string form.
     *
     */
    fun getSchema(config : Context): String {
        val graph = GraphFactory.createDefaultGraph()
        config.shapeConfigs.forEach { (uri, _) ->
            println("  --> Importing ${uri} into graph")
            val shapeSubGraph = GraphFactory.createDefaultGraph()
            if(config.catalogURL != null && !uri.startsWith("file") && checkCatalog(uri, config.catalogURL)){
                RDFDataMgr.read(shapeSubGraph, catalogUrl(uri, config.catalogURL))
            } else {
                RDFDataMgr.read(shapeSubGraph, uri)
            }
            if(shapeSubGraph.isShaclValid() && (if (config.strict) shapeSubGraph.isShaclCore() else true)){
                shapeSubGraph.find()
                    .forEach {
                        if (it.predicateMatches(RDF.type.asNode())) {
                            graph.add(Triple.create(it.subject, importedFrom, NodeFactory.createURI(uri)))
                        }
                        graph.add(it)
                    }
            } else {
                if(!shapeSubGraph.isShaclValid()){
                     println("Shacl File $uri is not valid Shacl, ignored")
                } else {
                    println("Shacl File $uri contains non-core Shacl constraints, ignored")
                }
            }

        }
        val context =
            ParseContext(Shapes.parse(graph), config.shapeConfigs.mapKeys { entry -> NodeFactory.createURI(entry.key) })

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
            .let { builder -> if (config.shapeConfigs.any{it.value.mutation}) builder.mutation(generateMutations(context, graphQLTypes.associateBy { it.name })) else builder}
            .additionalTypes(graphQLTypes.plus(graphQLInputTypes))
            .additionalDirectives(setOf(isDirective, propertyDirective, identifierDirective))
            .build()
        val schemaPrinter = SchemaPrinter(SchemaPrinter.Options.defaultOptions().includeSchemaElement {
            it !is GraphQLDirective || setOf("property", "is", "identifier").contains(it.name)
        })
        return schemaPrinter.print(schema)
    }

    private fun catalogUrl(id: String, catalogUrl: String) : String {
        return "$catalogUrl/api/packages/${URLEncoder.encode(id, "UTF-8")}/download"
    }



    internal fun checkCatalog(id: String, catalogUrl: String) : Boolean {
        val packageURL = URL("$catalogUrl/api/packages/${URLEncoder.encode(id, "UTF-8")}")
        val conn = packageURL.openConnection() as HttpURLConnection
        return conn.responseCode == 200
    }
}
