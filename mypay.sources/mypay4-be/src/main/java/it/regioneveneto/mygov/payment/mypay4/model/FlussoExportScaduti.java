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

import it.regioneveneto.mygov.payment.mypay4.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.Nested;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlussoExportScaduti extends BaseEntity {

  public final static String ALIAS = "FlussoExportScaduti";
  public final static String FIELDS = ""+ALIAS+".mygov_flusso_export_scaduti_id as FlussoExport_mygovFlussoExportScadutiId" +
      ","+ALIAS+".version as FlussoExport_version" +
      ","+ALIAS+".mygov_anagrafica_stato_id as FlussoExport_mygovAnagraficaStatoId"+
      ","+ALIAS+".dt_creazione as FlussoExport_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as FlussoExport_dtUltimaModifica" +
      ","+ALIAS+".mygov_ente_id as FlussoExport_mygovEnteId"+
      ","+ALIAS+".de_nome_file as FlussoExport_deNomeFile" +
      ","+ALIAS+".num_pagamenti_scaduti as FlussoExport_numPagamentiScaduti"+
      ","+ALIAS+".iuf as FlussoExport_iuf"+
      ","+ALIAS+".dt_scadenza as FlussoExport_dtScadenza"+
      ","+ALIAS+".de_percorso_file as FlussoExport_dePercorsoFile" +
      ","+ALIAS+".cod_request_token as FlussoExport_codRequestToken" +
      ","+ALIAS+".tipi_dovuto as FlussoExport_tipiDovuto" +
      ","+ALIAS+".cod_errore as FlussoExport_codErrore";

  private Long mygovFlussoExportScadutiId;
  private int version;
  @Nested(AnagraficaStato.ALIAS)
  private AnagraficaStato mygovAnagraficaStatoId;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  @Nested(Ente.ALIAS)
  private Ente mygovEnteId;
  private String deNomeFile;
  private Integer numPagamentiScaduti;
  private String iuf;
  private Date dtScadenza;
  private String dePercorsoFile;
  private String codRequestToken;
  private String tipiDovuto;
  private String codErrore;
}
