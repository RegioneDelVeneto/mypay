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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class MaxResultsHelper {

  @Value("${jdbc.limit.default:1000}")
  private int defaultQueryLimit;

  public <T> ListWithCount<T> manageMaxResults(Function<Integer, List<T>> queryFunction, Supplier<Integer> countFunction, int maxResults){
    return manageMaxResults(queryFunction, null, countFunction, maxResults);
  }

  public <T> ListWithCount<T> manageMaxResults(Function<Integer, List<T>> queryFunction, Supplier<Integer> countFunction){
    return manageMaxResults(queryFunction, countFunction, this.defaultQueryLimit);
  }

  public <T, R> ListWithCount<R> manageMaxResults(Function<Integer, List<T>> queryFunction, Function<T, R> mapper, Supplier<Integer> countFunction){
    return manageMaxResults(queryFunction, mapper, countFunction, this.defaultQueryLimit);
  }

  public <T, R> ListWithCount<R> manageMaxResults(Function<Integer, List<T>> queryFunction, Function<T, R> mapper, Supplier<Integer> countFunction, int maxResults){
    List<T> resultsToMap = queryFunction.apply(maxResults);
    List<R> results;
    if(mapper!=null)
      results = resultsToMap.stream().map(mapper).collect(Collectors.toList());
    else
      results = (List<R>) resultsToMap;
    if(results.size()==maxResults) {
      int count = countFunction.get();
      return new ListWithCount<>(results, maxResults, count);
    }
    return new ListWithCount<>(results, maxResults);
  }


}
