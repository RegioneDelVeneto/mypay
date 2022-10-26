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
package it.regioneveneto.mygov.payment.mypay4.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovEnteFunzionalitaId")
public class EnteFunzionalita extends BaseEntity {

  public final static String ALIAS = "EnteFunzionalita";
  public final static String FIELDS = ""+ALIAS+".mygov_ente_funzionalita_id as EnteFunzionalita_mygovEnteFunzionalitaId"+
      ","+ALIAS+".cod_ipa_ente as EnteFunzionalita_codIpaEnte,"+ALIAS+".cod_funzionalita as EnteFunzionalita_codFunzionalita"+
      ","+ALIAS+".flg_attivo as EnteFunzionalita_flgAttivo";

  private Long mygovEnteFunzionalitaId;
  private String codIpaEnte;
  private String codFunzionalita;
  private boolean flgAttivo;

  //calculated fields
  private Timestamp dtUltimaAbilitazione;
  private Timestamp dtUltimaDisabilitazione;

}
