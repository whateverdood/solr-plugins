package sandboxes.solrplugins


import com.googlecode.shawty.XPathExtractor

/**
 * Fake Extractors bean context.
 */
class ExtractorsGroovy {
    
    static beans = [:]
    
    static {
        def namespaces = ["h": "http://www.w3.org/1999/xhtml"]
        def forEach = "/h:html"
        def xpaths = [
            "title": "h:head/h:title/text()",
            "subject": "h:head/h:meta[@name='keywords']/@content",
            "body": "h:body/descendant::*[not(local-name(.)='script')]/text()",
            "text": "descendant::*[not(local-name(.)='script')]/text()"]
        XPathExtractor htmlExtractor = new XPathExtractor(forEach: forEach,
            fieldMappings: xpaths, namespaces: namespaces,
            xmlReaderClazz: "org.ccil.cowan.tagsoup.Parser")
        ExtractorsGroovy.addExtractor("text/html", htmlExtractor)
    }
    
    public static void addExtractor(String name, XPathExtractor extractor) {
        beans.name = extractor
    }
    
    public static Object getExtractor(String name) {
        return beans.name
    }
    
}
