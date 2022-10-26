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
import it.regioneveneto.mygov.payment.mypay4.model.fesp.CarrelloRp;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RpE;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.RptRt;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface RpEDao extends BaseDao {

  @SqlQuery("select " + RpE.ALIAS + ALL_FIELDS +
      " from mygov_rp_e " + RpE.ALIAS +
      " left join mygov_carrello_rp "+ CarrelloRp.ALIAS+" on "+
        CarrelloRp.ALIAS+".mygov_carrello_rp_id = "+ RpE.ALIAS+".mygov_carrello_rp_id " +
      " where "+ RpE.ALIAS+".mygov_rp_e_id = :id " )
  @RegisterFieldMapper(RpE.class)
  Optional<RpE> getById(Long id);

  @SqlQuery("select " + RpE.ALIAS + ALL_FIELDS +
      "  from mygov_rp_e " + RpE.ALIAS +
      " left join mygov_carrello_rp "+ CarrelloRp.ALIAS+" on "+
        CarrelloRp.ALIAS+".mygov_carrello_rp_id = "+ RpE.ALIAS+".mygov_carrello_rp_id " +
      " where "+ RpE.ALIAS+".id_session = :idSession ")
  @RegisterFieldMapper(RpE.class)
  Optional<RpE> getByIdSession(String idSession);

  @SqlQuery("select " + RpE.ALIAS + ALL_FIELDS +
      "  from mygov_rp_e " + RpE.ALIAS +
      "  join mygov_carrello_rp "+ CarrelloRp.ALIAS+" on "+
      CarrelloRp.ALIAS+".mygov_carrello_rp_id = "+ RpE.ALIAS+".mygov_carrello_rp_id " +
      " where "+ CarrelloRp.ALIAS+".mygov_carrello_rp_id = :id ")
  @RegisterFieldMapper(RpE.class)
  List<RpE> getByCart(Long id);

  @SqlUpdate(" insert into mygov_rp_e (" +
      " mygov_rp_e_id , version , mygov_carrello_rp_id , cod_ack_e , dt_creazione_rp , dt_ultima_modifica_rp , dt_creazione_e "+
      " , dt_ultima_modifica_e , cod_rp_silinviarp_id_psp , cod_rp_silinviarp_id_intermediario_psp , cod_rp_silinviarp_id_canale "+
      " , cod_rp_silinviarp_id_dominio , cod_rp_silinviarp_id_univoco_versamento , cod_rp_silinviarp_codice_contesto_pagamento "+
      " , de_rp_silinviarp_esito , cod_rp_silinviarp_redirect , cod_rp_silinviarp_url , cod_rp_silinviarp_fault_code "+
      " , de_rp_silinviarp_fault_string , cod_rp_silinviarp_id , de_rp_silinviarp_description , cod_rp_silinviarp_serial "+
      " , de_rp_versione_oggetto , cod_rp_dom_id_dominio , cod_rp_dom_id_stazione_richiedente , cod_rp_id_messaggio_richiesta "+
      " , dt_rp_data_ora_messaggio_richiesta , cod_rp_autenticazione_soggetto , cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco "+
      " , cod_rp_sogg_vers_id_univ_vers_codice_id_univoco , de_rp_sogg_vers_anagrafica_versante "+
      " , de_rp_sogg_vers_indirizzo_versante , de_rp_sogg_vers_civico_versante , cod_rp_sogg_vers_cap_versante "+
      " , de_rp_sogg_vers_localita_versante , de_rp_sogg_vers_provincia_versante , cod_rp_sogg_vers_nazione_versante "+
      " , de_rp_sogg_vers_email_versante , cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco "+
      " , cod_rp_sogg_pag_id_univ_pag_codice_id_univoco , de_rp_sogg_pag_anagrafica_pagatore , de_rp_sogg_pag_indirizzo_pagatore "+
      " , de_rp_sogg_pag_civico_pagatore , cod_rp_sogg_pag_cap_pagatore , de_rp_sogg_pag_localita_pagatore "+
      " , de_rp_sogg_pag_provincia_pagatore , cod_rp_sogg_pag_nazione_pagatore , de_rp_sogg_pag_email_pagatore "+
      " , dt_rp_dati_vers_data_esecuzione_pagamento , num_rp_dati_vers_importo_totale_da_versare "+
      " , cod_rp_dati_vers_tipo_versamento , cod_rp_dati_vers_id_univoco_versamento , cod_rp_dati_vers_codice_contesto_pagamento "+
      " , de_rp_dati_vers_iban_addebito , de_rp_dati_vers_bic_addebito , cod_e_silinviaesito_id_dominio "+
      " , cod_e_silinviaesito_id_univoco_versamento , cod_e_silinviaesito_codice_contesto_pagamento , de_e_silinviaesito_esito "+
      " , cod_e_silinviaesito_fault_code , de_e_silinviaesito_fault_string , cod_e_silinviaesito_id "+
      " , de_e_silinviaesito_description , cod_e_silinviaesito_serial , de_e_versione_oggetto , cod_e_dom_id_dominio "+
      " , cod_e_dom_id_stazione_richiedente , cod_e_id_messaggio_ricevuta , dt_e_data_ora_messaggio_ricevuta "+
      " , cod_e_riferimento_messaggio_richiesta , dt_e_riferimento_data_richiesta "+
      " , cod_e_istit_attes_id_univ_attes_tipo_id_univoco , cod_e_istit_attes_id_univ_attes_codice_id_univoco "+
      " , de_e_istit_attes_denominazione_attestante , cod_e_istit_attes_codice_unit_oper_attestante "+
      " , de_e_istit_attes_denom_unit_oper_attestante , de_e_istit_attes_indirizzo_attestante "+
      " , de_e_istit_attes_civico_attestante , cod_e_istit_attes_cap_attestante , de_e_istit_attes_localita_attestante "+
      " , de_e_istit_attes_provincia_attestante , cod_e_istit_attes_nazione_attestante "+
      " , cod_e_ente_benef_id_univ_benef_tipo_id_univoco , cod_e_ente_benef_id_univ_benef_codice_id_univoco "+
      " , de_e_ente_benef_denominazione_beneficiario , cod_e_ente_benef_codice_unit_oper_beneficiario "+
      " , de_e_ente_benef_denom_unit_oper_beneficiario , de_e_ente_benef_indirizzo_beneficiario "+
      " , de_e_ente_benef_civico_beneficiario , cod_e_ente_benef_cap_beneficiario , de_e_ente_benef_localita_beneficiario "+
      " , de_e_ente_benef_provincia_beneficiario , cod_e_ente_benef_nazione_beneficiario "+
      " , cod_e_sogg_vers_id_univ_vers_tipo_id_univoco , cod_e_sogg_vers_id_univ_vers_codice_id_univoco "+
      " , de_e_sogg_vers_anagrafica_versante , de_e_sogg_vers_indirizzo_versante , de_e_sogg_vers_civico_versante "+
      " , cod_e_sogg_vers_cap_versante , de_e_sogg_vers_localita_versante , de_e_sogg_vers_provincia_versante "+
      " , cod_e_sogg_vers_nazione_versante , de_e_sogg_vers_email_versante , cod_e_sogg_pag_id_univ_pag_tipo_id_univoco "+
      " , cod_e_sogg_pag_id_univ_pag_codice_id_univoco , de_e_sogg_pag_anagrafica_pagatore , de_e_sogg_pag_indirizzo_pagatore "+
      " , de_e_sogg_pag_civico_pagatore , cod_e_sogg_pag_cap_pagatore , de_e_sogg_pag_localita_pagatore "+
      " , de_e_sogg_pag_provincia_pagatore , cod_e_sogg_pag_nazione_pagatore , de_e_sogg_pag_email_pagatore "+
      " , cod_e_dati_pag_codice_esito_pagamento , num_e_dati_pag_importo_totale_pagato , cod_e_dati_pag_id_univoco_versamento "+
      " , cod_e_dati_pag_codice_contesto_pagamento , id_session , modello_pagamento , cod_e_silinviaesito_original_fault_code "+
      " , de_e_silinviaesito_original_fault_string , de_e_silinviaesito_original_fault_description "+
      " , cod_rp_silinviarp_original_fault_code , de_rp_silinviarp_original_fault_string "+
      " , de_rp_silinviarp_original_fault_description ) values ( " +
      " nextval('mygov_rp_e_mygov_rp_e_id_seq') "+
      " , :r.version" +
      " , :r.mygovCarrelloRpId?.mygovCarrelloRpId" +
      " , :r.codAckE" +
      " , coalesce(:r.dtCreazioneRp, now())" +
      " , coalesce(:r.dtUltimaModificaRp, now())" +
      " , :r.dtCreazioneE" +
      " , :r.dtUltimaModificaE" +
      " , :r.codRpSilinviarpIdPsp" +
      " , :r.codRpSilinviarpIdIntermediarioPsp" +
      " , :r.codRpSilinviarpIdCanale" +
      " , :r.codRpSilinviarpIdDominio" +
      " , :r.codRpSilinviarpIdUnivocoVersamento" +
      " , :r.codRpSilinviarpCodiceContestoPagamento" +
      " , :r.deRpSilinviarpEsito" +
      " , :r.codRpSilinviarpRedirect" +
      " , :r.codRpSilinviarpUrl" +
      " , :r.codRpSilinviarpFaultCode" +
      " , :r.deRpSilinviarpFaultString" +
      " , :r.codRpSilinviarpId" +
      " , :r.deRpSilinviarpDescription" +
      " , :r.codRpSilinviarpSerial" +
      " , :r.deRpVersioneOggetto" +
      " , :r.codRpDomIdDominio" +
      " , :r.codRpDomIdStazioneRichiedente" +
      " , :r.codRpIdMessaggioRichiesta" +
      " , :r.dtRpDataOraMessaggioRichiesta" +
      " , :r.codRpAutenticazioneSoggetto" +
      " , :r.codRpSoggVersIdUnivVersTipoIdUnivoco" +
      " , :r.codRpSoggVersIdUnivVersCodiceIdUnivoco" +
      " , :r.deRpSoggVersAnagraficaVersante" +
      " , :r.deRpSoggVersIndirizzoVersante" +
      " , :r.deRpSoggVersCivicoVersante" +
      " , :r.codRpSoggVersCapVersante" +
      " , left(:r.deRpSoggVersLocalitaVersante,"+RptRt.MAX_LENGTH_LOCALITA+")" +
      " , :r.deRpSoggVersProvinciaVersante" +
      " , :r.codRpSoggVersNazioneVersante" +
      " , :r.deRpSoggVersEmailVersante" +
      " , :r.codRpSoggPagIdUnivPagTipoIdUnivoco" +
      " , :r.codRpSoggPagIdUnivPagCodiceIdUnivoco" +
      " , :r.deRpSoggPagAnagraficaPagatore" +
      " , :r.deRpSoggPagIndirizzoPagatore" +
      " , :r.deRpSoggPagCivicoPagatore" +
      " , :r.codRpSoggPagCapPagatore" +
      " , left(:r.deRpSoggPagLocalitaPagatore,"+RptRt.MAX_LENGTH_LOCALITA+")" +
      " , :r.deRpSoggPagProvinciaPagatore" +
      " , :r.codRpSoggPagNazionePagatore" +
      " , :r.deRpSoggPagEmailPagatore" +
      " , :r.dtRpDatiVersDataEsecuzionePagamento" +
      " , :r.numRpDatiVersImportoTotaleDaVersare" +
      " , :r.codRpDatiVersTipoVersamento" +
      " , :r.codRpDatiVersIdUnivocoVersamento" +
      " , :r.codRpDatiVersCodiceContestoPagamento" +
      " , :r.deRpDatiVersIbanAddebito" +
      " , :r.deRpDatiVersBicAddebito" +
      " , :r.codESilinviaesitoIdDominio" +
      " , :r.codESilinviaesitoIdUnivocoVersamento" +
      " , :r.codESilinviaesitoCodiceContestoPagamento" +
      " , :r.deESilinviaesitoEsito" +
      " , :r.codESilinviaesitoFaultCode" +
      " , :r.deESilinviaesitoFaultString" +
      " , :r.codESilinviaesitoId" +
      " , :r.deESilinviaesitoDescription" +
      " , :r.codESilinviaesitoSerial" +
      " , :r.deEVersioneOggetto" +
      " , :r.codEDomIdDominio" +
      " , :r.codEDomIdStazioneRichiedente" +
      " , :r.codEIdMessaggioRicevuta" +
      " , :r.dtEDataOraMessaggioRicevuta" +
      " , :r.codERiferimentoMessaggioRichiesta" +
      " , :r.dtERiferimentoDataRichiesta" +
      " , :r.codEIstitAttesIdUnivAttesTipoIdUnivoco" +
      " , :r.codEIstitAttesIdUnivAttesCodiceIdUnivoco" +
      " , :r.deEIstitAttesDenominazioneAttestante" +
      " , :r.codEIstitAttesCodiceUnitOperAttestante" +
      " , :r.deEIstitAttesDenomUnitOperAttestante" +
      " , :r.deEIstitAttesIndirizzoAttestante" +
      " , :r.deEIstitAttesCivicoAttestante" +
      " , :r.codEIstitAttesCapAttestante" +
      " , :r.deEIstitAttesLocalitaAttestante" +
      " , :r.deEIstitAttesProvinciaAttestante" +
      " , :r.codEIstitAttesNazioneAttestante" +
      " , :r.codEEnteBenefIdUnivBenefTipoIdUnivoco" +
      " , :r.codEEnteBenefIdUnivBenefCodiceIdUnivoco" +
      " , :r.deEEnteBenefDenominazioneBeneficiario" +
      " , :r.codEEnteBenefCodiceUnitOperBeneficiario" +
      " , :r.deEEnteBenefDenomUnitOperBeneficiario" +
      " , :r.deEEnteBenefIndirizzoBeneficiario" +
      " , :r.deEEnteBenefCivicoBeneficiario" +
      " , :r.codEEnteBenefCapBeneficiario" +
      " , :r.deEEnteBenefLocalitaBeneficiario" +
      " , :r.deEEnteBenefProvinciaBeneficiario" +
      " , :r.codEEnteBenefNazioneBeneficiario" +
      " , :r.codESoggVersIdUnivVersTipoIdUnivoco" +
      " , :r.codESoggVersIdUnivVersCodiceIdUnivoco" +
      " , :r.deESoggVersAnagraficaVersante" +
      " , :r.deESoggVersIndirizzoVersante" +
      " , :r.deESoggVersCivicoVersante" +
      " , :r.codESoggVersCapVersante" +
      " , :r.deESoggVersLocalitaVersante" +
      " , :r.deESoggVersProvinciaVersante" +
      " , :r.codESoggVersNazioneVersante" +
      " , :r.deESoggVersEmailVersante" +
      " , :r.codESoggPagIdUnivPagTipoIdUnivoco" +
      " , :r.codESoggPagIdUnivPagCodiceIdUnivoco" +
      " , :r.deESoggPagAnagraficaPagatore" +
      " , :r.deESoggPagIndirizzoPagatore" +
      " , :r.deESoggPagCivicoPagatore" +
      " , :r.codESoggPagCapPagatore" +
      " , :r.deESoggPagLocalitaPagatore" +
      " , :r.deESoggPagProvinciaPagatore" +
      " , :r.codESoggPagNazionePagatore" +
      " , :r.deESoggPagEmailPagatore" +
      " , :r.codEDatiPagCodiceEsitoPagamento" +
      " , :r.numEDatiPagImportoTotalePagato" +
      " , :r.codEDatiPagIdUnivocoVersamento" +
      " , :r.codEDatiPagCodiceContestoPagamento" +
      " , :r.idSession" +
      " , :r.modelloPagamento" +
      " , :r.codESilinviaesitoOriginalFaultCode" +
      " , :r.deESilinviaesitoOriginalFaultString" +
      " , :r.deESilinviaesitoOriginalFaultDescription" +
      " , :r.codRpSilinviarpOriginalFaultCode" +
      " , :r.deRpSilinviarpOriginalFaultString" +
      " , :r.deRpSilinviarpOriginalFaultDescription );"
  )
  @GetGeneratedKeys("mygov_rp_e_id")
  long insert(@BindBean("r") RpE r);

  @SqlUpdate(" update mygov_rp_e "+
      " set version = :r.version" +
      ", mygov_carrello_rp_id = :r.mygovCarrelloRpId?.mygovCarrelloRpId" +
      ", cod_ack_e = :r.codAckE"+
      ", dt_creazione_rp = :r.dtCreazioneRp" +
      ", dt_ultima_modifica_rp = :r.dtUltimaModificaRp" +
      ", dt_creazione_e = :r.dtCreazioneE"+
      ", dt_ultima_modifica_e = :r.dtUltimaModificaE" +
      ", cod_rp_silinviarp_id_psp = :r.codRpSilinviarpIdPsp"+
      ", cod_rp_silinviarp_id_intermediario_psp = :r.codRpSilinviarpIdIntermediarioPsp"+
      ", cod_rp_silinviarp_id_canale = :r.codRpSilinviarpIdCanale" +
      ", cod_rp_silinviarp_id_dominio = :r.codRpSilinviarpIdDominio"+
      ", cod_rp_silinviarp_id_univoco_versamento = :r.codRpSilinviarpIdUnivocoVersamento"+
      ", cod_rp_silinviarp_codice_contesto_pagamento = :r.codRpSilinviarpCodiceContestoPagamento"+
      ", de_rp_silinviarp_esito = :r.deRpSilinviarpEsito" +
      ", cod_rp_silinviarp_redirect = :r.codRpSilinviarpRedirect"+
      ", cod_rp_silinviarp_url = :r.codRpSilinviarpUrl" +
      ", cod_rp_silinviarp_fault_code = :r.codRpSilinviarpFaultCode"+
      ", de_rp_silinviarp_fault_string = :r.deRpSilinviarpFaultString" +
      ", cod_rp_silinviarp_id = :r.codRpSilinviarpId"+
      ", de_rp_silinviarp_description = :r.deRpSilinviarpDescription" +
      ", cod_rp_silinviarp_serial = :r.codRpSilinviarpSerial"+
      ", de_rp_versione_oggetto = :r.deRpVersioneOggetto" +
      ", cod_rp_dom_id_dominio = :r.codRpDomIdDominio"+
      ", cod_rp_dom_id_stazione_richiedente = :r.codRpDomIdStazioneRichiedente"+
      ", cod_rp_id_messaggio_richiesta = :r.codRpIdMessaggioRichiesta"+
      ", dt_rp_data_ora_messaggio_richiesta = :r.dtRpDataOraMessaggioRichiesta"+
      ", cod_rp_autenticazione_soggetto = :r.codRpAutenticazioneSoggetto"+
      ", cod_rp_sogg_vers_id_univ_vers_tipo_id_univoco = :r.codRpSoggVersIdUnivVersTipoIdUnivoco"+
      ", cod_rp_sogg_vers_id_univ_vers_codice_id_univoco = :r.codRpSoggVersIdUnivVersCodiceIdUnivoco"+
      ", de_rp_sogg_vers_anagrafica_versante = :r.deRpSoggVersAnagraficaVersante"+
      ", de_rp_sogg_vers_indirizzo_versante = :r.deRpSoggVersIndirizzoVersante"+
      ", de_rp_sogg_vers_civico_versante = :r.deRpSoggVersCivicoVersante"+
      ", cod_rp_sogg_vers_cap_versante = :r.codRpSoggVersCapVersante"+
      ", de_rp_sogg_vers_localita_versante = left(:r.deRpSoggVersLocalitaVersante,"+RptRt.MAX_LENGTH_LOCALITA+")"+
      ", de_rp_sogg_vers_provincia_versante = :r.deRpSoggVersProvinciaVersante"+
      ", cod_rp_sogg_vers_nazione_versante = :r.codRpSoggVersNazioneVersante"+
      ", de_rp_sogg_vers_email_versante = :r.deRpSoggVersEmailVersante"+
      ", cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco = :r.codRpSoggPagIdUnivPagTipoIdUnivoco"+
      ", cod_rp_sogg_pag_id_univ_pag_codice_id_univoco = :r.codRpSoggPagIdUnivPagCodiceIdUnivoco"+
      ", de_rp_sogg_pag_anagrafica_pagatore = :r.deRpSoggPagAnagraficaPagatore"+
      ", de_rp_sogg_pag_indirizzo_pagatore = :r.deRpSoggPagIndirizzoPagatore"+
      ", de_rp_sogg_pag_civico_pagatore = :r.deRpSoggPagCivicoPagatore"+
      ", cod_rp_sogg_pag_cap_pagatore = :r.codRpSoggPagCapPagatore"+
      ", de_rp_sogg_pag_localita_pagatore = left(:r.deRpSoggPagLocalitaPagatore,"+RptRt.MAX_LENGTH_LOCALITA+")"+
      ", de_rp_sogg_pag_provincia_pagatore = :r.deRpSoggPagProvinciaPagatore"+
      ", cod_rp_sogg_pag_nazione_pagatore = :r.codRpSoggPagNazionePagatore"+
      ", de_rp_sogg_pag_email_pagatore = :r.deRpSoggPagEmailPagatore"+
      ", dt_rp_dati_vers_data_esecuzione_pagamento = :r.dtRpDatiVersDataEsecuzionePagamento"+
      ", num_rp_dati_vers_importo_totale_da_versare = :r.numRpDatiVersImportoTotaleDaVersare"+
      ", cod_rp_dati_vers_tipo_versamento = :r.codRpDatiVersTipoVersamento"+
      ", cod_rp_dati_vers_id_univoco_versamento = :r.codRpDatiVersIdUnivocoVersamento"+
      ", cod_rp_dati_vers_codice_contesto_pagamento = :r.codRpDatiVersCodiceContestoPagamento"+
      ", de_rp_dati_vers_iban_addebito = :r.deRpDatiVersIbanAddebito" +
      ", de_rp_dati_vers_bic_addebito = :r.deRpDatiVersBicAddebito"+
      ", cod_e_silinviaesito_id_dominio = :r.codESilinviaesitoIdDominio"+
      ", cod_e_silinviaesito_id_univoco_versamento = :r.codESilinviaesitoIdUnivocoVersamento"+
      ", cod_e_silinviaesito_codice_contesto_pagamento = :r.codESilinviaesitoCodiceContestoPagamento"+
      ", de_e_silinviaesito_esito = :r.deESilinviaesitoEsito" +
      ", cod_e_silinviaesito_fault_code = :r.codESilinviaesitoFaultCode"+
      ", de_e_silinviaesito_fault_string = :r.deESilinviaesitoFaultString" +
      ", cod_e_silinviaesito_id = :r.codESilinviaesitoId"+
      ", de_e_silinviaesito_description = :r.deESilinviaesitoDescription"+
      ", cod_e_silinviaesito_serial = :r.codESilinviaesitoSerial" +
      ", de_e_versione_oggetto = :r.deEVersioneOggetto"+
      ", cod_e_dom_id_dominio = :r.codEDomIdDominio" +
      ", cod_e_dom_id_stazione_richiedente = :r.codEDomIdStazioneRichiedente"+
      ", cod_e_id_messaggio_ricevuta = :r.codEIdMessaggioRicevuta"+
      ", dt_e_data_ora_messaggio_ricevuta = :r.dtEDataOraMessaggioRicevuta"+
      ", cod_e_riferimento_messaggio_richiesta = :r.codERiferimentoMessaggioRichiesta"+
      ", dt_e_riferimento_data_richiesta = :r.dtERiferimentoDataRichiesta"+
      ", cod_e_istit_attes_id_univ_attes_tipo_id_univoco = :r.codEIstitAttesIdUnivAttesTipoIdUnivoco"+
      ", cod_e_istit_attes_id_univ_attes_codice_id_univoco = :r.codEIstitAttesIdUnivAttesCodiceIdUnivoco"+
      ", de_e_istit_attes_denominazione_attestante = :r.deEIstitAttesDenominazioneAttestante"+
      ", cod_e_istit_attes_codice_unit_oper_attestante = :r.codEIstitAttesCodiceUnitOperAttestante"+
      ", de_e_istit_attes_denom_unit_oper_attestante = :r.deEIstitAttesDenomUnitOperAttestante"+
      ", de_e_istit_attes_indirizzo_attestante = :r.deEIstitAttesIndirizzoAttestante"+
      ", de_e_istit_attes_civico_attestante = :r.deEIstitAttesCivicoAttestante"+
      ", cod_e_istit_attes_cap_attestante = :r.codEIstitAttesCapAttestante"+
      ", de_e_istit_attes_localita_attestante = :r.deEIstitAttesLocalitaAttestante"+
      ", de_e_istit_attes_provincia_attestante = :r.deEIstitAttesProvinciaAttestante"+
      ", cod_e_istit_attes_nazione_attestante = :r.codEIstitAttesNazioneAttestante"+
      ", cod_e_ente_benef_id_univ_benef_tipo_id_univoco = :r.codEEnteBenefIdUnivBenefTipoIdUnivoco"+
      ", cod_e_ente_benef_id_univ_benef_codice_id_univoco = :r.codEEnteBenefIdUnivBenefCodiceIdUnivoco"+
      ", de_e_ente_benef_denominazione_beneficiario = :r.deEEnteBenefDenominazioneBeneficiario"+
      ", cod_e_ente_benef_codice_unit_oper_beneficiario = :r.codEEnteBenefCodiceUnitOperBeneficiario"+
      ", de_e_ente_benef_denom_unit_oper_beneficiario = :r.deEEnteBenefDenomUnitOperBeneficiario"+
      ", de_e_ente_benef_indirizzo_beneficiario = :r.deEEnteBenefIndirizzoBeneficiario"+
      ", de_e_ente_benef_civico_beneficiario = :r.deEEnteBenefCivicoBeneficiario"+
      ", cod_e_ente_benef_cap_beneficiario = :r.codEEnteBenefCapBeneficiario"+
      ", de_e_ente_benef_localita_beneficiario = :r.deEEnteBenefLocalitaBeneficiario"+
      ", de_e_ente_benef_provincia_beneficiario = :r.deEEnteBenefProvinciaBeneficiario"+
      ", cod_e_ente_benef_nazione_beneficiario = :r.codEEnteBenefNazioneBeneficiario"+
      ", cod_e_sogg_vers_id_univ_vers_tipo_id_univoco = :r.codESoggVersIdUnivVersTipoIdUnivoco"+
      ", cod_e_sogg_vers_id_univ_vers_codice_id_univoco = :r.codESoggVersIdUnivVersCodiceIdUnivoco"+
      ", de_e_sogg_vers_anagrafica_versante = :r.deESoggVersAnagraficaVersante"+
      ", de_e_sogg_vers_indirizzo_versante = :r.deESoggVersIndirizzoVersante"+
      ", de_e_sogg_vers_civico_versante = :r.deESoggVersCivicoVersante"+
      ", cod_e_sogg_vers_cap_versante = :r.codESoggVersCapVersante"+
      ", de_e_sogg_vers_localita_versante = :r.deESoggVersLocalitaVersante"+
      ", de_e_sogg_vers_provincia_versante = :r.deESoggVersProvinciaVersante"+
      ", cod_e_sogg_vers_nazione_versante = :r.codESoggVersNazioneVersante"+
      ", de_e_sogg_vers_email_versante = :r.deESoggVersEmailVersante"+
      ", cod_e_sogg_pag_id_univ_pag_tipo_id_univoco = :r.codESoggPagIdUnivPagTipoIdUnivoco"+
      ", cod_e_sogg_pag_id_univ_pag_codice_id_univoco = :r.codESoggPagIdUnivPagCodiceIdUnivoco"+
      ", de_e_sogg_pag_anagrafica_pagatore = :r.deESoggPagAnagraficaPagatore"+
      ", de_e_sogg_pag_indirizzo_pagatore = :r.deESoggPagIndirizzoPagatore"+
      ", de_e_sogg_pag_civico_pagatore = :r.deESoggPagCivicoPagatore" +
      ", cod_e_sogg_pag_cap_pagatore = :r.codESoggPagCapPagatore"+
      ", de_e_sogg_pag_localita_pagatore = :r.deESoggPagLocalitaPagatore"+
      ", de_e_sogg_pag_provincia_pagatore = :r.deESoggPagProvinciaPagatore"+
      ", cod_e_sogg_pag_nazione_pagatore = :r.codESoggPagNazionePagatore"+
      ", de_e_sogg_pag_email_pagatore = :r.deESoggPagEmailPagatore"+
      ", cod_e_dati_pag_codice_esito_pagamento = :r.codEDatiPagCodiceEsitoPagamento"+
      ", num_e_dati_pag_importo_totale_pagato = :r.numEDatiPagImportoTotalePagato"+
      ", cod_e_dati_pag_id_univoco_versamento = :r.codEDatiPagIdUnivocoVersamento"+
      ", cod_e_dati_pag_codice_contesto_pagamento = :r.codEDatiPagCodiceContestoPagamento" +
      ", id_session = :r.idSession"+
      ", modello_pagamento = :r.modelloPagamento"+
      ", cod_e_silinviaesito_original_fault_code = :r.codESilinviaesitoOriginalFaultCode"+
      ", de_e_silinviaesito_original_fault_string = :r.deESilinviaesitoOriginalFaultString"+
      ", de_e_silinviaesito_original_fault_description = :r.deESilinviaesitoOriginalFaultDescription"+
      ", cod_rp_silinviarp_original_fault_code = :r.codRpSilinviarpOriginalFaultCode"+
      ", de_rp_silinviarp_original_fault_string = :r.deRpSilinviarpOriginalFaultString"+
      ", de_rp_silinviarp_original_fault_description = :r.deRpSilinviarpOriginalFaultDescription"+
      " where mygov_rp_e_id = :r.mygovRpEId"
  )
  int update(@BindBean("r") RpE r);

}
