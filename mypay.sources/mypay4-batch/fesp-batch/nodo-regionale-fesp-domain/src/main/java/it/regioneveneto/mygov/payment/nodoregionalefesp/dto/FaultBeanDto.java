package it.regioneveneto.mygov.payment.nodoregionalefesp.dto;

public class FaultBeanDto {

	// FAULT BEAN
	private String faultId;
	private String faultCode;
	private String faultString;
	private String faultDescription;
	private Integer faultSerial;

	public String getFaultId() {
		return faultId;
	}

	public void setFaultId(String faultId) {
		this.faultId = faultId;
	}

	public String getFaultCode() {
		return faultCode;
	}

	public void setFaultCode(String faultCode) {
		this.faultCode = faultCode;
	}

	public String getFaultString() {
		return faultString;
	}

	public void setFaultString(String faultString) {
		this.faultString = faultString;
	}

	public String getFaultDescription() {
		return faultDescription;
	}

	public void setFaultDescription(String faultDescription) {
		this.faultDescription = faultDescription;
	}

	public Integer getFaultSerial() {
		return faultSerial;
	}

	public void setFaultSerial(Integer faultSerial) {
		this.faultSerial = faultSerial;
	}

}
