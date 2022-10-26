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
package it.regioneveneto.mygov.payment.mypay4.dto.fesp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RptRtDettaglioDto implements Serializable {
  private Long mygovRptRtDettaglioId;

  private BigDecimal numRptDatiVersDatiSingVersImportoSingoloVersamento;

  private BigDecimal numRptDatiVersDatiSingVersCommissioneCaricoPa;

  private String deRptDatiVersDatiSingVersIbanAccredito;

  private String deRptDatiVersDatiSingVersBicAccredito;

  private String deRptDatiVersDatiSingVersIbanAppoggio;

  private String deRptDatiVersDatiSingVersBicAppoggio;

  private String codRptDatiVersDatiSingVersCredenzialiPagatore;

  private String deRptDatiVersDatiSingVersCausaleVersamento;

  private String deRptDatiVersDatiSingVersDatiSpecificiRiscossione;

  private BigDecimal numRtDatiPagDatiSingPagSingoloImportoPagato;

  private String deRtDatiPagDatiSingPagEsitoSingoloPagamento;

  private Date dtRtDatiPagDatiSingPagDataEsitoSingoloPagamento;

  private String codRtDatiPagDatiSingPagIdUnivocoRiscossione;

  private String deRtDatiPagDatiSingPagCausaleVersamento;

  private String deRtDatiPagDatiSingPagDatiSpecificiRiscossione;

  private BigDecimal numRtDatiPagDatiSingPagCommissioniApplicatePsp;

  private String codRtDatiPagDatiSingPagAllegatoRicevutaTipo;

  private byte[] blbRtDatiPagDatiSingPagAllegatoRicevutaTest;

  private String codRptDatiVersDatiSingVersDatiMbdTipoBollo;

  private String codRptDatiVersDatiSingVersDatiMbdHashDocumento;

  private String codRptDatiVersDatiSingVersDatiMbdProvinciaResidenza;
}
