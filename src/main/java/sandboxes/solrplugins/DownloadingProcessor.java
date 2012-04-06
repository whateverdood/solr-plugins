package sandboxes.solrplugins;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
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
import org.apache.tika.parser.txt.CharsetDetector;

public class DownloadingProcessor extends UpdateRequestProcessor {

	private static final Logger LOG = Logger.getLogger(DownloadingProcessor.class);
	
	private Detector detector = new DefaultDetector();

	public DownloadingProcessor(UpdateRequestProcessor next) {
		super(next);
	}

	public void processAdd(AddUpdateCommand cmd) throws IOException {
		SolrInputDocument doc = cmd.getSolrInputDocument();
		String uri = (String) doc.getFieldValue("uri");
		
		if (StringUtils.isEmpty(uri)) {
			throw new IllegalArgumentException("No \"uri\" field set - nothing to do.");
		}
		
		byte[] rawContent = download(new URL(uri));
        String contentType = detectContentType(rawContent);

        // Save the raw-content so we can re-index in place.
		doc.setField("raw-content", Base64.encodeBase64String(rawContent));
        doc.setField("media-type", contentType);

		LOG.info("Downloaded [" + doc.getFieldValue("raw-content").toString().length() + 
		    "] bytes of [" + contentType + "] from [" + uri + "].");

		super.next.processAdd(cmd);
	}

	static String detectEncoding(byte[] bytes) {
	    CharsetDetector csDetector = new CharsetDetector();
	    csDetector.setText(bytes);
	    return csDetector.detect().getName(); // accept the best match
    }

    byte[] download(URL url) throws IOException {
		return IOUtils.toByteArray(url.openStream());
	}
	
	String detectContentType(byte[] bytes) throws IOException{
		return detector.detect(TikaInputStream.get(bytes), new Metadata()).toString();
	}
	
}
