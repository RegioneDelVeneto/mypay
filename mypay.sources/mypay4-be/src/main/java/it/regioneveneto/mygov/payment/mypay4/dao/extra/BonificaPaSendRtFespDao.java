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
package it.regioneveneto.mygov.payment.mypay4.dao.extra;

import it.regioneveneto.mygov.payment.mypay4.dao.BaseDao;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Giornale;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface BonificaPaSendRtFespDao extends BaseDao {
  @SqlQuery("select mg2.identificativo_dominio, mg2.codice_contesto_pagamento, min(mg2.mygov_giornale_id) as mygov_giornale_id" +
    "  from mygov_giornale mg2 " +
    " where mg2.tipo_evento = 'paSendRT'" +
    "   and mg2.sotto_tipo_evento = 'RES'" +
    "   and mg2.data_ora_evento between '2022-11-20' and '2022-11-22 13:00:00'" +
    "   and mg2.esito = 'KO'" +
    " group by mg2.identificativo_dominio, mg2.codice_contesto_pagamento" +
    " order by 3")
  @RegisterFieldMapper(Giornale.class)
  List<Giornale> getGiornaleDaBonificare();

  @SqlQuery("select parametri_specifici_interfaccia" +
    "  from mygov_giornale mg2 " +
    " where mg2.tipo_evento = 'paSendRT'" +
    "   and mg2.sotto_tipo_evento = 'REQ'" +
    "   and mg2.data_ora_evento between '2022-11-20' and '2022-11-22 13:00:00'" +
    "   and mg2.identificativo_dominio = :idDominio" +
    "   and mg2.codice_contesto_pagamento = :ccp" +
    " limit 1")
  String getReceiptFromGiornale(String idDominio, String ccp);
}
