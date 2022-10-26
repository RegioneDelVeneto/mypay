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
package it.regioneveneto.mygov.payment.mypay4.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to configure the logging of a method execution through the aspect @{@link LogExecutionAspect}.
 *
 * @param {@link ParamMode} enabled (default ON). If set to OFF, the logging will be disabled.
 * @param {@link ParamMode} params (default AUTO). If set to ON, the input params will be logged even if log level
 * is < DEBUG. If set to OFF, the input params will never be logged. If set to AUTO, the input params will be logged
 * only if log level >= DEBUG.
 * @param {@link ParamMode} returns (default AUTO). If set to ON, the return value will be logged even if log level
 * is < DEBUG. If set to OFF, the return value will never be logged. If set to AUTO, the return value will be logged
 * only if log level >= DEBUG.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecution {
  enum ParamMode { AUTO, ON, OFF }

  ParamMode enabled() default ParamMode.ON;
  ParamMode params() default ParamMode.AUTO;
  ParamMode returns() default ParamMode.AUTO;
}


