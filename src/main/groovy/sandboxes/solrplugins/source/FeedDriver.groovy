package sandboxes.solrplugins.source

import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Logger
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer
import org.apache.solr.client.solrj.request.UpdateRequest
import org.apache.solr.common.SolrInputDocument

import com.sun.syndication.io.SyndFeedInput

class FeedDriver {

    public static void main(String[] args) {
        
        BasicConfigurator.resetConfiguration()
        BasicConfigurator.configure()

        def cli = new groovy.util.CliBuilder(
            usage: "java ${FeedDriver.class.getName()} [feed url] ${System.getProperty('line.separator')}" +
            "Parses an Atom or RSS feed and submits entries to the cluster for indexing.",
            header: "options")
        cli.n(longOpt: "noIndex", "No index - download and parse only.")

        OptionAccessor options = cli.parse(args)
        if (!options || !args) {
            cli.usage()
            System.exit(1)
        }
        
        if (!options.arguments()) {
            throw new IllegalArgumentException("Nothing to do.")
        }

        SolrServer solr = new CommonsHttpSolrServer(
            "http://localhost:8983/solr/collection1")
        int commitWithinMsecs = 60000

        options.arguments().each { feed -> 
            def syndFeed = new SyndFeedInput().build(new InputStreamReader(
                new URL(feed).newInputStream()))
            syndFeed.entries.each { entry ->
                def req = new UpdateRequest()
                req.setCommitWithin(commitWithinMsecs)
                req.setParam("update.chain", "docHandler")
                
                def doc = new SolrInputDocument()
                doc.setField("id", entry.link)
                doc.setField("uri", entry.link)
                doc.setField("media-type", "text/html") // for now
                
                req.add(doc)
                
                if (options.n) {
                    Logger.getRootLogger().info("Not indexing: $doc")
                } else {
                    Logger.getRootLogger().info("Indexing: $doc")
                    req.process(solr)
                }
            }
        }
    }
}
