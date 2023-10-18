package be.ugent.solidlab.shapeshift.shacl2graphql

import graphql.Scalars
import graphql.introspection.Introspection
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLNonNull

internal val identifierDirective: GraphQLDirective = GraphQLDirective.newDirective()
    .name("identifier")
    .validLocation(Introspection.DirectiveLocation.FIELD_DEFINITION)
    .build()

internal val idField: GraphQLFieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
    .name("id")
    .description("Auto-generated property that will be assigned to the `iri` of the Thing that is being queried.")
    .type(GraphQLNonNull.nonNull(Scalars.GraphQLID))
    .withAppliedDirective(identifierDirective.toAppliedDirective())
    .build()

internal val isDirective: GraphQLDirective = GraphQLDirective.newDirective()
    .name("is")
    .argument(GraphQLArgument.newArgument().name("class").type(Scalars.GraphQLString))
    .validLocations(Introspection.DirectiveLocation.OBJECT, Introspection.DirectiveLocation.INPUT_OBJECT)
    .build()
internal val propertyDirective: GraphQLDirective = GraphQLDirective.newDirective()
    .name("property")
    .argument(GraphQLArgument.newArgument().name("iri").type(Scalars.GraphQLString))
    .validLocations(
        Introspection.DirectiveLocation.FIELD_DEFINITION,
        Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION
    )
    .build()
