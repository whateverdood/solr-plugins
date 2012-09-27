package sandboxes.solrplugins.support;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

/**
 * This class will interrogate all fields within the subclass to see if Spring's
 * {@link Autowired} annotation is found. If it is, it will use reflection to
 * grab the bean out of the appContext and place it in the field.
 */
public abstract class SpringRequestProcessor extends UpdateRequestProcessor {

	private static final Logger LOG = Logger
			.getLogger(SpringRequestProcessor.class.getName());

	public SpringRequestProcessor(UpdateRequestProcessor next) {
		super(next);

		for (Field field : this.getClass().getDeclaredFields()) {
			LOG.info("Found field ");
			if (field.getAnnotation(Autowired.class) != null) {
				Object bean = ApplicationContextUtil.getBean(field.getType());
				if(bean != null){
					boolean accessible = field.isAccessible();
					field.setAccessible(true);
					ReflectionUtils.setField(field, this, bean);
					field.setAccessible(accessible);
					LOG.fine("Bound " + field.getType().getSimpleName() + " bean to field [" + field.getName() + "]");
				} else {
					LOG.warning("Unable to bind bean of type [" + field.getType().getSimpleName() + "] to field [" + field.getName() + "]");
				}
			}

		}
	}
}
