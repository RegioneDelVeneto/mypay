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
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.AttivaRptDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.AttivaRptE;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
@Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
public class AttivaRptService {

  @Autowired
  private AttivaRptDao attivaRptDao;

  public Optional<AttivaRptE> getByKey(String codAttivarptIdentificativoDominio,
                                String codAttivarptIdentificativoUnivocoVersamento,
                                String codAttivarptCodiceContestoPagamento){
    return attivaRptDao.getByKey(codAttivarptIdentificativoDominio, codAttivarptIdentificativoUnivocoVersamento, codAttivarptCodiceContestoPagamento);
  }

  public Optional<AttivaRptE> getById(Long id){
    return attivaRptDao.getById(id);
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public void updateByKey(long mygovAttivaRptEId, Date dtEAttivarpt, BigDecimal numEAttivarptImportoSingoloVersamento,
                                String deEAttivarptIbanAccredito, String deEAttivarptBicAccredito, String codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco,
                                String codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco, String deEAttivarptEnteBenefDenominazioneBeneficiario,
                                String codEAttivarptEnteBenefCodiceUnitOperBeneficiario, String deEAttivarptEnteBenefDenomUnitOperBeneficiario,
                                String deEAttivarptEnteBenefIndirizzoBeneficiario, String deEAttivarptEnteBenefCivicoBeneficiario,
                                String codEAttivarptEnteBenefCapBeneficiario, String deEAttivarptEnteBenefLocalitaBeneficiario,
                                String deEAttivarptEnteBenefProvinciaBeneficiario, String codEAttivarptEnteBenefNazioneBeneficiario,
                                String deEAttivarptCredenzialiPagatore, String deEAttivarptCausaleVersamento, String deAttivarptEsito,
                                String codAttivarptFaultCode, String deAttivarptFaultString, String codAttivarptId, String deAttivarptDescription,
                                Integer codAttivarptSerial, String codAttivarptOriginalFaultCode, String deAttivarptOriginalFaultString,
                                String deAttivarptOriginalFaultDescription) {
    AttivaRptE attivaRptE = AttivaRptE.builder()
        .dtEAttivarpt(dtEAttivarpt)
        .numEAttivarptImportoSingoloVersamento(numEAttivarptImportoSingoloVersamento)
        .deEAttivarptIbanAccredito(deEAttivarptIbanAccredito)
        .deEAttivarptBicAccredito(deEAttivarptBicAccredito)
        .codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco(codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco)
        .codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco(codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco)
        .deEAttivarptEnteBenefDenominazioneBeneficiario(deEAttivarptEnteBenefDenominazioneBeneficiario)
        .codEAttivarptEnteBenefCodiceUnitOperBeneficiario(codEAttivarptEnteBenefCodiceUnitOperBeneficiario)
        .deEAttivarptEnteBenefDenomUnitOperBeneficiario(deEAttivarptEnteBenefDenomUnitOperBeneficiario)
        .deEAttivarptEnteBenefIndirizzoBeneficiario(deEAttivarptEnteBenefIndirizzoBeneficiario)
        .deEAttivarptEnteBenefCivicoBeneficiario(deEAttivarptEnteBenefCivicoBeneficiario)
        .codEAttivarptEnteBenefCapBeneficiario(codEAttivarptEnteBenefCapBeneficiario)
        .deEAttivarptEnteBenefLocalitaBeneficiario(deEAttivarptEnteBenefLocalitaBeneficiario)
        .deEAttivarptEnteBenefProvinciaBeneficiario(deEAttivarptEnteBenefProvinciaBeneficiario)
        .codEAttivarptEnteBenefNazioneBeneficiario(codEAttivarptEnteBenefNazioneBeneficiario)
        .deEAttivarptCredenzialiPagatore(deEAttivarptCredenzialiPagatore)
        .deEAttivarptCausaleVersamento(deEAttivarptCausaleVersamento)
        .deAttivarptEsito(deAttivarptEsito)
        .codAttivarptFaultCode(codAttivarptFaultCode)
        .deAttivarptFaultString(deAttivarptFaultString)
        .codAttivarptId(codAttivarptId)
        .deAttivarptDescription(deAttivarptDescription)
        .codAttivarptOriginalFaultCode(codAttivarptOriginalFaultCode)
        .deAttivarptOriginalFaultString(deAttivarptOriginalFaultString)
        .deAttivarptOriginalFaultDescription(deAttivarptOriginalFaultDescription)
        .codAttivarptSerial(codAttivarptSerial)
        .build();
    int updated = attivaRptDao.updateForAttivaRPT(mygovAttivaRptEId, attivaRptE);
    if(updated!=1)
      throw new MyPayException("invalid number of rows updated:"+updated+" for mygovAttivaRptEId:"+mygovAttivaRptEId);
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public long insert(AttivaRptE attivaRptE){
    return attivaRptDao.insertForAttivaRPT(attivaRptE);
  }
}
