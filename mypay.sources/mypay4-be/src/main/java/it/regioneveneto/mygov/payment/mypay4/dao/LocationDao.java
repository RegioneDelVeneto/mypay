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

import it.regioneveneto.mygov.payment.mypay4.model.Comune;
import it.regioneveneto.mygov.payment.mypay4.model.Nazione;
import it.regioneveneto.mygov.payment.mypay4.model.Provincia;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;
import java.util.Optional;

public interface LocationDao extends BaseDao {

  @SqlQuery(
      "  select "+Nazione.ALIAS+ALL_FIELDS+ " from mygov_nazione "+Nazione.ALIAS+
          "  order by "+Nazione.ALIAS+".nome_nazione"
  )
  @RegisterFieldMapper(Nazione.class)
  List<Nazione> getNazioni();

  @SqlQuery(
    "  select "+Nazione.ALIAS+ALL_FIELDS+ " from mygov_nazione "+Nazione.ALIAS+
      "  where "+Nazione.ALIAS+".nazione_id = :id"
  )
  @RegisterFieldMapper(Nazione.class)
  Nazione getNazione(Long id);

  @SqlQuery(
      "  select "+Nazione.ALIAS+ALL_FIELDS+ " from mygov_nazione "+Nazione.ALIAS+
          "  where "+Nazione.ALIAS+".codice_iso_alpha_2 = :codIso"
  )
  @RegisterFieldMapper(Nazione.class)
  Nazione getNazioneByCodIso(final String codIso);

  @SqlQuery(
      "  select "+Nazione.ALIAS+ALL_FIELDS+ " from mygov_nazione "+Nazione.ALIAS+
          "  where "+Nazione.ALIAS+".nome_nazione = :name"
  )
  @RegisterFieldMapper(Nazione.class)
  Nazione getNazioneByName(final String name);

  @SqlQuery(
      "  select "+Provincia.ALIAS+ALL_FIELDS+ " from mygov_provincia "+Provincia.ALIAS+
          "  order by "+Provincia.ALIAS+".provincia"
  )
  @RegisterFieldMapper(Provincia.class)
  List<Provincia> getProvince();

  @SqlQuery(
    "  select "+ Provincia.ALIAS+ALL_FIELDS+ " from mygov_provincia "+Provincia.ALIAS+
      "  where "+Provincia.ALIAS+".provincia_id = :id"
  )
  @RegisterFieldMapper(Provincia.class)
  Provincia getProvincia(Long id);

  @SqlQuery(
      "  select "+ Provincia.ALIAS+ALL_FIELDS+ " from mygov_provincia "+Provincia.ALIAS+
          "  where "+Provincia.ALIAS+".sigla = :sigla"
  )
  @RegisterFieldMapper(Provincia.class)
  Provincia getProvinciaBySigla(final String sigla);

  @SqlQuery(
    "  select "+Comune.ALIAS+".comune_id, "+Comune.ALIAS+".comune, "+Comune.ALIAS+".provincia_id "+
        " from mygov_comune "+Comune.ALIAS +
        "  where "+Comune.ALIAS+".comune_id = :id"
  )
  @RegisterFieldMapper(Comune.class)
  Comune getComune(Long id);

  @SqlQuery(
      "  select "+Comune.ALIAS+".comune_id, "+Comune.ALIAS+".comune, "+Comune.ALIAS+".provincia_id "+
          " from mygov_comune "+Comune.ALIAS+
          "  where "+Comune.ALIAS+".provincia_id = :provinciaId"+
          "  order by "+Comune.ALIAS+".comune"
  )
  @RegisterFieldMapper(Comune.class)
  List<Comune> getComuniByProvincia(Long provinciaId);

  //return a list because, even not common, there are some "comuni" having same name in different "provincia"
  @SqlQuery(
      "  select "+Comune.ALIAS+".comune_id, "+Comune.ALIAS+".comune, "+Comune.ALIAS+".provincia_id "+
          " from mygov_comune "+Comune.ALIAS+
          "  where "+Comune.ALIAS+".comune = :comune"
  )
  @RegisterFieldMapper(Comune.class)
  List<Comune> getComuneByName(String comune);

  @SqlQuery(
      "  select "+Comune.ALIAS+".comune_id, "+Comune.ALIAS+".comune, "+Comune.ALIAS+".provincia_id "+
          " from mygov_comune "+Comune.ALIAS+
          "  where "+Comune.ALIAS+".comune = :comune" +
          "    and "+Comune.ALIAS+".sigla_provincia = :siglaProvincia" +
          "  order by 1 desc" +
          "  limit 1"
  )
  @RegisterFieldMapper(Comune.class)
  Optional<Comune> getComuneByNameAndSiglaProvincia(String comune, String siglaProvincia);

}
