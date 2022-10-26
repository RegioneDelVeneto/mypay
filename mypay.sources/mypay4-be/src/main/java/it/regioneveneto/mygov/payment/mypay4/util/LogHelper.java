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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LogHelper {

  public static String methodToShortString(Method method) {
    return methodToString(method, false, false, false, false, false, false);
  }

  public static String methodToFullString(Method method) {
    return methodToString(method,true, true, true, true, true, true);
  }

  public static String methodToLongString(Method method) {
    return methodToString(method,true, true, true, true, false, false);
  }

  public static String methodToString(Method method) {
    return methodToString(method,false, false, true, false, false, false);
  }

  public static String methodToFullNameString(Method method) {
    return method.getDeclaringClass().getName()+"."+method.getName();
  }


  private static String methodToString(Method method, boolean includeModifier, boolean includeDeclaringClass, boolean includeArgs, boolean includeReturnType,
                         boolean useLongReturnAndArgumentTypeName, boolean useLongTypeName) {

    StringBuilder sb = new StringBuilder();
    if (includeModifier) {
      sb.append(Modifier.toString(method.getModifiers()));
      sb.append(" ");
    }
    if (includeReturnType) {
      appendType(sb, method.getReturnType(), useLongReturnAndArgumentTypeName);
      sb.append(" ");
    }
    if (includeDeclaringClass) {
      appendType(sb, method.getDeclaringClass(), useLongTypeName);
      sb.append(".");
    }
    sb.append(method.getName());
      sb.append("(");
      Class<?>[] parametersTypes = method.getParameterTypes();
      appendTypes(sb, parametersTypes, includeArgs, useLongReturnAndArgumentTypeName);
      sb.append(")");
    return sb.toString();
  }

  private static void appendTypes(StringBuilder sb, Class<?>[] types, boolean includeArgs,
                           boolean useLongReturnAndArgumentTypeName) {

    if (includeArgs) {
      for (int size = types.length, i = 0; i < size; i++) {
        appendType(sb, types[i], useLongReturnAndArgumentTypeName);
        if (i < size - 1) {
          sb.append(",");
        }
      }
    }
    else {
      if (types.length != 0) {
        sb.append("..");
      }
    }
  }

  private static void appendType(StringBuilder sb, Class<?> type, boolean useLongTypeName) {
    if (type.isArray()) {
      appendType(sb, type.getComponentType(), useLongTypeName);
      sb.append("[]");
    }
    else {
      sb.append(useLongTypeName ? type.getName() : type.getSimpleName());
    }
  }
}
