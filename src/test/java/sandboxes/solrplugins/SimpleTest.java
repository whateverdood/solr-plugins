package sandboxes.solrplugins;

import org.apache.log4j.BasicConfigurator;
import org.junit.Ignore;

@Ignore
public class SimpleTest {
    
    static {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }

}
