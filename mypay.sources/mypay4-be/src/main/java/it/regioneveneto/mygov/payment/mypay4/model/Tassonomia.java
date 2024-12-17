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

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovTassonomiaId")
public class Tassonomia extends BaseEntity {

  public static final String ALIAS = "Tassonomia";
  public static final String FIELDS = ""+ALIAS+".mygov_tassonomia_id as Tassonomia_mygovTassonomiaId,"+ALIAS+".version_my_pay as Tassonomia_versionMyPay"+
      ","+ALIAS+".tipo_ente as Tassonomia_tipoEnte,"+ALIAS+".descrizione_tipo_ente as Tassonomia_descrizioneTipoEnte"+
      ","+ALIAS+".prog_macro_area as Tassonomia_progMacroArea,"+ALIAS+".nome_macro_area as Tassonomia_nomeMacroArea"+
      ","+ALIAS+".desc_macro_area as Tassonomia_descMacroArea,"+ALIAS+".cod_tipo_servizio as Tassonomia_codTipoServizio"+
      ","+ALIAS+".tipo_servizio as Tassonomia_tipoServizio"+
      ","+ALIAS+".descrizione_tipo_servizio as Tassonomia_descrizioneTipoServizio"+
      ","+ALIAS+".motivo_riscossione as Tassonomia_motivoRiscossione"+
      ","+ALIAS+".dt_inizio_validita as Tassonomia_dtInizioValidita,"+ALIAS+".dt_fine_validita as Tassonomia_dtFineValidita"+
      ","+ALIAS+".codice_tassonomico as Tassonomia_codiceTassonomico,"+ALIAS+".dt_creazione as Tassonomia_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as Tassonomia_dtUltimaModifica,"+ALIAS+".version as Tassonomia_version";

  private Long mygovTassonomiaId;
  private int versionMyPay;  //workaround: properties added due to previous mistake on "version"
  private String tipoEnte;
  private String descrizioneTipoEnte;
  private String progMacroArea;
  private String nomeMacroArea;
  private String descMacroArea;
  private String codTipoServizio;
  private String tipoServizio;
  private String descrizioneTipoServizio;
  private String motivoRiscossione;
  private Date dtInizioValidita;
  private Date dtFineValidita;
  private String codiceTassonomico;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  private int version;
}
