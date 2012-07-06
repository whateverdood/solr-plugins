<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.apache.commons.codec.net.URLCodec"%>
<%@page import="com.googlecode.shawty.XPathExtractor"%>
<%@page import="sandboxes.solrplugins.*"%>
<%@page import="sandboxes.solrplugins.support.XPlainer"%>
<%@page import="org.apache.commons.io.*"%>
<%@page import="org.apache.commons.lang.*"%>
<%@page import="org.apache.solr.common.*"%>
<%@page import="java.net.URL"%>
<%@page import="java.util.*"%>
<%!
static final DownloadingUpdateProcessorFactory dlFactory = 
    new DownloadingUpdateProcessorFactory();
static final ExtractingUpdateProcessorFactory xFactory = 
    new ExtractingUpdateProcessorFactory();
List<String> dontDisplay = Arrays.asList(new String[] {
    "body", "text", "raw-content", "media-type", "uri"
});
%>
<%
String submitted = (String) request.getParameter("submitted");
String expression = (String) request.getParameter("x");
String url = (String) request.getParameter("url");

String visualized = null;
SolrInputDocument doc = null;

if (!StringUtils.isEmpty(submitted)) {
    if (!StringUtils.isEmpty(url)) {
        DownloadingProcessor downloader = 
            (DownloadingProcessor) dlFactory.getInstance(null, null, null);
        byte[] resource = downloader.download(new URL(url));
        
        String rawContent = new String(resource).toLowerCase();
        String mediaType = downloader.detectContentType(resource);
        
        XPathExtractor xctor = Extractors.getExtractor(mediaType);
        Map<String, String> xpaths = xctor.getFieldMappings();
        
        if (!StringUtils.isEmpty(expression)) {
            visualized = new XPlainer().xplain(
                xctor.getForEach(), xpaths, rawContent, mediaType);
        }

        ExtractingProcessor extractor = 
            (ExtractingProcessor) xFactory.getInstance(null, null, null);
        
        doc = new SolrInputDocument();
        doc.setField("raw-content", resource);
        doc.setField("media-type", mediaType);
        doc.setField("uri", url);
        doc = extractor.extractData(doc);
    } else {
        visualized = "Fill out the form!";
    }    
} else {
    expression = "//text()";
    url = "http://en.blog.wordpress.com/feed/";
}
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>XPlainer (XPath visualization)</title>
        <script src="jquery-1.7.2.min.js" type="text/javascript"></script>
        <style type="text/css">
            html, body {
                margin: 0 auto;
                padding: 0 1em;
            }
            table {
                width: 100%;
            }
            tr {
            }
            td {
                vertical-align: top;
            }
            .lhs {
                border-top: 1px solid #ccc;
            }
            .rhs {
                border-left: 1px solid #ccc;
                border-top: 1px solid #ccc;
                width: 66%;
            }
        </style>
    </head>
    <body>
    <h3></h3>
    <form name="viz" method="get">
        <label for="x">Active XPath expression:</label>
        <input type="text" name="x" value="<%=expression%>" size="70" onchange="document.getElementById('submit').removeAttribute('disabled');" />
        <label for="url">URL:</label>
        <input type="text" name="url" value="<%=url%>" size="70" onchange="document.getElementById('submit').removeAttribute('disabled');" />
        <input type="hidden" name="submitted" value="yes" />
        <input type="submit" value="Show Me" disabled="disabled" id="submit" />
    </form>
    <table>
    <tr>
        <th>Extracted Fields</th>
        <th>Resource: <%=url%></th>
    </tr>
    <tr>
    <td class="lhs">
    <%
    if (doc != null) {
        XPathExtractor extractor = Extractors.getExtractor(
            doc.getFieldValue("media-type").toString());
        String xpathRoot = (String) extractor.getForEach();
        Map<String, String> mappings = (Map<String, String>) extractor.getFieldMappings();
        URLCodec codec = new URLCodec();
        %>
        <dl><%
        for (String field : new TreeSet<String>(doc.keySet())) {
            if (dontDisplay.contains(field)) {
                continue;
            }%>
            <dt>
            <%
            if (null != extractor) {
                // YIKES this kind of blows
                String xpath = xpathRoot + "/" + mappings.get(field);
                xpath = xpath.replaceAll(" \\| ", " | " + xpathRoot + "/");
                xpath = xpath.replaceAll("h:", ""); // *sigh*
                String template = "?x={expression}&url={url}&submitted=yes";
                template = template.replace("{expression}", codec.encode(xpath));
                template = template.replace("{url}", codec.encode(url));
                %>
                <a href="<%=template%>" title="<%=xpath%>"><%=field%></a>
                <%
            } else {%>
                <%=field%><%
            }
            %>
            </dt>
            <dd><%=StringEscapeUtils.escapeHtml(doc.getFieldValue(field).toString())%></dd><%
        }%>
        </dl>
    <%    
    }%>
    </td>
    <td class="rhs" class="visualization">
        <%=visualized %>
    </td>
    </tr>
    </table>    
    </body>
</html>