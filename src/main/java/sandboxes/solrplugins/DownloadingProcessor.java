package sandboxes.solrplugins;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

import sandboxes.solrplugins.DownloadingProcessor;

public class DownloadingProcessor extends UpdateRequestProcessor {

	private static final Logger LOG = Logger
			.getLogger(DownloadingProcessor.class);

	public DownloadingProcessor(UpdateRequestProcessor next) {
		super(next);
	}

	public void processAdd(AddUpdateCommand cmd) throws IOException {
		SolrInputDocument doc = cmd.getSolrInputDocument();
		String uri = (String) doc.getFieldValue("uri");
		if (StringUtils.isEmpty(uri)) {
			throw new IllegalArgumentException(
					"No \"uri\" field set - nothing to do.");
		}
		URL url = new URL(uri);
		if (StringUtils.equalsIgnoreCase("file", url.getProtocol())) {
			doc.setField("raw-content", IOUtils.toString(url.openStream()));
		} else {
			doc.setField("raw-content", download(url));
		}

		// TODO: do this correctly
		doc.setField("media-type", "text/html");

		LOG.info("Downloaded [${doc.getFieldValue('raw-content').size()}] bytes from [$uri].");

		super.next.processAdd(cmd);
	}

	String download(URL url) {
		return url.toString();
	}
}
