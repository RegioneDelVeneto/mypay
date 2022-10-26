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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovImportDovutiId")
public class ExportDovuti extends BaseEntity {

  public final static String ALIAS = "ExportDovuti";
  public final static String FIELDS =
      " "+ALIAS+".mygov_export_dovuti_id as ExportDovuti_mygovExportDovutiId"+
      ","+ALIAS+".version as ExportDovuti_version"+
      ","+ALIAS+".mygov_ente_id as ExportDovuti_mygovEnteId"+
      ","+ALIAS+".mygov_anagrafica_stato_id as ExportDovuti_mygovAnagraficaStatoId"+
      ","+ALIAS+".de_nome_file_generato as ExportDovuti_deNomeFileGenerato"+
      ","+ALIAS+".num_dimensione_file_generato as ExportDovuti_numDimensioneFileGenerato"+
      ","+ALIAS+".dt_inizio_estrazione as ExportDovuti_dtInizioEstrazione"+
      ","+ALIAS+".dt_fine_estrazione as ExportDovuti_dtFineEstrazione"+
      ","+ALIAS+".dt_creazione as ExportDovuti_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as ExportDovuti_dtUltimaModifica"+
      ","+ALIAS+".cod_tipo_dovuto as ExportDovuti_codTipoDovuto"+
      ","+ALIAS+".cod_request_token as ExportDovuti_codRequestToken"+
      ","+ALIAS+".mygov_utente_id as ExportDovuti_mygovUtenteId"+
      ","+ALIAS+".flg_ricevuta as ExportDovuti_flgRicevuta"+
      ","+ALIAS+".flg_incrementale as ExportDovuti_flgIncrementale"+
      ","+ALIAS+".versione_tracciato as ExportDovuti_versioneTracciato";

  private Long mygovExportDovutiId;
  private int version;
  @Nested(Ente.ALIAS)
  private Ente mygovEnteId;
  @Nested(AnagraficaStato.ALIAS)
  private AnagraficaStato mygovAnagraficaStatoId;
  private String deNomeFileGenerato;
  private Long numDimensioneFileGenerato;
  private Date dtInizioEstrazione;
  private Date dtFineEstrazione;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  private String codTipoDovuto;
  private String codRequestToken;
  @Nested(Utente.ALIAS)
  private Utente mygovUtenteId;
  private boolean flgRicevuta;
  private boolean flgIncrementale;
  private String versioneTracciato;
}
