package sandboxes.solrplugins

import org.apache.log4j.Logger
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.processor.UpdateRequestProcessor

import com.googlecode.shawty.XPathExtractor

public class ExtractingProcessor extends UpdateRequestProcessor {

    private static final Logger LOG = Logger.getLogger(ExtractingProcessor)
    
    public ExtractingProcessor(UpdateRequestProcessor next) {
        super(next)
    }

    @Override
    void processAdd(AddUpdateCommand cmd) {
        SolrInputDocument doc = cmd.getSolrInputDocument()
        XPathExtractor extractor =
            Extractors.getExtractor(doc.getFieldValue("media-type"))
            
        if (!doc.getFieldValue("raw-content")) {
            throw new IllegalArgumentException("Can't extract field from null raw-content")
        }
        List extracted = extractor.extract(doc.getFieldValue("raw-content"))

        if (extracted.size()) {
            // we'll just use the first extract we find
            Map extract = extracted[0]
            extract.each { k, v ->
                doc.setField(k, v)
            }
            LOG.info "Extracted [${extract.keySet().size()}] fields."
        }
        
        super.next.processAdd(cmd)
    }
}