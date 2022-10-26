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

import it.regioneveneto.mygov.payment.mypay4.model.Avviso;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface AvvisoDao extends BaseDao {

  @SqlUpdate(
      "insert into mygov_avviso (" +
          "  mygov_avviso_id" +
          ", version" +
          ", dt_creazione" +
          ", dt_ultima_modifica" +
          ", mygov_ente_id" +
          ", cod_iuv" +
          ", cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco" +
          ", cod_rp_sogg_pag_id_univ_pag_codice_id_univoco" +
          ", de_rp_sogg_pag_anagrafica_pagatore" +
          ", de_rp_sogg_pag_indirizzo_pagatore" +
          ", de_rp_sogg_pag_civico_pagatore" +
          ", cod_rp_sogg_pag_cap_pagatore" +
          ", de_rp_sogg_pag_localita_pagatore" +
          ", de_rp_sogg_pag_provincia_pagatore" +
          ", cod_rp_sogg_pag_nazione_pagatore" +
          ", de_rp_sogg_pag_email_pagatore" +
          ") values (" +
          "  nextval('mygov_avviso_mygov_avviso_id_seq')" +
          ", :d.version" +
          ", now()" +
          ", now()" +
          ", :d.mygovEnteId.mygovEnteId" +
          ", :d.codIuv" +
          ", :d.codRpSoggPagIdUnivPagTipoIdUnivoco" +
          ", :d.codRpSoggPagIdUnivPagCodiceIdUnivoco" +
          ", :d.deRpSoggPagAnagraficaPagatore" +
          ", :d.deRpSoggPagIndirizzoPagatore" +
          ", :d.deRpSoggPagCivicoPagatore" +
          ", :d.codRpSoggPagCapPagatore" +
          ", :d.deRpSoggPagLocalitaPagatore" +
          ", :d.deRpSoggPagProvinciaPagatore" +
          ", :d.codRpSoggPagNazionePagatore" +
          ", :d.deRpSoggPagEmailPagatore)"
  )
  @GetGeneratedKeys("mygov_avviso_id")
  long insert(@BindBean("d") Avviso d);

  @SqlQuery(
      "    select "+ Avviso.ALIAS+ALL_FIELDS+", "+ Ente.FIELDS +
          "  from mygov_avviso "+Avviso.ALIAS+
          "  inner join mygov_ente "+Ente.ALIAS+" on "+Ente.ALIAS+".mygov_ente_id = "+Avviso.ALIAS+".mygov_ente_id " +
          " where "+Avviso.ALIAS+".cod_iuv = :codIuv " +
          "   and "+Ente.ALIAS+".cod_ipa_ente = :codIpaEnte ")
  @RegisterFieldMapper(Avviso.class)
  Optional<Avviso> getByIuvEnte(final String codIuv, final String codIpaEnte);
}
