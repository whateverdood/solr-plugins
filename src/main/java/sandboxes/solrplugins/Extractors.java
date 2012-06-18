package sandboxes.solrplugins;

import java.util.HashMap;

import com.googlecode.shawty.XPathExtractor;

public class Extractors {

	static HashMap<String, XPathExtractor> beans = initBeans();

	private static HashMap<String, XPathExtractor> initBeans() {
		HashMap<String, XPathExtractor> map = new HashMap<String, XPathExtractor>();
		XPathExtractor htmlExtractor = buildHtmlExtractor();
        map.put("text/html", htmlExtractor);
        map.put("application/xhtml+xml", htmlExtractor);
        map.put("text/x-server-parsed-html", htmlExtractor);
		return map;
	}

	private static XPathExtractor buildHtmlExtractor() {
		XPathExtractor htmlExtractor = new XPathExtractor();
		htmlExtractor.setForEach("/h:html");

		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("h", "http://www.w3.org/1999/xhtml");
		htmlExtractor.setNamespaces(namespaces);

		HashMap<String, String> xpaths = new HashMap<String, String>();
		xpaths.put("title",   "h:head/h:title/text()");
		xpaths.put("subject", "h:head/h:meta[@name='keywords']/@content");
        xpaths.put("relation", "h:body//h:a/@href | h:body//h:a/text()");
        // TODO: more Dublin Core index fields?
        xpaths.put("body",    "h:body/descendant::*[not(local-name(.)='script')]/text()");
		xpaths.put("text",    "descendant::*[not(local-name(.)='script' or local-name(.)='style')]/text()");
		xpaths.put("emphasizedText", 
		    "h:body//h:b/text() | h:body//h:i/text() | h:body//h:strong/text() | h:body//h:em/text() | h:body//h:h1/text() | h:body//h:h2/text() | h:body//h:h3/text()");
		htmlExtractor.setFieldMappings(xpaths);

		htmlExtractor.setXmlReaderClazz("org.ccil.cowan.tagsoup.Parser");

		return htmlExtractor;
	}

	public static void addExtractor(String name, XPathExtractor extractor) {
		beans.put(name, extractor);
	}

	public static XPathExtractor getExtractor(String name) {
		return beans.get(name);
	}

}
