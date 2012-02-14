package sandboxes.solrplugins

import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.util.NamedList
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.apache.solr.update.processor.UpdateRequestProcessorFactory

import com.googlecode.shawty.XPathExtractor

public class ExtractingUpdateProcessorFactory extends UpdateRequestProcessorFactory {

    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req,
        SolrQueryResponse resp, UpdateRequestProcessor next) {
        return new ExtractingProcessor(next)
    }
     
    @Override   
    void init(NamedList args) {
	}

    public class ExtractingProcessor extends UpdateRequestProcessor {
        
        public ExtractingProcessor(UpdateRequestProcessor next) {
            super(next)
        }
        
        @Override
        void processAdd(AddUpdateCommand cmd) {
            SolrInputDocument doc = cmd.getSolrInputDocument()
            XPathExtractor extractor = 
                Extractors.getExtractor(doc.getFieldValue("media-type"))
            List extracted = extractor.extract(doc.getFieldValue("raw-content"))
            
            if (extracted.size()) {
                // we'll just use the first extract we find
                Map extract = extracted[0]
                extract.each { k, v ->
                    doc.setField(k, v)
                }
            }
            super.next.processAdd(cmd)
        }
    }
}
