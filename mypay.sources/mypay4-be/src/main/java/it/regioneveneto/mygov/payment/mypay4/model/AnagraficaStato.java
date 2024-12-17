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

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovAnagraficaStatoId")
public class AnagraficaStato extends BaseEntity {

  public static final String ALIAS = "AnagraficaStato";
  public static final String FIELDS = ""+ALIAS+".mygov_anagrafica_stato_id as AnagraficaStato_mygovAnagraficaStatoId"+
      ","+ALIAS+".cod_stato as AnagraficaStato_codStato,"+ALIAS+".de_stato as AnagraficaStato_deStato"+
      ","+ALIAS+".de_tipo_stato as AnagraficaStato_deTipoStato,"+ALIAS+".dt_creazione as AnagraficaStato_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as AnagraficaStato_dtUltimaModifica";


  public static final String STATO_ENTE_ESERCIZIO = "ESERCIZIO";
  public static final String STATO_ENTE_PRE_ESERCIZIO = "PRE-ESERCIZIO";

  public static final String STATO_TIPO_CARRELLO = "carrel";
  public static final String STATO_CARRELLO_PAGATO = "PAGATO";
  public static final String STATO_CARRELLO_NON_PAGATO = "NON_PAGATO";

  public static final String STATO_TIPO_DOVUTO = "dovuto";
  public static final String STATO_DOVUTO_DA_PAGARE = "INSERIMENTO_DOVUTO";
  public static final String STATO_DOVUTO_PAGAMENTO_INIZIATO = "PAGAMENTO_INIZIATO";
  public static final String STATO_DOVUTO_PREDISPOSTO = "PREDISPOSTO";
  public static final String STATO_DOVUTO_COMPLETATO = "COMPLETATO";
  public static final String STATO_DOVUTO_ANNULLATO = "ANNULLATO";
  public static final String STATO_DOVUTO_ERRORE = "ERROR_DOVUTO";
  public static final String STATO_DOVUTO_ABORT = "ABORT";
  public static final String STATO_DOVUTO_SCADUTO = "SCADUTO";
  public static final String STATO_DOVUTO_SCADUTO_ELABORATO = "SCADUTO_ELABORATO";
  public static final String STATO_TIPO_FLUSSO = "flusso";
  public static final String STATO_ENTE = "ente";

  private Long mygovAnagraficaStatoId;
  private String codStato;
  private String deStato;
  private String deTipoStato;
  private Timestamp dtCreazione;
  private Timestamp dtUltimaModifica;
}
