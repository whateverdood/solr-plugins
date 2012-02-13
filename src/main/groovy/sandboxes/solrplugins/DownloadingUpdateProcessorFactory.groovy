package sandboxes.solrplugins

import java.io.IOException

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.apache.solr.update.processor.UpdateRequestProcessorFactory

public class DownloadingUpdateProcessorFactory extends UpdateRequestProcessorFactory {

    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, 
        SolrQueryResponse resp, UpdateRequestProcessor np) {
        return new DownloadingProcessor(np)
    }
        
    public class DownloadingProcessor extends UpdateRequestProcessor {
        
        public DownloadingProcessor(UpdateRequestProcessor np) {
            super(np)
        }

        void processAdd(AddUpdateCommand cmd) throws IOException {
            SolrInputDocument doc = cmd.getSolrInputDocument()
            if (!doc.getFieldValue("raw-content")) {
                doc.setField("raw-content", download(doc.getFieldValue("uri")))
            }
            super.next.processAdd(cmd)
        }
        
        String download(String uri) {
            return new URL(uri).text
        }
        
    }

}
