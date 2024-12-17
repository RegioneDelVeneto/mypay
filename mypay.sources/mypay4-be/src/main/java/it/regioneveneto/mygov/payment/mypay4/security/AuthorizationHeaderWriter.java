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
package it.regioneveneto.mygov.payment.mypay4.security;

import io.jsonwebtoken.Claims;
import it.regioneveneto.mygov.payment.mypay4.storage.JwtTokenUsageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.AbstractMap;

@Component
@Slf4j
@ConditionalOnWebApplication
public class AuthorizationHeaderWriter implements HeaderWriter {

	@Value("${jwt.rolling-token.enabled:true}")
	private boolean rollingTokenEnabled;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	JwtTokenUsageStorage jwtTokenUsageService;

	@Override
	public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {

		if(response.containsHeader(JwtRequestFilter.AUTHORIZATION_HEADER))
			return;

		boolean removeCookie = request.getAttribute(JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_ATTRIB)!=null &&
				!JwtAuthenticationEntryPoint.NOT_REMOVE_AUTH.contains((String)request.getAttribute(JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_ATTRIB)) &&
				jwtTokenUtil.isTokenInCookie();

		boolean forceUpdateToken = request.getAttribute(JwtRequestFilter.FORCE_TOKEN_UPDATE_ATTRIBUTE)!=null;

		if((forceUpdateToken || rollingTokenEnabled) && !removeCookie) {
			Claims claims = (Claims) request.getAttribute(JwtRequestFilter.CLAIMS_ATTRIBUTE);
			//generate a new auth token (rolling token)
			if (claims != null && !wasTokenAlreadyRolled(claims.getId())) {
				String newToken = jwtTokenUtil.generateToken(claims.getSubject(), claims);
				AbstractMap.SimpleImmutableEntry<String, String> authHeader = jwtTokenUtil.generateAuthorizationHeader(newToken);
				response.setHeader(authHeader.getKey(), authHeader.getValue());
				log.debug("rolling the authorization token");
			}
		}
		if(removeCookie){
			//remove auth cookie in case the jwt token is invalid/expired
			AbstractMap.SimpleImmutableEntry<String, String> authHeader = jwtTokenUtil.generateCookieRemovalHeader();
			response.setHeader(authHeader.getKey(), authHeader.getValue());
			log.debug("removed authentication cookie because it was: "+request.getAttribute(JwtAuthenticationEntryPoint.TOKEN_ERROR_CODE_ATTRIB));
		}
	}

	private boolean wasTokenAlreadyRolled(String jti) {
		Long rolledAt = jwtTokenUsageService.wasTokenRolled(jti);
		log.debug("wasTokenAlreadyRolled: " + jti + " : " + rolledAt);
		if (rolledAt == null) {
			jwtTokenUsageService.markTokenRolled(jti);
			return false;
		} else {
			return true;
		}
	}
}
