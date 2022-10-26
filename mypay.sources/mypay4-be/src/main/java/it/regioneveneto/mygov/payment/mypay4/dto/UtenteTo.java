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
package it.regioneveneto.mygov.payment.mypay4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UtenteTo extends BaseTo {

  private Long userId;
  private String username;
  private String codiceFiscale;
  private String email;
  private String emailNew;
  private Character emailSourceType;
  private String nome;
  private String cognome;
  private String indirizzo;
  private String civico;
  private String cap;
  private Long comuneId;
  private Long provinciaId;
  private Long nazioneId;
  private String comune;
  private String provincia;
  private String nazione;
  private String ruolo;
  private boolean flgAssociato;

  private LocalDateTime lastLogin;
  private String loginType;
  private String statoNascita;
  private String provinciaNascita;
  private String comuneNascita;
  private String dataNascita;

  private Map<String, Set<String>> entiRoles;
  private LocalDateTime dtUltimaAbilitazione;
  private LocalDateTime dtUltimaDisabilitazione;
  private boolean emailValidationNeeded;
}
