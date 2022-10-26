package it.regioneveneto.mygov.payment.utils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.StringUtils;

import it.regioneveneto.mygov.payment.constants.Constants;


/**
 * @author regione del veneto
 *
 */
public class Utils {

	public final static String DDMMYYYHHMMSS_FORMAT = "dd/MM/yyyy HH:mm:ss";
	public final static SimpleDateFormat DDMMYYYHHMMSS = new SimpleDateFormat(DDMMYYYHHMMSS_FORMAT);
	public final static SimpleDateFormat PLAIN_TIMESTAMP_UNTIL_MINUTES = new SimpleDateFormat("yyyyMMddHHmm");
	public final static SimpleDateFormat PLAIN_TIMESTAMP_UNTIL_SECONDS = new SimpleDateFormat("yyyyMMddHHmmss");

	public Utils() {
	}

	/**
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date getCurrentDateWithFormat(String format) throws ParseException {
		String strCurrentDate = DDMMYYYHHMMSS.format(new Date());
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.parse(strCurrentDate);
	}

	/**
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String query = url.getQuery();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
					URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}

	/**
	 * @param bytes
	 * @param mimeType
	 * @return
	 */
	public static DataHandler getDataHandlerFromBytes(byte[] bytes, String mimeType) {
		return new DataHandler(new ByteArrayDataSource(bytes, mimeType));
	}

	/**
	 * @param contentDH
	 * @return
	 */
	public static String getStringFromDataHandler(DataHandler contentDH) {
		String contentString = "";
		try {
			StringBuffer sb = new StringBuffer();
			InputStream is = contentDH.getInputStream();
			byte[] b = new byte[4096];
			for (int n; (n = is.read(b)) != -1;) {
				sb.append(new String(b, 0, n));
			}
			contentString = sb.toString();

		} catch (Exception e) {
			contentString = e.getMessage();
		}
		return contentString;
	}
	
	public static String getRandomUIDsenzaCarattereMeno() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	/**
	 * 
	 * @param IUV
	 * @return
	 */
	public static boolean validaIUV(String IUV) {

		if (StringUtils.isNotBlank(IUV)) {
			if (IUV.length() == 15) {
				if (!IUV.startsWith("00"))
					return true;
			} else if (IUV.length() == 25) {
				if (IUV.endsWith("0000")) {
					if (IUV.charAt(6) == '9' && IUV.charAt(7) == '9') // vecchio
																		// formato
																		// ente
						return true;
				} else // nuovo formato ente
					return true;
			} else if (IUV.length() == 17) {
				if (IUV.charAt(2) != '0' || IUV.charAt(3) != '0')
					return true;
			} else
				return false;
		} else
			return true;
		return false;
	}
	
	/**
	 * 
	 * @param pi
	 * @return
	 */
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
		if ((10 - s % 10) % 10 != pi.charAt(10) - '0')
			return false;
		return true;
	}
	
	/**
	 * Codice ABI (Associazione Bancaria Italiana) � un numero composto da cinque cifre e rappresenta l'istituto di credito
	 * 
	 * @param abi
	 * @return
	 */
	public static boolean isValidABI(String abi) {
		if (abi.length() != 5)
			return false;
		for (int i = 0; i < 11; i++) {
			if (abi.charAt(i) < '0' || abi.charAt(i) > '9')
				return false;
		}
		return true;
	}
	
	/**
	 * Codice BIC (standard ISO 9362) 
	 * 
	 * @param bic
	 * @return
	 */
	public static boolean isValidBIC(String bic) {
		BICUtilities bicUtil = new BICUtilities();
		return bicUtil.validateBIC(bic);
	}
	
	/**
	 * 
	 * @param cap
	 * @param nazioneId
	 * @return
	 */
	public static boolean isValidCAP(String cap, String nazioneId) {
		// SE nazione = ITALIA il cap dev'essere solo numerico, altrimenti
		// alfanumerico
		if ("1".equals(nazioneId)) {
			return cap.matches("^[0-9]{5}$");
		} else {
			return cap.matches("^[a-zA-Z0-9]{1,16}$");
		}
	}
	
	
	/**
	 * 
	 * @param cf
	 * @return
	 */
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

			if ("2".equals(trattCfNum) || "5".equals(trattCfNum)) {
				return false;
			}

			return true;

		} else {
			return false;
		}

	}
	
	/**
	 * 
	 * @param email
	 * @return
	 */
	public static boolean isValidEmail(final String email) {
		Pattern pattern = Pattern.compile(Constants.EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	
	/**
	 * 
	 * @param tipoRevoca
	 * @return
	 */
	public static boolean isValidTipoRevoca(String tipoRevoca) {
		if(tipoRevoca.equals(Constants.TIPOREVOCA.S_0.getValue()) || tipoRevoca.equals(Constants.TIPOREVOCA.S_1.getValue()) || tipoRevoca.equals(Constants.TIPOREVOCA.S_2.getValue()))
			return true;
		else
			return false;
	}
	
	
	
}
