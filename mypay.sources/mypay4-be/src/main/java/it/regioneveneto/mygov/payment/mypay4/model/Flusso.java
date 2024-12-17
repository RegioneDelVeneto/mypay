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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovFlussoId")
public class Flusso extends BaseEntity {

  public static final String ALIAS = "Flusso";
  public static final String FIELDS = ""+ALIAS+".mygov_flusso_id as Flusso_mygovFlussoId,"+ALIAS+".version as Flusso_version"+
      ","+ALIAS+".mygov_ente_id as Flusso_mygovEnteId,"+ALIAS+".mygov_anagrafica_stato_id as Flusso_mygovAnagraficaStatoId"+
      ","+ALIAS+".iuf as Flusso_iuf,"+ALIAS+".num_righe_totali as Flusso_numRigheTotali"+
      ","+ALIAS+".num_righe_importate_correttamente as Flusso_numRigheImportateCorrettamente"+
      ","+ALIAS+".dt_creazione as Flusso_dtCreazione,"+ALIAS+".dt_ultima_modifica as Flusso_dtUltimaModifica"+
      ","+ALIAS+".flg_attivo as Flusso_flgAttivo,"+ALIAS+".de_nome_operatore as Flusso_deNomeOperatore"+
      ","+ALIAS+".flg_spontaneo as Flusso_flgSpontaneo,"+ALIAS+".de_percorso_file as Flusso_dePercorsoFile"+
      ","+ALIAS+".de_nome_file as Flusso_deNomeFile,"+ALIAS+".pdf_generati as Flusso_pdfGenerati"+
      ","+ALIAS+".cod_request_token as Flusso_codRequestToken,"+ALIAS+".cod_errore as Flusso_codErrore";


  private Long mygovFlussoId;
  private int version;
  @Nested(Ente.ALIAS)
  private Ente mygovEnteId;
  @Nested(AnagraficaStato.ALIAS)
  private AnagraficaStato mygovAnagraficaStatoId;
  private String iuf;
  private Long numRigheTotali;
  private Long numRigheImportateCorrettamente;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  private boolean flgAttivo;
  private String deNomeOperatore;
  private Boolean flgSpontaneo;
  private String dePercorsoFile;
  private String deNomeFile;
  private Long pdfGenerati;
  private String codRequestToken;
  private String codErrore;
}
