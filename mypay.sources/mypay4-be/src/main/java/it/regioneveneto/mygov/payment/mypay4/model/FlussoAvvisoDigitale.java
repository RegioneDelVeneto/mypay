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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovFlussoAvvisoDigitaleId")
public class FlussoAvvisoDigitale extends BaseEntity {

  public static final String ALIAS = "FlussoAvvisoDigitale";
  public static final String FIELDS = ""+ALIAS+".mygov_flusso_avviso_digitale_id as FlussoAvvisoDigitale_mygovFlussoAvvisoDigitaleId"+
      ","+ALIAS+".version as FlussoAvvisoDigitale_version,"+ALIAS+".cod_fad_id_dominio as FlussoAvvisoDigitale_codFadIdDominio"+
      ","+ALIAS+".cod_fad_id_flusso as FlussoAvvisoDigitale_codFadIdFlusso"+
      ","+ALIAS+".mygov_anagrafica_stato_id as FlussoAvvisoDigitale_mygovAnagraficaStatoId"+
      ","+ALIAS+".cod_fad_tipo_flusso as FlussoAvvisoDigitale_codFadTipoFlusso"+
      ","+ALIAS+".cod_fad_e_presa_in_carico_id_flusso as FlussoAvvisoDigitale_codFadEPresaInCaricoIdFlusso"+
      ","+ALIAS+".num_fad_e_presa_in_carico_cod_e_presa_in_carico as FlussoAvvisoDigitale_numFadEPresaInCaricoCodEPresaInCarico"+
      ","+ALIAS+".de_fad_e_presa_in_carico_desc_e_presa_in_carico as FlussoAvvisoDigitale_deFadEPresaInCaricoDescEPresaInCarico"+
      ","+ALIAS+".de_fad_file_path as FlussoAvvisoDigitale_deFadFilePath"+
      ","+ALIAS+".de_fad_filename as FlussoAvvisoDigitale_deFadFilename"+
      ","+ALIAS+".num_fad_dimensione_file as FlussoAvvisoDigitale_numFadDimensioneFile"+
      ","+ALIAS+".num_fad_num_avvisi_nel_flusso as FlussoAvvisoDigitale_numFadNumAvvisiNelFlusso"+
      ","+ALIAS+".dt_creazione as FlussoAvvisoDigitale_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as FlussoAvvisoDigitale_dtUltimaModifica";

  private Long mygovFlussoAvvisoDigitaleId;
  private int version;
  private String codFadIdDominio;
  private String codFadIdFlusso;
  @Nested(AnagraficaStato.ALIAS)
  private AnagraficaStato mygovAnagraficaStatoId;
  private String codFadTipoFlusso;
  private String codFadEPresaInCaricoIdFlusso;
  private Integer numFadEPresaInCaricoCodEPresaInCarico;
  private String deFadEPresaInCaricoDescEPresaInCarico;
  private String deFadFilePath;
  private String deFadFilename;
  private Long numFadDimensioneFile;
  private Integer numFadNumAvvisiNelFlusso;
  private Date dtCreazione;
  private Date dtUltimaModifica;
}
