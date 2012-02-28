package sandboxes.solrplugins

import org.apache.log4j.Logger
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.search.DocList
import org.apache.solr.update.AddUpdateCommand
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.apache.solr.util.SolrPluginUtils

class MergingUpdateProcessor extends UpdateRequestProcessor {

    private static final Logger LOG = Logger.getLogger(MergingUpdateProcessor)
    
    SolrQueryRequest request
    
    public MergingUpdateProcessor(SolrQueryRequest req, UpdateRequestProcessor next) {
        super(next)
        request = req
    }
    
    @Override
    void processAdd(AddUpdateCommand cmd) throws IOException {
        
        SolrInputDocument doc = cmd.getSolrInputDocument()
        def id = doc.getFieldValue("id")
        
        if (LOG.isDebugEnabled())
            LOG.debug "Searching for doc id: [$id] "
        
        DocList docList = SolrPluginUtils.doSimpleQuery("id:\"$id\"", request, 0, 1)
            
        SolrDocumentList docs = SolrPluginUtils.docListToSolrDocumentList(
            docList, request.getSearcher(), 
            ["id", "uri", "title", "subject", "body", "text"] as Set, 
            new HashMap<SolrDocument, Integer>())
        
        if (docs.size()) {
            SolrDocument existing = docs.get(0)
            if (LOG.isDebugEnabled())
                LOG.debug "Found existing doc: [$existing]"
            doc = merge(doc, ClientUtils.toSolrInputDocument(existing))
        }
        
        next.processAdd(cmd)
	}
    
    // Merge precedence favors "mods"
    SolrInputDocument merge(SolrInputDocument mods, SolrInputDocument current) {
        if (LOG.isDebugEnabled())
            LOG.debug "Merging [$mods] with [$current]"
            
        current.each { k, v ->
            if (!mods.get(k)) {
                mods.setField(k, v.value)
            }
        }
        
        return mods
    }
    
}
