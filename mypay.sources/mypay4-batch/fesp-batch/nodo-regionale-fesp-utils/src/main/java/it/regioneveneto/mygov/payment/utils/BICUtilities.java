package it.regioneveneto.mygov.payment.utils;

/**
 * 
 * @author regione del veneto
 * 
 * Controllo validita BIC
 *
 */
public class BICUtilities {
	
	private static final int BIC8_LENGTH = 8;
    private static final int BIC11_LENGTH = 11;

    private static final int BANK_CODE_INDEX = 0;
    private static final int BANK_CODE_LENGTH = 4;
    private static final int COUNTRY_CODE_INDEX = BANK_CODE_INDEX + BANK_CODE_LENGTH;
    private static final int COUNTRY_CODE_LENGTH = 2;
    private static final int LOCATION_CODE_INDEX = COUNTRY_CODE_INDEX + COUNTRY_CODE_LENGTH;
    private static final int LOCATION_CODE_LENGTH = 2;
    private static final int BRANCH_CODE_INDEX = LOCATION_CODE_INDEX + LOCATION_CODE_LENGTH;
    private static final int BRANCH_CODE_LENGTH = 3;
    
    public boolean validateBIC(final String bic)  {
    	
    	if(!validateEmpty(bic))
    		return false;
    	
    	if(!validateLength(bic))
    		return false;
    	
    	if(!validateCase(bic))
    		return false;
        
    	if(!validateBankCode(bic))
    		return false;
        
    	if(!validateCountryCode(bic))
    		return false;
        
    	if(!validateLocationCode(bic))
    		return false;

        if(hasBranchCode(bic)) {
            if(!validateBranchCode(bic))
            	return false;
        }
    	
    	return true;
    }
    
    
    private boolean validateEmpty(final String bic) {
        if(bic == null) return false;
        if(bic.length() == 0) return false;
        return true;
    }
    
    private boolean validateLength(final String bic) {
        if(bic.length() != BIC8_LENGTH && bic.length() != BIC11_LENGTH) {
            return false;
        }
        return true;
    }
    
    private boolean validateCase(final String bic) {
        if(!bic.equals(bic.toUpperCase())) {
            return false;
        }
        return true;
    }

    private boolean validateBankCode(final String bic) {
        String bankCode = getBankCode(bic);
        for(final char ch : bankCode.toCharArray()) {
            if(!Character.isLetter(ch)) {
                return false;
            }
        }
        return true;
    }

    private boolean validateCountryCode(final String bic) {
        final String countryCode = getCountryCode(bic);
        if(countryCode.trim().length() < COUNTRY_CODE_LENGTH ||
                !countryCode.equals(countryCode.toUpperCase()) ||
                !Character.isLetter(countryCode.charAt(0)) ||
                !Character.isLetter(countryCode.charAt(1))) {
            return false;
        }

        return true;
    }

    private boolean validateLocationCode(final String bic) {
        final String locationCode = getLocationCode(bic);
        for(char ch : locationCode.toCharArray()) {
            if(!Character.isLetterOrDigit(ch)) {
               return false;
            }
        }
        return true;
    }

    private boolean validateBranchCode(final String bic) {
        final String branchCode = getBranchCode(bic);
        for(final char ch : branchCode.toCharArray()) {
            if(!Character.isLetterOrDigit(ch)) {
                return false;
            }
        }
        return true;
    }

     static String getBankCode(final String bic) {
        return bic.substring(BANK_CODE_INDEX, BANK_CODE_INDEX + BANK_CODE_LENGTH);
    }

    static String getCountryCode(final String bic) {
        return bic.substring(COUNTRY_CODE_INDEX, COUNTRY_CODE_INDEX + COUNTRY_CODE_LENGTH);
    }

    static String getLocationCode(final String bic) {
        return bic.substring(LOCATION_CODE_INDEX, LOCATION_CODE_INDEX + LOCATION_CODE_LENGTH);
    }

    static String getBranchCode(final String bic) {
        return bic.substring(BRANCH_CODE_INDEX, BRANCH_CODE_INDEX + BRANCH_CODE_LENGTH);
    }

    static boolean hasBranchCode(final String bic) {
        return bic.length() == BIC11_LENGTH;
    }

}
