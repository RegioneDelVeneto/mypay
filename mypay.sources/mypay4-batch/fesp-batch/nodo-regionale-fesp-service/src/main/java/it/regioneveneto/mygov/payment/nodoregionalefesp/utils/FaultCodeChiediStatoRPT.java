/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.utils;

/**
 * @author regione del veneto
 *
 */
public enum FaultCodeChiediStatoRPT {

	PPT_RPT_SCONOSCIUTA("PPT_RPT_SCONOSCIUTA"), 
	PPT_SINTASSI_EXTRAXSD("PPT_SINTASSI_EXTRAXSD"), 
	PPT_SEMANTICA("PPT_SEMANTICA"), 
	PPT_AUTENTICAZIONE("PPT_AUTENTICAZIONE"), 
	PPT_AUTORIZZAZIONE("PPT_AUTORIZZAZIONE"), 
	PPT_DOMINIO_SCONOSCIUTO("PPT_DOMINIO_SCONOSCIUTO"), 
	PPT_DOMINIO_DISABILITATO("PPT_DOMINIO_DISABILITATO"), 
	PPT_INTERMEDIARIO_PA_SCONOSCIUTO("PPT_INTERMEDIARIO_PA_SCONOSCIUTO"), 
	PPT_INTERMEDIARIO_PA_DISABILITATO("PPT_INTERMEDIARIO_PA_DISABILITATO"), 
	PPT_STAZIONE_INT_PA_SCONOSCIUTA("PPT_STAZIONE_INT_PA_SCONOSCIUTA"), 
	PPT_STAZIONE_INT_PA_DISABILITATA("PPT_STAZIONE_INT_PA_DISABILITATA"), 
	PPT_SUPERAMENTOSOGLIA("PPT_SUPERAMENTOSOGLIA");

	private final String faultCode;
	private String faultString;
	private String description;

	FaultCodeChiediStatoRPT(String faultCode) {
		this.faultCode = faultCode;
	}
	
	FaultCodeChiediStatoRPT(String faultCode, String faultString, String description) {
		this.faultCode = faultCode;
		this.faultString = faultString;
		this.description = description;
	}

	public String getFaultCode() {
		return faultCode;
	}

	public String getFaultString() {
		return faultString;
	}

	public String getDescription() {
		return description;
	}

	public void setFaultString(String faultString) {
		this.faultString = faultString;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
