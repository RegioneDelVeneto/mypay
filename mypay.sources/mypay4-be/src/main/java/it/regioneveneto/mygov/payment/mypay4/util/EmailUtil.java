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

import java.net.IDN;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailUtil {


  public static void main(String[] args) {
    String[] emailToValidate = {
            "a.b.c.@hotmail.it",
            " testconspazio@gmail.com"
    };

    for (String email : emailToValidate) {
      System.out.println(email + " : " + validateEmail(email));
    }
  }

  public static boolean validateEmail(CharSequence email) {
    return isValid(email);
  }


  private static final int MAX_LOCAL_PART_LENGTH = 64;

  private static final String LOCAL_PART_ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~\u0080-\uFFFF-]";
  private static final String LOCAL_PART_INSIDE_QUOTES_ATOM = "(?:[a-z0-9!#$%&'*.(),<>\\[\\]:;  @+/=?^_`{|}~\u0080-\uFFFF-]|\\\\\\\\|\\\\\\\")";
  /**
   * Regular expression for the local part of an email address (everything before '@')
   */
  private static final Pattern LOCAL_PART_PATTERN = Pattern.compile(
          "(?:" + LOCAL_PART_ATOM + "+|\"" + LOCAL_PART_INSIDE_QUOTES_ATOM + "+\")" +
                  "(?:\\." + "(?:" + LOCAL_PART_ATOM + "+|\"" + LOCAL_PART_INSIDE_QUOTES_ATOM + "+\")" + ")*",
          Pattern.CASE_INSENSITIVE
  );

  private static final int MAX_DOMAIN_PART_LENGTH = 255;

  private static final String DOMAIN_CHARS_WITHOUT_DASH = "[a-z\u0080-\uFFFF0-9!#$%&'*+/=?^_`{|}~]";
  private static final String DOMAIN_LABEL = DOMAIN_CHARS_WITHOUT_DASH + "++(?:-++" + DOMAIN_CHARS_WITHOUT_DASH + "++)*+";
  private static final String DOMAIN = DOMAIN_LABEL + "(?:\\." + DOMAIN_LABEL + ")*+";

  private static final String IP_DOMAIN = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
  //IP v6 regex taken from http://stackoverflow.com/questions/53497/regular-expression-that-matches-valid-ipv6-addresses
  private static final String IP_V6_DOMAIN =
          "(?:(?:[0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|(?:[0-9a-fA-F]{1,4}:){1,7}:|(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}|(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}|(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}|(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:(?:(?::[0-9a-fA-F]{1,4}){1,6})|:(?:(?::[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(?::[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(?:ffff(:0{1,4}){0,1}:){0,1}(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])|(?:[0-9a-fA-F]{1,4}:){1,4}:(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";


  private static boolean isValid(CharSequence value) {
    if (value == null || value.length() == 0) {
      return true;
    }

    // cannot split email string at @ as it can be a part of quoted local part of email.
    // so we need to split at a position of last @ present in the string:
    String stringValue = value.toString();
    int splitPosition = stringValue.lastIndexOf('@');

    // need to check if
    if (splitPosition < 0) {
      return false;
    }

    String localPart = stringValue.substring(0, splitPosition);
    String domainPart = stringValue.substring(splitPosition + 1);

    if (!isValidEmailLocalPart(localPart)) {
      return false;
    }

    return isValidEmailDomainAddress(domainPart);
  }

  private static boolean isValidEmailLocalPart(String localPart) {
    if (localPart.length() > MAX_LOCAL_PART_LENGTH) {
      return false;
    }
    Matcher matcher = LOCAL_PART_PATTERN.matcher(localPart);
    return matcher.matches();
  }


  /**
   * This is the maximum length of a domain name. But be aware that each label (parts separated by a dot) of the
   * domain name must be at most 63 characters long. This is verified by {@link IDN#toASCII(String)}.
   */

  /**
   * Regular expression for the domain part of an URL
   * <p>
   * A host string must be a domain string, an IPv4 address string, or "[", followed by an IPv6 address string,
   * followed by "]".
   */
  private static final Pattern DOMAIN_PATTERN = Pattern.compile(
          DOMAIN + "|\\[" + IP_V6_DOMAIN + "\\]", Pattern.CASE_INSENSITIVE
  );

  /**
   * Regular expression for the domain part of an email address (everything after '@')
   */
  private static final Pattern EMAIL_DOMAIN_PATTERN = Pattern.compile(
          DOMAIN + "|\\[" + IP_DOMAIN + "\\]|" + "\\[IPv6:" + IP_V6_DOMAIN + "\\]", Pattern.CASE_INSENSITIVE
  );


  private static boolean isValidEmailDomainAddress(String domain) {
    return isValidDomainAddress(domain, EMAIL_DOMAIN_PATTERN);
  }


  private static boolean isValidDomainAddress(String domain) {
    return isValidDomainAddress(domain, DOMAIN_PATTERN);
  }

  private static boolean isValidDomainAddress(String domain, Pattern pattern) {
    // if we have a trailing dot the domain part we have an invalid email address.
    // the regular expression match would take care of this, but IDN.toASCII drops the trailing '.'
    if (domain.endsWith(".")) {
      return false;
    }

    String asciiString;
    try {
      asciiString = IDN.toASCII(domain);
    } catch (IllegalArgumentException e) {
      return false;
    }

    if (asciiString.length() > MAX_DOMAIN_PART_LENGTH) {
      return false;
    }

    Matcher matcher = pattern.matcher(domain);
    return matcher.matches();
  }


}