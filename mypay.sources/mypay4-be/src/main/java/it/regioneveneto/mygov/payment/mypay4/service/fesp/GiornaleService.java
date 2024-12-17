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

import it.regioneveneto.mygov.payment.mypay4.dao.fesp.GiornaleDao;
import it.regioneveneto.mygov.payment.mypay4.dto.GiornaleTo;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Giornale;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleUUIDService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.MaxResultsHelper;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("giornaleFESP")
@Slf4j
public class GiornaleService {

  private static final String EMPTY = "-";

  @Autowired
  GiornaleDao giornaleDao;

  @Autowired
  GiornaleUUIDService giornaleUUIDService;


  @Autowired
  private MaxResultsHelper maxResultsHelper;



  // |---------------------------------------------|
  // |DO NOT ADD OTHER METHODS TO WRITE TO GIORNALE|
  // |---------------------------------------------|

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
  public void insert(Giornale giornale) {
    giornaleDao.insert(giornale);
  }

  /**
   * @deprecated "use @it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService methods instead"
   */
  @Deprecated()
  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRES_NEW)
  public void registraEvento(Date dataOraEvento, String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento,
                             String identificativoPrestatoreServiziPagamento, String tipoVersamento, String componente, String categoriaEvento, String tipoEvento,
                             String sottoTipoEvento, String identificativoFruitore, String identificativoErogatore, String identificativoStazioneIntermediarioPa,
                             String canalePagamento, String parametriSpecificiInterfaccia, String esito) {

    if (esito != null && esito.contains("OK")) {
      esito = Constants.GIORNALE_ESITO_EVENTO.OK.toString();
    } else {
      esito = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
    }

    Giornale giornale = Giornale.builder()
        .dataOraEvento(dataOraEvento!=null?dataOraEvento:new Date())
        .identificativoDominio(abbreviate(identificativoDominio,35))
        .identificativoUnivocoVersamento(abbreviate(identificativoUnivocoVersamento,35))
        .codiceContestoPagamento(abbreviate(codiceContestoPagamento,35))
        .identificativoPrestatoreServiziPagamento(abbreviate(identificativoPrestatoreServiziPagamento,35))
        .tipoVersamento(abbreviate(tipoVersamento,35))
        .componente(abbreviate(componente,35))
        .categoriaEvento(abbreviate(categoriaEvento,35))
        .tipoEvento(abbreviate(tipoEvento,35))
        .sottoTipoEvento(abbreviate(sottoTipoEvento,35))
        .identificativoFruitore(abbreviate(identificativoFruitore,50))
        .identificativoErogatore(abbreviate(identificativoErogatore,35))
        .identificativoStazioneIntermediarioPa(abbreviate(identificativoStazioneIntermediarioPa,50))
        .canalePagamento(abbreviate(canalePagamento,35))
        .parametriSpecificiInterfaccia(abbreviate(parametriSpecificiInterfaccia,16384))
        .esito(esito)
        .build();
    giornaleDao.insert(giornale);
  }

  // |---------------------------------------------|
  // |DO NOT ADD OTHER METHODS TO WRITE TO GIORNALE|
  // |ONLY USE registraEvento()                    |
  // ----------------------------------------------|

  public List<GiornaleTo> searchGiornale(String identificativoDominio, String identificativoUnivocoVersamento,
                                       String categoriaEvento, String tipoEvento,
                                       String identificativoPrestatoreServiziPagamento, String esito,
                                       LocalDate dataOraEventoFrom, LocalDate dataOraEventoTo) {
    LocalDateTime dataOraEventoToAtEndOfDay = dataOraEventoTo.atStartOfDay().plusDays(1).minusSeconds(1);
    return maxResultsHelper.manageMaxResults(
      maxResults -> giornaleDao.searchGiornale(identificativoDominio, identificativoUnivocoVersamento,
          categoriaEvento, tipoEvento, identificativoPrestatoreServiziPagamento, esito,
          dataOraEventoFrom.atStartOfDay(), dataOraEventoToAtEndOfDay, maxResults)
          .stream().map(this::mapToDto).collect(Collectors.toList()),
        () -> giornaleDao.searchGiornaleCount(identificativoDominio, identificativoUnivocoVersamento,
            categoriaEvento, tipoEvento, identificativoPrestatoreServiziPagamento, esito,
            dataOraEventoFrom.atStartOfDay(), dataOraEventoToAtEndOfDay) );
  }

  public Giornale getGiornaleById(Long mygovGiornaleId){
    return giornaleDao.getGiornaleById(mygovGiornaleId);
  }

  public GiornaleTo mapToDto(Giornale giornale){
  return GiornaleTo.builder()
      .mygovGiornaleId(giornale.getMygovGiornaleId())
      .dataOraEvento(Utilities.toLocalDateTime(giornale.getDataOraEvento()))
      .idDominio(giornale.getIdentificativoDominio())
      .iuv(giornale.getIdentificativoUnivocoVersamento())
      .evento(giornale.getTipoEvento()+" - "+giornale.getSottoTipoEvento()+" ("+giornale.getCategoriaEvento()+")")
      .idPsp(giornale.getIdentificativoPrestatoreServiziPagamento())
      .build();
  }

  public GiornaleTo mapToDetailDto(Giornale giornale){
    return mapToDto(giornale).toBuilder()
        .tipo(giornale.getTipoEvento())
        .sottotipo(giornale.getSottoTipoEvento())
        .categoria(giornale.getCategoriaEvento())
        .esito(giornale.getEsito())
        .contestoPagamento(giornale.getCodiceContestoPagamento())
        .tipoVersamento(giornale.getTipoVersamento())
        .componente(giornale.getComponente())
        .idFruitore(giornale.getIdentificativoFruitore())
        .idErogatore(giornale.getIdentificativoErogatore())
        .idStazione(giornale.getIdentificativoStazioneIntermediarioPa())
        .canalePagamento(giornale.getCanalePagamento())
        .parametriInterfaccia(giornale.getParametriSpecificiInterfaccia())
        .build();
  }

  @Cacheable(value= CacheService.CACHE_NAME_ALL_OBJECTS, key="{#root.method}")
  public List<String> getAllPspFesp(){
    return giornaleDao.getAllPsp().stream().sorted().collect(Collectors.toList());
  }

  public List<String> getAllTipoEventoFesp() {
    return Arrays.stream(Constants.GIORNALE_TIPO_EVENTO_FESP.values())
        .map(Constants.GIORNALE_TIPO_EVENTO_FESP::toString)
        .sorted()
        .collect(Collectors.toList());
  }

  public List<String> getAllCategoriaEventoFesp(){
    return Arrays.stream(Constants.GIORNALE_CATEGORIA_EVENTO.values())
        .map(Constants.GIORNALE_CATEGORIA_EVENTO::toString)
        .sorted()
        .collect(Collectors.toList());
  }

  public List<String> getAllEsitoEventoFesp(){
    return Arrays.stream(Constants.GIORNALE_ESITO_EVENTO.values())
        .map(Constants.GIORNALE_ESITO_EVENTO::toString)
        .sorted()
        .collect(Collectors.toList());
  }

  private String abbreviate(String s, int maxLength){
    return StringUtils.abbreviate(StringUtils.defaultIfBlank(s,EMPTY),maxLength);
  }

}
