package sandboxes.solrplugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import com.googlecode.shawty.XPathExtractor;

public class ExtractingProcessor extends UpdateRequestProcessor {

	private static final Logger LOG = Logger
			.getLogger(ExtractingProcessor.class.getName());

	private Parser parser;

	private ParseContext context;

	private ContentHandler handler;

	public ExtractingProcessor(UpdateRequestProcessor next) {
		super(next);
		parser = new AutoDetectParser();
		context = new ParseContext();
		context.set(Parser.class, parser);
		handler = new BodyContentHandler();
	}

	@Override
	public void processAdd(AddUpdateCommand cmd) throws IOException {
		SolrInputDocument doc = cmd.getSolrInputDocument();

		if (doc.getFieldValue("raw-content") == null) {
			throw new IllegalArgumentException(
			    "Can't extract metadata fields from no raw-content");
		}

		try {
	        extractData(doc);		    
		} catch (Exception e) {
		    throw new IOException(e);
		}

		if (LOG.isLoggable(Level.FINE))
		    LOG.fine("Extracted: " + doc.toString());
		
		super.next.processAdd(cmd);
	}

	void extractData(SolrInputDocument doc) throws Exception {
		String mediaType = (String) doc.getFieldValue("media-type");
		
		if (LOG.isLoggable(Level.FINE))
		    LOG.fine("Trying to extract content for type [" + mediaType + "]");
		
        XPathExtractor extractor = Extractors.getExtractor(mediaType);
		
		if (extractor != null) {
			extractKnownContent(doc, extractor);
		} else {
			extractGenericContent(doc);
		}
	}

    @SuppressWarnings("unchecked")
	void extractKnownContent(SolrInputDocument doc, XPathExtractor extractor) 
	    throws Exception {
	    
		byte[] bytes = toByteArray(doc.getFieldValue("raw-content"));
		List<Map<String, ?>> extracted = extractor.extract(
		    new String(bytes, DownloadingProcessor.detectEncoding(bytes)));

		if (!extracted.isEmpty()) {
			// we'll just use the first extract we find
			Map<String, ?> extract = extracted.get(0);
			for (String key : extract.keySet()) {
				doc.setField(key, extract.get(key));
			}
		}
	}

    void extractGenericContent(SolrInputDocument doc) throws IOException {
        Metadata metadata = new Metadata();
        
        InputStream stream = TikaInputStream.get(
            toByteArray(doc.getFieldValue("raw-content")));
        try {
            parser.parse(stream, handler, metadata, context);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Unable to perform generic Tika extraction:", ex);
        } finally {
            stream.close();
        }

        // TODO: Need to extract into a known context-set
        for (String key : metadata.names()) {
            doc.setField(key, metadata.get(key));
        }

        doc.setField("body", handler.toString());
    }

    byte[] toByteArray(Object fieldValue) {
	    if (fieldValue instanceof byte[]) {
	        return (byte[]) fieldValue;
	    } else if (fieldValue instanceof String) {
	        // SOLR binary field types are Base64 encoded Strings
	        String stringFieldValue = (String) fieldValue;
            if (Base64.isBase64(stringFieldValue)) {
	            return Base64.decodeBase64(stringFieldValue);
	        } else {
	            return stringFieldValue.getBytes();
	        }
	    }
        return fieldValue.toString().getBytes(); // ?
    }

}
