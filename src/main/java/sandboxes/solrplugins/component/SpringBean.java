package sandboxes.solrplugins.component;

import java.util.logging.Logger;

import org.springframework.stereotype.Component;

@Component
public class SpringBean {

	private static final Logger LOG = Logger.getLogger(SpringBean.class.getName());
	
	public SpringBean(){
		LOG.info("********************* HEY LOOK! Spring got wired up! *********************");
	}
	
	public boolean isWorking(){
		LOG.info("It's alive muhahaha");
		return true;
	}
}
