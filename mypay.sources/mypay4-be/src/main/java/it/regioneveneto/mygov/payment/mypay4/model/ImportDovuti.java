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
public class ImportDovuti extends BaseEntity {

  public static final String ALIAS = "ImportDovuti";
  public static final String FIELDS =
      " "+ALIAS+".mygov_import_dovuti_id as ImportDovuti_mygovImportDovutiId"+
      ","+ALIAS+".version as ImportDovuti_version"+
      ","+ALIAS+".mygov_ente_id as ImportDovuti_mygovEnteId"+
      ","+ALIAS+".mygov_utente_id as ImportDovuti_mygovUtenteId"+
      ","+ALIAS+".mygov_anagrafica_stato_id as ImportDovuti_mygovAnagraficaStatoId"+
      ","+ALIAS+".cod_request_token as ImportDovuti_codRequestToken"+
      ","+ALIAS+".dt_creazione as ImportDovuti_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as ImportDovuti_dtUltimaModifica"+
      ","+ALIAS+".de_nome_file_scarti as ImportDovuti_deNomeFileScarti"+
      ","+ALIAS+".cod_errore as ImportDovuti_codErrore";

  private Long mygovImportDovutiId;
  private int version;
  @Nested(Ente.ALIAS)
  private Ente mygovEnteId;
  @Nested(Utente.ALIAS)
  private Utente mygovUtenteId;
  @Nested(AnagraficaStato.ALIAS)
  private AnagraficaStato mygovAnagraficaStatoId;
  private String codRequestToken;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  private String deNomeFileScarti;
  private String codErrore;
}
