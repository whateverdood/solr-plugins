package sandboxes.solrplugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
			.getLogger(ExtractingProcessor.class);

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
					"Can't extract field from null raw-content");
		}

		extractData(doc);

		LOG.debug("Extracted content: " + doc.toString());

		super.next.processAdd(cmd);
	}

	void extractData(SolrInputDocument doc) throws IOException {
		XPathExtractor extractor = Extractors.getExtractor((String) doc
				.getFieldValue("media-type"));
		if (extractor != null) {
			extractKnownContent(doc, extractor);
		} else {
			extractGenericContent(doc);
		}
	}

	void extractKnownContent(SolrInputDocument doc, XPathExtractor extractor) {
		@SuppressWarnings("unchecked")
		List<Map<String, ?>> extracted = extractor.extract(new String(
				(byte[]) doc.getFieldValue("raw-content")));

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
		
		InputStream stream = TikaInputStream.get((byte[]) doc.getFieldValue("raw-content"));
		try {
			parser.parse(stream, handler, metadata, context);
		} catch (Exception ex) {
			LOG.error("Unable to perform generic Tika extraction.", ex);
		} finally {
			stream.close();
		}

		//TODO: Need to extract into a known context-set
		for (String key : metadata.names()) {
			doc.setField(key, metadata.get(key));
		}

		doc.setField("body", handler.toString());
	}
}
