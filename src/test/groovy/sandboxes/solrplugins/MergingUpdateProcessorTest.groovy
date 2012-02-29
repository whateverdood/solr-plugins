package sandboxes.solrplugins;

import static org.junit.Assert.*

import org.apache.log4j.BasicConfigurator
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.apache.solr.util.SolrPluginUtils
import org.gmock.GMockTestCase
import org.junit.Test

class MergingUpdateProcessorTest extends GMockTestCase {
    
    static {
        BasicConfigurator.resetConfiguration()
        BasicConfigurator.configure()
    }

    @Test
    void testMerges() throws Exception {
        
        SolrDocument current = new SolrDocument()
        current.setField("id", "http://foo/bar")
        current.setField("uri", "http://foo/bar")
        current.setField("title", "foo")
        current.setField("body", "The quick brown fox jumps over the lazy dog.")
        current.setField("text", "foo The quick brown fox jumps over the lazy dog.")
        current.setField("added", "yesterday")
        current.setField("score", "1.0")
        
        SolrDocumentList solrDocList = mock(SolrDocumentList)
        solrDocList.size().returns(1).stub()
        solrDocList.get(0).returns(current).stub()
        
        SolrQueryRequest request = [getSearcher: {}] as SolrQueryRequest
        SolrQueryResponse response = [:] as SolrQueryResponse

        SolrPluginUtils pluginUtils = mock(SolrPluginUtils)
        pluginUtils.static {
            doSimpleQuery("id:\"http://foo/bar\"", request, 0, 1).
                returns(null).stub()
            docListToSolrDocumentList(
                null, request.getSearcher(), null, 
                new HashMap<SolrDocument, Integer>()).returns(solrDocList).stub()
        }

        def nextStep = new UpdateRequestProcessor(null) {
            void processAdd(AddUpdateCommand cmd) {
                SolrInputDocument actual = cmd.getSolrInputDocument()
                assertEquals "New field got lost?", "bar baz", actual.getFieldValue("subject")
                assertEquals "Old field got lost?", "yesterday", actual.getFieldValue("added")
                assertNull actual.getFieldValue("score")
            }
        }

        SolrInputDocument modified = new SolrInputDocument()
        modified.setField("id", "http://foo/bar")
        modified.setField("uri", "http://foo/bar")
        modified.setField("title", "foo")
        modified.setField("body", "The quick brown fox jumps over the lazy dog.")
        modified.setField("text", "foo The quick brown fox jumps over the lazy dog.")
        modified.setField("subject", "bar baz")

        play {
            MergingUpdateProcessor mup =
                new MergingUpdateProcessorFactory().getInstance(
                    request, response, nextStep)
                
            mup.processAdd(
                [getSolrInputDocument: { return modified }] as AddUpdateCommand)
        }
    }
    
}
