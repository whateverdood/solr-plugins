package sandboxes.solrplugins.source;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;


import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

public class Feeder {

    protected String solrServerUrl;
    protected int commitWithinMsecs;
    protected SolrServer solrCluster;
    protected boolean noIndex;

    public void setSolrServerUrl(String solrUrl) throws Exception {
        this.solrServerUrl = solrUrl;
    }

    public void setCommitWithinMsecs(int commitWithinMsecs) {
        this.commitWithinMsecs = commitWithinMsecs;
    }
    
    public void setNoIndex(boolean noIndex) {
        this.noIndex = noIndex;
    }

    @SuppressWarnings("unchecked")
    public void indexFeed(String url) throws Exception {
        SyndFeed syndFeed = new SyndFeedInput().build(new InputStreamReader(new URL(url)
            .openStream()));

        for (SyndEntry entry : (List<SyndEntry>) syndFeed.getEntries()) {
            indexDoc(buildDoc(entry));
        }
    }

    private void indexDoc(SolrInputDocument doc) throws Exception {
        if (noIndex) {
            Logger.getAnonymousLogger().info("Not indexing: " + doc.toString());
        } else {
            Logger.getAnonymousLogger().info("Indexing: " + doc.toString());
            UpdateRequest request = new UpdateRequest();
            request.setParam("update.chain", "docHandler");
            request.setCommitWithin(commitWithinMsecs);
            request.add(doc);
            request.process(getSolrCluster());
        }
    }

    private SolrServer getSolrCluster() throws Exception {
        if (null == solrCluster) {
            solrCluster = new CommonsHttpSolrServer(this.solrServerUrl);
        }
        return solrCluster;
    }

    private SolrInputDocument buildDoc(SyndEntry entry) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", entry.getLink());
        doc.setField("uri", entry.getLink());
        return doc;
    }

}
