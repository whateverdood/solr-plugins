package sandboxes.solrplugins;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;

public class DownloadingProcessor extends UpdateRequestProcessor {

	private static final Logger LOG = Logger
			.getLogger(DownloadingProcessor.class);
	
	private Detector detector = new DefaultDetector();
	

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
		byte[] rawContent = download(new URL(uri));
		//We want to save the raw-content so we can index in place. Saving a byte[] since we aren't guaranteed to have text
		doc.setField("raw-content", rawContent);

		doc.setField("media-type", detectContentType(rawContent));

		LOG.info("Downloaded [${doc.getFieldValue('raw-content').size()}] bytes from [$uri].");

		super.next.processAdd(cmd);
	}

	protected byte[] download(URL url) throws IOException {
		return IOUtils.toByteArray(url.openStream());
	}
	
	protected String detectContentType(byte[] content) throws IOException{
		return detector.detect(TikaInputStream.get(content), new Metadata()).toString();
	}
	
}
