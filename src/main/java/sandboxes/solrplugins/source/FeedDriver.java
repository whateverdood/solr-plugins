package sandboxes.solrplugins.source;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

public class FeedDriver {
	
	private static final Logger LOG = Logger.getLogger(FeedDriver.class);

	private static String solrUrl = "http://localhost:8983/solr/collection1";

	private static int commitWithinMsecs = 60000;

	private static SolrServer solrCluster = buildSolrServer();

	private static boolean noIndex = false;

	private static SolrServer buildSolrServer() {
		try {
			return new CommonsHttpSolrServer(solrUrl);
		} catch (MalformedURLException ex) {
			LOG.error("The supplied solrUrl was malformed, please verify.", ex);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();

		// create the command line parser
		CommandLineParser parser = new GnuParser();

		// create the Options
		Options options = new Options();
		options.addOption("n", "noIndex", false,
				"No index - download and parse only default: true.");
		options.addOption("h", "help", false, "Prints this help dialogue");

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("help")) {
				printHelp(options);
			} else if (line.hasOption("noIndex")) {
				noIndex = true;
			}

			List<String> parsedArgs = line.getArgList();
			if (parsedArgs.isEmpty()) {
				printHelp(options);
				System.exit(1);
			}

			for (String arg : parsedArgs) {
				SyndFeed syndFeed = new SyndFeedInput()
						.build(new InputStreamReader(new URL(arg).openStream()));

				for (SyndEntry entry : (List<SyndEntry>) syndFeed.getEntries()) {
					indexDoc(buildDoc(entry));
				}
			}

		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		} catch (SolrServerException ex) {
			System.out
					.println("SolrServerException detected, are you sure you have a solr instance running at location: "
							+ solrUrl);
		}
	}

	private static void indexDoc(SolrInputDocument doc)
			throws SolrServerException, IOException {
		if (noIndex) {
			Logger.getRootLogger().info("Not indexing: " + doc.toString());
		} else {
			Logger.getRootLogger().info("Indexing: " + doc.toString());
			UpdateRequest request = new UpdateRequest();
			request.setParam("update.chain", "docHandler");
			request.setCommitWithin(commitWithinMsecs);
			request.add(doc);
			request.process(solrCluster);
		}
	}

	private static SolrInputDocument buildDoc(SyndEntry entry) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("id", entry.getLink());
		doc.setField("uri", entry.getLink());
		return doc;
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"java " + FeedDriver.class.getName() + " [feed url]",
						"options",
						options,
						"Parses an Atom or RSS feed and submits entries to the cluster for indexing.",
						true);
	}
}
