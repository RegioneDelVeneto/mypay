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
import lombok.*;
import org.jdbi.v3.core.mapper.Nested;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygov_export_conservazione_id")
public class ExportConservazione extends BaseEntity {

  public static final String ALIAS = "ExportConservazione";
  public static final String FIELDS =
      " "+ALIAS+".mygov_export_conservazione_id as ExportConservazione_mygovExportConservazioneId"+
      ","+ALIAS+".version as ExportConservazione_version"+
      ","+ALIAS+".mygov_ente_id as ExportConservazione_mygovEnteId"+
      ","+ALIAS+".mygov_anagrafica_stato_id as ExportConservazione_mygovAnagraficaStatoId"+
      ","+ALIAS+".de_nome_file_generato as ExportConservazione_deNomeFileGenerato"+
      ","+ALIAS+".num_dimensione_file_generato as ExportConservazione_numDimensioneFileGenerato"+
      ","+ALIAS+".dt_inizio_estrazione as ExportConservazione_dtInizioEstrazione"+
      ","+ALIAS+".dt_fine_estrazione as ExportConservazione_dtFineEstrazione"+
      ","+ALIAS+".dt_creazione as ExportConservazione_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as ExportConservazione_dtUltimaModifica"+
      ","+ALIAS+".cod_request_token as ExportConservazione_codRequestToken"+
      ","+ALIAS+".mygov_utente_id as ExportConservazione_mygovUtenteId"+
     
      ","+ALIAS+".tipo_tracciato as ExportConservazione_tipoTracciato";

  private Long mygovExportConservazioneId;
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
  private String codRequestToken;
  @Nested(Utente.ALIAS)
  private Utente mygovUtenteId;

  private String tipoTracciato;
  
  
}
