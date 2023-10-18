import be.ugent.solidlab.shapeshift.shacl2graphql.isShaclCore
import be.ugent.solidlab.shapeshift.shacl2graphql.isShaclValid
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.sparql.graph.GraphFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class ShaclTest {
    @Test
    fun shaclCoreTest(){
        val uri = "file://${Paths.get("").toAbsolutePath()}/src/test/resources/contact-SHACL.ttl"
        val shapeSubGraph = GraphFactory.createDefaultGraph()
        RDFDataMgr.read(shapeSubGraph, uri)
        Assertions.assertTrue(shapeSubGraph.isShaclCore())
    }

    @Test
    fun shaclNonCoreTest(){
        val uri = "file://${Paths.get("").toAbsolutePath()}/src/test/resources/sparql-SHACL.ttl"
        val shapeSubGraph = GraphFactory.createDefaultGraph()
        RDFDataMgr.read(shapeSubGraph, uri)
        Assertions.assertFalse(shapeSubGraph.isShaclCore())
    }

    @Test
    fun shaclValidTest(){
        val uri = "file://${Paths.get("").toAbsolutePath()}/src/test/resources/sparql-SHACL.ttl"
        val shapeSubGraph = GraphFactory.createDefaultGraph()
        RDFDataMgr.read(shapeSubGraph, uri)
        Assertions.assertTrue(shapeSubGraph.isShaclValid())
    }

    @Test
    fun shaclNonValidTest(){
        val uri = "file://${Paths.get("").toAbsolutePath()}/src/test/resources/invalid-SHACL.ttl"
        val shapeSubGraph = GraphFactory.createDefaultGraph()
        RDFDataMgr.read(shapeSubGraph, uri)
        Assertions.assertFalse(shapeSubGraph.isShaclValid())
    }
}