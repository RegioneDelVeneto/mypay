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

import it.regioneveneto.mygov.payment.mypay4.dto.DovutoMultibeneficiarioTo;
import it.regioneveneto.mygov.payment.mypay4.model.DovutoMultibeneficiario;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.math.BigDecimal;
import java.util.List;

public interface DovutoMultibeneficiarioDao extends BaseDao {
	
	  @SqlUpdate(" insert into mygov_dovuto_multibeneficiario (" +
		  "   mygov_dovuto_multibeneficiario_id"+
		  "	, cod_iud"+
		  " , cod_iuv"+
		  " , codice_fiscale_ente"+
		  " , de_rp_ente_benef_denominazione_beneficiario"+
		  " , cod_rp_dati_vers_dati_sing_vers_iban_accredito"+
		  " , de_rp_ente_benef_indirizzo_beneficiario"+
		  " , de_rp_ente_benef_civico_beneficiario"+
		  " , cod_rp_ente_benef_cap_beneficiario"+
		  " , de_rp_ente_benef_localita_beneficiario"+
		  " , de_rp_ente_benef_provincia_beneficiario"+
		  " , cod_rp_ente_benef_nazione_beneficiario"+
		  " , de_rp_dati_vers_dati_sing_vers_causale_versamento"+
		  " , de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione"+
		  " , num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento"+
		  " , mygov_Dovuto_id"+
		  " , dt_creazione"+
		  " , dt_ultima_modifica"+
		  ") values ("+
		  "   nextval('mygov_dovuto_multibenef_mygov_dovuto_multibenef_id_seq')"+
		  " , :dM.codIud"+
		  " , :dM.codIuv"+
		  " , :dM.codiceFiscaleEnte"+
		  " , :dM.deRpEnteBenefDenominazioneBeneficiario"+
		  " , :dM.codRpDatiVersDatiSingVersIbanAccredito"+
		  " , :dM.deRpEnteBenefIndirizzoBeneficiario"+
		  " , :dM.deRpEnteBenefCivicoBeneficiario"+
		  " , :dM.codRpEnteBenefCapBeneficiario"+
		  " , :dM.deRpEnteBenefLocalitaBeneficiario"+
		  " , :dM.deRpEnteBenefProvinciaBeneficiario"+
		  " , :dM.codRpEnteBenefNazioneBeneficiario"+
		  " , :dM.deRpDatiVersDatiSingVersCausaleVersamento"+
	      " , :dM.deRpDatiVersDatiSingVersDatiSpecificiRiscossione"+
		  " , :dM.numRpDatiVersDatiSingVersImportoSingoloVersamento"+
		  " , :dM.mygovDovutoId?.mygovDovutoId"+
		  " , :dM.dtCreazione"+
		  " , :dM.dtUltimaModifica)")
	  @GetGeneratedKeys("mygov_dovuto_multibeneficiario_id")
	  long insert(@BindBean("dM") DovutoMultibeneficiario dM);
	  
	  @SqlQuery("SELECT " +
			      DovutoMultibeneficiario.ALIAS + ".mygov_dovuto_id, " +
	              DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_denominazione_beneficiario, " +
	              DovutoMultibeneficiario.ALIAS + ".codice_fiscale_ente, " +
	              DovutoMultibeneficiario.ALIAS + ".cod_rp_dati_vers_dati_sing_vers_iban_accredito, " +
	              DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_indirizzo_beneficiario, " +
	              DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_civico_beneficiario, " +
	              DovutoMultibeneficiario.ALIAS + ".cod_rp_ente_benef_cap_beneficiario, " +
	              DovutoMultibeneficiario.ALIAS + ".cod_rp_ente_benef_nazione_beneficiario, " +
	              DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_provincia_beneficiario, " +
	              DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_localita_beneficiario, " +
				  DovutoMultibeneficiario.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento, " +
			      DovutoMultibeneficiario.ALIAS + ".de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, " +
	              DovutoMultibeneficiario.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento "  +
	              "FROM mygov_dovuto_multibeneficiario " +  DovutoMultibeneficiario.ALIAS +
	              " WHERE "+DovutoMultibeneficiario.ALIAS+".mygov_dovuto_id = :id"
	              )
	  DovutoMultibeneficiarioTo getDovutoMultibeneficiarioByIdDovuto(Long id);


	@SqlUpdate(" update mygov_dovuto_multibeneficiario " +
			" set "+
			"   cod_iud = :dM.codIud"+
			" , cod_iuv = :dM.codIuv"+
			" , codice_fiscale_ente = :dM.codiceFiscaleEnte"+
			" , de_rp_ente_benef_denominazione_beneficiario = :dM.deRpEnteBenefDenominazioneBeneficiario"+
			" , cod_rp_dati_vers_dati_sing_vers_iban_accredito = :dM.codRpDatiVersDatiSingVersIbanAccredito"+
			" , de_rp_ente_benef_indirizzo_beneficiario = :dM.deRpEnteBenefIndirizzoBeneficiario"+
			" , de_rp_ente_benef_civico_beneficiario = :dM.deRpEnteBenefCivicoBeneficiario"+
			" , cod_rp_ente_benef_cap_beneficiario = :dM.codRpEnteBenefCapBeneficiario"+
			" , de_rp_ente_benef_localita_beneficiario = :dM.deRpEnteBenefLocalitaBeneficiario"+
			" , de_rp_ente_benef_provincia_beneficiario = :dM.deRpEnteBenefProvinciaBeneficiario"+
			" , cod_rp_ente_benef_nazione_beneficiario = :dM.codRpEnteBenefNazioneBeneficiario"+
			" , de_rp_dati_vers_dati_sing_vers_causale_versamento = :dM.deRpDatiVersDatiSingVersCausaleVersamento"+
			" , de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione = :dM.deRpDatiVersDatiSingVersDatiSpecificiRiscossione"+
			" , num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento = :dM.numRpDatiVersDatiSingVersImportoSingoloVersamento"+
			" , dt_ultima_modifica = :dM.dtUltimaModifica "+
			" where mygov_dovuto_multibeneficiario_id = :dM.mygovDovutoMultibeneficiarioId")
	int update(@BindBean("dM") DovutoMultibeneficiario dM);


	@SqlQuery("SELECT " +
			DovutoMultibeneficiario.ALIAS + ".mygov_dovuto_multibeneficiario_id, " +
			DovutoMultibeneficiario.ALIAS + ".cod_iud, " +
			DovutoMultibeneficiario.ALIAS + ".cod_iuv, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_denominazione_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".codice_fiscale_ente, " +
			DovutoMultibeneficiario.ALIAS + ".cod_rp_dati_vers_dati_sing_vers_iban_accredito, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_indirizzo_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_civico_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".cod_rp_ente_benef_cap_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".cod_rp_ente_benef_nazione_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_provincia_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_localita_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento, " +
			DovutoMultibeneficiario.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento "  +
			"FROM mygov_dovuto_multibeneficiario " +  DovutoMultibeneficiario.ALIAS +
			" WHERE "+DovutoMultibeneficiario.ALIAS+".mygov_dovuto_id = :idDovuto"
	)
	@RegisterFieldMapper(DovutoMultibeneficiario.class)
	DovutoMultibeneficiario getByIdDovuto(Long idDovuto);

	@SqlUpdate(" delete from mygov_dovuto_multibeneficiario " +
			" where mygov_dovuto_multibeneficiario_id = :dM.mygovDovutoMultibeneficiarioId ")
	int delete(@BindBean("dM") DovutoMultibeneficiario dM);

	@SqlUpdate(" delete from mygov_dovuto_multibeneficiario " +
			" where mygov_dovuto_id = :idDovuto ")
	int deleteByIdDovuto(Long idDovuto);

	@SqlUpdate(
			" delete from mygov_dovuto_multibeneficiario " +
					" where mygov_dovuto_id in (<ids>)")
	int deleteByIdsDovuti(@BindList(onEmpty= BindList.EmptyHandling.NULL_STRING) List<Long> ids);

	@SqlQuery("SELECT " +
			DovutoMultibeneficiario.ALIAS + ".mygov_dovuto_id, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_denominazione_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".codice_fiscale_ente, " +
			DovutoMultibeneficiario.ALIAS + ".cod_rp_dati_vers_dati_sing_vers_iban_accredito, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_indirizzo_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_civico_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".cod_rp_ente_benef_cap_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".cod_rp_ente_benef_nazione_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_provincia_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_ente_benef_localita_beneficiario, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione, " +
			DovutoMultibeneficiario.ALIAS + ".de_rp_dati_vers_dati_sing_vers_causale_versamento, " +
			DovutoMultibeneficiario.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento "  +
			"FROM mygov_dovuto_multibeneficiario " +  DovutoMultibeneficiario.ALIAS +
			" WHERE "+DovutoMultibeneficiario.ALIAS+".mygov_dovuto_id IN (<ids>)"
	)
	List<DovutoMultibeneficiarioTo> getListDovutoMultibeneficiarioByIdDovuto(@BindList("ids") List<Long> ids);

	@SqlQuery("SELECT " + "COUNT(" + DovutoMultibeneficiario.ALIAS + ".mygov_dovuto_multibeneficiario_id" +")"
			+ " FROM mygov_dovuto_multibeneficiario " +  DovutoMultibeneficiario.ALIAS +
			   " WHERE " +DovutoMultibeneficiario.ALIAS+".mygov_dovuto_id = :id")
	int checkExsistDovutoMultibeneficiarioByIdDovuto(long id);

	@SqlQuery("SELECT " +
			DovutoMultibeneficiario.ALIAS + ".num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento "  +
			"FROM mygov_dovuto_multibeneficiario " +  DovutoMultibeneficiario.ALIAS +
			" WHERE "+DovutoMultibeneficiario.ALIAS+".mygov_dovuto_id = :idDovuto"
	)
	BigDecimal getImportoDovutoMbByIdDovuto(Long idDovuto);

}
