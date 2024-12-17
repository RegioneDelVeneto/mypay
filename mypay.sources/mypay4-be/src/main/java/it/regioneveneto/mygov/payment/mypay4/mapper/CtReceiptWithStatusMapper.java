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
package it.regioneveneto.mygov.payment.mypay4.mapper;

import it.regioneveneto.mygov.payment.mypay4.dto.fesp.CtReceiptWithStatus;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@NoArgsConstructor
public class CtReceiptWithStatusMapper extends CtReceiptAbstractMapper implements RowMapper<CtReceiptWithStatus> {

  @Override
  public CtReceiptWithStatus map(ResultSet rs, StatementContext ctx) throws SQLException {
    CtReceiptWithStatus ctReceipt = new CtReceiptWithStatus();
    ctReceipt = super.baseCtReceiptMap(ctReceipt, rs, ctx);
    //status of fesp.mygov_receipt
    ctReceipt.setStatus(rs.getString("status"));
    //original xml of receipt by PagoPa
    ctReceipt.setReceiptBytes(rs.getBytes("receipt_bytes"));

    return ctReceipt;
  }

}
