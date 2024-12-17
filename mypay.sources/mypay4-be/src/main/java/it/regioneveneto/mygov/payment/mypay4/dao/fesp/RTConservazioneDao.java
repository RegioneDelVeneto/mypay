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
package it.regioneveneto.mygov.payment.mypay4.dao.fesp;

import it.regioneveneto.mygov.payment.mypay4.dao.BaseDao;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RT_Conservazione;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface RTConservazioneDao extends BaseDao {

    @SqlUpdate(" insert into rt_conservazione ("+
            " rt_conservazione_id , version , rpt_rt_estrazione_id , mygov_rpt_rt_id , identificativo_dominio " +
            ", identificativo_univoco_versamento , codice_contesto_pagamento , identificativo , rt_xml , data_registrazione " +
            ", oggetto , tipo_soggetto_destinatario , nominativo_destinatario , identificativo_destinatario " +
            ", indirizzo_riferimento_destinatario , identificativo_beneficiario , id_aggregazione , identificativo_versante " +
            ", nominativo_versante , esito_pagamento , esito_conservazione , errore_conservazione  ,id_conservazione, tipo_documento "+
            ") values (" +
            " nextval('rt_conservazione_id_seq'), " +
            ":r.version, " +
            ":r.rptRtEstrazioneId, " +
            ":r.mygovRptRtId, " +
            ":r.identificativoDominio, " +
            ":r.identificativoUnivocoVersamento, " +
            ":r.codiceContestoPagamento, " +
            ":r.identificativo, " +
            ":r.rtXML, " +
            ":r.dataRegistrazione, " +
            ":r.oggetto, " +
            ":r.tipoSoggettoDestinatario, " +
            ":r.nominativoDestinatario, " +
            ":r.identificativoDestinatario, " +
            ":r.indirizzoRiferimentoDestinatario," +
            ":r.identificativoBeneficiario, " +
            ":r.idAggregazione, " +
            ":r.identificativoVersante, " +
            ":r.nominativoVersante, " +
            ":r.esitoPagamento, " +
            ":r.esitoConservazione, " + // campi non valorizzati in service insertRtConservazione
            ":r.erroreConservazione, " +
            //":r.dataConservazione, " +
            ":r.idConservazione," +
            ":r.tipoDocumento" +
            ")"
    )
    @GetGeneratedKeys("rt_conservazione_id")
    Long insert(@BindBean("r") RT_Conservazione r);

    @SqlQuery(
            "select " + RT_Conservazione.ALIAS + ALL_FIELDS +
                    "  from  rt_conservazione " + RT_Conservazione.ALIAS +
                    " where " + RT_Conservazione.ALIAS + ".identificativo = :receiptId "
    )
    @RegisterFieldMapper(RT_Conservazione.class)
    Optional<RT_Conservazione> getByIdentificativoAndDominio(String receiptId);
}
