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
package it.regioneveneto.mygov.payment.mypay4.model.fesp;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import it.regioneveneto.mygov.payment.mypay4.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "rptConservazioneId")
public class RPT_Conservazione extends BaseEntity {

    public static final String ALIAS = "FESP_RPT_Conservazione";
    public static final String FIELDS = ""+ALIAS+".rpt_conservazione_id as FESP_RPT_Conservazione_rptConservazioneId,"+ALIAS+".version as FESP_RPT_Conservazione_version"+
            ","+ALIAS+".rpt_rt_estrazione_id as FESP_RPT_Conservazione_rptRtEstrazioneId"+
            ","+ALIAS+".mygov_giornale_id as FESP_RPT_Conservazione_mygovGiornaleId"+
            ","+ALIAS+".mygov_rpt_rt_id as FESP_RPT_Conservazione_mygovRptRtId"+
            ","+ALIAS+".identificativo_dominio as FESP_RPT_Conservazione_identificativoDominio"+
            ","+ALIAS+".identificativo_univoco_versamento as FESP_RPT_Conservazione_identificativoUnivocoVersamento"+
            ","+ALIAS+".codice_contesto_pagamento as FESP_RPT_Conservazione_codiceContestoPagamento"+
            ","+ALIAS+".identificativo as FESP_RPT_Conservazione_identificativo"+
            ","+ALIAS+".rpt_xml as FESP_RPT_Conservazione_rptXML"+
            ","+ALIAS+".data_registrazione as FESP_RPT_Conservazione_dataRegistrazione"+
            ","+ALIAS+".oggetto as FESP_RPT_Conservazione_oggetto"+
            ","+ALIAS+".tipo_soggetto_pagatore as FESP_RPT_Conservazione_tipoSoggettoPagatore"+
            ","+ALIAS+".nominativo_pagatore as FESP_RPT_Conservazione_nominativoPagatore"+
            ","+ALIAS+".identificativo_pagatore as FESP_RPT_Conservazione_identificativoPagatore"+
            ","+ALIAS+".indirizzo_riferimento_pagatore as FESP_RPT_Conservazione_indirizzoRiferimentoPagatore"+
            ","+ALIAS+".tipo_soggetto_beneficiario as FESP_RPT_Conservazione_tipoSoggettoBeneficiario"+
            ","+ALIAS+".nominativo_beneficiario as FESP_RPT_Conservazione_nominativoBeneficiario"+
            ","+ALIAS+".identificativo_beneficiario as FESP_RPT_Conservazione_identificativoBeneficiario"+
            ","+ALIAS+".indirizzo_riferimento_beneficiario as FESP_RPT_Conservazione_indirizzoRiferimentoBeneficiario"+
            ","+ALIAS+".id_aggregazione as FESP_RPT_Conservazione_idAggregazione"+
            ","+ALIAS+".identificativo_versante as FESP_RPT_Conservazione_identificativoVersante"+
            ","+ALIAS+".nominativo_versante as FESP_RPT_Conservazione_nominativoVersante"+
            ","+ALIAS+".esito_pagamento as FESP_RPT_Conservazione_esitoPagamento"+
            ","+ALIAS+".esito_conservazione as FESP_RPT_Conservazione_esitoConservazione"+
            ","+ALIAS+".errore_conservazione as FESP_RPT_Conservazione_erroreConservazione"+
            ","+ALIAS+".data_conservazione as FESP_RPT_Conservazione_dataConservazione"+
            ","+ALIAS+".id_conservazione as FESP_RPT_Conservazione_idConservazione"+
               ","+ALIAS+".tipo_documento as FESP_RPT_Conservazione_tipoDocumento";

    private Long rptConservazioneId;
    private int version;
    private Long rptRtEstrazioneId;////Sempre null
    private Long mygovGiornaleId;//Sempre null
    private Long mygovRptRtId;
    private String identificativoDominio;
    private String identificativoUnivocoVersamento;
    private String codiceContestoPagamento;
    private String identificativo;
    private String rptXML;
    private Date dataRegistrazione;
    private String oggetto;
    private String tipoSoggettoPagatore;
    private String nominativoPagatore;
    private String identificativoPagatore;
    private String indirizzoRiferimentoPagatore;
    private String tipoSoggettoBeneficiario;
    private String nominativoBeneficiario;
    private String identificativoBeneficiario;
    private String indirizzoRiferimentoBeneficiario;
    private String idAggregazione;
    private String identificativoVersante;
    private String nominativoVersante;
    private String esitoPagamento; //LO devo aggiornare in esito??
    private String esitoConservazione;
    private String erroreConservazione;
    private String dataConservazione;
    private String idConservazione;

    private String tipoDocumento;
}
