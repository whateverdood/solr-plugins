package sandboxes.solrplugins;


import java.util.logging.Logger;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

public class DownloadingUpdateProcessorFactory extends
		UpdateRequestProcessorFactory {

	private static final Logger LOG = Logger
			.getLogger(DownloadingUpdateProcessorFactory.class.getName());
	
	boolean useJdkHttpClient = false;

	public DownloadingUpdateProcessorFactory() {
		// Do nothing.
	}

	@Override
	public void init(@SuppressWarnings("rawtypes") NamedList args) {
		super.init(args);
		Object jdkHttp = args.get("useJdkHttpClient");
		if (jdkHttp != null) {
		    useJdkHttpClient = Boolean.valueOf(jdkHttp.toString());
		    if (useJdkHttpClient) {
		        LOG.warning("Using the JDK HTTP client stack.");
		    }
		}
		LOG.info("Initialized.");
	}

	@Override
	public UpdateRequestProcessor getInstance(SolrQueryRequest req,
			SolrQueryResponse resp, UpdateRequestProcessor np) {
		DownloadingProcessor downloadingProcessor = new DownloadingProcessor(np);
		downloadingProcessor.useJdkHttpClient = useJdkHttpClient;
        return downloadingProcessor;
	}
}
