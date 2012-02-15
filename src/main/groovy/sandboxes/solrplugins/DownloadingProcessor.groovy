package sandboxes.solrplugins

import java.io.IOException

import org.apache.log4j.Logger
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.processor.UpdateRequestProcessor

public class DownloadingProcessor extends UpdateRequestProcessor {

    private static final Logger LOG = Logger.getLogger(DownloadingProcessor.class)
    
    public DownloadingProcessor(UpdateRequestProcessor next) {
        super(next)
    }

    void processAdd(AddUpdateCommand cmd) throws IOException {
        SolrInputDocument doc = cmd.getSolrInputDocument()
        String uri = doc.getFieldValue("uri")
        if (!uri) {
            throw new IllegalArgumentException("No \"uri\" field set - nothing to do.")
        }
        URL url = new URL(uri)
        switch (url.protocol) {
            case "file":
                doc.setField("raw-content", url.text)
                break
            default:
                doc.setField("raw-content", download(url))
        }
        // TODO: do this correctly
        doc.setField("media-type", "text/html")

        LOG.debug "Downloaded [${doc.getFieldValue('raw-content').size()}] bytes from [$uri]."
        
        super.next.processAdd(cmd)
    }

    String download(URL url) {
        return url.text
    }
}
