/**
 * 
 */
package it.regioneveneto.mygov.payment.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author regione del veneto
 *
 */
public class NullSafeProxy<T> implements InvocationHandler {
	/**
	 * 
	 */
	private T target;

	/**
	 * @param target
	 * @param classLoader
	 */
	public NullSafeProxy(final T target) {
		super();

		this.target = target;
	}

	/**
	 * @return
	 */
	public T getTarget() {
		return this.target;
	}

	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if (this.target == null)
			return NullSafeProxyFactory.getProxy(method.getReturnType(), null);

		return NullSafeProxyFactory.getProxy(method.getReturnType(), method.invoke(this.target, args));
	}
}
