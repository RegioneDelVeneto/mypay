package it.regioneveneto.mygov.payment.nodoregionalefesp.dto.myprofile;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RolesDto {

	private String message;
	private List<RoleDto> resultRoles;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<RoleDto> getResultRoles() {
		return resultRoles;
	}

	public void setResultRoles(List<RoleDto> resultRoles) {
		this.resultRoles = resultRoles;
	}

	
}
