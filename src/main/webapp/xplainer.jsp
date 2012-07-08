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
        
        visualized = new XPlainer().xplain(
            xctor.getForEach(), xpaths, rawContent, mediaType);

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
    url = "http://www.samharris.org"; // good metadata here
}
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>XPlainer (XPath visualization of extracted metadata)</title>
        <script type="text/javascript" src="jquery-1.7.2.min.js"></script>
        <script type="text/javascript" src="xplainer.js"></script>
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
            .fieldRevealer {
                color: blue;
                cursor: pointer;
                text-decoration: underline;
            }
        </style>
    </head>
    <body>
    <h3></h3>
    <form name="viz" method="get">
        <label for="url">Visualize the metadata extraction for URL:</label>
        <input type="text" name="url" value="<%=url%>" size="70" onforminput="document.getElementById('submit').removeAttribute('disabled');" />
        <input type="hidden" name="submitted" value="yes" />
        <input type="submit" value="Show Me" disabled="disabled" id="submit" />
    </form>
    <table>
    <tr>
        <th>Extracted fields</th>
        <th>...for resource: <%=url%></th>
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
            }

            String xpath = "";
            if (null != extractor) {
                // YIKES this is kind of smelly!
                xpath = xpathRoot + "/" + mappings.get(field);
                xpath = xpath.replaceAll(" \\| ", " | " + xpathRoot + "/");
                xpath = xpath.replaceAll("h:", ""); // *sigh*
            }
            %>
            <dt title="<%=xpath%>">
                <span class="fieldRevealer" id="<%=field%>"><%=field%></span>
            </dt>
            <dd title="<%=xpath%>"><%=StringEscapeUtils.escapeHtml(doc.getFieldValue(field).toString())%></dd><%
        }%>
        </dl>
    <%    
    }%>
    </td>
    <td class="rhs visualization">
        <%=visualized %>
    </td>
    </tr>
    </table>    
    </body>
</html>