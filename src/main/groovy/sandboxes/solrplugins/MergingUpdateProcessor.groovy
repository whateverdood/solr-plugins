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
        def id = doc?.getFieldValue("id")
        
        if (LOG.isDebugEnabled())
            LOG.debug "Searching for doc id: [$id] "
        
        SolrDocumentList docs = SolrPluginUtils.docListToSolrDocumentList(
            SolrPluginUtils.doSimpleQuery("id:\"$id\"", request, 0, 1), 
            request.getSearcher(), null, new HashMap<SolrDocument, Integer>())
        
        if (docs.size()) {
            SolrDocument existing = docs.get(0)
            LOG.info "Found existing doc for id: [$id]; will merge."
            doc = merge(doc, ClientUtils.toSolrInputDocument(existing))
        }
        
        next.processAdd(cmd)
	}
    
    /*
     * Merging favors "fresh" over "existing", except for field values that existed
     * on "old". The idea is a newer version of the doc might have been downloaded
     * but we don't want to loose any meta-data that was added to the document,
     * such as "likes", "read", "shared", etc. 
     */
    SolrInputDocument merge(SolrInputDocument fresh, SolrInputDocument existing) {
        if (LOG.isDebugEnabled())
            LOG.debug "Merging fresh: ${System.getProperty('line.separator')} [$fresh] " +
                "${System.getProperty('line.separator')}with existing: " +
                "${System.getProperty('line.separator')}[$existing]"
            
        existing.each { k, v ->
            if (!fresh.get(k)) {
                if (!"score".equals(k)) {
                    fresh.setField(k, v.value)
                }
            }
        }
        
        return fresh
    }
    
}
