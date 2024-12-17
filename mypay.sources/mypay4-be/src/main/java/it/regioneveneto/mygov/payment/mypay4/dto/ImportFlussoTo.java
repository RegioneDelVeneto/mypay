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
package it.regioneveneto.mygov.payment.mypay4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ImportFlussoTo {
    private String IUD;
    private String codIuv;
    private String tipoIdentificativoUnivoco;
    private String codiceIdentificativoUnivoco;
    private String anagraficaPagatore;
    private String indirizzoPagatore;
    private String civicoPagatore;
    private String capPagatore;
    private String localitaPagatore;
    private String provinciaPagatore;
    private String nazionePagatore;
    private String emailPagatore;
    private Date dataScadenzaPagamento;
    private Double importoDovuto;
    private Double commissioneCaricoPa;
    private String tipoDovuto;
    private String tipoVersamento;
    private String causaleVersamento;
    private String datiSpecificiRiscossione;
    private String bilancio;
    private String flagGeneraIuv;
    private String flagMultiBeneficiario;
    private String codiceFiscaleEnteSecondario;
    private String denominazioneEnteSecondario;
    private String ibanAccreditoEnteSecondario;
    private String indirizzoEnteSecondario;
    private String civicoEnteSecondario;
    private String capEnteSecondario;
    private String localitaEnteSecondario;
    private String provinciaEnteSecondario;
    private String nazioneEnteSecondario;
    private String datiSpecificiRiscossioneEnteSecondario;
    private String causaleVersamentoEnteSecondario;
    private Double importoVersamentoEnteSecondario;
    private String azione;
    private String numRigaFlusso;
    private String codErrore;
    private String deErrore;
    private BigInteger idFlusso;
    private String iuf;
}
