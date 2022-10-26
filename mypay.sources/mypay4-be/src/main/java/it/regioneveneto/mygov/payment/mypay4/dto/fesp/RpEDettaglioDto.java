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
public class RpEDettaglioDto implements Serializable {

  private Long mygovRpEDettaglioId;

  private BigDecimal numRpDatiVersDatiSingVersImportoSingoloVersamento;

  private BigDecimal numRpDatiVersDatiSingVersCommissioneCaricoPa;

  private String codRpDatiVersDatiSingVersIbanAccredito;

  private String codRpDatiVersDatiSingVersBicAccredito;

  private String codRpDatiVersDatiSingVersIbanAppoggio;

  private String codRpDatiVersDatiSingVersBicAppoggio;

  private String codRpDatiVersDatiSingVersCredenzialiPagatore;

  private String deRpDatiVersDatiSingVersCausaleVersamento;

  private String deRpDatiVersDatiSingVersDatiSpecificiRiscossione;

  private BigDecimal numEDatiPagDatiSingPagSingoloImportoPagato;

  private String deEDatiPagDatiSingPagEsitoSingoloPagamento;

  private Date dtEDatiPagDatiSingPagDataEsitoSingoloPagamento;

  private String codEDatiPagDatiSingPagIdUnivocoRiscoss;

  private String deEDatiPagDatiSingPagCausaleVersamento;

  private String deEDatiPagDatiSingPagDatiSpecificiRiscossione;

  private BigDecimal numEDatiPagDatiSingPagCommissioniApplicatePsp;

  private String codRpDatiVersDatiSingVersDatiMbdTipoBollo;

  private String codRpDatiVersDatiSingVersDatiMbdHashDocumento;

  private String codRpDatiVersDatiSingVersDatiMbdProvinciaResidenza;

  private String codEDatiPagDatiSingPagAllegatoRicevutaTipo;

  private byte[] blbEDatiPagDatiSingPagAllegatoRicevutaTest;
}
