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

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.FlussoQuadSpcDao;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.FlussoRendSpcDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.fesp.FespException;
import it.regioneveneto.mygov.payment.mypay4.exception.fesp.FespValidatorException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.FlussoSpc;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FlussoSPC;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FlussoSpcService {

  @Autowired
  private EnteService enteService;

  @Autowired
  private FlussoQuadSpcDao flussoQuadSpcDao;

  @Autowired
  private FlussoRendSpcDao flussoRendSpcDao;

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
  public List<FlussoSPC> getFlussoSpc(Constants.FLG_TIPO_FLUSSO TIPO_FLUSSO, String identificativoDominio,
                                      String identificativoPsp, LocalDate from, LocalDate to, String prodOrDisp) {
    List<FlussoSpc> flussi;
    Ente ente = enteService.getEnteByCodFiscale(identificativoDominio);
    if (ente == null)
      throw new FespValidatorException("Codice fiscale Ente [" + identificativoDominio + "] non valido o non presente in database");

    String codIpaEnte = ente.getCodIpaEnte();
    if (TIPO_FLUSSO.equals(Constants.FLG_TIPO_FLUSSO.QUADRATURA)) {
      flussi = flussoQuadSpcDao.getByCodIpaEnte(codIpaEnte, from, to.plusDays(1), prodOrDisp);
    } else {
      flussi = flussoRendSpcDao.getByCodIpaEnteIdentificativoPsp(codIpaEnte, identificativoPsp, from, to.plusDays(1), prodOrDisp);
    }
    return flussi.stream().map(f -> this.mapToDto(TIPO_FLUSSO, identificativoDominio, f)).collect(Collectors.toList());
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
  public FlussoSPC getFlussoSpcEsclStato(Constants.FLG_TIPO_FLUSSO TIPO_FLUSSO, String identificativoDominio, String codIdentificativoFlusso,
                                             String identificativoPsp, Date dtDataOraFlusso, String codStato) {
    List<FlussoSpc> flussi;
    Ente ente = enteService.getEnteByCodFiscale(identificativoDominio);
    if (ente == null)
      throw new FespValidatorException("Codice fiscale Ente [" + identificativoDominio + "] non valido o non presente in database");

    String codIpaEnte = ente.getCodIpaEnte();
    if (TIPO_FLUSSO.equals(Constants.FLG_TIPO_FLUSSO.QUADRATURA)) {
      flussi = flussoQuadSpcDao.getByEnteIdFlussoExclStato(codIpaEnte, codIdentificativoFlusso, dtDataOraFlusso, codStato);
    } else {
      flussi = flussoRendSpcDao.getByEnteIdFlussoExclStato(codIpaEnte, identificativoPsp, codIdentificativoFlusso, dtDataOraFlusso, codStato);
    }
    if (flussi != null && flussi.size() > 1)
      throw new FespException(String.format("'(TIPO [ %s ], codiceIpaEnte [ %s ], identificativoPsp [ %s ], codIdentificativoFlusso [ %s ])' is not unique. " +
          "%d records found.", TIPO_FLUSSO.name(), codIpaEnte, identificativoPsp, codIdentificativoFlusso, flussi.size()));
    return CollectionUtils.isEmpty(flussi) ? null : this.mapToDto(TIPO_FLUSSO, identificativoDominio, flussi.get(0));
  }

  private FlussoSPC mapToDto(Constants.FLG_TIPO_FLUSSO tipoFlusso, String identificativoDominio, FlussoSpc flusso) {
    FlussoSPC dto = new FlussoSPC();
    dto.setFlgTipoFlusso(tipoFlusso.getValue());
    dto.setIdentificativoDominio(identificativoDominio);
    dto.setIdentificativoPSP(flusso.getIdentificativoPsp());
    dto.setCodIdentificativoFlusso(flusso.getCodIdentificativoFlusso());
    dto.setDeNomeFileScaricato(flusso.getDeNomeFileScaricato());
    dto.setNumDimensioneFileScaricato(flusso.getNumDimensioneFileScaricato());
    dto.setDtDataOraFlusso(Utilities.toXMLGregorianCalendar(flusso.getDtDataOraFlusso()));
    dto.setDtCreazione(Utilities.toXMLGregorianCalendar(flusso.getDtCreazione()));
    dto.setDtUltimaModifica(Utilities.toXMLGregorianCalendar(flusso.getDtUltimaModifica()));
    return dto;
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.SUPPORTS)
  public Optional<FlussoSpc> getRendicontazioneByKeyInsertable(String codIpaEnte, String codIdentificativoFlusso,
                                                               String identificativoPsp, Date dtDataOraFlusso) {
    List<FlussoSpc> flussi = flussoRendSpcDao.getByEnteIdFlussoExclStato(codIpaEnte, identificativoPsp, codIdentificativoFlusso, dtDataOraFlusso, Constants.STATO_ESECUZIONE_FLUSSO_ERRORE_CARICAMENTO);
    if(flussi.size()==0)
      return Optional.empty();
    else if(flussi.size()>1) {
      log.error("multiple flussi found ({}) matching search parameters codIpaEnte:{}, identificativoPsp:{}, codIdentificativoFlusso:{}, dtDataOraFlusso:{}",
          flussi.size(), codIpaEnte, identificativoPsp, codIdentificativoFlusso, dtDataOraFlusso);
      throw new MyPayException("multiple flussi found when expecting only one");
    } else {
      return Optional.of(flussi.get(0));
    }
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public Integer insertRendicontazione(String codIpaEnte, String codIdentificativoFlusso,
                                       String identificativoPsp, Timestamp dtDataOraFlusso) {
    return flussoRendSpcDao.insert(codIpaEnte, identificativoPsp, codIdentificativoFlusso, dtDataOraFlusso, Constants.STATO_ESECUZIONE_FLUSSO_IN_CARICAMENTO);
  }

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public Integer updateRendicontazione(Integer idMygovFlussoRendSpc, String nomeFileScaricato, long numDimensioneFileScaricato, final String codStato) {
    return flussoRendSpcDao.update(idMygovFlussoRendSpc, nomeFileScaricato, numDimensioneFileScaricato, codStato);
  }
}
