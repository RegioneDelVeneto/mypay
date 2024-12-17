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

import it.regioneveneto.mygov.payment.mypay4.model.fesp.RPT_Conservazione;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RT_Conservazione;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Date;
import java.util.List;

public interface NodoFespDao {

    @SqlQuery(
            " select " +  RPT_Conservazione.ALIAS + ".rpt_xml, " +
                    RPT_Conservazione.ALIAS + ".data_registrazione, " +
                    RPT_Conservazione.ALIAS + ".identificativo, " +
                    RPT_Conservazione.ALIAS + ".oggetto, " +
                    RPT_Conservazione.ALIAS + ".tipo_soggetto_pagatore, " +
                    RPT_Conservazione.ALIAS + ".nominativo_pagatore, " +
                    RPT_Conservazione.ALIAS + ".identificativo_pagatore, " +
                    RPT_Conservazione.ALIAS + ".indirizzo_riferimento_pagatore, " +
                    RPT_Conservazione.ALIAS + ".nominativo_beneficiario, " +
                    RPT_Conservazione.ALIAS + ".identificativo_beneficiario, " +
                    RPT_Conservazione.ALIAS + ".indirizzo_riferimento_beneficiario, " +
                    RPT_Conservazione.ALIAS + ".id_aggregazione, " +
                    RPT_Conservazione.ALIAS + ".identificativo_versante, " +
                    RPT_Conservazione.ALIAS + ".nominativo_versante, " +
                    RPT_Conservazione.ALIAS + ".identificativo_univoco_versamento, " +
                    //	"coalesce(esito_pagamento,'OK')," +
                    RPT_Conservazione.ALIAS + ".codice_contesto_pagamento " +
                    " from rpt_conservazione " + RPT_Conservazione.ALIAS +
                    " where " + RPT_Conservazione.ALIAS +".identificativo_dominio= :codiceFiscaleEnte " +
                    " and " + RPT_Conservazione.ALIAS + ".data_registrazione >= :dtInizioEstrazione " +
                    " and " + RPT_Conservazione.ALIAS + ".data_registrazione <= :dtFineEstrazione"
    )
    @RegisterFieldMapper(RPT_Conservazione.class)
    List<RPT_Conservazione> getListaRPT(String codiceFiscaleEnte, Date dtInizioEstrazione, Date dtFineEstrazione);



    @SqlQuery(
            " select " + RT_Conservazione.ALIAS + ".rt_xml, " +
                    RT_Conservazione.ALIAS + ".data_registrazione, " +
                    RT_Conservazione.ALIAS + ".identificativo, " +
                    RT_Conservazione.ALIAS + ".oggetto, " +
                    RT_Conservazione.ALIAS + ".tipo_soggetto_destinatario, " +
                    RT_Conservazione.ALIAS + ".nominativo_destinatario, " +
                    RT_Conservazione.ALIAS + ".identificativo_destinatario, " +
                    RT_Conservazione.ALIAS + ".indirizzo_riferimento_destinatario, " +
                    RT_Conservazione.ALIAS + ".identificativo_beneficiario , " +
                    RT_Conservazione.ALIAS + ".identificativo_versante, " +
                    RT_Conservazione.ALIAS + ".nominativo_versante ," +
                    RT_Conservazione.ALIAS + ".identificativo_univoco_versamento, " +
                    RT_Conservazione.ALIAS + ".esito_pagamento, " +
                    RT_Conservazione.ALIAS + ".codice_contesto_pagamento "  +
                    " from rt_conservazione " + RT_Conservazione.ALIAS +
                    " where " + RT_Conservazione.ALIAS + ".identificativo_dominio= :codiceFiscaleEnte " +
                    " and " + RT_Conservazione.ALIAS + ".data_registrazione >= :dtInizioEstrazione " +
                    " and " + RT_Conservazione.ALIAS + ".data_registrazione <= :dtFineEstrazione"

    )
    @RegisterFieldMapper(RT_Conservazione.class)
    List<RT_Conservazione> getListaRT(final String codiceFiscaleEnte,final Date dtInizioEstrazione,final Date dtFineEstrazione);


}
