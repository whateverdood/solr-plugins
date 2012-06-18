package sandboxes.solrplugins;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.txt.CharsetDetector;

public class DownloadingProcessor extends UpdateRequestProcessor {

	private static final Logger LOG = Logger.getLogger(
	    DownloadingProcessor.class.getName());
	
	private Detector detector = new DefaultDetector();

    HttpClient httpClient;
    
    ThreadLocal<HttpContext> httpContext = new ThreadLocal<HttpContext>() {
        protected HttpContext initialValue() {
            return new BasicHttpContext();
        };
    };
    
	public DownloadingProcessor(UpdateRequestProcessor next, HttpClient httpClient) {
		super(next);
		this.httpClient = httpClient;
	}

	public void processAdd(AddUpdateCommand cmd) throws IOException {
		SolrInputDocument doc = cmd.getSolrInputDocument();
		String uri = (String) doc.getFieldValue("uri");
		
		if (StringUtils.isEmpty(uri)) {
			throw new IllegalArgumentException(
			    "No \"uri\" field set - nothing to do.");
		}
		
		byte[] rawContent = download(new URL(uri));
        String contentType = detectContentType(rawContent);

        // Save the raw-content so we can re-index in place.
		doc.setField("raw-content", Base64.encodeBase64String(rawContent));
        doc.setField("media-type", contentType);

		LOG.info("Downloaded [" + 
		    doc.getFieldValue("raw-content").toString().length() + 
		    "] bytes of [" + contentType + "] from [" + uri + "].");

		super.next.processAdd(cmd);
	}

	public static String detectEncoding(byte[] bytes) {
	    CharsetDetector csDetector = new CharsetDetector();
	    csDetector.setText(bytes);
	    return csDetector.detect().getName(); // accept the best match
    }

    public byte[] download(URL url) throws IOException {
        if (LOG.isLoggable(Level.FINE))
            LOG.fine("Trying to download [" + url + "]");
        
        if (httpClient == null) {
            return IOUtils.toByteArray(url.openStream());
        } else {
            HttpGet get = new HttpGet(url.toString());
            HttpResponse response = httpClient.execute(get, httpContext.get());
            if (200 == response.getStatusLine().getStatusCode()) {
                HttpEntity entityBody = response.getEntity();
                return IOUtils.toByteArray(entityBody.getContent());
            }
            IOUtils.toByteArray(response.getEntity().getContent()); // consume
            throw new IOException("Couldn't download [" + url + "] <- " + 
                response.getStatusLine().toString());            
        }
	}
	
	public String detectContentType(byte[] bytes) throws IOException{
		return detector.detect(TikaInputStream.get(bytes), new Metadata()).toString();
	}
	
}
