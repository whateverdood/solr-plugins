package sandboxes.solrplugins;

import org.apache.log4j.Logger;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

public class ExtractingUpdateProcessorFactory extends
		UpdateRequestProcessorFactory {

	private static final Logger LOG = Logger
			.getLogger(ExtractingUpdateProcessorFactory.class);

	public ExtractingUpdateProcessorFactory() {
		// Do nothing.
	}

	@Override
	public void init(@SuppressWarnings("rawtypes") NamedList args) {
		super.init(args);
		LOG.info("Initialized.");
	}

	@Override
	public UpdateRequestProcessor getInstance(SolrQueryRequest req,
			SolrQueryResponse resp, UpdateRequestProcessor next) {
		return new ExtractingProcessor(next);
	}

}
