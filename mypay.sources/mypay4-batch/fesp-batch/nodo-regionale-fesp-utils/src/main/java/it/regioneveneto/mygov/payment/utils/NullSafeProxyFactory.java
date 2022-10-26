/**
 * 
 */
package it.regioneveneto.mygov.payment.utils;

import java.lang.reflect.Proxy;

/**
 * @author regione del veneto
 *
 */
public class NullSafeProxyFactory {

	/**
	 * @param interfaces
	 * @param target
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProxy(final Class<T> interfaces, final Object target) {
		ClassLoader classLoader = null;

		if (target == null)
			classLoader = Thread.currentThread().getContextClassLoader();
		else
			classLoader = target.getClass().getClassLoader();

		return (T) Proxy.newProxyInstance(classLoader, new Class[] { interfaces }, new NullSafeProxy<Object>(target));
	}

	/**
	 * @param proxy
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getTarget(final T proxy) {
		return ((NullSafeProxy<T>) proxy).getTarget();
	}
}
