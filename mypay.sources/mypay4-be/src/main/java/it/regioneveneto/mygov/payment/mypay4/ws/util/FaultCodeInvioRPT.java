/**
 *     MyPay - Payment portal of Regione Veneto.
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.mygov.payment.mypay4.ws.util;

public enum FaultCodeInvioRPT {
	
	PPT_RPT_DUPLICATA("PPT_RPT_DUPLICATA"),
	PPT_SINTASSI_XSD("PPT_SINTASSI_XSD"),
	PPT_SINTASSI_EXTRAXSD("PPT_SINTASSI_EXTRAXSD"),
	PPT_AUTENTICAZIONE("PPT_AUTENTICAZIONE"),
	PPT_AUTORIZZAZIONE("PPT_AUTORIZZAZIONE"),
	PPT_SEMANTICA("PPT_SEMANTICA"),
	PPT_DOMINIO_SCONOSCIUTO("PPT_DOMINIO_SCONOSCIUTO"),
	PPT_DOMINIO_DISABILITATO("PPT_DOMINIO_DISABILITATO"),
	PPT_STAZIONE_INT_PA_SCONOSCIUTA("PPT_STAZIONE_INT_PA_SCONOSCIUTA"),
	PPT_STAZIONE_INT_PA_DISABILITATA("PPT_STAZIONE_INT_PA_DISABILITATA"),
	PPT_INTERMEDIARIO_PA_SCONOSCIUTO("PPT_INTERMEDIARIO_PA_SCONOSCIUTO"),
	PPT_INTERMEDIARIO_PA_DISABILITATO("PPT_INTERMEDIARIO_PA_DISABILITATO"),
	PPT_CANALE_SCONOSCIUTO("PPT_CANALE_SCONOSCIUTO"),
	PPT_CANALE_IRRAGGIUNGIBILE("PPT_CANALE_IRRAGGIUNGIBILE"),
	PPT_CANALE_SERVIZIO_NONATTIVO("PPT_CANALE_SERVIZIO_NONATTIVO"),
	PPT_CANALE_DISABILITATO("PPT_CANALE_DISABILITATO"),
	PPT_CANALE_NONRISOLVIBILE("PPT_CANALE_NONRISOLVIBILE"),
	PPT_CANALE_INDISPONIBILE("PPT_CANALE_INDISPONIBILE"),
	PPT_CANALE_ERR_PARAM_PAG_IMM("PPT_CANALE_ERR_PARAM_PAG_IMM"),
	PPT_INTERMEDIARIO_PSP_SCONOSCIUTO("PPT_INTERMEDIARIO_PSP_SCONOSCIUTO"),
	PPT_INTERMEDIARIO_PSP_DISABILITATO("PPT_INTERMEDIARIO_PSP_DISABILITATO"),
	PPT_PSP_SCONOSCIUTO("PPT_PSP_SCONOSCIUTO"),
	PPT_PSP_DISABILITATO("PPT_PSP_DISABILITATO"),
	PPT_SUPERAMENTOSOGLIA("PPT_SUPERAMENTOSOGLIA"),
	PPT_TIPOFIRMA_SCONOSCIUTO("PPT_TIPOFIRMA_SCONOSCIUTO"),
	PPT_ERRORE_FORMATO_BUSTA_FIRMATA("PPT_ERRORE_FORMATO_BUSTA_FIRMATA"),
	PPT_FIRMA_INDISPONIBILE("PPT_FIRMA_INDISPONIBILE"),
	PPT_CANALE_TIMEOUT("PPT_CANALE_TIMEOUT"),
	PPT_CANALE_ERRORE_RESPONSE("PPT_CANALE_ERRORE_RESPONSE"),
	PPT_CANALE_ERRORE("PPT_CANALE_ERRORE"),
	PPT_ESITO_SCONOSCIUTO("PPT_ESITO_SCONOSCIUTO"),
	PPT_SYSTEM_ERROR("PPT_SYSTEM_ERROR"),
	PPT_IBAN_NON_CENSITO("PPT_IBAN_NON_CENSITO");
	
	private final String faultCode;
	private String faultString;
	private String description;

	FaultCodeInvioRPT(String faultCode) {
		this.faultCode = faultCode;
	}
	
	FaultCodeInvioRPT(String faultCode, String faultString, String description) {
		this.faultCode = faultCode;
		this.faultString = faultString;
		this.description = description;
	}

	public String getFaultCode() {return faultCode;	}

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
