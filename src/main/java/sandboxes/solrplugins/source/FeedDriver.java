package sandboxes.solrplugins.source;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.solr.client.solrj.SolrServerException;

public class FeedDriver {
	
	private static String solrUrl = "http://localhost:8983/solr/collection1";

	private static int commitWithinMsecs = 60000;

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
		options.addOption("s", "solrUrl", true, "SOLR node url");
		options.addOption("c", "commitWithinMsecs", true, "Commit within msecs");

		try {
            Feeder feeder = new Feeder();
            feeder.setCommitWithinMsecs(commitWithinMsecs);
            feeder.setSolrServerUrl(solrUrl);

            // parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("help")) {
				printHelp(options);
				System.exit(1);
			}
			if (line.hasOption("noIndex")) {
				feeder.setNoIndex(true);
			}
			if (line.hasOption('s')) {
	            feeder.setSolrServerUrl(line.getOptionValue('s'));
			}
			if (line.hasOption('c')) {
			    feeder.setCommitWithinMsecs(
			        Integer.parseInt(line.getOptionValue('c')));
			}

			List<String> parsedArgs = line.getArgList();
			if (parsedArgs.isEmpty()) {
				printHelp(options);
				System.exit(1);
			}
			
			for (String arg : parsedArgs) {
				feeder.indexFeed(arg);
			}

		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		} catch (SolrServerException ex) {
			System.out
					.println("SolrServerException detected, are you sure you have a solr instance running at location: "
							+ solrUrl);
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
		    "java " + FeedDriver.class.getName() + " [feed url1]+",
		    "options", options,
		    "Parses an Atom or RSS feed and submits entries to the cluster for indexing.",
		    true);
	}
}
