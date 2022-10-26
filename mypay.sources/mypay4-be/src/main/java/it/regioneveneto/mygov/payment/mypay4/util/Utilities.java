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

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.ValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.*;
import it.regioneveneto.mygov.payment.mypay4.util.render.FieldBean;
import it.veneto.regione.schemas._2012.pagamenti.ente.Bilancio;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtAccertamento;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtCapitolo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Utilities {

  private final static ThreadLocal<SimpleDateFormat> date_fmt_UUID = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyMMddHHmmssSSS"));

  private final static String SEP = "-";
  private static final ThreadLocal<NumberFormat> number_fmt_IT = ThreadLocal.withInitial(() -> NumberFormat.getNumberInstance(Locale.ITALIAN));

  private static final ThreadLocal<SimpleDateFormat> date_fmt_IT = ThreadLocal.withInitial(() -> {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    simpleDateFormat.setLenient(false);
    return simpleDateFormat;
  });

  public static String parseImportoString(BigDecimal importo) {
    String importoString = number_fmt_IT.get().format(importo);
    if (!importoString.contains(",")) {
      importoString = importoString + ",00";
    } else {
      if (importoString.split(",")[1].length() == 1) {
        importoString = importoString + "0";
      }
    }
    return importoString;

  }

  public static final Set<String> tipiVersamento = Collections
        .unmodifiableSet(new HashSet<>(Arrays.asList(Constants.PAY_BONIFICO_BANCARIO_TESORERIA,
            Constants.PAY_BONIFICO_POSTALE, Constants.PAY_ADDEBITO_DIRETTO, Constants.PAY_CARTA_PAGAMENTO,
            Constants.PAY_PRESSO_PSP, Constants.PAY_MYBANK)));

  public static String formatNumeroAvviso(String applicationCode, String codIuv) {
    String numeroAvviso = "0" + applicationCode + " ";

    String[] partsArrayList = codIuv.split("(?<=\\G....)");

    int i = 0;
    for (String p : partsArrayList) {

      numeroAvviso += p;
      i++;

      if (i < partsArrayList.length) {
        numeroAvviso += " ";
      }
    }
    return numeroAvviso;
  }

  public static String formatNewNumeroAvviso(String auxDigit, String codIuv) {
    String numeroAvviso = "";

    String[] partsArrayList = (auxDigit + codIuv).split("(?<=\\G....)");

    int i = 0;
    for (String p : partsArrayList) {

      numeroAvviso += p;
      i++;

      if (i < partsArrayList.length) {
        numeroAvviso += " ";
      }
    }

    return numeroAvviso;
  }

  /**
   * This is the same method as Utilities.ABBIREVIA() in MyPay3.
   * @param value
   * @param num
   * @return
   */
  public static String shortenString(String value, int num){
    if (value == null) return "";
    else if (value.length() <= num) return value;
    else return value.substring(0, num);
  }


  public static String getStringFromBigDecimalGroup(BigDecimal val) {
    val = val.setScale(2);

    NumberFormat nf = NumberFormat.getNumberInstance(Locale.ITALIAN);
    DecimalFormat df = (DecimalFormat)nf;

    df.setMaximumFractionDigits(2);
    df.setMinimumFractionDigits(2);
    df.setGroupingUsed(true);

    String result = df.format(val);

    return result;
  }

  public static boolean isValidEmail(final String email) {
    Pattern pattern = Pattern.compile(Constants.EMAIL_PATTERN);
    Matcher matcher = pattern.matcher(email);
    return matcher.matches();
  }

  public static boolean isValidCAP(String cap, String codiceIsoNazione) {
    // SE nazione = ITALIA il cap dev'essere solo numerico, altrimenti
    // alfanumerico
    if ("IT".equalsIgnoreCase(codiceIsoNazione)) {
      return cap.matches("^[0-9]{5}$");
    } else {
      return cap.matches("^[a-zA-Z0-9]{1,16}$");
    }
  }

  public static boolean isValidUrl(String urlString){
    if(StringUtils.isBlank(urlString))
      return true;
    try{
      new URL(urlString);
      return true;
    }catch(Exception e){
      return false;
    }
  }

  /**
   * @param stringa
   * @return
   */
  public static boolean validaIndirizzoAnagrafica(String stringa, Boolean isPspPosteItaliane) {
    stringa = bonificaIndirizzoAnagrafica(stringa);
    if (isPspPosteItaliane) {
      Pattern pattern = Pattern.compile(Constants.ANAGRAFICA_INDIRIZZO_PATTERN);
      Matcher matcher = pattern.matcher(stringa);
      return matcher.matches();
    } else {
      // limitare ai caratteri ASCII dal decimale 32 (spazio) al decimale
      // 126
      for (int i = 0; i < stringa.length(); i++) {
        char character = stringa.charAt(i);
        int ascii = character;
        if (ascii < 32 || ascii > 126) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * @param stringa
   * @return
   */
  public static boolean validaCivicoAnagrafica(String stringa, Boolean isPspPosteItaliane) {
    if (isPspPosteItaliane) {
      Pattern pattern = Pattern.compile(Constants.ANAGRAFICA_CIVICO_PATTERN);
      Matcher matcher = pattern.matcher(stringa);
      return matcher.matches();
    } else {
      // limitare ai caratteri ASCII dal decimale 32 (spazio) al decimale
      // 126
      for (int i = 0; i < stringa.length(); i++) {
        char character = stringa.charAt(i);
        int ascii = character;
        if (ascii < 32 || ascii > 126) {
          return false;
        }
      }
      return true;
    }
  }

  public static boolean validateWithValidationType(String validationType, String value, String valMin, String valMax) {
    if (StringUtils.isBlank(value))
      return false;
    switch (validationType) {
      case "id_univoco_FG":
        return isValidCFOrPIVA(value);
      case "data":
        return isValidData(value);
      case "yearInRange":
        return isYearInRange(value, valMin, valMax);
      default:
        return false;
    }
  }

  public static boolean isValidCFOrPIVA(String cf) {
    return isValidCF(cf) || isValidPIVA(cf);
  }

  public static boolean isValidCFpf(String cf) {
    return StringUtils.length(cf) == 16 && isValidCF(cf);
  }

  public static boolean isValidCF(String cf) {

    if (StringUtils.isNotBlank(cf))
      cf = cf.toUpperCase();

    if (cf.length() == 16) {

      CFUtilities.UCheckDigit ucheckDigit = new CFUtilities.UCheckDigit(cf);
      return ucheckDigit.controllaCorrettezza();

    } else if (cf.length() == 11) {
      CFUtilities.UCheckNum ucheckNum = new CFUtilities.UCheckNum(cf);
      boolean isOkNumeric = ucheckNum.controllaCfNum();

      // se ritorna con false il codice fiscale errato
      if (!isOkNumeric) {
        return false;
      }

      // se ritorna con true richiamare il metodo trattCfNum e considerare
      // il valore della
      // String restituita: se "2" o "5" il codice fiscale e' errato,
      // diversamente e' corretto

      String trattCfNum = ucheckNum.trattCfNum();

      return !"2".equals(trattCfNum) && !"5".equals(trattCfNum);

    } else {
      return false;
    }
  }

  public static boolean isValidPIVA(String pi) {
    int i, c, s;
    if (pi.length() == 0)
      return false;
    if (pi.length() != 11)
      return false;
    for (i = 0; i < 11; i++) {
      if (pi.charAt(i) < '0' || pi.charAt(i) > '9')
        return false;
    }
    s = 0;
    for (i = 0; i <= 9; i += 2)
      s += pi.charAt(i) - '0';
    for (i = 1; i <= 9; i += 2) {
      c = 2 * (pi.charAt(i) - '0');
      if (c > 9)
        c = c - 9;
      s += c;
    }
    return (10 - s % 10) % 10 == pi.charAt(10) - '0';
  }

  public static boolean isValidIban(String iban) {
    return iban != null && iban.length() == Constants.LUNGHEZZA_IBAN;
  }

  public static boolean isValidBic(String bic) {
    return bic != null && bic.length() <= Constants.LUNGHEZZA_MASSIMA_BIC;
  }

  public static boolean isValidCodIdUnivocoConAnonimo(boolean anonimo, String tipoPersona, String codIdUnivoco) {
    if (anonimo && Constants.CODICE_FISCALE_ANONIMO.equals(codIdUnivoco) && !Constants.TIPOIDENTIFICATIVOUNIVOCO_G.equals(tipoPersona)) {
      return true;
    } else if (Constants.TIPOIDENTIFICATIVOUNIVOCO_G.equals(tipoPersona)) {
      return Utilities.isValidPIVA(codIdUnivoco);
    } else if (Constants.TIPOIDENTIFICATIVOUNIVOCO_F.equals(tipoPersona)) {
      return Utilities.isValidCFpf(codIdUnivoco);
    } else
      return false;
  }

  public static boolean isValidData(String data) {
    try {
      Date resultData = date_fmt_IT.get().parse(data.trim());
      //check if date is within a range 100 years (in past and future) from now;
      return Math.abs(resultData.getTime()-System.currentTimeMillis()) < 1000L*86400*360*100;
    } catch (ParseException pe) {
      return false;
    }
  }

  public static boolean isYearInRange(String year, String diffYearMin, String diffYearMax) {
    try {
      int yearValue = Integer.parseInt(year);
      int diffYearMinValue = 0;
      if (diffYearMin != null) {
        diffYearMinValue = Integer.parseInt(diffYearMin);
      }

      int diffYearMaxValue = 0;
      if (diffYearMax != null) {
        diffYearMaxValue = Integer.parseInt(diffYearMax);
      }
      Calendar cal = Calendar.getInstance();
      int currentYear = cal.get(Calendar.YEAR);
      if (diffYearMin != null && diffYearMax != null) {
        return (yearValue >= (currentYear - diffYearMinValue) && yearValue <= (currentYear + diffYearMaxValue));
      } else if (diffYearMin != null) {
        return (yearValue >= (currentYear - diffYearMinValue));
      } else if (diffYearMax != null) {
        return (yearValue <= (currentYear + diffYearMaxValue));
      }
      return true;

    } catch (Exception pe) {
      return false;
    }
  }

  public static boolean isValidImageDimensions(BufferedImage image, int maxWidth, int maxHeight) {
    int w = image.getWidth();
    int h = image.getHeight();
    if (w <= maxWidth && h <= maxHeight && h > 0 && w > 0)
      return true;
    else
      return false;
  }

  /**
   * validazione importo
   *
   * @param importo BigDecimal
   * @return String
   */
  public static String verificaImporto(BigDecimal importo) {

    if (importo.compareTo(BigDecimal.ZERO) <= 0) {
      String msg = "Importo inserito non valido";
      try {
        msg = "L'importo inserito non è valido.";
      } catch (Exception e) {}
      return msg;
    }
    if (importo.compareTo(Constants.MAX_AMOUNT)>0) {
      String msg = "Importo superiore al massimo consentito";
      return msg;
    }
    return null;
  }

  public static String getTotaleInXSD(List<FieldBean> fieldBeans) {
    for (FieldBean fb : fieldBeans) {
      if (fb.getName().equals("sys_type")) {
        Map<String, String> extraAttr = fb.getExtraAttr();
        if (extraAttr.containsKey(Constants.XSD_TOTAL_INCLUDED)) {
          String val = extraAttr.get(Constants.XSD_TOTAL_INCLUDED);
          if (StringUtils.isNotBlank(val)) {
            return val;
          }
        }
      }
    }
    return null;
  }

  public static Optional<FieldBean> getMapOfExtraAttrFromXSD(List<FieldBean> fieldBeans) {
    return fieldBeans.stream().filter(fb -> Predicate.isEqual(fb.getName()).test(Constants.XSD_NAME_VOCI_BILANCIO)).findFirst();
  }

  public static void fillEnumerationListOfVociBilancioFromXSD(List<FieldBean> fieldBeans) {
    getMapOfExtraAttrFromXSD(fieldBeans).ifPresent(fb ->
        fb.getEnumerationList().addAll(fb.getMapOfExtraAttrByKey(Constants.XSD_EXTRAATTR_CAPITOLI_MAP).keySet()));
  }

  public static String getCodBilancioFromXSD(List<FieldBean> fieldBeans, Map<String, String> formMap) {
    var capitolo = formMap.getOrDefault(Constants.XSD_NAME_VOCI_BILANCIO, "");
    return getMapOfExtraAttrFromXSD(fieldBeans).map(fb -> fb.getMapOfExtraAttrByKey(Constants.XSD_EXTRAATTR_CAPITOLI_MAP))
        .map(m -> m.get(capitolo)).orElse(null);
  }


  /**
   * @return
   */
  public static String getRandomIUD() {
    return "000" + getRandomicUUID();
  }

  /**
   * @return
   */
  public static String getRandomIdMessaggioRichiesta() {

    String prefixString = getRandomicUUID().substring(0, 3);
    String randomString = getRandomicUUID();

    return prefixString + randomString;
  }

  /**
   * @return
   */
  public static String getRandomicIdSession() {
    return UUID.randomUUID().toString();
  }

  /**
   * @return randomUUID senza caratteri "-"
   */
  public static String getRandomicUUID() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  public static Integer toInteger(String s) {
    return StringUtils.isBlank(s) ? null : Integer.parseInt(s);
  }
  /*
   * Converts java.util.Date to javax.xml.datatype.XMLGregorianCalendar
   */
  public static XMLGregorianCalendar toXMLGregorianCalendar(GregorianCalendar gCalendar) {
    try {
      return  DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
    } catch (DatatypeConfigurationException ex) {
      throw new MyPayException("error converting date", ex);
    }
  }

  public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
    GregorianCalendar gCalendar = new GregorianCalendar();
    if(date!=null)
      gCalendar.setTime(date);
    return toXMLGregorianCalendar(gCalendar);
  }

  public static XMLGregorianCalendar toXMLGregorianCalendar(LocalDate localDate) {
    //default time zone
    ZoneId defaultZoneId = ZoneId.systemDefault();

    //local date + atStartOfDay() + default time zone + toInstant() = Date
    Date date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
    return toXMLGregorianCalendar(date);
  }

  public static XMLGregorianCalendar toXMLGregorianCalendar(Calendar calendar) {
    if(calendar instanceof GregorianCalendar)
      return toXMLGregorianCalendar((GregorianCalendar) calendar);

    GregorianCalendar gCalendar = new GregorianCalendar();
    gCalendar.setTimeInMillis(calendar.getTimeInMillis());
    return toXMLGregorianCalendar(gCalendar);
  }


  public static LocalDate toLocalDate(XMLGregorianCalendar xmlGc) {
    return toLocalDate(xmlGc.toGregorianCalendar());
  }

  public static LocalDate toLocalDate(GregorianCalendar gc) {
    return LocalDate.of(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc.get(Calendar.DATE));
  }

  public static LocalDate toLocalDate(Date date) {
    return date==null?null:new java.sql.Date(date.getTime()).toLocalDate();
  }

  public static LocalDateTime toLocalDateTime(Date date) {
    return date==null?null:new java.sql.Timestamp(date.getTime()).toLocalDateTime();
  }

  public static Date addDays(Date date, int days) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DATE, days);
    return cal.getTime();
  }

  public static Date toDate(LocalDate localDate){
    return localDate==null?null:Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }

  public static java.sql.Date toSqlDate(LocalDate localDate){
    return localDate==null?null:new java.sql.Date(toDate(localDate).getTime());
  }

  public static Calendar toCalendarAtMidnight(final Date date) {
    final Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY,0);
    cal.set(Calendar.MINUTE,0);
    cal.set(Calendar.SECOND,0);
    cal.set(Calendar.MILLISECOND,0);
    return cal;
  }

  public static Date toDateAtMidnight(final Date date) {
    return toCalendarAtMidnight(date).getTime();
  }

  public static XMLGregorianCalendar toXMLGregorianCalendarWithoutTimezone(Date date) {
    GregorianCalendar gCalendar = new GregorianCalendar();
    gCalendar.setTime(date);
    XMLGregorianCalendar xmlCalendar = null;
    try {
      xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
      xmlCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
    } catch (DatatypeConfigurationException ex) {}
    return xmlCalendar;
  }

  public static <T, R> R ifNotNull(T in, Function<T, R> func) {
    return in==null?null:func.apply(in);
  }
  public static <R> R ifNotBlank(String in, Function<String, R> func) {
    return (in==null || in.trim().length()==0)?null:func.apply(in);
  }

  public static BigDecimal parseImportoString(String importo) throws Exception {

    String pattern = "^(([0-9]){1,9})$|^(([0-9]){1,9})\\,([0-9]){1,2}$";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(importo);

    if (m.find()) {
      NumberFormat number_fmt_IT = NumberFormat.getNumberInstance(Locale.ITALIAN);
      Number number = number_fmt_IT.parse(importo);
      BigDecimal impBigDecimal = new BigDecimal(number.doubleValue());

      if (impBigDecimal.compareTo(BigDecimal.ZERO) == 0) {
        throw new Exception("Importo non valido");
      }
      return impBigDecimal;
    } else {
      throw new Exception("Importo non valido");
    }
  }

  public static Optional<String> toOptional(String s){
    if(StringUtils.isBlank(s) || "n/a".equalsIgnoreCase(s.trim()))
      return Optional.empty();
    else
      return Optional.of(s);
  }

  /**
   *
   * @param importo String
   * @return BigDecimal
   */
  public static BigDecimal toBigDecimal(String importo) {
    if (StringUtils.isNotEmpty(importo)) {
      int strLength = importo.length();
      if (strLength<4) {
        String imp = importo.replaceAll("\\,", ".");
        return new BigDecimal(imp);
      }
      String simbolo = importo.substring(strLength-3, strLength-2);
      boolean isVirgola = simbolo.equalsIgnoreCase(",");
      boolean isPunto = simbolo.equalsIgnoreCase(".");

      if (isVirgola) {
        String imp = importo.replaceAll("\\.", "").replaceAll("\\,", "\\.");
        BigDecimal bd = new BigDecimal(imp);
        return bd;
      }
      if (isPunto) {
        String imp = importo.replaceAll("\\,", "\\.");
        BigDecimal bd = new BigDecimal(imp);
        return bd;
      }
      String imp = importo.replaceAll("\\,", "");
      BigDecimal bd = new BigDecimal(imp);
      return bd;
    }
    return null;
  }

  public static void setIfNotBlank(String in, Consumer<? super String> consumer) {
    if (StringUtils.isNotBlank(in)) {consumer.accept(in);}
  }

  public static Function<String, String> getTruncatedAt(int inspector) {
    return s -> StringUtils.defaultString(s).length() > inspector? s.substring(0, inspector) : s;
  }

  /**
   * Applies this function to the given arguments like {@link StringUtils#defaultString(String, String)}
   * but if the first function argument is also {@code empty} returns the second one.
   *
   * @return the function result
   */
  public static BiFunction<String, String, String> getDefaultString() {
    return (firstArg, secondArg) -> StringUtils.defaultString(firstArg).isEmpty()? secondArg : firstArg;
  }

  public static Function<String, String> ifEqualsOrElse(String stringToCheck, String orElse) {
    return s -> stringToCheck.equals(s)? stringToCheck : orElse;
  }

  public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    String query = url.getQuery();
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      query_pairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
          URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
    }
    return query_pairs;
  }
  // bonifiche stringhe da MyPay3
  public static String rebuildValidFormat(String in, String regex) {
    // se il carattere non previsto e' un diacritico sostituirlo con il
    // corrispondente non diacritico
    String rebuiltString = StringUtils.stripAccents(in);
    // tutti gli altri caratteri non previsti sostituirli con il carattere spazio
    return Pattern.compile(regex).matcher(rebuiltString).replaceAll(" ");
  }

  /*
   * Converts XMLGregorianCalendar to java.util.Date in Java
   */
  public static Date toDate(XMLGregorianCalendar calendar) {
    return Optional.ofNullable(calendar)
        .map(XMLGregorianCalendar::toGregorianCalendar)
        .map(GregorianCalendar::getTime)
        .orElse(null);
  }

  public static boolean validaVersioneTracciatoExport(String versioneTracciato) {
    if (org.apache.commons.lang3.StringUtils.isBlank(versioneTracciato))
      throw new IllegalArgumentException("Versione tracciato nulla");

    for (Constants.VERSIONE_TRACCIATO_EXPORT vte : Constants.VERSIONE_TRACCIATO_EXPORT.values())
      if (vte.value.equals(versioneTracciato))
        return true;
    return false;
  }

  public static Date toMidnight(Date data) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(data);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  public static boolean isTrue(Boolean value) {
    return value != null && value;
  }

  public static String createUrl(String baseUrl, String fileUrl) {
    String downloadUrl = baseUrl+fileUrl;
    URL url;
    try {
      url = new URL(downloadUrl);
      downloadUrl = url.toURI().toASCIIString();
    } catch (MalformedURLException | URISyntaxException e) {
      log.error("Error in building download URL :" + downloadUrl);
    }
    return downloadUrl;
  }

  public static boolean validaIUV(String IUV, boolean flagGeneraIuv, String applicationCode) {

    if (StringUtils.isNotBlank(IUV)) {
      if (IUV.length() == 15) {
        return !IUV.startsWith("00");
      } else if (IUV.length() == 25) {
        if (IUV.endsWith("0000")) {
          // vecchio
          // formato
          // ente
          return IUV.charAt(6) == '9' && IUV.charAt(7) == '9';
        } else // nuovo formato ente
          return true;
      } else if (IUV.length() == 17) {
        if (flagGeneraIuv)
          return true;
        return IUV.startsWith(applicationCode) && (IUV.charAt(2) != '0' || IUV.charAt(3) != '0');
      } else
        return false;
    } else
      return true;
  }

  /*
   * Controlla che la somma degli importi contenuti nell' xml del bilancio
   * corrisponda all' importo del dovuto corrispondente
   */
  public static boolean verificaImportoBilancio(Bilancio bilancio, BigDecimal importoSingoloVersamento) {
    BigDecimal importoBilancio = new BigDecimal(0);
    List<CtCapitolo> listaCapitoli = bilancio.getCapitolos();
    // un bilancio ha almeno un capitolo
    for (CtCapitolo capitolo : listaCapitoli) {
      List<CtAccertamento> listaAccertamenti = capitolo.getAccertamentos();
      // un capitolo ha almeno un accertamento
      for (CtAccertamento accertamento : listaAccertamenti) {
        BigDecimal importoAccertamento = accertamento.getImporto();
        // Il controllo che sia almeno 0.00 viene fatto nella
        // validazione xsd
        importoBilancio = importoBilancio.add(importoAccertamento);
      }
    }
    return importoBilancio.compareTo(importoSingoloVersamento) == 0;
  }

  public static boolean validaTipoVersamento(String stringa) {

    if (StringUtils.isBlank(stringa)) {
      // Il campo non e' obbligatorio
      return true;
    }

    String[] token = stringa.split("\\|");
    for (int i = 0; i < token.length; i++) {
      boolean find = false;
      for (Constants.TIPO_VERSAMENTO type : Constants.TIPO_VERSAMENTO.values()) {
        if (type.value.equalsIgnoreCase(token[i].trim())) {
          find = true;
          break;
        }
      }
      if (!find)
        return false;
    }
    // non posso mettere 'ALL' con altre cose
    return !stringa.contains(Constants.TIPO_VERSAMENTO.TUTTI.value) || token.length <= 1;

  }

  public static boolean validaIUD(String IUD) {

    if (StringUtils.isNotBlank(IUD)) {
      return !IUD.startsWith("000");
    }

    return false;
  }

  public static boolean validaDatiSpecificiRiscossione(String stringa) {

    Pattern pattern = Pattern.compile(Constants.DATI_SPECIFICI_RISCOSSIONE_PATTERN);
    Matcher matcher = pattern.matcher(stringa);

    return matcher.matches();
  }

  public static String bonificaIndirizzoAnagrafica(String stringa) {

    // se il carattere non previsto e' un diacritico sostituirlo con il
    // corrispondente non diacritico
    String stringaBonificata = StringUtils.stripAccents(stringa);

    // tutti gli altri caratteri non previsti sostituirli con il carattere
    // spazio
    Pattern pattern = Pattern.compile(Constants.NOT_ANAGRAFICA_INDIRIZZO_PATTERN);
    Matcher matcher = pattern.matcher(stringaBonificata);
    stringaBonificata = matcher.replaceAll(" ");

    return stringaBonificata;
  }

  public static String bonificaCivicoAnagrafica(String stringa) {

    // se il carattere non previsto e' un diacritico sostituirlo con il
    // corrispondente non diacritico
    String stringaBonificata = StringUtils.stripAccents(stringa);

    // tutti gli altri caratteri non previsti sostituirli con il carattere
    // spazio
    Pattern pattern = Pattern.compile(Constants.NOT_ANAGRAFICA_CIVICO_PATTERN);
    Matcher matcher = pattern.matcher(stringaBonificata);
    stringaBonificata = matcher.replaceAll(" ");

    return stringaBonificata;
  }

  public static boolean checkIfStatoInserito(Ente ente) {
    return Optional.ofNullable(ente)
        .map(Ente::getCdStatoEnte)
        .map(AnagraficaStato::getCodStato)
        .filter(Constants.STATO_ENTE_INSERITO::equals)
        .isPresent();
  }

  public static String generateQRCodeString(String cfEnteCreditore, String auxDigit, String applicationCode, String codIuv,
                                            BigDecimal importo) {
    return Constants.AVVISO_PAGAMENTO_CODICE_IDENTIFICATIVO + Constants.AVVISO_PAGAMENTO_SEPARATORE
        + Constants.AVVISO_PAGAMENTO_VERSIONE + Constants.AVVISO_PAGAMENTO_SEPARATORE
        + auxDigit + applicationCode + codIuv + Constants.AVVISO_PAGAMENTO_SEPARATORE
        + cfEnteCreditore + Constants.AVVISO_PAGAMENTO_SEPARATORE
        + importo.multiply(new BigDecimal(100)).setScale(0).toString();
  }

  public static String formatNumeroAvviso15digit(String applicationCode, String codIuv) {
    return formatCodAvviso("0" + applicationCode + " ", codIuv);
  }

  public static String formatNumeroAvviso17digit(String auxDigit, String codIuv) {
    return formatCodAvviso("",auxDigit + codIuv);
  }

  public static String formatCodAvviso(String prefix, String codAvviso) {
    String[] parts = codAvviso.split("(?<=\\G....)");
    return prefix + String.join(" ", parts);
  }

  public static BufferedImage getImageFromBase64String(String base64image) throws IOException {
    byte[] imageByte = Base64.decodeBase64(base64image);
    ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
    return ImageIO.read(bis);
  }

  public static BufferedImage covertToGrayscale(BufferedImage image){
    int width = image.getWidth();
    int height = image.getHeight();

    for(int y = 0; y < height; y++){
      for(int x = 0; x < width; x++){
        int p = image.getRGB(x,y);

        int a = (p>>24) & 0xff;
        int r = (p>>16) & 0xff;
        int g = (p>>8) & 0xff;
        int b = p&0xff;

        int avg = (r+g+b)/3;

        //replace RGB value with avg
        p = (a<<24) | (avg<<16) | (avg<<8) | avg;
        image.setRGB(x, y, p);
      }
    }
    return image;
  }

  public static String getBase64StringFromImage(BufferedImage image) throws IOException{
    String imageString = null;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", bos);
    byte[] imgBytes = bos.toByteArray();
    imageString = new String(Base64.encodeBase64(imgBytes), StandardCharsets.UTF_8);
    bos.close();
    return imageString;
  }

  public static String getYYYY_MM() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM");
    return simpleDateFormat.format(new Date());
  }

  public static String getYYYY_MM(Timestamp ts) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM");
    return simpleDateFormat.format(ts);
  }
  public static boolean isWSUSer(String codFedUserId) {
    return (codFedUserId.endsWith("-WS_USER"));
  }

  public static Long ZERO_LONG = 0l;
  public static <T extends Number> T coalesce(T first, T other) {
    return first!=null ? first : other;
  }

  public static String nowTimestamp(){
    return " ["+System.currentTimeMillis()+"] ";
  }

  public static boolean isAvviso(String iuv){
    int length = StringUtils.length(StringUtils.deleteWhitespace(iuv));
    return length == Constants.IUV_GENERATOR_15_LENGTH || length == Constants.IUV_GENERATOR_17_LENGTH;
  }

  public static String numeroAvvisoToIuvValidator(String numeroAvviso) {
    String iuv;
    if (numeroAvviso!= null && numeroAvviso.length() == 18) {
      if (numeroAvviso.startsWith("0")) {
        iuv = numeroAvviso.substring(3);
      } else if (numeroAvviso.startsWith(Constants.SMALL_IUV_AUX_DIGIT)) {
        iuv = numeroAvviso.substring(1);
      } else {
        throw new ValidatorException("Codice avviso / IUV non corretto.");
      }
    } else if (numeroAvviso!= null && (numeroAvviso.length() == 15 || numeroAvviso.length() == 17)) {
      iuv = numeroAvviso;
    } else {
      throw new ValidatorException("Codice avviso / IUV non corretto.");
    }
    return iuv;
  }

  public static String getFilenameAvviso(String ente, String iuv){
    return "MYPAY_AVVISO_"+Utilities.formatEnteIuvString(ente, iuv)+".pdf";
  }
  public static String getFilenameRt(String ente, String iuv){
    return "MYPAY_RT_"+Utilities.formatEnteIuvString(ente, iuv)+".pdf";
  }
  public static String formatEnteIuvString(String ente, String iuv){
    return StringUtils.upperCase(
      StringUtils.firstNonBlank(StringUtils.strip(ente), RandomStringUtils.randomAlphanumeric(5))
        +"_"
        + StringUtils.firstNonBlank(StringUtils.strip(iuv), RandomStringUtils.randomAlphanumeric(17))
    );
  }

  public static String humanReadableByteCountSI(long bytes) {
    if (-1000 < bytes && bytes < 1000) {
      return bytes + " B";
    }
    CharacterIterator ci = new StringCharacterIterator("kMGTPE");
    while (bytes <= -999_950 || bytes >= 999_950) {
      bytes /= 1000;
      ci.next();
    }
    return String.format("%.1f %cB", bytes / 1000.0, ci.current());
  }

  public static String humanReadableByteCountBin(long bytes) {
    long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
    if (absB < 1024) {
      return bytes + " B";
    }
    long value = absB;
    CharacterIterator ci = new StringCharacterIterator("KMGTPE");
    for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
      value >>= 10;
      ci.next();
    }
    value *= Long.signum(bytes);
    return String.format("%.1f %ciB", value / 1024.0, ci.current());
  }

  public static boolean isPositive(Long value){
    return ObjectUtils.defaultIfNull(value,0L)> 0L;
  }

  public static String getRandomUUIDWithTimestamp(){
    //last part of UUID is current timestamp (epoch) as hex-number
    return StringUtils.substringBeforeLast(UUID.randomUUID().toString(),SEP)+
      SEP+String.format("%x", Long.valueOf(date_fmt_UUID.get().format(new Date())));
  }

  public static Optional<Date> parseDateFromUUIDWithTimestamp(String uuid){
    try {
      String uuidDateFormatted = "" + Long.parseLong(StringUtils.substringAfterLast(uuid, SEP), 16);
      return Optional.of(date_fmt_UUID.get().parse(uuidDateFormatted));
    }catch(Exception e){
      log.debug("parseDateFromUUIDWithTimestamp error", e);
      return Optional.empty();
    }
  }
  public static boolean carrelloIsPresent(Dovuto dovuto){
    return null !=dovuto.getMygovCarrelloId() && null !=dovuto.getMygovCarrelloId().getMygovCarrelloId();
  }
  public static Boolean verifyCarrelloloOK(Carrello carrello) {
    Boolean carrelloOK = null;
    if (carrello==null) {
      return null;
    }
    try {
        AnagraficaStato anagraficaStatoCarrello = carrello.getMygovAnagraficaStatoId();
        if (anagraficaStatoCarrello.getDeTipoStato().equals(Constants.STATO_TIPO_CARRELLO)
                && (anagraficaStatoCarrello.getCodStato().equals(Constants.STATO_CARRELLO_NON_PAGATO)
                || anagraficaStatoCarrello.getCodStato().equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI)
                || anagraficaStatoCarrello.getCodStato().equals(Constants.STATO_CARRELLO_DECORRENZA_TERMINI_PARZIALE))) {
         carrelloOK = Boolean.FALSE;
        }
        if (anagraficaStatoCarrello.getDeTipoStato().equals(Constants.STATO_TIPO_CARRELLO)
                && (anagraficaStatoCarrello.getCodStato().equals(Constants.STATO_CARRELLO_PAGATO))) {
          carrelloOK = Boolean.TRUE;
        }
    } catch (Exception e) {}
    return carrelloOK;
  }

}
