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
import it.regioneveneto.mygov.payment.mypay4.model.AvvisoDigitale;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface AvvisoDigitaleDao extends BaseDao {

  @SqlUpdate(
    "insert into mygov_avviso_digitale ("   +
        "   mygov_avviso_digitale_id" +
        " , version" +
        " , cod_ad_id_dominio" +
        " , de_ad_anag_beneficiario" +
        " , cod_ad_id_messaggio_richiesta" +
        " , cod_ad_cod_avviso" +
        " , de_ad_sog_pag_anag_pagatore" +
        " , cod_ad_sog_pag_id_univ_pag_tipo_id_univ" +
        " , cod_ad_sog_pag_id_univ_pag_cod_id_univ" +
        " , dt_ad_data_scadenza_pagamento" +
        " , dt_ad_data_scadenza_avviso" +
        " , num_ad_importo_avviso" +
        " , de_ad_email_soggetto" +
        " , de_ad_cellulare_soggetto" +
        " , de_ad_desc_pagamento" +
        " , de_ad_url_avviso" +
        " , cod_id_flusso_av" +
        " , cod_e_ad_id_dominio" +
        " , cod_e_ad_id_messaggio_richiesta" +
        " , cod_id_flusso_e" +
        " , mygov_anagrafica_stato_id" +
        " , num_ad_tentativi_invio" +
        " , dt_creazione" +
        " , dt_ultima_modifica" +
        " , cod_tassonomia_avviso" +
        " , dati_sing_vers_iban_accredito" +
        " , dati_sing_vers_iban_appoggio" +
        " , tipo_pagamento" +
        " , tipo_operazione" +
        ") values (" +
        " nextval('mygov_avviso_digitale_id_seq')" +
        " , :d.version" +
        " , :d.codAdIdDominio" +
        " , :d.deAdAnagBeneficiario" +
        " , :d.codAdIdMessaggioRichiesta" +
        " , :d.codAdCodAvviso" +
        " , :d.deAdSogPagAnagPagatore" +
        " , :d.codAdSogPagIdUnivPagTipoIdUniv" +
        " , :d.codAdSogPagIdUnivPagCodIdUniv" +
        " , :d.dtAdDataScadenzaPagamento" +
        " , :d.dtAdDataScadenzaAvviso" +
        " , :d.numAdImportoAvviso" +
        " , :d.deAdEmailSoggetto" +
        " , :d.deAdCellulareSoggetto" +
        " , :d.deAdDescPagamento" +
        " , :d.deAdUrlAvviso" +
        " , :d.codIdFlussoAv" +
        " , :d.codEAdIdDominio" +
        " , :d.codEAdIdMessaggioRichiesta" +
        " , :d.codIdFlussoE" +
        " , :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
        " , :d.numAdTentativiInvio" +
        " , now()" +
        " , now()" +
        " , :d.codTassonomiaAvviso" +
        " , :d.datiSingVersIbanAccredito" +
        " , :d.datiSingVersIbanAppoggio" +
        " , :d.tipoPagamento" +
        " , :d.tipoOperazione)"
  )
  @GetGeneratedKeys("mygov_avviso_digitale_id")
  Long insert(@BindBean("d") AvvisoDigitale d);


  @SqlUpdate(
      "update mygov_avviso_digitale set " +
          "   version = :d.version" +
          " , cod_ad_id_dominio = :d.codAdIdDominio" +
          " , de_ad_anag_beneficiario = :d.deAdAnagBeneficiario" +
          " , cod_ad_id_messaggio_richiesta = :d.codAdIdMessaggioRichiesta" +
          " , cod_ad_cod_avviso = :d.codAdCodAvviso" +
          " , de_ad_sog_pag_anag_pagatore = :d.deAdSogPagAnagPagatore" +
          " , cod_ad_sog_pag_id_univ_pag_tipo_id_univ = :d.codAdSogPagIdUnivPagTipoIdUniv" +
          " , cod_ad_sog_pag_id_univ_pag_cod_id_univ = :d.codAdSogPagIdUnivPagCodIdUniv" +
          " , dt_ad_data_scadenza_pagamento = :d.dtAdDataScadenzaPagamento" +
          " , dt_ad_data_scadenza_avviso = :d.dtAdDataScadenzaAvviso" +
          " , num_ad_importo_avviso = :d.numAdImportoAvviso" +
          " , de_ad_email_soggetto = :d.deAdEmailSoggetto" +
          " , de_ad_cellulare_soggetto = :d.deAdCellulareSoggetto" +
          " , de_ad_desc_pagamento = :d.deAdDescPagamento" +
          " , de_ad_url_avviso = :d.deAdUrlAvviso" +
          " , cod_id_flusso_av = :d.codIdFlussoAv" +
          " , cod_e_ad_id_dominio = :d.codEAdIdDominio" +
          " , cod_e_ad_id_messaggio_richiesta = :d.codEAdIdMessaggioRichiesta" +
          " , cod_id_flusso_e = :d.codIdFlussoE" +
          " , mygov_anagrafica_stato_id = :d.mygovAnagraficaStatoId.mygovAnagraficaStatoId" +
          " , num_ad_tentativi_invio = :d.numAdTentativiInvio" +
          " , dt_creazione = :d.dtCreazione" +
          " , dt_ultima_modifica = :d.dtUltimaModifica" +
          " , cod_tassonomia_avviso = :d.codTassonomiaAvviso" +
          " , dati_sing_vers_iban_accredito = :d.datiSingVersIbanAccredito" +
          " , dati_sing_vers_iban_appoggio = :d.datiSingVersIbanAppoggio" +
          " , tipo_pagamento = :d.tipoPagamento" +
          " , tipo_operazione = :d.tipoOperazione" +
          " where mygov_avviso_digitale_id = :d.mygovAvvisoDigitaleId"
  )
  int update(@BindBean("d") AvvisoDigitale d);

  @SqlQuery(
      "    select "+ AvvisoDigitale.ALIAS+ALL_FIELDS+", "+AnagraficaStato.FIELDS+
          "  from mygov_avviso_digitale "+AvvisoDigitale.ALIAS+
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+ AvvisoDigitale.ALIAS+".mygov_anagrafica_stato_id " +
          "  where "+AvvisoDigitale.ALIAS+".mygov_avviso_digitale_id = :mygovAvvisoDigitaleId")
  @RegisterFieldMapper(AvvisoDigitale.class)
  AvvisoDigitale getById(Long mygovAvvisoDigitaleId);

  @SqlQuery(
      "    select "+ AvvisoDigitale.ALIAS+ALL_FIELDS+", "+AnagraficaStato.FIELDS+
          "  from mygov_avviso_digitale "+AvvisoDigitale.ALIAS+
          " inner join mygov_anagrafica_stato "+ AnagraficaStato.ALIAS+" on "+AnagraficaStato.ALIAS+".mygov_anagrafica_stato_id = "+ AvvisoDigitale.ALIAS+".mygov_anagrafica_stato_id " +
          "  where "+AvvisoDigitale.ALIAS+".cod_ad_id_dominio = :idDominio" +
          "  and "+AvvisoDigitale.ALIAS+".cod_ad_cod_avviso = :codiceAvviso")
  @RegisterFieldMapper(AvvisoDigitale.class)
  List<AvvisoDigitale> getByIdDominioECodiceAvviso(String idDominio, String codiceAvviso);


  @SqlUpdate(
      "update mygov_avviso_digitale set " +
          "   mygov_anagrafica_stato_id = :mygovAnagraficaStatoId " +
          " , dt_ultima_modifica = now() " +
          " where mygov_avviso_digitale_id = :mygovAvvisoDigitaleId"
  )
  int updateState(Long mygovAvvisoDigitaleId, Long mygovAnagraficaStatoId);

  @SqlUpdate(
      "update mygov_avviso_digitale set " +
          "   mygov_anagrafica_stato_id = :mygovAnagraficaStatoId " +
          " , dt_ultima_modifica = now() " +
          " , tipo_operazione = :tipoOperazione " +
          " where mygov_avviso_digitale_id = :mygovAvvisoDigitaleId"
  )
  int updateStateOpType(Long mygovAvvisoDigitaleId, Long mygovAnagraficaStatoId, String tipoOperazione);
}
