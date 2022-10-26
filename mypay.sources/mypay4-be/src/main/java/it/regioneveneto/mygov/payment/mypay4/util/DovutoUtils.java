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

import it.regioneveneto.mygov.payment.mypay4.model.DovutoElaborato;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.List;

/**
 * This is the Utils for Dovuto and DovutoElaborato Services.
 */
@Slf4j
public class DovutoUtils {

  public static boolean isAllDovutiElaboratiNelloStessoCarrello(List<DovutoElaborato> listaDovutiElaborati) {
    Assert.notEmpty(listaDovutiElaborati);

    if (listaDovutiElaborati.size() == 1)
      return true;

    Long currentCarrelloId = null;
    for (DovutoElaborato de : listaDovutiElaborati) {
      if (currentCarrelloId != null) {
        if (currentCarrelloId != de.getMygovCarrelloId().getMygovCarrelloId())
          return false;
      }
      currentCarrelloId = de.getMygovCarrelloId().getMygovCarrelloId();
    }
    return true;
  }

  public static DovutoElaborato getLastDovutoElaboratoOrPositivo(List<DovutoElaborato> listaDovutiElaborati) {
    Assert.notEmpty(listaDovutiElaborati);

    DovutoElaborato dovutoMax = null;

    for (DovutoElaborato de : listaDovutiElaborati) {
      if (de.getCodEDatiPagCodiceEsitoPagamento() != null
          && de.getCodEDatiPagCodiceEsitoPagamento().equals(Constants.CODICE_ESITO_PAGAMENTO_OK)) {
        return de;
      }
      // prima volta
      if (dovutoMax == null) {
        dovutoMax = de;
      }
      if (de.getDtUltimaModificaRp() != null
          && de.getDtUltimaModificaRp().after(dovutoMax.getDtUltimaModificaRp())) {
        dovutoMax = de;
      }
    }
    return dovutoMax;
  }
}
