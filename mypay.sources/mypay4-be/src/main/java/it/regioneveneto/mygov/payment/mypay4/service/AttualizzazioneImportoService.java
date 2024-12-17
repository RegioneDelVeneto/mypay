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
package it.regioneveneto.mygov.payment.mypay4.service;


import it.regioneveneto.mygov.payment.mypay4.dto.AttualizzazioneTo;
import it.regioneveneto.mygov.payment.mypay4.model.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.service.client.PndRestClient;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional(propagation = Propagation.SUPPORTS)
public class AttualizzazioneImportoService {

  @Autowired
  private PndRestClient pndRestClient;


  public AttualizzazioneTo attualizzaImporto(String iuv, EnteTipoDovuto enteTipoDovuto, Ente ente) {
    AttualizzazioneTo attualizzazione = new AttualizzazioneTo();
    BigDecimal importoPosizione = BigDecimal.ZERO;
    String bilancioStr = null;
    String urlAutenticazione = enteTipoDovuto.getUrlNotificaPnd();
    String urlAttualizzazione = enteTipoDovuto.getUrlNotificaAttualizzazionePnd();
    log.debug("urlAutenticazione: " + urlAutenticazione);
    log.debug("urlAttualizzazione: " + urlAttualizzazione);
    if (urlAutenticazione != null && urlAttualizzazione != null) {
      log.debug("Attualizzazione importo SEND");
      //PND Autenticazione
      Map<String, Object> respAutenticazione = pndRestClient.pndAutenticazione(urlAutenticazione, enteTipoDovuto.getUserPnd(), enteTipoDovuto.getPswPnd());
      Object token = respAutenticazione.get("token");
      if (token != null) {
        log.debug("token String: {}", token);
        //PND Attualizzazione
        String numeroAvvisoIn = Utilities.iuvToNumeroAvviso(iuv, ente.getApplicationCode(), false);
        Map<String, Object> respAttualizzazione = pndRestClient.pndAttualizzazione(urlAttualizzazione, token.toString(), ente.getCodiceFiscaleEnte(), numeroAvvisoIn);
        /*
         * 002 = codice avviso sconosciuto questo codice si ottiene qualora il chiamante abbia fornito un numeroAvviso non presente, presso il Gestionale, come attributo di alcuna notifica gestita per l’ente mittente.
         * 003 = avviso non ancora notificato questo codice si ottiene qualora il chiamante abbia fornito un numeroAvviso di cui non siano presenti nessuna delle due date (dataVisualizzazione e dataPerfezionamentoDecorrenzaTermini entrambe <valore vuoto>. Indica che la notifica non è ancora arrivata a perfezionamento per presa visione o decorrenza termini.
         * 004 = avviso non pagabile (pagato o ritirato): questo codice si ottiene qualora il numeroAvviso non risulti pagabile
         */
        String errorCode = (String) respAttualizzazione.get("codice");
        String errorDetail = (String) respAttualizzazione.get("dettaglio");
        if (StringUtils.equals(errorCode, "002")) {
          log.warn("Codice avviso sconosciuto: iuv[{}] ente[{}] detail[{}]", numeroAvvisoIn, ente.getCodiceFiscaleEnte(), errorDetail);
          throw new AttualizzaImportoException(errorCode, "Codice avviso sconosciuto");
        } else if (StringUtils.equals(errorCode, "003")) {
          log.warn("Codice avviso non notificato: iuv[{}] ente[{}] detail[{}]", numeroAvvisoIn, ente.getCodiceFiscaleEnte(), errorDetail);
          throw new AttualizzaImportoException(errorCode, "Codice avviso non notificato");
        } else if (StringUtils.equals(errorCode, "004")) {
          log.error("Codice avviso non pagabile: iuv[{}] ente[{}] detail[{}]", numeroAvvisoIn, ente.getCodiceFiscaleEnte(), errorDetail);
          throw new AttualizzaImportoException(errorCode, "Codice avviso non pagabile", true);
        } else if (StringUtils.isNotBlank(errorCode)) {
          log.error("Errore [{}]: iuv[{}] ente[{}] detail[{}]", errorCode, numeroAvvisoIn, ente.getCodiceFiscaleEnte(), errorDetail);
          throw new AttualizzaImportoException(errorCode, "Errore di sistema");
        }

        Object numeroAvviso = respAttualizzazione.get("numeroAvviso");
        if (numeroAvviso != null && ((String) numeroAvviso).endsWith(iuv)) {
          importoPosizione = BigDecimal.valueOf((Integer) respAttualizzazione.get("importoPosizione")).movePointLeft(2);

          attualizzazione.setImportoPosizione(importoPosizione);
          //Costruisco la Stringa bilancio dalla lista bilancio contenuta nella risposta del servizio pndAttualizzazione
          List<Map<String, Object>> bilancioList = (List<Map<String, Object>>) respAttualizzazione.get("bilancio");
          if (bilancioList != null && !bilancioList.isEmpty()) {
            if (StringUtils.isNotBlank((String)bilancioList.get(0).get("capitolo")) &&
                bilancioList.get(0).containsKey("importo")) {
              StringBuilder sb = new StringBuilder("<bilancio>");
              BigDecimal importoFromBilancio = BigDecimal.ZERO;
              for (Map<String, Object> bb : bilancioList) {
                if (StringUtils.isNotBlank((String)bb.get("capitolo"))) {
                  sb.append("<capitolo>");
                  sb.append("<codCapitolo>");
                  String capBilancio = StringUtils.firstNonEmpty((String)bb.get("capitolo"), "");
                  sb.append(capBilancio);
                  sb.append("</codCapitolo>");
                  if (StringUtils.isNotBlank((String)bb.get("ufficio"))) {
                    sb.append("<codUfficio>");
                    sb.append(bb.get("ufficio"));
                    sb.append("</codUfficio>");
                  }
                  sb.append("<accertamento>");
                  if (StringUtils.isNotBlank((String)bb.get("accertamento"))) {
                    sb.append("<codAccertamento>");
                    sb.append(bb.get("accertamento"));
                    sb.append("</codAccertamento>");
                  }
                  sb.append("<importo>");
                  sb.append(bb.get("importo"));
                  importoFromBilancio = importoFromBilancio.add(BigDecimal.valueOf((Double) bb.get("importo")));
                  sb.append("</importo></accertamento></capitolo>");
                }
              }
              sb.append("</bilancio>");
              if (importoFromBilancio.compareTo(importoPosizione) == 0) {
                bilancioStr = sb.toString();
                attualizzazione.setBilancio(bilancioStr);
                log.debug("bilancio da inserire: " + bilancioStr);
              } else {
                log.error("Errore importo bilancio");
                throw new AttualizzaImportoException(FaultCodeConstants.PAA_SYSTEM_ERROR, "Errore nell'importo del bilancio", true);
              }
            } else {
              log.error("Capitolo o importo non presenti");
              throw new AttualizzaImportoException(FaultCodeConstants.PAA_SYSTEM_ERROR, "Capitolo di bilancio o importo non presente", true);
            }
          }
        } else {
          log.error("Errore numero avviso: iuv[{}] ente[{}] response[{}]", numeroAvvisoIn, ente.getCodiceFiscaleEnte(), respAttualizzazione);
          throw new AttualizzaImportoException(FaultCodeConstants.PAA_SYSTEM_ERROR, "Errore numero avviso");
        }
      } else {
        Object esito = respAutenticazione.get("esito");
        if (esito != null) {
          throw new AttualizzaImportoException(FaultCodeConstants.PAA_SYSTEM_ERROR, "Esito: " + esito);
        } else {
          throw new AttualizzaImportoException(FaultCodeConstants.PAA_SYSTEM_ERROR, respAutenticazione.get("errore").toString());
        }
      }
    }

    return attualizzazione;
  }

}
