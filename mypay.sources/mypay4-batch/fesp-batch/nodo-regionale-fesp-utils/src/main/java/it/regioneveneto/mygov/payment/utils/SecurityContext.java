/**
 * 
 */
package it.regioneveneto.mygov.payment.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import it.regioneveneto.mygov.payment.nodoregionalefesp.to.UtenteTO;

/**
 * @author regione del veneto
 * 
 */
public class SecurityContext {

	/**
	 * 
	 */
	private static final String UTENTE_SESSION_KEY = "utente";
	
	private static final String ROLES_SESSION_KEY = "ROLES_SESSION_KEY";
	
	/**
	 * 
	 */
	private static final String FEDERA_PROFILE_SESSION_KEY = "it.regioneveneto.mygov.payment.pa.web.filter.federaProfile";

	/**
	 * 
	 */
	private static final Log logger = LogFactory.getLog(SecurityContext.class);

	private static final String ALL_ENTI_SESSION_KEY = "ALL_ENTI_SESSION_KEY";

	/**
	 * 
	 */
	private SecurityContext() {
		super();
	}

	/**
	 * @return
	 */
	public static synchronized UtenteTO getUtente() {
		return (UtenteTO) getSecurityObject(UTENTE_SESSION_KEY);
	}

	/**
	 * @param utente
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static synchronized void setUtente(final UtenteTO utente) {
		setSecurityObject(UTENTE_SESSION_KEY, utente);
	}
	
	/**
	 * @return
	 */
	public static synchronized List<String> getRoles() {
		return (List<String>) getSecurityObject(ROLES_SESSION_KEY);
	}

	/**
	 * @param utente
	 */
	public static synchronized void setRoles(final List<String> roles) {
		setSecurityObject(ROLES_SESSION_KEY, roles);
	}

	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static synchronized Map<String, List<String>> getFederaUserProfile() {
		Map<String, List<String>> securityObject = (Map<String, List<String>>) getSecurityObject(FEDERA_PROFILE_SESSION_KEY);
		return securityObject;
	}

	/**
	 * @param securityObjectSessionKey
	 * @return
	 */
	private static Object getSecurityObject(final String securityObjectSessionKey) {
		Object securityObject = null;

		HttpServletRequest request = HttpServletRequestHandle.get();
		if (request != null) {
			HttpSession session = request.getSession();
			if (session != null) {
				synchronized (session) {
					securityObject = session.getAttribute(securityObjectSessionKey);
				}
			}
		}

		if (securityObject == null) {
			logger.debug("securityObject [" + securityObjectSessionKey + "] not binded to current session");

			return null;
		}

		return securityObject;
	}

	/**
	 * @param securityObjectSessionKey
	 * @param securityObject
	 */
	private static void setSecurityObject(final String securityObjectSessionKey, final Object securityObject) {
		HttpServletRequest request = HttpServletRequestHandle.get();
		if (request == null) {
			logger.error("http servlet request not binded to current thread");

			throw new RuntimeException("http servlet request not binded to current thread");
		}

		HttpSession session = request.getSession(true);
		synchronized (session) {
			if (securityObject == null) {
				session.removeAttribute(securityObjectSessionKey);

				logger.debug("securityObject [" + securityObjectSessionKey + "] unbinded from current session");
			} else {
				session.setAttribute(securityObjectSessionKey, securityObject);

				logger.debug("securityObject [" + securityObjectSessionKey + "] binded to current session");
			}
		}
	}
}
