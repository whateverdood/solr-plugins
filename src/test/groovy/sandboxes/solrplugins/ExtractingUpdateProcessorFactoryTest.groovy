package sandboxes.solrplugins;

import static org.junit.Assert.*

import org.apache.solr.common.SolrInputDocument
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.junit.Test

class ExtractingUpdateProcessorFactoryTest {
    
    @Test
    public void test() {
        def resource = "file:src/test/resources/download-this.html"

        final SolrInputDocument doc = new SolrInputDocument()
        doc.setField("uri", resource)
        doc.setField("media-type", "text/html")
        doc.setField("raw-content", new URL(resource).text)

        def request = [:] as SolrQueryRequest
        def response = [:] as SolrQueryResponse
        def nextStep = new UpdateRequestProcessor(null) {
            void processAdd(AddUpdateCommand cmd) {
                // "title", "subject", "body", and "text" should all have been
                // populated at this point
                ["title", "subject", "body", "text"].each { field ->
                    assertNotNull "[$field] was not extracted", doc.getFieldValue(field)
                }
                println "Extracted doc is: $doc"
            }
        }

        ExtractingUpdateProcessorFactory.ExtractingProcessor ep =
            new ExtractingUpdateProcessorFactory().getInstance(
                request, response, nextStep)

        ep.processAdd([getSolrInputDocument: { doc }] as AddUpdateCommand)
    }
    
}
