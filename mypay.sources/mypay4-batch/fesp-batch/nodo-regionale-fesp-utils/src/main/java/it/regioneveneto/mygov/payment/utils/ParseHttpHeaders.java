package it.regioneveneto.mygov.payment.utils;

import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author regione del veneto
 *
 */

public class ParseHttpHeaders {

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddressByHeader(HttpServletRequest request) {
		String ipAddress = null;
		String ipHeader = request.getHeader("X-FORWARDED-FOR");
		if (ipHeader == null)
			ipAddress = request.getRemoteAddr();
		else {
			if (ipHeader.contains(",")) {
				try {
					Scanner scanner = new Scanner(ipHeader);
					scanner.useDelimiter(",");

					//seleziono solo il primo indirizzo ip (che corrisponde al client)
					ipAddress = scanner.next().trim();

					scanner.close();
				}
				catch (Exception ex) {
					ipAddress = request.getRemoteAddr();
				}
			}
			else {
				ipAddress = ipHeader;
			}
		}
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		return ipAddress;
	}
}
