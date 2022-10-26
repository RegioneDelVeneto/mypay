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

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DovutoDipendente extends BaseTo implements Serializable {

  private String name;
  private String validDependsOn;
  private String validDependsOnUids;
  private String valueDependsOn;
  private String valueDependsOnUids;
  private String hiddenDependsOn;
  private String hiddenDependsOnUids;
  private String mandatoryDependsOn;
  private String mandatoryDependsOnUids;
  private String enabledDependsOn;
  private String enabledDependsOnUids;
  private String causaleFunction;
  private String causaleFunctionUids;
  private String causaleVisualizzataFunction;
  private String causaleVisualizzataFunctionUids;
  private String renderType;
}
