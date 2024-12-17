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
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Giornale;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface GiornaleDao extends BaseDao {
  @SqlUpdate(
      "INSERT INTO mygov_giornale (" +
          " mygov_giornale_id" +
          " , version" +
          " , data_ora_evento" +
          " , identificativo_dominio" +
          " , identificativo_univoco_versamento"+
          " , codice_contesto_pagamento" +
          " , identificativo_prestatore_servizi_pagamento" +
          " , tipo_versamento" +
          " , componente"+
          " , categoria_evento" +
          " , tipo_evento" +
          " , sotto_tipo_evento" +
          " , identificativo_fruitore" +
          " , identificativo_erogatore"+
          " , identificativo_stazione_intermediario_pa" +
          " , canale_pagamento" +
          " , parametri_specifici_interfaccia" +
          " , esito"+
          ") values ("+
          "   nextval('mygov_giornale_mygov_giornale_id_seq')"+
          " , :g.version"+
          " , coalesce(:g.dataOraEvento, now())"+
          " , :g.identificativoDominio"+
          " , :g.identificativoUnivocoVersamento"+
          " , :g.codiceContestoPagamento"+
          " , :g.identificativoPrestatoreServiziPagamento"+
          " , :g.tipoVersamento"+
          " , :g.componente"+
          " , :g.categoriaEvento"+
          " , :g.tipoEvento"+
          " , :g.sottoTipoEvento"+
          " , :g.identificativoFruitore"+
          " , :g.identificativoErogatore"+
          " , :g.identificativoStazioneIntermediarioPa"+
          " , :g.canalePagamento"+
          " , :g.parametriSpecificiInterfaccia"+
          " , :g.esito)")
  @GetGeneratedKeys("mygov_giornale_id")
  Long insert(@BindBean("g") Giornale g);

  String SQL_SEARCH_GIORNALE =
        "  from mygov_giornale " + Giornale.ALIAS +
        " where ("+Giornale.ALIAS+".identificativo_dominio = :identificativoDominio or :identificativoDominio is null)" +
        "   and ("+Giornale.ALIAS+".identificativo_univoco_versamento = :identificativoUnivocoVersamento or :identificativoUnivocoVersamento is null)" +
        "   and ("+Giornale.ALIAS+".categoria_evento = :categoriaEvento or :categoriaEvento is null)" +
        "   and ("+Giornale.ALIAS+".tipo_evento = :tipoEvento or :tipoEvento is null)" +
        "   and ("+Giornale.ALIAS+".identificativo_prestatore_servizi_pagamento = :identificativoPrestatoreServiziPagamento or :identificativoPrestatoreServiziPagamento is null)" +
        "   and ("+Giornale.ALIAS+".esito = :esito or :esito is null)" +
        "   and ("+Giornale.ALIAS+".data_ora_evento >= :dataOraEventoFrom::TIMESTAMP or :dataOraEventoFrom::TIMESTAMP is null)" +
        "   and ("+Giornale.ALIAS+".data_ora_evento <= :dataOraEventoTo::TIMESTAMP or :dataOraEventoTo::TIMESTAMP is null)";

  @SqlQuery(
      "select " +
          Giornale.ALIAS + ".data_ora_evento, " +
          Giornale.ALIAS + ".mygov_giornale_id, " +
          Giornale.ALIAS + ".identificativo_dominio, " +
          Giornale.ALIAS + ".identificativo_univoco_versamento, " +
          Giornale.ALIAS + ".tipo_evento, " +
          Giornale.ALIAS + ".sotto_tipo_evento, " +
          Giornale.ALIAS + ".categoria_evento, " +
          Giornale.ALIAS + ".identificativo_prestatore_servizi_pagamento " +
          SQL_SEARCH_GIORNALE +
          " order by "+Giornale.ALIAS+".data_ora_evento DESC " +
          " limit <queryLimit>"
  )
  @RegisterFieldMapper(Giornale.class)
  List<Giornale> searchGiornale(String identificativoDominio, String identificativoUnivocoVersamento,
                                String categoriaEvento, String tipoEvento,
                                String identificativoPrestatoreServiziPagamento, String esito,
                                LocalDateTime dataOraEventoFrom, LocalDateTime dataOraEventoTo,
                                @Define int queryLimit);

  @SqlQuery(
      "select count(1) " +
          SQL_SEARCH_GIORNALE
  )
  int searchGiornaleCount(String identificativoDominio, String identificativoUnivocoVersamento,
                          String categoriaEvento, String tipoEvento,
                          String identificativoPrestatoreServiziPagamento, String esito,
                          LocalDateTime dataOraEventoFrom, LocalDateTime dataOraEventoTo);

  @SqlQuery(
      "   select distinct "+ Giornale.ALIAS+".identificativo_prestatore_servizi_pagamento " +
          " from mygov_giornale "+ Giornale.ALIAS
      //" order by 1 ASC " //order is made java side for performance reasons
  )
  List<String> getAllPsp();

  @SqlQuery(
      "select " + Giornale.ALIAS + ALL_FIELDS +
          "  from mygov_giornale " + Giornale.ALIAS +
          " where " + Giornale.ALIAS + ".mygov_giornale_id = :mygovGiornaleId "
  )

  @RegisterFieldMapper(Giornale.class)
  Giornale getGiornaleById(Long mygovGiornaleId);

  @SqlQuery(
          "select " + Giornale.ALIAS + ALL_FIELDS +
                  "  from mygov_giornale " + Giornale.ALIAS +
                  " where " + Giornale.ALIAS + ".codice_contesto_pagamento = :receiptId " +
                  " and "  + Giornale.ALIAS + ".tipo_evento = :paSendRT " +
                  " and "  + Giornale.ALIAS + ".sotto_tipo_evento = :req "
  )
  @RegisterFieldMapper(Giornale.class)
  List<Giornale> getByCCPandTipoEvento(String receiptId, String paSendRT, String req);

  @SqlQuery(
          "select " + Giornale.ALIAS + ALL_FIELDS +
                  "  from mygov_giornale " + Giornale.ALIAS +
                  " where "  + Giornale.ALIAS + ".tipo_evento = :paGetPayment " +
                  " and "  + Giornale.ALIAS + ".sotto_tipo_evento = :res " +
                  " and "  + Giornale.ALIAS + ".data_ora_evento >=  :dataInizioRecuperoConservazione "

  )
  @RegisterFieldMapper(Giornale.class)
  List<Giornale> getByTipoAndSottoTipo(String paGetPayment, String res, LocalDate dataInizioRecuperoConservazione);

}
