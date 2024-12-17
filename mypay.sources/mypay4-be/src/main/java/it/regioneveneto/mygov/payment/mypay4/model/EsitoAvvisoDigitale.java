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
import org.jdbi.v3.core.mapper.Nested;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovEsitoAvvisoDigitaleId")
public class EsitoAvvisoDigitale extends BaseEntity {

  public static final String ALIAS = "EsitoAvvisoDigitale";
  public static final String FIELDS = ""+ALIAS+".mygov_esito_avviso_digitale_id as EsitoAvvisoDigitale_mygovEsitoAvvisoDigitaleId"+
      ","+ALIAS+".version as EsitoAvvisoDigitale_version"+
      ","+ALIAS+".mygov_avviso_digitale_id as EsitoAvvisoDigitale_mygovAvvisoDigitaleId"+
      ","+ALIAS+".num_e_ad_esito_av_tipo_canale_esito as EsitoAvvisoDigitale_numEAdEsitoAvTipoCanaleEsito"+
      ","+ALIAS+".cod_e_ad_esito_av_id_canale_esito as EsitoAvvisoDigitale_codEAdEsitoAvIdCanaleEsito"+
      ","+ALIAS+".dt_e_ad_esito_av_data_esito as EsitoAvvisoDigitale_dtEAdEsitoAvDataEsito"+
      ","+ALIAS+".num_e_ad_esito_av_codice_esito as EsitoAvvisoDigitale_numEAdEsitoAvCodiceEsito"+
      ","+ALIAS+".de_e_ad_esito_av_desc_esito as EsitoAvvisoDigitale_deEAdEsitoAvDescEsito";

  private Long mygovEsitoAvvisoDigitaleId;
  private int version;
  @Nested(AvvisoDigitale.ALIAS)
  private AvvisoDigitale mygovAvvisoDigitaleId;
  private int numEAdEsitoAvTipoCanaleEsito;
  private String codEAdEsitoAvIdCanaleEsito;
  private Date dtEAdEsitoAvDataEsito;
  private Integer numEAdEsitoAvCodiceEsito;
  private String deEAdEsitoAvDescEsito;
}
