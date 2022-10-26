package it.regioneveneto.mygov.payment.nodoregionalefesp.utils;

import java.math.BigInteger;

public class IuvGeneratorUtils {

	public IuvGeneratorUtils() {
	}

	public static StringBuffer calculateCheckDigits(StringBuffer creditorReference) {

		StringBuffer checkDigits = null;
		StringBuffer tempCreditorReference = new StringBuffer(creditorReference);
		tempCreditorReference.append("271500");
		BigInteger creditorReferenceNum = new BigInteger(tempCreditorReference.toString());

		BigInteger modulo = creditorReferenceNum.mod(new BigInteger("97"));
		BigInteger checkDigitsNum = new BigInteger("98").subtract(modulo);

		if (checkDigitsNum.intValue() < 10) {
			checkDigits = new StringBuffer("0");
			checkDigits.append(checkDigitsNum.toString());
		} else {
			checkDigits = new StringBuffer(checkDigitsNum.toString());
		}

		return checkDigits;
	}

}
