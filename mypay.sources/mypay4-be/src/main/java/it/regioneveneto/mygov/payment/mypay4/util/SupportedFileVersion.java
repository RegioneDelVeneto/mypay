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

public enum SupportedFileVersion {

    VERSIONE_1_0("1.0", "1_0"),
    VERSIONE_1_1("1.1", "1_1"),
    VERSIONE_1_2("1.2", "1_2"),
    VERSIONE_1_3("1.3", "1_3"),
    VERSIONE_1_4("1.4", "1_4");

    private String versione;
    private String versione_file;

    private SupportedFileVersion(String versione, String versione_file) {
        this.versione = versione;
        this.versione_file = versione_file;
    }

    private SupportedFileVersion() {
    }

    public String getVersione() {
        return versione;
    }

    public void setVersione(String versione) {
        this.versione = versione;
    }

    public String getVersione_file() {
        return versione_file;
    }

    public void setVersione_file(String versione_file) {
        this.versione_file = versione_file;
    }

    public static SupportedFileVersion GET_VERSIONE(String value) {
        for (SupportedFileVersion supportedFileVersion : SupportedFileVersion.values()) {
            if (supportedFileVersion.getVersione().equalsIgnoreCase(value)) {
                return supportedFileVersion;
            }
        }
        return null;
    }

    public static SupportedFileVersion GET_VERSIONE_FILE(String value) {
        for (SupportedFileVersion supportedFileVersion : SupportedFileVersion.values()) {
            if (supportedFileVersion.getVersione_file().equalsIgnoreCase(value)) {
                return supportedFileVersion;
            }
        }
        return null;
    }

    public static String printValues() {
        String result = "";
        for (SupportedFileVersion supportedFileVersion : SupportedFileVersion.values()) {
            result += supportedFileVersion.getVersione_file() + " | ";
        }
        return result.substring(0, result.length() - 3);
    }

}