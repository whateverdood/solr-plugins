<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> 
<%@ page import="sandboxes.solrplugins.source.*" %>
<%@ page import="org.apache.commons.lang.StringUtils"%>
<%
final StringBuilder flash = new StringBuilder("");
final String url = (String) request.getParameter("url");
if (!StringUtils.isEmpty(url)) {
    Runnable run = new Runnable() {
        public void run() {
            try {
                Feeder feeder = new Feeder();
                feeder.setCommitWithinMsecs(60*1000);
                feeder.setSolrServerUrl("http://0.0.0.0:8983/solr/collection1");
                feeder.indexFeed(url);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    };
    new Thread(run).start();
    flash.append("\"" + url + "\" submitted for indexing.");
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Index an ATOM or RSS Feed</title>
        <link rel="stylesheet" type="text/css" href="/solr/css/screen.css">
        <style type="text/css">
        body {
            text-align: left;
        }
        .title {
            font-size: 16pt;
            font-weight: bold;
        }
        .description {
        }
        .feed-form {
            text-align: center;
        }
        .flash {
            background-color: aliceblue;
            border: 1px solid lightblue;
            padding: 0.2em;
        }
        .help {
            font-style: italic;
        }
        div {
            margin: 0.5em 0;
        }
        ul {
            list-style-type: disc;
            list-style-position: inside;
        }
        a {
            text-decoration: underline;
        }
        </style>
    </head>
    <body>
        <div class="title">solr-plugins: Index a Feed via "docHandler"</div>
        <%
        if (!StringUtils.isEmpty(flash.toString())) {%>
            <div class="flash"><%=flash%></div><%
        }
        %>
        <div class="description">
            <p>Use this page to download an ATOM or RSS feed and index the 
            articles referenced therein. Article docs are sent to the 
            <a href="/solr/#/singlecore/config" target="_blank">"docHandler"</a> 
            custom update request processor chain.</p>
        </div>
        <div class="feed-form">
            <form method="GET">
                <label for="url">Feed URL:</label>
                <input type="text" name="url" value="<%=url%>" size="80">
                <input type="submit" name="submit" value="Go">
            </form>
        </div>
        <div class="help">
            <p>If you don't have any candidate feeds you may click on any of the 
            following to index them:</p> 
            <ul>
                <li><a href="?url=http%3A%2F%2Fen.blog.wordpress.com%2Ffeed%2F">http://en.blog.wordpress.com/feed/</a></li>
                <li><a href="?url=http%3A%2F%2Ffreshlypressed.wordpress.com%2Ffeed%2F">http://freshlypressed.wordpress.com/feed/</a></li>
                <li><a href="?url=http%3A%2F%2Ffeeds.gawker.com%2Fgawker%2Ffull">http://feeds.gawker.com/gawker/full</a></li>
                <li><a href="?url=http%3A%2F%2Ffeeds.gawker.com%2Fgizmodo%2Ffull">http://feeds.gawker.com/gizmodo/full</a></li>
            </ul>
        </div>
    </body>
</html>