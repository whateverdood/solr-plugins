package sandboxes.solrplugins;

import org.junit.Assert;
import org.junit.Test;

public class SpringProcessorTest {

	@Test
	public void testBinding() {
		TestSpringProcessor processor = new TestSpringProcessor(null);
		
		Assert.assertTrue("Spring binding isn't working :(", processor.isBindingWorking());
	}
}
