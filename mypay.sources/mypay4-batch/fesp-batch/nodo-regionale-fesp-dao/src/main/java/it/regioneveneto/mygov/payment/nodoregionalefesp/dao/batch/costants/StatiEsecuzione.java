/**
 * 
 */
package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.batch.costants;


/**
 * @author regione del veneto
 *
 */
public enum StatiEsecuzione {
	
    CARICATO("OK"), 
    ERRORE_CARICAMENTO("KO"), 
    IN_CARICAMENTO("IN_CARICAMENTO");

    private String value;

    private StatiEsecuzione(String value) {
        this.value = value;
    }

    public static StatiEsecuzione lookup(String name) {

        for (StatiEsecuzione statiEsecuzione : values()) {
            if (statiEsecuzione.getValue().equalsIgnoreCase(name)) {
                return statiEsecuzione;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
