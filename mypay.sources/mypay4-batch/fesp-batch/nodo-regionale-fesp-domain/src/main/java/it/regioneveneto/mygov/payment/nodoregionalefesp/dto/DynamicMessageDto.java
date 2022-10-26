/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dto;

import java.io.Serializable;

/**
 * @author regione del veneto
 * 
 */
public class DynamicMessageDto implements Serializable {

	/**
	 * 
	 */
	private String code;

	/**
	 * 
	 */
	private Object[] arguments;

	/**
	 * @param code
	 * @param arguments
	 */
	public DynamicMessageDto(final String code, final Object... arguments) {
		super();

		this.code = code;
		this.arguments = arguments;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the arguments
	 */
	public Object[] getArguments() {
		return arguments;
	}
}
