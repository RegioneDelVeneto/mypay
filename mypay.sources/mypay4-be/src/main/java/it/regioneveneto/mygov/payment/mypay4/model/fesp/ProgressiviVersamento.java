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
package it.regioneveneto.mygov.payment.mypay4.model.fesp;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import it.regioneveneto.mygov.payment.mypay4.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ProgressiviVersamento extends BaseEntity {

  public final static String ALIAS = "FESP_ProgressiviVersamento";
  public final static String FIELDS = ""+ALIAS+".id as FESP_ProgressiviVersamento_id,"+ALIAS+".version as FESP_ProgressiviVersamento_version"+
      ","+ALIAS+".cod_ipa_ente as FESP_ProgressiviVersamento_codIpaEnte"+
      ","+ALIAS+".tipo_generatore as FESP_ProgressiviVersamento_tipoGeneratore"+
      ","+ALIAS+".tipo_versamento as FESP_ProgressiviVersamento_tipoVersamento"+
      ","+ALIAS+".progressivo_versamento as FESP_ProgressiviVersamento_progressivoVersamento";

  private Long id;
  private int version;
  private String codIpaEnte;
  private String tipoGeneratore;
  private String tipoVersamento;
  private Long progressivoVersamento;
}
