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
package it.regioneveneto.mygov.payment.mypay4.scheduled.importflusso.pagopa.util;

import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classe di utilità per la gestione dei file.
 * <p>
 * Contiene un metodo per la creazione della cartella di appoggio dei file da inviare a PagoPA
 * che è comune sia al servizio di esportazione delle posizioni debitorie
 * sia al servizio di elaborazione dei flussi massivi di posizioni debitorie.
 * </p>
 */
public class FileUtils {

    /**
     * Crea la cartella di appoggio dei file da inviare a PagoPA
     * secondo la convenzione: <directory_root_elaborazione>/<codice_fiscale_ente>_<id_flusso>.
     *
     * @param theRootWorkingFolder directory root di elaborazione
     * @param codiceFiscaleEnte codice fiscale dell'ente
     * @return percorso della cartella creata
     * @throws MyPayException se si verifica un errore durante la creazione della cartella
     */
    public static Path createFluxExportFolder(String theRootWorkingFolder, String codiceFiscaleEnte) {
        try {
            Path rootWorkingFolderPath = Paths.get(theRootWorkingFolder);
            Files.createDirectories(rootWorkingFolderPath);
            Path folderEnteFlusso = rootWorkingFolderPath.resolve(codiceFiscaleEnte);
            return Files.createDirectories(folderEnteFlusso);
        } catch (Exception e) {
            throw new MyPayException("Error during the creation of the folder for exporting the flow", e);
        }
    }

}
