package sandboxes.solrplugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith(Parameterized.class)
public class ExtractingUpdateProcessorFactoryTest {

	private SolrInputDocument doc;
	private Map<String, String> expectedValues;
	private int expectedBodySize;

	public ExtractingUpdateProcessorFactoryTest(String contentUri,
			String suppliedContentType, Map<String, String> expectedValues,
			int expectedBodySize) throws MalformedURLException, IOException {
		doc = new SolrInputDocument();
		doc.setField("uri", contentUri);
		doc.setField("media-type", suppliedContentType);
		doc.setField("raw-content",
				IOUtils.toByteArray(new URL(contentUri).openStream()));

		this.expectedValues = expectedValues;
		this.expectedBodySize = expectedBodySize;
	}

	@Parameters
	public static Collection<Object[]> parameters() {
		Collection<Object[]> params = new ArrayList<Object[]>();

		Map<String, String> expected = new HashMap<String, String>();
		expected.put("media-type", "text/html");
		expected.put("title", "This is a test HTML document");
		expected.put("subject", "test solr lucene apache");
		params.add(new Object[] { "file:src/test/resources/download-this.html",
				"text/html", expected, 1900 });

		expected = new HashMap<String, String>();
		expected.put("Content-Type",
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		expected.put("date", "2012-02-28T04:14:00Z");
		expected.put("Keywords", "test solr lucene apache");
		params.add(new Object[] { "file:src/test/resources/download-this.docx",
				null, expected, 2000 });

		expected = new HashMap<String, String>();
		expected.put("Content-Type", "application/pdf");
		expected.put("created", "Mon Feb 27 23:44:56 EST 2012");
		expected.put("title", "Microsoft Word - download-this.docx");
		params.add(new Object[] { "file:src/test/resources/download-this.pdf",
				null, expected, 2900 });

		expected = new HashMap<String, String>();
		expected.put("Content-Type", "image/jpeg");
		expected.put("Software", "Picasa");
		expected.put("tiff:ImageLength", "546");
		expected.put("tiff:ImageWidth", "800");
		params.add(new Object[] { "file:src/test/resources/download-this.jpg",
				null, expected, 0 });

		expected = new HashMap<String, String>();
		expected.put("Content-Type", "application/rss+xml");
		expected.put("title", "BTI360 Blog");
		expected.put("description", "All the latest from BTI360");
		params.add(new Object[] {
				"file:src/test/resources/download-this-rss.xml", null,
				expected, 16100 });

		return params;
	}

	@Test
	public void extractContent() throws Exception {
		UpdateRequestProcessor nextStep = mock(UpdateRequestProcessor.class);

		doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				for (String key : expectedValues.keySet()) {
					assertEquals(expectedValues.get(key),
							doc.getFieldValue(key));
				}

				int bodySize = doc.getFieldValue("body") == null ? 0 : doc
						.getFieldValue("body").toString().length();
				assertTrue("Body size was less than what was expected.",
						expectedBodySize <= bodySize);

				return null;
			}
		}).when(nextStep).processAdd(any(AddUpdateCommand.class));

		ExtractingProcessor ep = (ExtractingProcessor) new ExtractingUpdateProcessorFactory()
				.getInstance(mock(SolrQueryRequest.class),
						mock(SolrQueryResponse.class), nextStep);

		AddUpdateCommand epCmd = when(
				mock(AddUpdateCommand.class).getSolrInputDocument())
				.thenReturn(doc).getMock();
		ep.processAdd(epCmd);
	}

}
