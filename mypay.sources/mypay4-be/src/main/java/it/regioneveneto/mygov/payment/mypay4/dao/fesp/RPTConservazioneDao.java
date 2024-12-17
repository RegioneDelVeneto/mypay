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
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RPT_Conservazione;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface RPTConservazioneDao extends BaseDao {


    @SqlUpdate(" insert into rpt_conservazione ("+
            " rpt_conservazione_id " +
            " , version , rpt_rt_estrazione_id , mygov_giornale_id , mygov_rpt_rt_id , identificativo_dominio , identificativo_univoco_versamento "+
            ", codice_contesto_pagamento , identificativo , rpt_xml , data_registrazione , oggetto "+
            ", tipo_soggetto_pagatore , nominativo_pagatore , identificativo_pagatore , indirizzo_riferimento_pagatore "+
            ", tipo_soggetto_beneficiario , nominativo_beneficiario , identificativo_beneficiario "+
            ", indirizzo_riferimento_beneficiario , id_aggregazione , identificativo_versante, nominativo_versante " +
            ", esito_pagamento , esito_conservazione , errore_conservazione , id_conservazione, tipo_documento "+ //, data_conservazione
            ") values ("+
            " nextval('rpt_conservazione_id_seq'), " +
            ":r.version, "+
            ":r.rptRtEstrazioneId, "+
            ":r.mygovGiornaleId, "+
            ":r.mygovRptRtId, " +
            ":r.identificativoDominio, " +
            ":r.identificativoUnivocoVersamento, " +
            ":r.codiceContestoPagamento, " +
            ":r.identificativo, " +
            ":r.rptXML, " +
            "coalesce(:r.dataRegistrazione, now()), "+  //  ":r.dataRegistrazione, " +
            ":r.oggetto, " +
            ":r.tipoSoggettoPagatore, " +
            ":r.nominativoPagatore, " +
            ":r.identificativoPagatore, " +
            ":r.indirizzoRiferimentoPagatore, " +
            ":r.tipoSoggettoBeneficiario, " +
            ":r.nominativoBeneficiario, " +
            ":r.identificativoBeneficiario, " +
            ":r.indirizzoRiferimentoBeneficiario, " +
            ":r.idAggregazione, " +
            ":r.identificativoVersante, " +
            ":r.nominativoVersante, " +
            ":r.esitoPagamento, " + // campi non valorizzati in service insertRptConservazione
            ":r.esitoConservazione, " +
            ":r.erroreConservazione, " +
           //":r.dataConservazione, " +
            ":r.idConservazione," +
            ":r.tipoDocumento" +
            ")"
    )
    @GetGeneratedKeys("rpt_conservazione_id")
    Long insert(@BindBean("r") RPT_Conservazione r);

    @SqlQuery(
            "select " + RPT_Conservazione.ALIAS + ALL_FIELDS +
                    "  from  rpt_conservazione " + RPT_Conservazione.ALIAS +
                    " where " + RPT_Conservazione.ALIAS + ".identificativo = :identificativo "
    )
    @RegisterFieldMapper(RPT_Conservazione.class)
    Optional<RPT_Conservazione> getByIdentificativo(String identificativo);
}
