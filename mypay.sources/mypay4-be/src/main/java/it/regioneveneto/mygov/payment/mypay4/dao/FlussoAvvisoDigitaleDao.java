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

import it.regioneveneto.mygov.payment.mypay4.model.FlussoAvvisoDigitale;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface FlussoAvvisoDigitaleDao extends BaseDao {

  @SqlUpdate(
      "insert into mygov_flusso_avviso_digitale (" +
          "   mygov_flusso_avviso_digitale_id" +
          " , version" +
          " , cod_fad_id_dominio" +
          " , cod_fad_id_flusso" +
          " , mygov_anagrafica_stato_id" +
          " , cod_fad_tipo_flusso" +
          " , cod_fad_e_presa_in_carico_id_flusso" +
          " , num_fad_e_presa_in_carico_cod_e_presa_in_carico" +
          " , de_fad_e_presa_in_carico_desc_e_presa_in_carico" +
          " , de_fad_file_path" +
          " , de_fad_filename" +
          " , num_fad_dimensione_file" +
          " , num_fad_num_avvisi_nel_flusso" +
          " , dt_creazione" +
          " , dt_ultima_modifica" +
          " ) values (" +
          "   nextval('mygov_flusso_avviso_digitale_id_seq')" +
          " , :d.version" +
          " , :d.codFadIdDominio" +
          " , :d.codFadIdFlusso" +
          " , :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
          " , :d.codFadTipoFlusso" +
          " , :d.codFadEPresaInCaricoIdFlusso" +
          " , :d.numFadEPresaInCaricoCodEPresaInCarico" +
          " , :d.deFadEPresaInCaricoDescEPresaInCarico" +
          " , :d.deFadFilePath" +
          " , :d.deFadFilename" +
          " , :d.numFadDimensioneFile" +
          " , :d.numFadNumAvvisiNelFlusso" +
          " , now()" +
          " , now())"
  )
  @GetGeneratedKeys("mygov_flusso_avviso_digitale_id")
  long insert(@BindBean("d") FlussoAvvisoDigitale d);

  @SqlQuery(
      "select "+ FlussoAvvisoDigitale.ALIAS + ALL_FIELDS +
          " from mygov_flusso_avviso_digitale " + FlussoAvvisoDigitale.ALIAS +
          " where "+FlussoAvvisoDigitale.ALIAS+".cod_fad_id_flusso = :codFadIdFlusso"
  )
  @RegisterFieldMapper(FlussoAvvisoDigitale.class)
  List<FlussoAvvisoDigitale> getByCodFadIdFlusso(String codFadIdFlusso);

}
