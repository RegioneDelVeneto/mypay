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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Possibly<T> {
  final T value;
  final Exception exception;
  private Possibly(T value) {
    if(value == null)
      throw new RuntimeException(
          "value of Possibly cannot be null");
    this.value = value;
    this.exception = null;
  }
  private Possibly(Exception exception) {
    if(exception == null)
      throw new RuntimeException(
          "exception of Possibly cannot be null");
    this.value = null;
    this.exception = exception;
  }
  public static <T> Possibly<T> of(T value ) {
    return new Possibly(value);
  }
  public static <T> Possibly<T> of(Exception exception ) {
    return new Possibly(exception);
  }
  public static <T> Possibly<T> of(Supplier<T> supplier) {
    try{
      return new Possibly<>(supplier.get());
    }catch(Exception e) {
      return new Possibly(e);
    }
  }
  public static <P,T> Function<P,Possibly<T>> of(Function<P,T> function) {
    return (P p) -> {
      try {
        return new Possibly<>(function.apply(p));
      } catch (Exception e) {
        return new Possibly(e);
      }
    };
  }

  public boolean is() {
    return value != null;
  }
  public Possibly<T> doIfException(Consumer<Exception> action) {
    if(exception != null) {
      action.accept(exception);
    }
    return this;
  }
  public T orIfException(T valueIfException){
    return is() ? value : valueIfException;
  }
  public T orNull(){
    return is() ? value : null;
  }
  public Optional<T> getValue() {
    return Optional.ofNullable(value);
  }
  public Optional<Exception> getException() {
    return Optional.ofNullable(exception);
  }

  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if(is())
      return value;
    throw exceptionSupplier.get();
  }
}
