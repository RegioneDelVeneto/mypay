package it.regioneveneto.mygov.payment.nodoregionalefesp.utils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfigLoader {

	private static final Log logger = LogFactory.getLog(ConfigLoader.class);

	private Properties props = new Properties();
	private static ConfigLoader instance = null;

	private long time = 0;

	private ConfigLoader() {
		super();

		try {
			logger.debug("Read config file");
			File f = new File(ConfigLoader.class.getResource("/config.properties").getPath());
			time = f.lastModified();
			props.load(getClass().getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			logger.error("Non e stato possibile caricare il file di configurazione config.properties");
		}
	}

	public static ConfigLoader getInstance() {
		if (instance == null) {
			instance = new ConfigLoader();
		} else {
			File f = new File(ConfigLoader.class.getResource("/config.properties").getPath());
			if (f.lastModified() != instance.time) {
				logger.info("Reload config file " + f.getPath());
				instance = new ConfigLoader();
			}
		}

		return instance;
	}

	public String getProperty(String key) {
		return getProperty(key, null);
	}

	public String getProperty(String key, String defaultValue) {
		String out = props.getProperty(key);
		logger.debug("getProperty = " + key + " : " + out);
		if (out == null) {
			return defaultValue;
		}
		return out;
	}

}
