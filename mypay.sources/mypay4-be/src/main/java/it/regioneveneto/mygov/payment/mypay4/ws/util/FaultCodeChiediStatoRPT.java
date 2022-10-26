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

/**
 * @author Tamiazzo Igor
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
