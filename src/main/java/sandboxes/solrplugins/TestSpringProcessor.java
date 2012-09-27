package sandboxes.solrplugins;

import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import sandboxes.solrplugins.component.SpringBean;
import sandboxes.solrplugins.support.SpringRequestProcessor;

public class TestSpringProcessor extends SpringRequestProcessor {
	
	@Autowired
	SpringBean bean;

	public TestSpringProcessor(UpdateRequestProcessor next) {
		super(next);
	}
	
	public boolean isBindingWorking(){
		return bean.isWorking();
	}

}
