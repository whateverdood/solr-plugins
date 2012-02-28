package sandboxes.solrplugins;

import org.apache.log4j.Logger
import org.apache.solr.common.util.NamedList
import org.apache.solr.request.SolrQueryRequest
import org.apache.solr.response.SolrQueryResponse
import org.apache.solr.update.processor.UpdateRequestProcessor
import org.apache.solr.update.processor.UpdateRequestProcessorFactory

import sandboxes.solrplugins.ExtractingProcessor;

public class ExtractingUpdateProcessorFactoryGroovy extends UpdateRequestProcessorFactory {

    private static final Logger LOG = Logger.getLogger(ExtractingUpdateProcessorFactoryGroovy.class)
    
    public ExtractingUpdateProcessorFactoryGroovy() {
        // Do nothing.
    }

    @Override
    public void init(NamedList args) {
        super.init(args)
        LOG.info "Initialized."
    }

    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req,
        SolrQueryResponse resp, UpdateRequestProcessor next) {
        return new ExtractingProcessorGroovy(next)
    }
}

