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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "rtConservazioneId")
public class RT_Conservazione extends BaseEntity {

    public static final String ALIAS = "FESP_RT_Conservazione";
    public static final String FIELDS = ""+ALIAS+".rt_conservazione_id as FESP_RT_Conservazione_rtConservazioneId" +
            ","+ALIAS+".version as FESP_RT_Conservazione_version"+
            ","+ALIAS+".rpt_rt_estrazione_id as FESP_RT_Conservazione_rptRtEstrazioneId"+
            ","+ALIAS+".mygov_rpt_rt_id as FESP_RT_Conservazione_mygovRptRtId"+
            ","+ALIAS+".identificativo_dominio as FESP_RT_Conservazione_identificativoDominio"+
            ","+ALIAS+".identificativo_univoco_versamento as FESP_RT_Conservazione_identificativoUnivocoVersamento"+
            ","+ALIAS+".codice_contesto_pagamento as FESP_RT_Conservazione_codiceContestoPagamento"+
            ","+ALIAS+".identificativo as FESP_RT_Conservazione_identificativo"+
            ","+ALIAS+".rt_xml as FESP_RT_Conservazione_rtXML"+
            ","+ALIAS+".data_registrazione as FESP_RT_Conservazione_dataRegistrazione"+
            ","+ALIAS+".oggetto as FESP_RT_Conservazione_oggetto"+
            ","+ALIAS+".tipo_soggetto_destinatario as FESP_RT_Conservazione_tipoSoggettoDestinatario"+
            ","+ALIAS+".nominativo_destinatario as FESP_RT_Conservazione_nominativoDestinatario"+
            ","+ALIAS+".identificativo_destinatario as FESP_RT_Conservazione_identificativoDestinatario"+
            ","+ALIAS+".indirizzo_riferimento_destinatario as FESP_RT_Conservazione_indirizzoRiferimentoDestinatario"+
            ","+ALIAS+".identificativo_beneficiario as FESP_RT_Conservazione_identificativoBeneficiario"+
            ","+ALIAS+".id_aggregazione as FESP_RT_Conservazione_idAggregazione"+
            ","+ALIAS+".identificativo_versante as FESP_RT_Conservazione_identificativoVersante"+
            ","+ALIAS+".nominativo_versante as FESP_RT_Conservazione_nominativoVersante"+
            ","+ALIAS+".esito_pagamento as FESP_RT_Conservazione_esitoPagamento"+
            ","+ALIAS+".esito_conservazione as FESP_RT_Conservazione_esitoConservazione"+
            ","+ALIAS+".errore_conservazione as FESP_RT_Conservazione_erroreConservazione"+
            ","+ALIAS+".data_conservazione as FESP_RT_Conservazione_dataConservazione"+
            ","+ALIAS+".id_conservazione as FESP_RT_Conservazione_idConservazione"+
             ","+ALIAS+".tipo_documento as FESP_RT_Conservazione_tipoDocumento"
            ;

    private long rtConservazioneId;
    private int version;
    private Long rptRtEstrazioneId; ////Sempre null
    private Long mygovRptRtId;
    private String identificativoDominio;
    private String identificativoUnivocoVersamento;
    private String codiceContestoPagamento;
    private String identificativo;
    private byte[] rtXML;
    private Date dataRegistrazione;
    private String oggetto;
    private String tipoSoggettoDestinatario;
    private String nominativoDestinatario;
    private String identificativoDestinatario;
    private String indirizzoRiferimentoDestinatario;
    private String identificativoBeneficiario;
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
