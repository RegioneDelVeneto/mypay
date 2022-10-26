package it.regioneveneto.mygov.payment.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * @author regione del veneto
 *
 */
public class PropertiesUtil extends PropertyPlaceholderConfigurer {

	@SuppressWarnings("rawtypes")
	private Map propertiesMap;

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#processProperties(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.util.Properties)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
		super.processProperties(beanFactory, props);

		this.propertiesMap = new HashMap<String, String>();

		for (Object key : props.keySet()) {
			String keyStr = key.toString();

			this.propertiesMap.put(keyStr, parseStringValue(props.getProperty(keyStr), props, new HashSet()));
		}
	}

	/**
	 * @param name
	 * @return
	 */
	public String getProperty(String name) {
		return (String) this.propertiesMap.get(name);
	}
}
