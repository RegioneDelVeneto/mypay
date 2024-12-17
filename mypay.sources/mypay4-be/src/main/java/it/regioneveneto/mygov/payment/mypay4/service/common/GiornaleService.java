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
package it.regioneveneto.mygov.payment.mypay4.service.common;

import com.google.gson.Gson;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.exception.WSFaultResponseWrapperException;
import it.regioneveneto.mygov.payment.mypay4.model.common.Giornale;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.XmlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("giornaleCommon")
@Slf4j
public class GiornaleService {

  private static final String EMPTY = "-";

  public static final String SEPARATOR = "|";

  @Autowired
  private it.regioneveneto.mygov.payment.mypay4.service.GiornaleService giornalePaService;

  @Autowired
  private it.regioneveneto.mygov.payment.mypay4.service.fesp.GiornaleService giornaleFespService;

  @Autowired
  private GiornaleUUIDService giornaleUUIDService;

  private final Gson gson = new Gson();


  // |---------------------------------------------|
  // |DO NOT ADD OTHER METHODS TO WRITE TO GIORNALE|
  // |ONLY USE registraEvento()                    |
  // |---------------------------------------------|

  private void insertGiornaleImpl(Giornale giornale){
    if(giornale instanceof it.regioneveneto.mygov.payment.mypay4.model.Giornale)
      giornalePaService.insert((it.regioneveneto.mygov.payment.mypay4.model.Giornale)giornale);
    else
      giornaleFespService.insert((it.regioneveneto.mygov.payment.mypay4.model.fesp.Giornale)giornale);
  }

  private void insertGiornale(Giornale giornale) {
    try {
      //handle cases where iuv, idDominio and CCP are multiple
      if (StringUtils.contains(giornale.getIdentificativoUnivocoVersamento(), SEPARATOR)) {
        String[] iuvs = ObjectUtils.firstNonNull(StringUtils.split(giornale.getIdentificativoUnivocoVersamento(), SEPARATOR), new String[]{});
        String[] idDominios = ObjectUtils.firstNonNull(StringUtils.split(giornale.getIdentificativoDominio(), SEPARATOR), new String[]{});
        String[] ccps = ObjectUtils.firstNonNull(StringUtils.split(giornale.getCodiceContestoPagamento(), SEPARATOR), new String[]{});
        if (iuvs.length != idDominios.length || iuvs.length != ccps.length || iuvs.length < 2) {
          log.error("incoherent giornale data: {}", ReflectionToStringBuilder.toString(giornale));
          throw new MyPayException("incoherent giornale data");
        }
        for (int i = 0; i < iuvs.length; i++) {
          giornale.setIdentificativoUnivocoVersamento(iuvs[i]);
          giornale.setIdentificativoDominio(idDominios[i]);
          giornale.setCodiceContestoPagamento(ccps[i]);
          this.insertGiornaleImpl(giornale);
        }
      } else {
        this.insertGiornaleImpl(giornale);
      }
    } catch(Exception e){
      log.warn("ignoring error on insert in giornale", e);
    }
  }

  private Optional<GiornaleUUIDService.GiornaleUUID> recordSoapEventDataImpl(Optional<GiornaleUUIDService.GiornaleUUID> uuid, Optional<Constants.GIORNALE_MODULO> module,
                                                                             Optional<Giornale> giornale, Optional<String> parametriSpecificiInterfaccia){
    //check if other data is present on this giornale event
    if(uuid.isPresent()){
      //data is present: retrieve it
      GiornaleUUIDService.GiornaleData giornaleData = giornaleUUIDService.getGiornaleData(uuid.get());
      if(giornaleData == null){
        if(module.isPresent())
          log.error("null giornaleData, uuid[{}] giornale[{}] parametriSpecifiInterfaccia[{}]", uuid.get(), giornale, parametriSpecificiInterfaccia);
        throw new MyPayException("error writing to giornale");
      }
      //add this data
      giornale.ifPresentOrElse(g -> {
        g.setParametriSpecificiInterfaccia(giornaleData.getParametriSpecificiInterfaccia());
        giornaleData.setGiornale(g);
      }, () -> parametriSpecificiInterfaccia.ifPresent(p -> giornaleData.getGiornale().setParametriSpecificiInterfaccia(p)));
      //write giornale
      this.insertGiornale(giornaleData.getGiornale());

      //remove this giornaleData from map
      giornaleUUIDService.removeGiornaleDataAndUUID(uuid.get());
      //return empty
      return Optional.empty();
    } else {
      //data is not present: just store it in map
      uuid = Optional.of(new GiornaleUUIDService.GiornaleUUID());
      GiornaleUUIDService.GiornaleData giornaleData = GiornaleUUIDService.GiornaleData.builder()
        .giornale(giornale.orElse(null))
        .parametriSpecificiInterfaccia(parametriSpecificiInterfaccia.orElse(null))
        .giornaleUUID(uuid.get())
        .build();
      giornaleUUIDService.setGiornaleData(uuid.get(), giornaleData);
      //return uuid
      return uuid;
    }
  }

  private Optional<GiornaleUUIDService.GiornaleUUID> recordSoapEventDataImpl(Optional<GiornaleUUIDService.GiornaleUUID> uuid, Constants.GIORNALE_MODULO module,
                                                                             Date dataOraEvento, String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento,
                                                                             String identificativoPrestatoreServiziPagamento, String tipoVersamento, String componente, String categoriaEvento, String tipoEvento,
                                                                             String sottoTipoEvento, String identificativoFruitore, String identificativoErogatore, String identificativoStazioneIntermediarioPa,
                                                                             String canalePagamento, String esito) {
    Giornale giornale = (module==Constants.GIORNALE_MODULO.PA ?
      it.regioneveneto.mygov.payment.mypay4.model.Giornale.builder() :
      it.regioneveneto.mygov.payment.mypay4.model.fesp.Giornale.builder())
      .dataOraEvento(dataOraEvento!=null?dataOraEvento:new Date())
      .identificativoDominio(abbreviate(identificativoDominio,35, true))
      .identificativoUnivocoVersamento(abbreviate(identificativoUnivocoVersamento,35, true))
      .codiceContestoPagamento(abbreviate(codiceContestoPagamento,35, true))
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
      .esito((StringUtils.contains(esito, "OK") ? Constants.GIORNALE_ESITO_EVENTO.OK : Constants.GIORNALE_ESITO_EVENTO.KO).toString())
      .build();
    return this.recordSoapEventDataImpl(uuid, Optional.of(module), Optional.of(giornale), Optional.empty());
  }

  private Optional<GiornaleUUIDService.GiornaleUUID> recordSoapEventDataImpl(Optional<GiornaleUUIDService.GiornaleUUID> uuid, String parametriSpecificiInterfaccia){
    return this.recordSoapEventDataImpl(uuid, Optional.empty(), Optional.empty(),
      Optional.ofNullable(abbreviate(XmlUtils.minifyXml(parametriSpecificiInterfaccia),16384)));
  }

  public void recordSoapEventFirst(Constants.GIORNALE_SOTTOTIPO_EVENTO msgType, String parametriSpecificiInterfaccia){
    this.recordSoapEventDataImpl(Optional.empty(), parametriSpecificiInterfaccia)
      .ifPresentOrElse(giornaleUUID -> giornaleUUIDService.setGiornaleUUID(msgType, giornaleUUID),
        () -> log.error("empty UUID on recordSoapEventFirst msgType[{}], psi[{}]", msgType, parametriSpecificiInterfaccia, new RuntimeException("stackTrace")));
  }

  public void recordSoapResponseExceptionFirst(Throwable exception){
    final Constants.GIORNALE_SOTTOTIPO_EVENTO msgType = Constants.GIORNALE_SOTTOTIPO_EVENTO.RES;
    if(giornaleUUIDService.getGiornaleUUID(msgType).isEmpty())
      this.recordSoapEventDataImpl(Optional.empty(), Optional.ofNullable(exception).map(ExceptionUtils::getStackTrace).orElse(""))
        .ifPresentOrElse(giornaleUUID -> giornaleUUIDService.setGiornaleUUID(msgType, giornaleUUID),
          () -> log.error("empty UUID on recordSoapResponseExceptionFirst msgType[{}], exception[{}]", msgType, exception, new RuntimeException("stackTrace")));
  }

  public void recordSoapEventLast(Constants.GIORNALE_MODULO module, Constants.GIORNALE_SOTTOTIPO_EVENTO msgType,
                                  Date dataOraEvento, String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento,
                                  String identificativoPrestatoreServiziPagamento, String tipoVersamento, String componente, String categoriaEvento, String tipoEvento,
                                  String identificativoFruitore, String identificativoErogatore, String identificativoStazioneIntermediarioPa,
                                  String canalePagamento, String esito){
    giornaleUUIDService.getGiornaleUUID(msgType).ifPresentOrElse(
      giornaleUUID -> this.recordSoapEventDataImpl(Optional.of(giornaleUUID), module,
        dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
        identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento,
        msgType.toString(), identificativoFruitore, identificativoErogatore, identificativoStazioneIntermediarioPa,
        canalePagamento, esito),
      ()->log.debug("untracked Soap event [{}] [{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}]", msgType, dataOraEvento, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
        identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento,
        msgType, identificativoFruitore, identificativoErogatore, identificativoStazioneIntermediarioPa,
        canalePagamento, esito)
    );
  }

  public void recordSoapEventFirst(Constants.GIORNALE_MODULO module, Constants.GIORNALE_SOTTOTIPO_EVENTO msgType,
                                   Date dataOraEvento, String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento,
                                   String identificativoPrestatoreServiziPagamento, String tipoVersamento, String componente, String categoriaEvento, String tipoEvento,
                                   String identificativoFruitore, String identificativoErogatore, String identificativoStazioneIntermediarioPa,
                                   String canalePagamento, String esito){
    this.recordSoapEventDataImpl(Optional.empty(), module,
        dataOraEvento,identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
        identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento,
        msgType.toString(), identificativoFruitore, identificativoErogatore, identificativoStazioneIntermediarioPa,
        canalePagamento, esito)
      .ifPresentOrElse(giornaleUUID -> giornaleUUIDService.setGiornaleUUID(msgType, giornaleUUID),
        () -> log.error("empty UUID on recordSoapEventLast msgType[{}], dt[{}] idDom[{}] iuv[{}] tipoEv[{}]",
          msgType, dataOraEvento,identificativoDominio, identificativoUnivocoVersamento, tipoEvento, new RuntimeException("stackTrace")));
  }

  public void recordSoapEventLast(Constants.GIORNALE_SOTTOTIPO_EVENTO msgType, String parametriSpecificiInterfaccia){
    giornaleUUIDService.getGiornaleUUID(msgType).ifPresentOrElse(
      giornaleUUID -> this.recordSoapEventDataImpl(Optional.of(giornaleUUID), parametriSpecificiInterfaccia),
      ()->log.debug("untracked Soap event [{}] [{}]", msgType, parametriSpecificiInterfaccia)
    );
  }

  public <T> T wrapRecordSoapServerEvent(Constants.GIORNALE_MODULO module, String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento,
                                         String identificativoPrestatoreServiziPagamento, String tipoVersamento, String componente, String categoriaEvento, String tipoEvento,
                                         String identificativoFruitore, String identificativoErogatore, String identificativoStazioneIntermediarioPa, String canalePagamento,
                                         Supplier<T> responseSupplier, Function<T, String> esitoFunction){
    // log request to giornale
    this.recordSoapEventLast(module, Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ, null, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
      identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, identificativoFruitore,
      identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, Constants.GIORNALE_ESITO_EVENTO.OK.toString());
    T response;
    String outcome = null;
    try{
      // process the web service
      try{
        response = responseSupplier.get();
      }catch(WSFaultResponseWrapperException wsfe){
        if(log.isWarnEnabled())
          log.warn("WSFaultResponseWrapperException [{}]", ReflectionToStringBuilder.toString(wsfe.getRawFaultResponse()));
        response = (T) wsfe.getRawFaultResponse();
      }
      // retrieve esito
      outcome = esitoFunction.apply(response);
      return response;
    }catch(Exception e){
      log.error("error on wrapRecordSoapServerEvent", e);
      outcome = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
      throw e;
    }finally{
      // log response to giornale
      this.recordSoapEventFirst(module, Constants.GIORNALE_SOTTOTIPO_EVENTO.RES, null, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
        identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, identificativoFruitore,
        identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, outcome);
    }
  }

  public <T> T wrapRecordSoapClientEvent(Constants.GIORNALE_MODULO module, String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento,
                                         String identificativoPrestatoreServiziPagamento, String tipoVersamento, String componente, String categoriaEvento, String tipoEvento,
                                         String identificativoFruitore, String identificativoErogatore, String identificativoStazioneIntermediarioPa, String canalePagamento,
                                         Supplier<T> responseSupplier, Function<T, String> esitoFunction){
    // log request to giornale
    this.recordSoapEventFirst(module, Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ, null, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
      identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, identificativoFruitore,
      identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, Constants.GIORNALE_ESITO_EVENTO.OK.toString());
    T response;
    String outcome = null;
    try{
      // process the web service
      response = responseSupplier.get();
      // retrieve esito
      outcome = esitoFunction.apply(response);
      return response;
    }catch(Exception e){
      log.error("error on wrapRecordSoapClientEvent", e);
      outcome = Constants.GIORNALE_ESITO_EVENTO.KO.toString();
      this.recordSoapResponseExceptionFirst(e);
      throw e;
    }finally{
      // log response to giornale
      this.recordSoapEventLast(module, Constants.GIORNALE_SOTTOTIPO_EVENTO.RES, null, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
        identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento, identificativoFruitore,
        identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento, outcome);
    }
  }

  public <T> ResponseEntity<T> wrapRecordRestClientEvent(
    Constants.GIORNALE_MODULO module, String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento,
    String identificativoPrestatoreServiziPagamento, String tipoVersamento, String componente, String categoriaEvento, String tipoEvento,
    String identificativoFruitore, String identificativoErogatore, String identificativoStazioneIntermediarioPa, String canalePagamento,
    RestTemplate restTemplate,
    HttpMethod httpMethod,
    String url,
    Object body,
    Class<T> responseType,
    Function<ResponseEntity<T>, String> esitoFunction){

    HttpEntity<?> bodyEntity = body==null ? HttpEntity.EMPTY : new HttpEntity<>(body);

    return  this.wrapRecordRestClientEvent(module, identificativoDominio, identificativoUnivocoVersamento, codiceContestoPagamento,
            identificativoPrestatoreServiziPagamento, tipoVersamento, componente, categoriaEvento, tipoEvento,
            identificativoFruitore, identificativoErogatore, identificativoStazioneIntermediarioPa, canalePagamento,
            restTemplate, httpMethod, url, bodyEntity, responseType, esitoFunction, false);

  }

  public <T> ResponseEntity<T> wrapRecordRestClientEvent(
      Constants.GIORNALE_MODULO module, String identificativoDominio, String identificativoUnivocoVersamento, String codiceContestoPagamento,
      String identificativoPrestatoreServiziPagamento, String tipoVersamento, String componente, String categoriaEvento, String tipoEvento,
      String identificativoFruitore, String identificativoErogatore, String identificativoStazioneIntermediarioPa, String canalePagamento,
      RestTemplate restTemplate,
      HttpMethod httpMethod,
      String url,
      HttpEntity<?> body,
      Class<T> responseType,
      Function<ResponseEntity<T>, String> esitoFunction,
      boolean isMultipartFormData){
    // log request to giornale
    Giornale giornale = (module==Constants.GIORNALE_MODULO.PA ?
      it.regioneveneto.mygov.payment.mypay4.model.Giornale.builder() :
      it.regioneveneto.mygov.payment.mypay4.model.fesp.Giornale.builder())
      .dataOraEvento(new Date())
      .identificativoDominio(abbreviate(identificativoDominio,35, true))
      .identificativoUnivocoVersamento(abbreviate(identificativoUnivocoVersamento,35, true))
      .codiceContestoPagamento(abbreviate(codiceContestoPagamento,35, true))
      .identificativoPrestatoreServiziPagamento(abbreviate(identificativoPrestatoreServiziPagamento,35))
      .tipoVersamento(abbreviate(tipoVersamento,35))
      .componente(abbreviate(componente,35))
      .categoriaEvento(abbreviate(categoriaEvento,35))
      .tipoEvento(abbreviate(tipoEvento,35))
      .sottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.REQ.name())
      .identificativoFruitore(abbreviate(identificativoFruitore,50))
      .identificativoErogatore(abbreviate(identificativoErogatore,35))
      .identificativoStazioneIntermediarioPa(abbreviate(identificativoStazioneIntermediarioPa,50))
      .canalePagamento(abbreviate(canalePagamento,35))
      .esito(Constants.GIORNALE_ESITO_EVENTO.OK.name())
      .build();

    ResponseEntity<T> response;
    String outcome = null;
    try {
      // fill request info specific fields
      String jsonBody;
      if (isMultipartFormData) {
        jsonBody = "multipart upload: body content omissis...";
      } else {
        jsonBody = Optional.ofNullable(body).map(HttpEntity::getBody).map(gson::toJson).orElse(null);
      }

      giornale.setParametriSpecificiInterfaccia(abbreviate("url["+url+"] method["+httpMethod.name()+"] body["+jsonBody+"]", 16384));

      // execute the rest call
      if (isMultipartFormData) {
        response = restTemplate.exchange(url, httpMethod, body, responseType);
      } else {
        response = restTemplate.exchange(url, httpMethod, body==null ? HttpEntity.EMPTY : body, responseType);
      }

      this.insertGiornale(giornale); //insert REQ OK

      // retrieve esito
      outcome = esitoFunction.apply(response);
      StringBuilder responseInfo = new StringBuilder("status["+response.getStatusCodeValue()+"]");
      Optional.of(response.getHeaders()).map(HttpHeaders::getLocation).ifPresent(location -> responseInfo.append(" location[").append(location).append("]"));
      Optional.ofNullable(response.getBody()).map(gson::toJson).ifPresent(b -> responseInfo.append(" body[").append(b).append("]"));
      giornale.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.name());
      giornale.setParametriSpecificiInterfaccia(abbreviate(responseInfo.toString(), 16384));
      giornale.setEsito((StringUtils.contains(outcome, "OK") ? Constants.GIORNALE_ESITO_EVENTO.OK : Constants.GIORNALE_ESITO_EVENTO.KO).toString());
      this.insertGiornale(giornale); //insert RES OK/KO
      return response;
    }catch(RestClientResponseException rcre){
      //request sent ok, error on response
      this.insertGiornale(giornale); //insert REQ OK (but exception on response)
      giornale.setSottoTipoEvento(Constants.GIORNALE_SOTTOTIPO_EVENTO.RES.name());
      giornale.setParametriSpecificiInterfaccia(abbreviate("status["+rcre.getRawStatusCode()+"] body["+rcre.getResponseBodyAsString()+"]", 16384));
      giornale.setEsito(Constants.GIORNALE_ESITO_EVENTO.KO.name());
      this.insertGiornale(giornale); //insert RES KO
      throw rcre;
    }catch(Exception e){
      log.error("error on wrapRecordRestClientEvent", e);
      giornale.setEsito(Constants.GIORNALE_ESITO_EVENTO.KO.name());
      this.insertGiornale(giornale); //insert KO
      throw e;
    }
  }

  private String abbreviate(String s, int maxLength){
    return abbreviate(s ,maxLength, false);
  }
  private String abbreviate(String s, int maxLength, boolean allowMultiple){
    if(allowMultiple && s!=null)
      return Stream.of(StringUtils.split(s, SEPARATOR)).map(x -> this.abbreviate(x, maxLength, false)).collect(Collectors.joining(SEPARATOR));
    else
      return StringUtils.abbreviate(StringUtils.defaultIfBlank(s,EMPTY), maxLength);
  }

}
