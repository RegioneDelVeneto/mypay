/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author regione del veneto
 * @author regione del veneto
 * @author regione del veneto
 * 
 */
public class MessagesDto {

	private List<DynamicMessageDto> successMessages;
	private List<DynamicMessageDto> informationMessages;
	private List<DynamicMessageDto> warningMessages;
	private List<DynamicMessageDto> errorMessages;

	public MessagesDto() {
		super();
		successMessages = new ArrayList<DynamicMessageDto>();
		informationMessages = new ArrayList<DynamicMessageDto>();
		warningMessages = new ArrayList<DynamicMessageDto>();
		errorMessages = new ArrayList<DynamicMessageDto>();
	}

	/**
	 * @return the successMessages
	 */
	public List<DynamicMessageDto> getSuccessMessages() {
		return successMessages;
	}

	/**
	 * @param successMessages the successMessages to set
	 */
	public void setSuccessMessages(List<DynamicMessageDto> successMessages) {
		this.successMessages = successMessages;
	}

	/**
	 * @return the informationMessages
	 */
	public List<DynamicMessageDto> getInformationMessages() {
		return informationMessages;
	}

	/**
	 * @param informationMessages the informationMessages to set
	 */
	public void setInformationMessages(List<DynamicMessageDto> informationMessages) {
		this.informationMessages = informationMessages;
	}

	/**
	 * @return the warningMessages
	 */
	public List<DynamicMessageDto> getWarningMessages() {
		return warningMessages;
	}

	/**
	 * @param warningMessages the warningMessages to set
	 */
	public void setWarningMessages(List<DynamicMessageDto> warningMessages) {
		this.warningMessages = warningMessages;
	}

	/**
	 * @return the errorMessages
	 */
	public List<DynamicMessageDto> getErrorMessages() {
		return errorMessages;
	}

	/**
	 * @param errorMessages the errorMessages to set
	 */
	public void setErrorMessages(List<DynamicMessageDto> errorMessages) {
		this.errorMessages = errorMessages;
	}

}
