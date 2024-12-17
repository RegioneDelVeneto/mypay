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
package it.regioneveneto.mygov.payment.mypay4.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ListWithCount<T> extends ArrayList<T> {

  private final int count, limit;
  private final boolean incomplete;

  public <R> ListWithCount<R> makeClone(){
    return new ListWithCount<>(new ArrayList<>(), this.limit, this.count);
  }

  public ListWithCount(List<T> originalList, int limit, int count){
    super(originalList);
    this.limit = limit;
    this.count = count;
    this.incomplete = limit<count;
  }

  public ListWithCount(List<T> originalList, int limit){
    super(originalList);
    this.limit = limit;
    this.count = originalList.size();
    this.incomplete = false;
  }

}
