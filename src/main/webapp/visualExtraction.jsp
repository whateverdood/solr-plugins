<%@page import="sandboxes.solrplugins.support.XPathVisualizer"%>
<%@page import="org.apache.commons.io.IOUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="org.apache.http.HttpEntity"%>
<%@page import="org.apache.http.HttpResponse"%>
<%@page import="org.apache.http.client.methods.HttpGet"%>
<%@page import="org.apache.http.impl.client.DefaultHttpClient"%>
<%@page import="org.apache.http.impl.client.cache.CachingHttpClient"%>
<%@page import="org.apache.http.client.HttpClient"%>
<%!
static final HttpClient httpClient = new CachingHttpClient(new DefaultHttpClient());
%>
<%
String expression = (String) request.getParameter("x");
String url = (String) request.getParameter("url");
String visualized = null;

if (!StringUtils.isEmpty(expression) && !StringUtils.isEmpty(url)) {
    HttpGet get = new HttpGet(url);
    HttpResponse got = httpClient.execute(get);
    int status = got.getStatusLine().getStatusCode();
    if (200 == status) {
        HttpEntity entityBody = got.getEntity();
        String resource = IOUtils.toString(entityBody.getContent());
        visualized = new XPathVisualizer().visualize(expression, resource);
    } else {
        visualized = "Darn: " + got.getStatusLine();
    }
} else {
    visualized = "Fill out the form!";
}
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>XPath Visualizer</title>
</head>
<body>
<form name="viz" method="get">
<label for="x">XPath expression:</label>
<input type="text" name="x" value="<%=expression%>" size="50" />
<label for="url">URL:</label>
<input type="text" name="url" value="<%=url%>" size="50" />
<input type="submit"></input>
</form>
<div class="visualization">
<%=visualized %>
</div>
</body>
</html>