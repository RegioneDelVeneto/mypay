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

import it.regioneveneto.mygov.payment.mypay4.dto.TassonomiaCodDescTo;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface TassonomiaDao extends BaseDao {

  @SqlQuery(
      "select " +
      "   tipo_ente as code" +
      " , descrizione_tipo_ente as description" +
      " from mygov_tassonomia" +
      " group by tipo_ente, descrizione_tipo_ente"

  )
  List<TassonomiaCodDescTo> getTipoEnteForSelect();

  @SqlQuery(
      "select " +
          "   prog_macro_area as code" +
          " , prog_macro_area||'.'||nome_macro_area as description" +
          " from mygov_tassonomia" +
          " where tipo_ente = :tipoEnte" +
          " group by prog_macro_area, nome_macro_area" +
          " order by prog_macro_area"
  )
  List<TassonomiaCodDescTo> getMacroAreaForSelect(String tipoEnte);

  @SqlQuery(
      "select " +
          "   cod_tipo_servizio as code" +
          " , cod_tipo_servizio||'.'||tipo_servizio as description" +
          " from mygov_tassonomia" +
          " where tipo_ente = :tipoEnte" +
          "   and prog_macro_area = :macroArea" +
          " group by cod_tipo_servizio, tipo_servizio" +
          " order by cod_tipo_servizio"
  )
  List<TassonomiaCodDescTo> getTipoServizioForSelect(String tipoEnte, String macroArea);

  @SqlQuery(
      "select " +
          "   motivo_riscossione as code" +
          " , motivo_riscossione as description" +
          " from mygov_tassonomia" +
          " where tipo_ente = :tipoEnte" +
          "   and prog_macro_area = :macroArea" +
          "   and cod_tipo_servizio = :codTipoServizio" +
          " group by motivo_riscossione"
  )
  List<TassonomiaCodDescTo> getMotivoRiscossioneforSelect(String tipoEnte, String macroArea, String codTipoServizio);

  @SqlQuery(
      "select " +
          "   codice_tassonomico as code" +
          " , codice_tassonomico as description" +
          " from mygov_tassonomia" +
          " where tipo_ente = :tipoEnte" +
          "   and prog_macro_area = :macroArea" +
          "   and cod_tipo_servizio = :codTipoServizio" +
          "   and motivo_riscossione = :motivoRiscossione" +
          " group by codice_tassonomico "
  )
  List<TassonomiaCodDescTo> getCodTassFromSelect(String tipoEnte, String macroArea, String codTipoServizio, String motivoRiscossione);
}
