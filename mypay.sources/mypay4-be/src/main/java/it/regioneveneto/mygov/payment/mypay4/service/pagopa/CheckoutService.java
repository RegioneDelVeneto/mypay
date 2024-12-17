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
package it.regioneveneto.mygov.payment.mypay4.service.pagopa;

import it.regioneveneto.mygov.payment.mypay4.dto.EsitoTo;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.checkout.Cart;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.checkout.Fault;
import it.regioneveneto.mygov.payment.mypay4.dto.pagopa.checkout.PaymentNotice;
import it.regioneveneto.mygov.payment.mypay4.service.common.GiornaleService;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.veneto.regione.pagamenti.nodoregionalefesp.nodoregionaleperpa.FaultBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

@Service
@Slf4j
public class CheckoutService {

  @Value("${pa.pagopa.checkout.baseUrl}")
  private String baseUrl;

  @Value("${nodoRegionaleFesp.identificativoIntermediarioPA}")
  private String identificativoIntermediarioPA;

  @Value("${nodoRegionaleFesp.identificativoStazioneIntermediarioPA}")
  private String identificativoStazioneIntermediarioPA;

  private final RestTemplate restTemplate;

  @Autowired
  private GiornaleService giornaleService;

  public CheckoutService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  public EsitoTo sendCart(Cart cart) {
    String faultCode, faultDescription;
    long httpCallStart = System.currentTimeMillis();
    try {
      ResponseEntity<Fault> response = giornaleService.wrapRecordRestClientEvent(
        Constants.GIORNALE_MODULO.FESP,
        cart.getPaymentNotices().stream().map(PaymentNotice::getFiscalCode).collect(Collectors.joining(GiornaleService.SEPARATOR)),
          cart.getPaymentNotices().stream().map(PaymentNotice::getNoticeNumber).collect(Collectors.joining(GiornaleService.SEPARATOR)),
          cart.getPaymentNotices().stream().map(pn -> "-").collect(Collectors.joining(GiornaleService.SEPARATOR)),
          null,
          null,
          Constants.COMPONENTE_FESP,
          Constants.GIORNALE_CATEGORIA_EVENTO.INTERFACCIA.toString(),
          Constants.GIORNALE_TIPO_EVENTO_FESP.checkoutCart.toString(),
          identificativoIntermediarioPA,
          identificativoIntermediarioPA,
          identificativoStazioneIntermediarioPA,
          null,
          restTemplate,
          HttpMethod.POST,
          baseUrl+"/carts",
          cart,
          Fault.class,
          r -> r.getStatusCode().is3xxRedirection() && r.getHeaders().getLocation()!=null ?
              Constants.GIORNALE_ESITO_EVENTO.OK.toString() :
              Constants.GIORNALE_ESITO_EVENTO.KO.toString()
      );

      if( response.getStatusCode().is3xxRedirection() && response.getHeaders().getLocation()!=null ){
        return EsitoTo.builder()
            .esito(Constants.STATO_ESITO_OK)
            .url(response.getHeaders().getLocation().toString())
            .build();
      } else {
        FaultBean faultBean = new FaultBean();
        if(response.getBody()!=null) {
          faultBean.setFaultCode(response.getBody().getStatus());
          faultBean.setFaultString(response.getBody().getTitle());
          faultBean.setDescription(response.getBody().getDetail());
        }
        log.error("error processing cart: {} - body: {}", cart, response.getBody());
        return EsitoTo.builder()
            .esito(Constants.STATO_ESITO_KO)
            .faultBean(faultBean)
            .build();
      }
    } catch(HttpServerErrorException hse){
      int statusCode = hse.getRawStatusCode();
      String body = hse.getResponseBodyAsString();
      log.warn("HttpServerErrorException returned code[{}] body[{}]",statusCode, body);
      faultCode = Integer.toString(statusCode);
      faultDescription = body;
    } catch(HttpClientErrorException hce){
      int statusCode = hce.getRawStatusCode();
      String body = hce.getResponseBodyAsString();
      log.warn("HttpClientErrorException returned code[{}] body[{}]",statusCode, body);
      faultCode = Integer.toString(statusCode);
      faultDescription = body;
    } catch(RestClientException e){
      log.error("error sending cart[{}]", cart, e);
      faultCode = ExceptionUtils.getMessage(e) + " -- "+ExceptionUtils.getRootCauseMessage(e);
      faultDescription = ExceptionUtils.getStackTrace(e);
    } finally {
      long elapsed = Math.max(0, System.currentTimeMillis() - httpCallStart);
      log.info("elapsed time(ms) for sending cart[{}]: {}", cart, elapsed);
    }
    //return in case of exception
    FaultBean faultBean = new FaultBean();
    faultBean.setFaultCode(faultCode);
    faultBean.setFaultString(faultDescription);
    return EsitoTo.builder()
        .esito(Constants.STATO_ESITO_KO)
        .faultBean(faultBean)
        .build();
  }
}
