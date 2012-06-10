package sandboxes.solrplugins;


import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

public class DownloadingUpdateProcessorFactory extends
		UpdateRequestProcessorFactory {

	private static final Logger LOG = Logger
			.getLogger(DownloadingUpdateProcessorFactory.class.getName());
	
	HttpClient httpClient = new CachingHttpClient(new DefaultHttpClient());

	public DownloadingUpdateProcessorFactory() {
		// Do nothing.
	}

	@Override
	public void init(@SuppressWarnings("rawtypes") NamedList args) {
		super.init(args);
		Object useJREHttpClient = args.get("useJREHttpClient");
		if (useJREHttpClient != null) {
		    if (Boolean.valueOf(useJREHttpClient.toString())) {
		        LOG.warning("Using the JDK HTTP client stack.");
		        httpClient = null;
		    }
		}
		LOG.info("Initialized.");
	}

	@Override
	public UpdateRequestProcessor getInstance(SolrQueryRequest req,
			SolrQueryResponse resp, UpdateRequestProcessor np) {
		DownloadingProcessor downloadingProcessor = new DownloadingProcessor(
		    np, httpClient);
        return downloadingProcessor;
	}

}
