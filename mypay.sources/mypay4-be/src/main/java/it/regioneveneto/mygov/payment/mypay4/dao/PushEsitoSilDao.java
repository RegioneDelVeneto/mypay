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
import java.util.Optional;

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

  @SqlQuery( "select " + PushEsitoSil.ALIAS + ".mygov_push_esito_sil_id" +
      " from mygov_push_esito_sil " + PushEsitoSil.ALIAS +
      " join mygov_dovuto_elaborato "+ DovutoElaborato.ALIAS +
      "   on " + PushEsitoSil.ALIAS +".mygov_dovuto_elaborato_id = " + DovutoElaborato.ALIAS +".mygov_dovuto_elaborato_id " +
      " join mygov_flusso "+ Flusso.ALIAS +
      "   on " + DovutoElaborato.ALIAS +".mygov_flusso_id = " + Flusso.ALIAS +".mygov_flusso_id " +
      " join mygov_ente_tipo_dovuto "+ EnteTipoDovuto.ALIAS +
      "   on " + EnteTipoDovuto.ALIAS +".cod_tipo = " + DovutoElaborato.ALIAS +".cod_tipo_dovuto " +
      "  and " + EnteTipoDovuto.ALIAS +".mygov_ente_id = " + Flusso.ALIAS +".mygov_ente_id " +
      "where " + PushEsitoSil.ALIAS +".flg_esito_invio_push = false " +
      "  and " + PushEsitoSil.ALIAS +".num_tentativi_effettuati < " + EnteTipoDovuto.ALIAS +".max_tentativi_inoltro_esito " +
      "  and " + EnteTipoDovuto.ALIAS +".mygov_ente_sil_id is not null " )
  List<Long> findNotificationIdsToSend();

  @SqlQuery("select " + PushEsitoSil.ALIAS + ALL_FIELDS +", " + DovutoElaborato.FIELDS +
      " from mygov_push_esito_sil " + PushEsitoSil.ALIAS +
      " join mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
      " on "+PushEsitoSil.ALIAS +".mygov_dovuto_elaborato_id = " + DovutoElaborato.ALIAS +".mygov_dovuto_elaborato_id "+
      " where "+PushEsitoSil.ALIAS+".mygov_push_esito_sil_id = :idPushEsitoSil ")
  @RegisterFieldMapper(PushEsitoSil.class)
  PushEsitoSil getById(Long idPushEsitoSil);

  @SqlQuery("select " + PushEsitoSil.ALIAS + ALL_FIELDS + ", " + DovutoElaborato.FIELDS +
      ", " + Flusso.ALIAS+".mygov_ente_id as nested_mygov_ente_id " +
      ", " + DovutoElaborato.ALIAS+".mygov_carrello_id as nested_mygov_carrello_id " +
      ", " + EnteTipoDovuto.ALIAS +".max_tentativi_inoltro_esito as nested_max_tentativi_inoltro_esito " +
      " from mygov_push_esito_sil " + PushEsitoSil.ALIAS +
      " join mygov_dovuto_elaborato " + DovutoElaborato.ALIAS +
      "   on " +PushEsitoSil.ALIAS + ".mygov_dovuto_elaborato_id = " + DovutoElaborato.ALIAS + ".mygov_dovuto_elaborato_id " +
      " join mygov_flusso " + Flusso.ALIAS +
      "   on " + DovutoElaborato.ALIAS + ".mygov_flusso_id = " + Flusso.ALIAS +".mygov_flusso_id " +
      " join mygov_ente_tipo_dovuto "+ EnteTipoDovuto.ALIAS +
      "   on " + EnteTipoDovuto.ALIAS +".cod_tipo = " + DovutoElaborato.ALIAS +".cod_tipo_dovuto " +
      "  and " + EnteTipoDovuto.ALIAS +".mygov_ente_id = " + Flusso.ALIAS +".mygov_ente_id " +
      " where " +PushEsitoSil.ALIAS + ".mygov_push_esito_sil_id = :idPushEsitoSil " +
      "   and " + PushEsitoSil.ALIAS + ".flg_esito_invio_push = false " +
      "   and " + PushEsitoSil.ALIAS +".num_tentativi_effettuati < " + EnteTipoDovuto.ALIAS +".max_tentativi_inoltro_esito " +
      " for update skip locked " )
  @RegisterFieldMapper(PushEsitoSil.class)
  Optional<PushEsitoSil> getToSendByIdLockOrSkip(Long idPushEsitoSil);

  @SqlQuery("select " + PushEsitoSil.ALIAS + ALL_FIELDS +
    " from mygov_push_esito_sil " + PushEsitoSil.ALIAS +
    " where "+PushEsitoSil.ALIAS +".mygov_dovuto_elaborato_id = :mygovDovutoElaboratoId ")
  @RegisterFieldMapper(PushEsitoSil.class)
  Optional<PushEsitoSil> getByDovutoElaboratoId(Long mygovDovutoElaboratoId);

  @SqlUpdate("update mygov_push_esito_sil " +
      "set dt_ultimo_tentativo = :p.dtUltimoTentativo" +
      " , num_tentativi_effettuati = :p.numTentativiEffettuati" +
      " , flg_esito_invio_push = :p.flgEsitoInvioPush" +
      " , cod_esito_invio_fault_code = :p.codEsitoInvioFaultCode" +
      " , de_esito_invio_fault_description = :p.deEsitoInvioFaultDescription" +
      " where mygov_push_esito_sil_id = :p.mygovPushEsitoSilId")
  int updatePushEsitoSil(@BindBean("p") PushEsitoSil p);
}
