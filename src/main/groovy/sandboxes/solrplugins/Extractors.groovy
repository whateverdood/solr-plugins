package sandboxes.solrplugins

import com.googlecode.shawty.XPathExtractor

/**
 * Fake Extractors bean context.
 */
class Extractors {
    
    static beans = [:]
    
    public static void addExtractor(String name, XPathExtractor extractor) {
        beans.name = extractor
    }
    
    public static Object getBean(String name) {
        return beans.name
    }
    
}
