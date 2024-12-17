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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovFlussoTassonomiaId")
public class FlussoTassonomia extends BaseEntity {

  public static final String ALIAS = "FlussoTassonomia";
  public static final String FIELDS = ""+ALIAS+".mygov_flusso_tassonomia_id as FlussoTassonomia_mygovFlussoTassonomiaId"+
      ","+ALIAS+".version as FlussoTassonomia_version"+
      ","+ALIAS+".mygov_anagrafica_stato_id as FlussoTassonomia_mygovAnagraficaStatoId,"+ALIAS+".iuft as FlussoTassonomia_iuft"+
      ","+ALIAS+".num_righe_totali as FlussoTassonomia_numRigheTotali"+
      ","+ALIAS+".num_righe_elaborate_correttamente as FlussoTassonomia_numRigheElaborateCorrettamente"+
      ","+ALIAS+".dt_creazione as FlussoTassonomia_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as FlussoTassonomia_dtUltimaModifica"+
      ","+ALIAS+".de_nome_operatore as FlussoTassonomia_deNomeOperatore"+
      ","+ALIAS+".de_percorso_file as FlussoTassonomia_dePercorsoFile,"+ALIAS+".de_nome_file as FlussoTassonomia_deNomeFile"+
      ","+ALIAS+".cod_request_token as FlussoTassonomia_codRequestToken,"+ALIAS+".cod_errore as FlussoTassonomia_codErrore"+
      ","+ALIAS+".hash as FlussoTassonomia_hash";

  private Long mygovFlussoTassonomiaId;
  private int version;
  @Nested(AnagraficaStato.ALIAS)
  private AnagraficaStato mygovAnagraficaStatoId;
  private String iuft;
  private Long numRigheTotali;
  private Long numRigheElaborateCorrettamente;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  private String deNomeOperatore;
  private String dePercorsoFile;
  private String deNomeFile;
  private String codRequestToken;
  private String codErrore;
  private String hash;
}
