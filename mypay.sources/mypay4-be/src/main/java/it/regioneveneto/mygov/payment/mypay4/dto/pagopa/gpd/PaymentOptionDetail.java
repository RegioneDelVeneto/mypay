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
public class PaymentOptionDetail {

  public enum PAY_OPT_STATUS { PO_UNPAID, PO_PAID, PO_PARTIALLY_REPORTED, PO_REPORTED }

  private String nav;
  private String iuv;
  private String organizationFiscalCode;
  private Long amount;
  private String description;
  private boolean isPartialPayment;
  private String dueDate;
  private String retentionDate;
  private String paymentDate;
  private String reportingDate;
  private String insertingDate;
  private String paymentMethod;
  private Long fee;
  private Long notificationFee;
  private String pspCompany;
  private String idReceipt;
  private String idFlowReporting;
  private PAY_OPT_STATUS status;
  private String lastUpdatedDate;
  private List<TransferDetail> transfer;
}
