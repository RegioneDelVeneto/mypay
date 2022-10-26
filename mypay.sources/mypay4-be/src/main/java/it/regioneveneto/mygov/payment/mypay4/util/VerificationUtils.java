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

import it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants;
import it.veneto.regione.pagamenti.ente.FaultBean;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtIdentificativoUnivocoPersonaFG;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static it.regioneveneto.mygov.payment.mypay4.ws.util.FaultCodeConstants.*;

@Slf4j
public class VerificationUtils {

  public static FaultBean checkIdentificativoUnivocoPersonaFG(String codIpaEnte, CtIdentificativoUnivocoPersonaFG univocoPersonaFG) {
    if (univocoPersonaFG == null || univocoPersonaFG.getTipoIdentificativoUnivoco() == null ||
        StringUtils.isBlank(univocoPersonaFG.getTipoIdentificativoUnivoco().value())) {
      String msg = "Tipo identificativo univoco non presente";
      log.error(msg);
      return VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_TIPO_IDENTIFICATIVO_PAGATORE_NON_VALIDO, msg, null);
    }
    List<String> possibileTipoIdUnivocos = Arrays.asList(Constants.TIPOIDENTIFICATIVOUNIVOCO_F, Constants.TIPOIDENTIFICATIVOUNIVOCO_G);
    if (!possibileTipoIdUnivocos.contains(univocoPersonaFG.getTipoIdentificativoUnivoco().value())) {
      String msg = "Tipo identificativo univoco non valido: " + univocoPersonaFG.getTipoIdentificativoUnivoco().value();
      log.error(msg);
      return VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_TIPO_IDENTIFICATIVO_PAGATORE_NON_VALIDO, msg, null);

    }
    if (StringUtils.isBlank(univocoPersonaFG.getCodiceIdentificativoUnivoco())) {
      String msg = "Codice identificativo univoco non presente";
      log.error(msg);
      return VerificationUtils.getFaultBean(codIpaEnte, CODE_PAA_CODICE_IDENTIFICATIVO_PAGATORE_NON_PRESENTE, msg, null);
    }
    return null;
  }

  public static FaultBean checkDateExtraction(String codIpa, Date from, Date to) {
    if (from == null) {
      return getFaultBean(codIpa, CODE_PAA_ENTE_NON_VALIDO, "Data inizio non valida",null);
    }
    if (to == null) {
      return getFaultBean(codIpa, CODE_PAA_ENTE_NON_VALIDO, "Data fine non valida",null);
    }
    if (to.before(from)) {
      return getFaultBean(codIpa, CODE_PAA_ENTE_NON_VALIDO, "Intervallo data Extraction non valida",null);
    }
    return null;
  }


  public static FaultBean getFaultBean(String faultID, String faultCode, String faultString, String description, Integer serial) {
    if (!FaultCodeConstants.PAA_PAGAMENTO_NON_INIZIATO_CODE.equals(faultCode) &&
        !FaultCodeConstants.PAA_PAGAMENTO_IN_CORSO_CODE.equals(faultCode) &&
        !FaultCodeConstants.PAA_PAGAMENTO_ANNULLATO_CODE.equals(faultCode) &&
        !FaultCodeConstants.PAA_PAGAMENTO_SCADUTO_CODE.equals(faultCode))
      log.error(faultCode + " " + faultString + " " + description);

    FaultBean faultBean = new FaultBean();
    faultBean.setId(faultID);
    faultBean.setFaultCode(faultCode);
    faultBean.setFaultString(faultString);
    faultBean.setDescription(description);
    faultBean.setSerial(serial);
    return faultBean;
  }

  public static it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean getFespFaultBean(String faultID, String faultCode, String faultString, String description, Integer serial) {
    it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean faultBean = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
    faultBean.setId(faultID);
    faultBean.setFaultCode(faultCode);
    faultBean.setFaultString(faultString);
    faultBean.setDescription(description);
    faultBean.setSerial(serial);
    return faultBean;
  }

  public static it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean getFespFaultBean(String faultID, String faultCode, String faultString) {
    it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean faultBean = new it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean();
    faultBean.setId(faultID);
    faultBean.setFaultCode(faultCode);
    faultBean.setFaultString(faultString);
    return faultBean;
  }

  public static it.veneto.regione.pagamenti.pa.FaultBean convertToPaFaultBean(it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean fb) {
    return getPaFaultBean(fb.getId(), fb.getFaultCode(), fb.getFaultString(), fb.getDescription(), fb.getSerial());
  }

  public static it.veneto.regione.pagamenti.pa.FaultBean getPaFaultBean(String faultID, String faultCode, String faultString, String description, Integer serial) {
    it.veneto.regione.pagamenti.pa.FaultBean faultBean = new it.veneto.regione.pagamenti.pa.FaultBean();
    faultBean.setId(faultID);
    faultBean.setFaultCode(faultCode);
    faultBean.setFaultString(faultString);
    faultBean.setDescription(description);
    faultBean.setSerial(serial);
    return faultBean;
  }

  public static it.veneto.regione.pagamenti.pa.FaultBean convertToPaFaultBean(FaultBean fb) {
    return getPaFaultBean(fb.getId(), fb.getFaultCode(), fb.getFaultString(), fb.getDescription(), fb.getSerial());
  }

  public static FaultBean getFaultBean(String faultID, String faultCode, String faultString, String description) {
    return getFaultBean(faultID, faultCode, faultString, description, null);
  }

  public static gov.telematici.pagamenti.ws.nodoregionaleperspc.FaultBean getNodoFaultBean(String faultID, String faultCode, String faultString, String description, Integer serial) {
    gov.telematici.pagamenti.ws.nodoregionaleperspc.FaultBean faultBean = new gov.telematici.pagamenti.ws.nodoregionaleperspc.FaultBean();
    faultBean.setId(faultID);
    faultBean.setFaultCode(faultCode);
    faultBean.setFaultString(faultString);
    faultBean.setDescription(description);
    faultBean.setSerial(serial);
    return faultBean;
  }
}
