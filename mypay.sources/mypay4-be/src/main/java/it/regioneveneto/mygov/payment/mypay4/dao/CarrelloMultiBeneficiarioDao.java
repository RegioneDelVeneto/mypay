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

import it.regioneveneto.mygov.payment.mypay4.model.AnagraficaStato;
import it.regioneveneto.mygov.payment.mypay4.model.CarrelloMultiBeneficiario;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

import java.util.List;
import java.util.Optional;

public interface CarrelloMultiBeneficiarioDao extends BaseDao {
  @SqlUpdate(
      "insert into mygov_carrello_multi_beneficiario (" +
          "  mygov_carrello_multi_beneficiario_id" +
          ", version" +
          ", mygov_anagrafica_stato_id" +
          ", cod_ipa_ente" +
          ", cod_ack_carrello_rp" +
          ", dt_creazione" +
          ", dt_ultima_modifica" +
          ", id_session_carrello" +
          ", id_session_carrellofesp" +
          ", risposta_pagamento_url" +
          ", de_rp_silinviacarrellorp_esito" +
          ", cod_rp_silinviacarrellorp_url" +
          ", cod_rp_silinviacarrellorp_fault_code" +
          ", de_rp_silinviacarrellorp_fault_string" +
          ", cod_rp_silinviacarrellorp_id" +
          ", de_rp_silinviacarrellorp_description" +
          ", cod_rp_silinviacarrellorp_serial" +
          ", cod_rp_silinviacarrellorp_original_fault_code" +
          ", de_rp_silinviacarrellorp_original_fault_string" +
          ", de_rp_silinviacarrellorp_original_fault_description" +
          ") values (" +
          "  nextval('mygov_carrello_multi_beneficiario_id_seq')" +
          ", :c.version" +
          ", :c.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
          ", :c.codIpaEnte" +
          ", :c.codAckCarrelloRp" +
          ", :c.dtCreazione" +
          ", :c.dtUltimaModifica" +
          ", :c.idSessionCarrello" +
          ", :c.idSessionCarrellofesp" +
          ", :c.rispostaPagamentoUrl" +
          ", :c.deRpSilinviacarrellorpEsito" +
          ", :c.codRpSilinviacarrellorpUrl" +
          ", :c.codRpSilinviacarrellorpFaultCode" +
          ", :c.deRpSilinviacarrellorpFaultString" +
          ", :c.codRpSilinviacarrellorpId" +
          ", :c.deRpSilinviacarrellorpDescription" +
          ", :c.codRpSilinviacarrellorpSerial" +
          ", :c.codRpSilinviacarrellorpOriginalFaultCode" +
          ", :c.deRpSilinviacarrellorpOriginalFaultString" +
          ", :c.deRpSilinviacarrellorpOriginalFaultDescription)"
  )
  @GetGeneratedKeys("mygov_carrello_multi_beneficiario_id")
  Long insert(@BindBean("c") CarrelloMultiBeneficiario c);


  @SqlQuery("select " + CarrelloMultiBeneficiario.ALIAS + ALL_FIELDS +", "+AnagraficaStato.FIELDS+
      " from mygov_carrello_multi_beneficiario " + CarrelloMultiBeneficiario.ALIAS +
      " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+ CarrelloMultiBeneficiario.ALIAS+".mygov_anagrafica_stato_id " +
      " where " + CarrelloMultiBeneficiario.ALIAS + ".mygov_carrello_multi_beneficiario_id = :id"
  )
  @RegisterFieldMapper(CarrelloMultiBeneficiario.class)
  CarrelloMultiBeneficiario getById(Long id);

  @SqlQuery("select " + CarrelloMultiBeneficiario.ALIAS + ALL_FIELDS +", "+AnagraficaStato.FIELDS+
      " from mygov_carrello_multi_beneficiario " + CarrelloMultiBeneficiario.ALIAS +
      " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+ CarrelloMultiBeneficiario.ALIAS+".mygov_anagrafica_stato_id " +
      " where " + CarrelloMultiBeneficiario.ALIAS + ".id_session_carrello = :idSession"
  )
  @RegisterFieldMapper(CarrelloMultiBeneficiario.class)
  List<CarrelloMultiBeneficiario> getByIdSession(String idSession);

  @SqlUpdate("update mygov_carrello_multi_beneficiario" +
      " set version = :c.version"+
      " , mygov_anagrafica_stato_id = :c.mygovAnagraficaStatoId.mygovAnagraficaStatoId"+
      " , cod_ipa_ente = :c.codIpaEnte"+
      " , cod_ack_carrello_rp = :c.codAckCarrelloRp"+
      " , dt_creazione = :c.dtCreazione"+
      " , dt_ultima_modifica = :c.dtUltimaModifica"+
      " , id_session_carrello = :c.idSessionCarrello"+
      " , id_session_carrellofesp = :c.idSessionCarrellofesp"+
      " , risposta_pagamento_url = :c.rispostaPagamentoUrl"+
      " , de_rp_silinviacarrellorp_esito = :c.deRpSilinviacarrellorpEsito"+
      " , cod_rp_silinviacarrellorp_url = :c.codRpSilinviacarrellorpUrl"+
      " , cod_rp_silinviacarrellorp_fault_code = :c.codRpSilinviacarrellorpFaultCode"+
      " , de_rp_silinviacarrellorp_fault_string = :c.deRpSilinviacarrellorpFaultString"+
      " , cod_rp_silinviacarrellorp_id = :c.codRpSilinviacarrellorpId"+
      " , de_rp_silinviacarrellorp_description = :c.deRpSilinviacarrellorpDescription"+
      " , cod_rp_silinviacarrellorp_serial = :c.codRpSilinviacarrellorpSerial"+
      " , cod_rp_silinviacarrellorp_original_fault_code = :c.codRpSilinviacarrellorpOriginalFaultCode"+
      " , de_rp_silinviacarrellorp_original_fault_string = :c.deRpSilinviacarrellorpOriginalFaultString"+
      " , de_rp_silinviacarrellorp_original_fault_description = :c.deRpSilinviacarrellorpOriginalFaultDescription" +
      " where mygov_carrello_multi_beneficiario_id = :c.mygovCarrelloMultiBeneficiarioId"
  )
  int update(@BindBean("c") CarrelloMultiBeneficiario c);

  @SqlQuery("select " + CarrelloMultiBeneficiario.ALIAS + ALL_FIELDS +", "+AnagraficaStato.FIELDS+
      " from mygov_carrello_multi_beneficiario " + CarrelloMultiBeneficiario.ALIAS +
      " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+ CarrelloMultiBeneficiario.ALIAS+".mygov_anagrafica_stato_id " +
      " where " + CarrelloMultiBeneficiario.ALIAS + ".id_session_carrellofesp = :idSession"
  )
  @RegisterFieldMapper(CarrelloMultiBeneficiario.class)
  Optional<CarrelloMultiBeneficiario> getByIdSessionFesp(String idSession);

  @SqlQuery(" select " + CarrelloMultiBeneficiario.ALIAS + ALL_FIELDS +
      " from mygov_carrello_multi_beneficiario " + CarrelloMultiBeneficiario.ALIAS +
      " where "+CarrelloMultiBeneficiario.ALIAS + ".mygov_anagrafica_stato_id = :mygovAnagraficaStatoId " +
      "   and "+CarrelloMultiBeneficiario.ALIAS + ".dt_creazione \\< now() - make_interval(0,0,0,0,0,:deltaMinutes,0) " +
      "   <if(notInCarrello)> and not exists (select 1 from mygov_carrello mc where mc.mygov_carrello_multi_beneficiario_id = "+CarrelloMultiBeneficiario.ALIAS+".mygov_carrello_multi_beneficiario_id) <endif>" +
      " limit <queryLimit> " +
      " for update skip locked"
  )
  @RegisterFieldMapper(CarrelloMultiBeneficiario.class)
  @UseStringTemplateEngine
  List<CarrelloMultiBeneficiario> getOlderByState(int deltaMinutes, Long mygovAnagraficaStatoId, @Define int queryLimit, @Define boolean notInCarrello);

  @SqlUpdate("update mygov_carrello_multi_beneficiario" +
      " set mygov_anagrafica_stato_id = :mygovAnagraficaStatoId"+
      " where mygov_carrello_multi_beneficiario_id = :mygovCarrelloMultiBeneficiarioId"
  )
  int updateStato(Long mygovCarrelloMultiBeneficiarioId, Long mygovAnagraficaStatoId);

  @SqlUpdate("delete from mygov_carrello_multi_beneficiario" +
      " where mygov_carrello_multi_beneficiario_id = :mygovCarrelloMultiBeneficiarioId" +
      "   and mygov_anagrafica_stato_id = :mygovAnagraficaStatoId"
  )
  int delete(Long mygovCarrelloMultiBeneficiarioId, Long mygovAnagraficaStatoId);

}
