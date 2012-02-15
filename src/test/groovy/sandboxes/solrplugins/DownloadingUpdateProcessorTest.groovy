package sandboxes.solrplugins;

import static org.junit.Assert.*

import org.apache.solr.common.SolrInputDocument
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.junit.Test

class DownloadingUpdateProcessorTest {
    
    @Test
    public void downloads() throws Exception {
        final SolrInputDocument doc = new SolrInputDocument()
        doc.setField("uri", "file:src/test/resources/download-this.html")

        def request = [:] as SolrQueryRequest
        def response = [:] as SolrQueryResponse
        // duck-typing doesn't seem to work for "nextStep"
        def nextStep = new UpdateRequestProcessor(null) {
            void processAdd(AddUpdateCommand cmd) {
                // the "raw-content" field should have been populated when
                // this step is executed 
                assertNotNull doc.getFieldValue("raw-content")
            }
        }
        
        DownloadingProcessor dldr = 
            new DownloadingUpdateProcessorFactory().getInstance(
                request, response, nextStep)
                
        dldr.processAdd([getSolrInputDocument: { doc }] as AddUpdateCommand)
    }
    
}
