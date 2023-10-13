import be.ugent.solidlab.shapeshift.shacl2graphql.SHACLToGraphQL.checkCatalog
import be.ugent.solidlab.shapeshift.shacl2graphql.SHACLToGraphQL.getSchema
import be.ugent.solidlab.shapeshift.shacl2graphql.ShapeConfig
import be.ugent.solidlab.shapeshift.shacl2graphql.Context
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Paths


class URITest {



    @Test
    fun testCheckCatalog(){
        val catalogURL = "https://catalog.solidlab.be"
        val existingId = "https://data.knows.idlab.ugent.be/person/office/shape/research-stay"
        val fakeId = "https://data.knows.idlab.ugent.be/person/office/shape/research-play"

        val shouldBeTrue = checkCatalog(existingId, catalogURL)
        val shouldBeFalse = checkCatalog(fakeId, catalogURL)

        Assertions.assertTrue(shouldBeTrue)
        Assertions.assertFalse(shouldBeFalse)
    }

    @Test
    fun testFromCatalog(){
        val catalogURL = "https://catalog.solidlab.be"
        val existingId = "https://data.vlaanderen.be/shacl/adresregister-SHACL.ttl"

        val schema = getSchema(Context(catalogURL, false, mapOf(pair = Pair(existingId, ShapeConfig(true, listOf())))))

        Assertions.assertNotNull(schema)
    }

    @Test
    fun testFromRandomUri(){
        val existingId = "https://data.vlaanderen.be/shacl/adresregister-SHACL.ttl"

        val schema = getSchema(Context(null, false, mapOf(pair = Pair(existingId, ShapeConfig(false, listOf())))))

        Assertions.assertNotNull(schema)
    }

    @Test
    fun testFromFileSystem(){
        val existingId = "file://${Paths.get("").toAbsolutePath()}/src/test/resources/contact-SHACL.ttl"

        val schema = getSchema(Context(null, false, mapOf(pair = Pair(existingId, ShapeConfig(false, listOf())))))

        Assertions.assertNotNull(schema)
    }


}