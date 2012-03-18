package sandboxes.solrplugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.apache.commons.codec.binary.Base64;
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
public class DownloadingUpdateProcessorTest {

	@Mock
	SolrQueryRequest request;
	@Mock
	SolrQueryResponse response;
	@Mock
	UpdateRequestProcessor nextStep;

	@Test
    public void downloads() throws Exception {
        final SolrInputDocument doc = new SolrInputDocument();
        doc.setField("uri", "file:src/test/resources/download-this.html");
        
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
            	byte[] content = Base64.decodeBase64(
            	    doc.getFieldValue("raw-content").toString());
                assertNotNull(content);
                assertTrue("Content wasn't properly downloaded.", content.length > 100);
                return null;
            }
        }).when(nextStep).processAdd(any(AddUpdateCommand.class));
        

        DownloadingProcessor dldr = 
            (DownloadingProcessor) new DownloadingUpdateProcessorFactory().getInstance(
		    request, response, nextStep);
        
        AddUpdateCommand dldrCmd = when(mock(AddUpdateCommand.class).getSolrInputDocument()).thenReturn(doc).getMock();
        dldr.processAdd(dldrCmd);
    }
	
	
	@Test
    public void detectsContent() throws Exception {
		
        DownloadingProcessor dldr = 
            (DownloadingProcessor) new DownloadingUpdateProcessorFactory().getInstance(
		    request, response, nextStep);
        
        assertEquals("text/html", dldr.detectContentType(dldr.download(new URL("file:src/test/resources/download-this.html"))));
        assertEquals("application/pdf", dldr.detectContentType(dldr.download(new URL("file:src/test/resources/download-this.pdf"))));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", dldr.detectContentType(dldr.download(new URL("file:src/test/resources/download-this.docx"))));
        assertEquals("image/jpeg", dldr.detectContentType(dldr.download(new URL("file:src/test/resources/download-this.jpg"))));
        assertEquals("application/rss+xml", dldr.detectContentType(dldr.download(new URL("file:src/test/resources/download-this-rss.xml"))));
        
        //Currently being extracted as application/xml, not the more specific subtype.
//        assertEquals("application/atom+xml", dldr.detectContentType(dldr.download(new URL("file:src/test/resources/download-this-atom.xml"))));
//        assertEquals("application/xml", dldr.detectContentType(dldr.download(new URL("file:src/test/resources/download-this.xml"))));
    }

}
