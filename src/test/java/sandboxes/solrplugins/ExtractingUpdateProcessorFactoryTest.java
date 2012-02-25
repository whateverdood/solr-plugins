package sandboxes.solrplugins;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class ExtractingUpdateProcessorFactoryTest {
	
	@Mock
	SolrQueryRequest request;
	@Mock
	SolrQueryResponse response;
	@Mock
	UpdateRequestProcessor nextStep;

	@Test
    public void downloads() throws Exception {
		String resource = "file:src/test/resources/download-this.html";
        
		final SolrInputDocument doc = new SolrInputDocument();
        doc.setField("uri", resource);
        doc.setField("media-type", "text/html");
        doc.setField("raw-content", IOUtils.toString(new URL(resource).openStream()));
        
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
            	for(String field : Arrays.asList("title", "subject", "body", "text")){
            		assertNotNull(field + " was not extracted", doc.getFieldValue(field));
            	}
            	for(String key : doc.keySet()){
            		System.out.println("Key: " + key + " Value: " + doc.getFieldValue(key));
            	}
                return null;
            }
        }).when(nextStep).processAdd(any(AddUpdateCommand.class));
        

        ExtractingProcessor ep = 
            (ExtractingProcessor) new ExtractingUpdateProcessorFactory().getInstance(
		    request, response, nextStep);
        
        AddUpdateCommand epCmd = when(mock(AddUpdateCommand.class).getSolrInputDocument()).thenReturn(doc).getMock();
        ep.processAdd(epCmd);
    }

}
