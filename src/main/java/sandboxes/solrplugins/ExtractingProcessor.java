package sandboxes.solrplugins;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

import com.googlecode.shawty.XPathExtractor;

public class ExtractingProcessor extends UpdateRequestProcessor {

	private static final Logger LOG = Logger
			.getLogger(ExtractingProcessor.class);

	public ExtractingProcessor(UpdateRequestProcessor next) {
		super(next);
	}

	@Override
	public void processAdd(AddUpdateCommand cmd) throws IOException {
		SolrInputDocument doc = cmd.getSolrInputDocument();
		XPathExtractor extractor = Extractors.getExtractor((String) doc
				.getFieldValue("media-type"));

		if (StringUtils.isEmpty((String) doc.getFieldValue("raw-content"))) {
			throw new IllegalArgumentException(
					"Can't extract field from null raw-content");
		}

		@SuppressWarnings("unchecked")
		List<Map<String, ?>> extracted = extractor.extract(doc
				.getFieldValue("raw-content"));

		if (!extracted.isEmpty()) {
			// we'll just use the first extract we find
			Map<String, ?> extract = extracted.get(0);
			for (String key : extract.keySet()) {
				doc.setField(key, extract.get(key));
			}
			LOG.info("Extracted [${extract.keySet().size()}] fields.");
		}

		super.next.processAdd(cmd);
	}
}
