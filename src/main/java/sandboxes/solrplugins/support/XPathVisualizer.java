package sandboxes.solrplugins.support;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class XPathVisualizer {
    
    /**
     * Mark-up the parts of the supplied <code>xml</code> that match
     * <code>expression</code> in an XHTML div. 
     * @param expression XPath to visualize.
     * @param xml Well-formed XML.
     * @return An XHTML div containing the marked-up XML.
     * @throws Exception
     */
    public String visualize(String expression, String xml) throws Exception {
        
        Document doc = toDom(xml);

        XPath xpath = xPathFactory.newXPath();
        NodeList nodes = (NodeList) xpath.evaluate(
            expression, doc, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node hit = nodes.item(i);
            if (hit instanceof CharacterData) {
                // can't add an Attr to CharacterData types, so wrap it.
                Element hitSpan = doc.createElement("span");
                hitSpan.setAttribute("class", "hit");
                
                Node hitParent = hit.getParentNode();
                hitParent.removeChild(hit);
                hitParent.appendChild(hitSpan);
                hitSpan.appendChild(hit);
            } else if (hit instanceof Attr) {
                // TODO: handle this
            } else if (hit instanceof Element) {
                Element e = (Element) hit;
                String classAttribute = e.getAttribute("class");
                if (StringUtils.isEmpty(classAttribute)) {
                    e.setAttribute("class", "hit");                    
                } else {
                    e.setAttribute("class", "hit " + classAttribute);
                }
            } else {
                // TODO: ?
            }
        }

        return transform(doc);
    }

    private Document toDom(String xml) throws Exception {
        try {
            Transformer transformer = factory.newTransformer();
            XMLReader reader = new Parser();
            reader.setFeature("http://xml.org/sax/features/namespaces", false);
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
            DOMResult result = new DOMResult();
            InputSource source = new InputSource(new StringReader(xml));
            transformer.transform(new SAXSource(reader, source), result);
            return (Document) result.getNode();
        } catch (Exception e) {
            // *sniff* Something smells.
            InputSource inputSource = new InputSource(new StringReader(xml));
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document dom = builder.parse(inputSource);
            dom.normalize();
            return dom;
        }
    }

    /**
     * Use an XML Stylesheet to perform the actual transformation.
     * @param doc
     * @return
     * @throws Exception
     */
    String transform(Document doc) throws Exception {
        
        Transformer transformer = factory.newTransformer(
            new StreamSource(new StringReader(xsl)));
            
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
    static {
        factory = TransformerFactory.newInstance();
    }
    
    static XPathFactory xPathFactory;
    static {
        xPathFactory = XPathFactory.newInstance();
    }

    // TODO: pull this out
    static String xsl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
		"<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" + 
		"<xsl:output method=\"html\" version=\"1.0\" encoding=\"UTF-8\" indent=\"yes\" omit-xml-declaration=\"yes\"/>\n" + 
		"\n" + 
		"<xsl:strip-space elements=\"*\"/>\n" + 
		"\n" + 
		"<!-- identity template -->\n" + 
		"<xsl:template match=\"/\">\n" + 
		"    <div>\n" + 
		"        <style type=\"text/css\">\n" + 
		"        ul {\n" + 
		"            background-color: inherit;\n" + 
		"            color: Purple;\n" + 
		"            list-style: none;\n" + 
		"            margin-left: -1em;\n" + 
		"        }\n" + 
		"        li {\n" + 
		"            background-color: inherit;\n" + 
		"            display: inline;\n" + 
		"        }\n" + 
		"        span.tag {\n" + 
		"            color: Purple;\n" + 
		"        }\n" + 
		"        span.attr-name {\n" + 
		"            color: Red;\n" + 
		"        }\n" + 
		"        span.attr-value {\n" + 
		"            color: Blue;\n" + 
		"        }\n" + 
		"        span.text {\n" + 
		"            color: Black;\n" + 
		"        }\n" + 
		"        .hit {\n" + 
		"            background-color: LightGoldenRodYellow;\n" + 
		"        }\n" + 
		"        </style>\n" + 
		"        <xsl:apply-templates />\n" + 
		"    </div>\n" + 
		"</xsl:template>\n" + 
		"\n" + 
		"<xsl:template match=\"*\">\n" + 
		"    <ul>\n" + 
		"        <xsl:choose>\n" + 
		"            <xsl:when test=\"@class='hit'\">\n" + 
		"                <xsl:text disable-output-escaping=\"yes\">\n" + 
		"                    &lt;li class=\"hit\"&gt;\n" + 
		"                </xsl:text>\n" + 
		"            </xsl:when>\n" + 
		"            <xsl:otherwise>\n" + 
		"                <xsl:text disable-output-escaping=\"yes\">\n" + 
		"                    &lt;li&gt;\n" + 
		"                </xsl:text>\n" + 
		"            </xsl:otherwise>\n" + 
		"        </xsl:choose>\n" + 
		"        <span class=\"tag\">\n" + 
		"            <xsl:text>&lt;</xsl:text><xsl:value-of select=\"local-name(.)\"/><xsl:apply-templates select=\"@*[not(local-name(.)='class')]\"/><xsl:text>&gt;</xsl:text>\n" + 
		"        </span>\n" + 
		"        <xsl:apply-templates select=\"node()\"/>\n" + 
		"        <span class=\"tag\">\n" + 
		"            <xsl:text>&lt;/</xsl:text><xsl:value-of select=\"name()\"/><xsl:text>&gt;</xsl:text>\n" + 
		"        </span>\n" + 
		"        <xsl:text disable-output-escaping=\"yes\">\n" + 
		"            &lt;/li&gt;\n" + 
		"        </xsl:text>\n" + 
		"    </ul>\n" + 
		"</xsl:template>\n" + 
		"\n" + 
		"<xsl:template match=\"span[@class='hit']\">\n" + 
		"    <xsl:call-template name=\"text-hit\" select=\"text()\"/>\n" + 
		"</xsl:template>\n" + 
		"\n" + 
		"<xsl:template match=\"@*\">\n" + 
		"    <xsl:text> </xsl:text>\n" + 
		"    <span class=\"attr-name\"><xsl:value-of select=\"name()\"/></span>\n" + 
		"    <xsl:text>=\"</xsl:text><span class=\"attr-value\"><xsl:value-of select=\".\"/></span><xsl:text>\"</xsl:text>\n" + 
		"</xsl:template>\n" + 
		"\n" + 
		"<xsl:template name=\"text-hit\" match=\"text()\">\n" + 
		"    <span class=\"text hit\">\n" + 
		"        <xsl:value-of select=\".\"/>\n" + 
		"    </span>\n" + 
		"</xsl:template>\n" + 
		"\n" + 
		"<xsl:template match=\"text()\">\n" + 
		"    <span class=\"text\">\n" + 
		"        <xsl:value-of select=\".\"/>\n" + 
		"    </span>\n" + 
		"</xsl:template>\n" + 
		"\n" + 
		"</xsl:stylesheet>";

}
