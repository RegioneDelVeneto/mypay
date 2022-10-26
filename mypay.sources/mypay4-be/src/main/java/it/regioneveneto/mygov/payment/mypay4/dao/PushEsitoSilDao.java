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
package it.regioneveneto.mygov.payment.mypay4.dao;

import it.regioneveneto.mygov.payment.mypay4.model.DovutoElaborato;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.model.Flusso;
import it.regioneveneto.mygov.payment.mypay4.model.PushEsitoSil;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface PushEsitoSilDao extends BaseDao{

  @SqlUpdate("INSERT INTO mygov_push_esito_sil (" +
      "mygov_push_esito_sil_id, " +
      "mygov_dovuto_elaborato_id, " +
      "dt_creazione, " +
      "dt_ultimo_tentativo, " +
      "num_tentativi_effettuati, " +
      "flg_esito_invio_push, " +
      "cod_esito_invio_fault_code, " +
      "de_esito_invio_fault_description" +
      ") VALUES (" +
      "nextval('mygov_push_esito_sil_mygov_push_esito_sil_id_seq') " +
      ", :p.mygovDovutoElaboratoId?.mygovDovutoElaboratoId" +
      ", :p.dtCreazione" +
      ", :p.dtUltimoTentativo" +
      ", :p.numTentativiEffettuati" +
      ", :p.flgEsitoInvioPush" +
      ", :p.codEsitoInvioFaultCode" +
      ", :p.deEsitoInvioFaultDescription)")
  @GetGeneratedKeys("mygov_push_esito_sil_id")
  Long insertNewPushEsitoSil(@BindBean("p") PushEsitoSil p);

  @SqlQuery("select " + PushEsitoSil.ALIAS + ALL_FIELDS +
      "from mygov_push_esito_sil " + PushEsitoSil.ALIAS +", " +
      "     mygov_dovuto_elaborato "+ DovutoElaborato.ALIAS +", " +
      "     mygov_flusso "+ Flusso.ALIAS +", " +
      "     mygov_ente_tipo_dovuto "+ EnteTipoDovuto.ALIAS+
      " where " + PushEsitoSil.ALIAS +".flg_esito_invio_push = false "+
      " and " + PushEsitoSil.ALIAS +".mygov_dovuto_elaborato_id = " + DovutoElaborato.ALIAS +".mygov_dovuto_elaborato_id "+
      " and " + DovutoElaborato.ALIAS +".mygov_flusso_id = " + Flusso.ALIAS +".mygov_flusso_id "+
      " and " + EnteTipoDovuto.ALIAS +".cod_tipo = " + DovutoElaborato.ALIAS +".cod_tipo_dovuto "+
      " and " + EnteTipoDovuto.ALIAS +".mygov_ente_id = " + Flusso.ALIAS +".mygov_ente_id "+
      " and " + PushEsitoSil.ALIAS +".num_tentativi_effettuati < " + EnteTipoDovuto.ALIAS +".max_tentativi_inoltro_esito "+
      " and " + EnteTipoDovuto.ALIAS +".mygov_ente_sil_id is not null ")
  @RegisterFieldMapper(PushEsitoSil.class)
  List<PushEsitoSil> getEsitiToSend();

  @SqlQuery("select " + PushEsitoSil.ALIAS + ALL_FIELDS +", " + DovutoElaborato.FIELDS +
      " from mygov_push_esito_sil " + PushEsitoSil.ALIAS +
      " join mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
      " on "+PushEsitoSil.ALIAS +".mygov_dovuto_elaborato_id = " + DovutoElaborato.ALIAS +".mygov_dovuto_elaborato_id "+
      " where "+PushEsitoSil.ALIAS+".mygov_push_esito_sil_id = :idPushEsitoSil ")
  @RegisterFieldMapper(PushEsitoSil.class)
  PushEsitoSil getById(Long idPushEsitoSil);

  @SqlUpdate("update mygov_push_esito_sil " +
      "set mygov_dovuto_elaborato_id = :p.mygovDovutoElaboratoId?.mygovDovutoElaboratoId" +
      " , dt_creazione = :p.dtCreazione" +
      " , dt_ultimo_tentativo = :p.dtUltimoTentativo" +
      " , num_tentativi_effettuati = :p.numTentativiEffettuati" +
      " , flg_esito_invio_push = :p.flgEsitoInvioPush" +
      " , cod_esito_invio_fault_code = :p.codEsitoInvioFaultCode" +
      " , de_esito_invio_fault_description = :p.deEsitoInvioFaultDescription)")
  int updatePushEsitoSil(@BindBean("p") PushEsitoSil p);
}
