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

import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteSil;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface EnteSilDao extends BaseDao {

  @SqlUpdate(
      " insert into mygov_ente_sil (" +
          "  mygov_ente_sil_id" +
          " ,mygov_ente_id" +
          " ,nome_applicativo" +
          " ,de_url_inoltro_esito_pagamento_push" +
          " ,cod_service_account_jwt_entrata_client_id" +
          " ,de_service_account_jwt_entrata_client_mail" +
          " ,cod_service_account_jwt_entrata_secret_key_id" +
          " ,cod_service_account_jwt_entrata_secret_key" +
          " ,cod_service_account_jwt_uscita_client_id" +
          " ,de_service_account_jwt_uscita_client_mail" +
          " ,cod_service_account_jwt_uscita_secret_key_id" +
          " ,cod_service_account_jwt_uscita_secret_key" +
          " ,flg_jwt_attivo" +
          " ,mygov_ente_tipo_dovuto_id" +
          ") values ( "+
          " nextval('mygov_ente_sil_mygov_ente_sil_id_seq') "+
          " ,:d.mygovEnteId.mygovEnteId" +
          " ,:d.nomeApplicativo" +
          " ,:d.deUrlInoltroEsitoPagamentoPush" +
          " ,:d.codServiceAccountJwtEntrataClientId" +
          " ,:d.deServiceAccountJwtEntrataClientMail" +
          " ,:d.codServiceAccountJwtEntrataSecretKeyId" +
          " ,:d.codServiceAccountJwtEntrataSecretKey" +
          " ,:d.codServiceAccountJwtUscitaClientId" +
          " ,:d.deServiceAccountJwtUscitaClientMail" +
          " ,:d.codServiceAccountJwtUscitaSecretKeyId" +
          " ,:d.codServiceAccountJwtUscitaSecretKey" +
          " ,:d.flgJwtAttivo" +
          " ,:d.mygovEnteTipoDovutoId?.mygovEnteTipoDovutoId)")
  @GetGeneratedKeys("mygov_ente_sil_id")
  long insert(@BindBean("d") EnteSil d);

  @SqlUpdate(
      "delete from mygov_ente_sil where mygov_ente_sil_id = :mygovEnteSilId"
  )
  int delete(Long mygovEnteSilId);

  @SqlUpdate(
      "update mygov_ente_sil set " +
          "  mygov_ente_id = :d.mygovEnteId.mygovEnteId" +
          " ,nome_applicativo = :d.nomeApplicativo " +
          " ,de_url_inoltro_esito_pagamento_push = :d.deUrlInoltroEsitoPagamentoPush" +
          " ,cod_service_account_jwt_entrata_client_id = :d.codServiceAccountJwtEntrataClientId " +
          " ,de_service_account_jwt_entrata_client_mail = :d.deServiceAccountJwtEntrataClientMail" +
          " ,cod_service_account_jwt_entrata_secret_key_id = :d.codServiceAccountJwtEntrataSecretKeyId " +
          " ,cod_service_account_jwt_entrata_secret_key = :d.codServiceAccountJwtEntrataSecretKey" +
          " ,cod_service_account_jwt_uscita_client_id = :d.codServiceAccountJwtUscitaClientId" +
          " ,de_service_account_jwt_uscita_client_mail = :d.deServiceAccountJwtUscitaClientMail" +
          " ,cod_service_account_jwt_uscita_secret_key_id = :d.codServiceAccountJwtUscitaSecretKeyId" +
          " ,cod_service_account_jwt_uscita_secret_key = :d.codServiceAccountJwtUscitaSecretKey" +
          " ,flg_jwt_attivo = :d.flgJwtAttivo" +
          " ,mygov_ente_tipo_dovuto_id = :d.mygovEnteTipoDovutoId?.mygovEnteTipoDovutoId" +
      " where mygov_ente_sil_id = :d.mygovEnteSilId")
  int update(@BindBean("d") EnteSil d);

  @SqlQuery(
      "select "+EnteSil.ALIAS+ALL_FIELDS+", "+Ente.FIELDS+", "+EnteTipoDovuto.FIELDS +
          " from mygov_ente_sil "+EnteSil.ALIAS +
          " inner join mygov_ente "+Ente.ALIAS +
          "    on "+Ente.ALIAS+".mygov_ente_id = "+EnteSil.ALIAS+".mygov_ente_id" +
          " left join mygov_ente_tipo_dovuto "+EnteTipoDovuto.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = "+EnteSil.ALIAS+".mygov_ente_tipo_dovuto_id" +
          " where "+EnteSil.ALIAS+".mygov_ente_sil_id = :mygovEnteSilId"
  )
  @RegisterFieldMapper(EnteSil.class)
  EnteSil getById(Long mygovEnteSilId);

  @SqlQuery(
      "select "+EnteSil.ALIAS+ALL_FIELDS+", "+Ente.FIELDS+", "+EnteTipoDovuto.FIELDS +
          " from mygov_ente_sil "+EnteSil.ALIAS +
          " inner join mygov_ente "+Ente.ALIAS +
          "    on "+Ente.ALIAS+".mygov_ente_id = "+EnteSil.ALIAS+".mygov_ente_id" +
          "  left join mygov_ente_tipo_dovuto "+EnteTipoDovuto.ALIAS +
          "    on "+EnteTipoDovuto.ALIAS+".mygov_ente_tipo_dovuto_id = "+EnteSil.ALIAS+".mygov_ente_tipo_dovuto_id" +
          " where "+Ente.ALIAS+".mygov_ente_id = :mygovEnteId" +
          "   and ( (:codTipoDovuto is null and "+EnteSil.ALIAS+".mygov_ente_tipo_dovuto_id is null" +
          "         or "+EnteTipoDovuto.ALIAS+".cod_tipo = :codTipoDovuto )" +
          "      or "+EnteSil.ALIAS+".mygov_ente_tipo_dovuto_id is null )" +
          " order by "+EnteSil.ALIAS+".mygov_ente_tipo_dovuto_id NULLS LAST"
  )
  @RegisterFieldMapper(EnteSil.class)
  List<EnteSil> getByEnteTipoDovuto(Long mygovEnteId, String codTipoDovuto);

}
