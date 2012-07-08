package sandboxes.solrplugins;

import java.util.Arrays;
import java.util.HashMap;

import com.googlecode.shawty.XPathExtractor;
import com.googlecode.shawty.pp.Lowerer;
import com.googlecode.shawty.pp.NoGPlusOnes;

public class Extractors {

	static HashMap<String, XPathExtractor> beans = initBeans();

	private static HashMap<String, XPathExtractor> initBeans() {
		HashMap<String, XPathExtractor> map = new HashMap<String, XPathExtractor>();
		XPathExtractor htmlExtractor = newHtmlExtractor();
        map.put("text/html", htmlExtractor);
        map.put("application/xhtml+xml", htmlExtractor);
        map.put("text/x-server-parsed-html", htmlExtractor);
		return map;
	}

	private static XPathExtractor newHtmlExtractor() {
		XPathExtractor extractor = new XPathExtractor();
		extractor.setForEach("/html");

		HashMap<String, String> xpaths = new HashMap<String, String>();
		// Dublin Core metadata (http://dublincore.org/documents/dces/)
		xpaths.put("creator",     "head/meta[@name='author']/@content");
		xpaths.put("date",        "(head/meta[@name='date']/@content | head/meta[@name='revised']/@content | head/meta[@name='published']/@content)[0]");
		xpaths.put("description", "head/meta[@name='abstract']/@content | head/meta[@name='description']/@content");
		xpaths.put("format",      "head/meta[@http-equiv='content-type']/@content");
		xpaths.put("identifier",  "(head/meta[@name='identifier']/@content | head/meta[@name='dc.identifier']/@content)[0]");
        xpaths.put("language",    "head/meta[@http-equiv='content-language']/@content");
        xpaths.put("publisher",   "head/meta[@name='owner']/@content");
        xpaths.put("relation",    "head/link[@rel='alternate']/@href");
        xpaths.put("rights",      "head/meta[@name='copyright']/@content");
		xpaths.put("subject",     "head/meta[@name='keywords']/@content");
        xpaths.put("title",       "head/title/text()");
        xpaths.put("type",        "head/meta[@name='resource-type']/@content");
        // other (i.e. made-up :-)
        xpaths.put("body",        "body/descendant::*[not(local-name(.)='script')]/text()");
        xpaths.put("text",        "descendant::*[not(local-name(.)='script' or local-name(.)='style')]/text()");
        xpaths.put("links",       "//a/@href");
		xpaths.put("emphasizedText", 
		    "body//b/text() | body//i/text() | body//strong/text() | body//em/text() | body//h1/text() | body//h2/text() | body//h3/text()");
		
		extractor.setFieldMappings(xpaths);
		extractor.setXmlReaderClazz("org.ccil.cowan.tagsoup.Parser");
		
        extractor.setPreprocessors(Arrays.asList(
            new Lowerer(), // Lower the input so the XPath expressions become case-insensitive :-/
            new NoGPlusOnes())); // Easier to just mangle the dumb g:plusone tags then deal with declaring a ton of namespaces. 

		return extractor;
	}

	public static void addExtractor(String name, XPathExtractor extractor) {
		beans.put(name, extractor);
	}

	public static XPathExtractor getExtractor(String name) {
		return beans.get(name);
	}

}
