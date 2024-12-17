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
package it.regioneveneto.mygov.payment.mypay4.dto.pagopa.gpd;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DebtPositionDetail {

  public enum PERSON_TYPE { F, G }
  public enum DEBT_POS_STATUS { DRAFT, PUBLISHED, VALID, INVALID, EXPIRED, PARTIALLY_PAID, PAID, REPORTED }

  private String iupd;
  private String organizationFiscalCode;
  private PERSON_TYPE type;
  private String companyName;
  private String officeName;
  private String insertedDate;
  private String publishDate;
  private String validityDate;
  private String paymentDate;
  private DEBT_POS_STATUS status;
  private String lastUpdatedDate;
  private List<PaymentOptionDetail> paymentOption;
}
