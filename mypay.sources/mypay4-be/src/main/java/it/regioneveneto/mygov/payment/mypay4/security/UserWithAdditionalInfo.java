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

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Getter
@ToString(callSuper = true)
@Builder(toBuilder = true)
public class UserWithAdditionalInfo implements UserDetails {

  private final String phoneNumber;
  private final String username;
  private final String userAlias;
  private final String firstName;
  private final String familyName;
  private final String email;
  private final String emailNew;
  private final char emailSourceType;
  private final String codiceFiscale;
  private final String userId;

  private final boolean federaAuthorized;
  private final String legalEntity;
  private final String nazione;
  private final String provincia;
  private final String comune;
  private final String cap;
  private final String indirizzo;
  private final String civico;

  private final String authenticatingAuthority;
  private final String authenticationMethod;
  private final String statoNascita;
  private final String provinciaNascita;
  private final String comuneNascita;
  private final String dataNascita;

  private final boolean sysAdmin;
  //authorized list of "ente" (key) and roles for each "ente" (value)
  private final Map<String, Set<String>> entiRoles;

  private final Collection<GrantedAuthority> authorities = Collections.emptyList();

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return "$2a$10$Y8zuzAtbwHbVacVLLBK/ve3Fb0veCvNBJppMUG9XtOLky5SaGLyHq"; //password: "thePassword"
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
