package sandboxes.solrplugins.support;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextUtil {

	protected static final ApplicationContext appContext = new ClassPathXmlApplicationContext(
			new String[] { "applicationContext.xml" });

	private ApplicationContextUtil() {
		//NOOP - Singleton
	};
	
	public static <T> T getBean(Class<T> clazz){
		return appContext.getBean(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String beanName){
		return (T) appContext.getBean("beanName");
	}

}
