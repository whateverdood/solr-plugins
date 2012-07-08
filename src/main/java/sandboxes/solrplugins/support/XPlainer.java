package sandboxes.solrplugins.support;

import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sandboxes.solrplugins.Extractors;

public class XPlainer {
    
    private static final Logger LOG = Logger
        .getLogger(XPlainer.class.getName());

    /**
     * Mark-up the parts of the supplied <code>xml</code> that match
     * <code>xpaths</code> in an XHTML div. 
     * @param xpaths XPaths to visualize.
     * @param content Well-formed XML.
     * @return An XHTML div containing the marked-up XML.
     * @throws Exception
     */
    public String xplain(
        String prefix, Map<String, String> xpaths, String content, String mediaType) 
        throws Exception {
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("xplaining [" + xpaths + "] in some [" + mediaType + "]");
        }
        
        Document doc = (Document) Extractors.getExtractor(mediaType).toDom(content);
        
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Made the DOM");
        }        

        XPath xpath = xPathFactory.newXPath();
        Node top = (Node) xpath.evaluate(prefix, doc, XPathConstants.NODE);
        
        for (String field : xpaths.keySet()) {            
            String x = xpaths.get(field);
            String hitClass = field + "_hit";
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Evaluating XPath [" + x + "] for field [" + field + "].");
            }
            
            NodeList nodes = (NodeList) xpath.evaluate(x, top, XPathConstants.NODESET);
            
            for (int i = 0; i < nodes.getLength(); i++) {
                Node hit = nodes.item(i);
                if (hit instanceof CharacterData) {
                    Node hitParent = hit.getParentNode();
                    if (hitParent instanceof Element) {
                        hitParent = addClass((Element) hitParent, hitClass);
                    } else {
                        // have to wrap CharacterData nodes in a new element
                        Element hitSpan = doc.createElement("span");
                        hitSpan = addClass(hitSpan, hitClass);
                        
                        hitParent.removeChild(hit);
                        hitParent.appendChild(hitSpan);
                        hitSpan.appendChild(hit);                        
                    }
                    
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("XPath [" + x + "] hit for field [" + field + 
                            "] on CharacterData -> [" + hitParent + "]");
                    }
                } else if (hit instanceof Attr) {
                    Element e = ((Attr) hit).getOwnerElement();
                    e = addClass(e, hitClass);
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("XPath [" + x + "] hit for field [" + field + 
                            "] on Attribute -> [" + hit + "]");
                    }
                } else if (hit instanceof Element) {
                    Element e = (Element) hit;
                    addClass(e, hitClass);
                    
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("XPath [" + x + "] hit for field [" + field + 
                            "] on Element -> [" + hit + "]");
                    }
                } else {
                    // TODO: ?
                    
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("XPath [" + x + "] hit for field [" + field + 
                            "] on ? -> [" + hit + "]");
                    }
                }
            }
        }

        return transform(doc);
    }
    
    private Element addClass(Element e, String value) {
        String classAttribute = e.getAttribute("class");
        if (StringUtils.isEmpty(classAttribute)) {
            e.setAttribute("class", value);                    
        } else {
            if (!classAttribute.contains(value)) {
                e.setAttribute("class", value + " " + classAttribute);                
            }
        }
        return e;
    }

    List<String> htmlMediaTypes = Arrays.asList(
        "application/xhtml+xml", "text/html", "text/x-server-parsed-html");
    
    /**
     * Use an XML Stylesheet to perform the actual transformation.
     * @param doc
     * @return
     * @throws Exception
     */
    String transform(Document doc) throws Exception {
        Transformer transformer = stylesheet.newTransformer();
        StringWriter output = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(output));
        return output.toString();
    }

    static DocumentBuilderFactory builderFactory;
    static {
        builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(false);
        builderFactory.setValidating(false);
    }
    
    static TransformerFactory factory;
    static Templates stylesheet;
    static {
        factory = TransformerFactory.newInstance();
        try {
            stylesheet = factory.newTemplates(new StreamSource(
                new File("webapps/solrplugins/WEB-INF/classes/xplainer.xsl")));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    static XPathFactory xPathFactory;
    static {
        xPathFactory = XPathFactory.newInstance();
    }

}
