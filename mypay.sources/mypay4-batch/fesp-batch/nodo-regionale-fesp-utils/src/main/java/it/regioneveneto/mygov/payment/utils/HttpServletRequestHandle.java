/**
 * 
 */
package it.regioneveneto.mygov.payment.utils;

import java.util.Stack;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;


/**
 * @author regione del veneto
 *
 */
public class HttpServletRequestHandle {

	/**
	 * 
	 */
	private static final Log logger = LogFactory.getLog(HttpServletRequestHandle.class);

	/**
	 * 
	 */
	private static ThreadLocal<Stack<HttpServletRequest>> threadLocalStackHttpServletRequest = new ThreadLocal<Stack<HttpServletRequest>>();

	/**
	 * 
	 */
	private HttpServletRequestHandle() {
		super();
	}

	/**
	 * @param profile
	 */
	public static void bind(final HttpServletRequest httpServletRequest) {
		Assert.notNull(httpServletRequest, "'httpServletRequestle' must not be null");

		Stack<HttpServletRequest> stackHttpServletRequest = threadLocalStackHttpServletRequest.get();
		if (stackHttpServletRequest == null) {
			stackHttpServletRequest = new Stack<HttpServletRequest>();
			threadLocalStackHttpServletRequest.set(stackHttpServletRequest);
		}

		stackHttpServletRequest.push(httpServletRequest);

		logger.debug("http servlet request binded to current thread");
	}

	/**
	 * 
	 */
	public static void unbind() {
		Stack<HttpServletRequest> stackHttpServletRequest = threadLocalStackHttpServletRequest.get();
		if (stackHttpServletRequest == null) {
			logger.error("http servlet request not binded to current thread");

			return;
		}

		if (stackHttpServletRequest.isEmpty())
			logger.error("http servlet request not binded to current thread");
		else {
			stackHttpServletRequest.pop();

			logger.debug("http servlet request unbinded from current thread");
		}

		if (stackHttpServletRequest.isEmpty())
			threadLocalStackHttpServletRequest.remove();
	}

	/**
	 * @return
	 */
	public static HttpServletRequest get() {
		Stack<HttpServletRequest> stackHttpServletRequest = threadLocalStackHttpServletRequest.get();
		if ((stackHttpServletRequest == null) || stackHttpServletRequest.isEmpty())
			return null;

		return stackHttpServletRequest.peek();
	}
}
