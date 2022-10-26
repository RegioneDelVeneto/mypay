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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovStandardTipoDovutoId")
public class StandardTipoDovuto extends BaseEntity {

  public final static String ALIAS = "StandardTipoDovuto";
  public final static String FIELDS = ""+ALIAS+".mygov_standard_tipo_dovuto_id as StandardTipoDovuto_mygovStandardTipoDovutoId"+
      ","+ALIAS+".cod_tipo as StandardTipoDovuto_codTipo"+
      ","+ALIAS+".de_tipo as StandardTipoDovuto_deTipo"+
      ","+ALIAS+".cod_xsd_causale as StandardTipoDovuto_codXsdCausale"+
      ","+ALIAS+".macro_area as StandardTipoDovuto_macroArea" +
      ","+ALIAS+".tipo_servizio as StandardTipoDovuto_tipoServizio"+
      ","+ALIAS+".motivo_riscossione as StandardTipoDovuto_motivoRiscossione"+
      ","+ALIAS+".cod_tassonomico as StandardTipoDovuto_codTassonomico";

  private Long mygovStandardTipoDovutoId;
  private String codTipo;
  private String deTipo;
  private String codXsdCausale;
  private String macroArea;
  private String tipoServizio;
  private String motivoRiscossione;
  private String codTassonomico;
}
