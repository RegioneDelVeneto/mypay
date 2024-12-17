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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "comuneId")
public class Comune extends BaseEntity {

  public static final String ALIAS = "Comune";
  public static final String FIELDS = ""+ALIAS+".comune_id as Comune_comuneId,"+ALIAS+".comune as Comune_comune,"+ALIAS+".provincia_id as Comune_provinciaId"+
      ","+ALIAS+".sigla_provincia as Comune_siglaProvincia,"+ALIAS+".cod_belfiore as Comune_codBelfiore"+
      ","+ALIAS+".codice_istat as Comune_codiceIstat,"+ALIAS+".var_cod_belfiore as Comune_varCodBelfiore"+
      ","+ALIAS+".var_provincia as Comune_varProvincia,"+ALIAS+".var_comune as Comune_varComune";

  private Long comuneId;
  private String comune;
  private Long provinciaId;
  private String siglaProvincia;
  private String codBelfiore;
  private String codiceIstat;
  private String varCodBelfiore;
  private String varProvincia;
  private String varComune;
}
