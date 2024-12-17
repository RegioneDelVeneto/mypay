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
public class TipiVersamento extends BaseEntity {

  public static final String ALIAS = "FESP_TipiVersamento";
  public static final String FIELDS = ""+ALIAS+".id as FESP_TipiVersamento_id,"+ALIAS+".tipo_versamento as FESP_TipiVersamento_tipoVersamento"+
      ","+ALIAS+".iuv_codice_tipo_versamento_id as FESP_TipiVersamento_iuvCodiceTipoVersamentoId"+
      ","+ALIAS+".descrizione as FESP_TipiVersamento_descrizione";

  private Long id;
  private String tipoVersamento;
  private String iuvCodiceTipoVersamentoId;
  private String descrizione;

}
