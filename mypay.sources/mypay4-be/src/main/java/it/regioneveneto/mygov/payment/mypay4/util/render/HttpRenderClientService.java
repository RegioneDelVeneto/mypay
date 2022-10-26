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
package it.regioneveneto.mygov.payment.mypay4.util.render;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.regioneveneto.mygov.payment.mypay4.service.common.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HttpRenderClientService {

  @Value("${mydic.schema.host}")
  private String mydicHost;

  @Value("${mydic.schema.context}")
  private String mydicContext;

  private final RestTemplate restTemplate;

  private final Gson gsonInstance;

  private final Type listOfFieldBeanType = new TypeToken<List<FieldBean>>() {}.getType();


  public HttpRenderClientService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
    this.gsonInstance = new Gson();
  }

  @Cacheable(value=CacheService.CACHE_NAME_MY_DICTIONARY_XSD)
  public List<FieldBean> getFieldBeansByCodXsd(String codXsdCausale) {
    String mydicUrl = mydicHost + mydicContext + codXsdCausale;
    try {
      String json = restTemplate.getForObject(mydicUrl, String.class);
      List<FieldBean> listaDefinizioniDovuto = gsonInstance.fromJson(json, listOfFieldBeanType);
      addDefaultCssClass(listaDefinizioniDovuto);
      return listaDefinizioniDovuto;
    } catch (Exception e) {
      log.error("Errore nel recupero del JSON da MyDictionary [{}]", mydicUrl, e);
      return getDefaultXsd();
    }
  }

  private List<FieldBean> getDefaultXsd(){
    List<FieldBean> listaFieldBean = new ArrayList<>();

    FieldBean sys_send_mysearch = new FieldBean();
    sys_send_mysearch.setName("sys_send_mysearch");
    sys_send_mysearch.setRequired(false);
    sys_send_mysearch.setHtmlRender(RenderType.NONE);
    sys_send_mysearch.setDefaultValue("true");
    sys_send_mysearch.setInsertableOrder(0);
    sys_send_mysearch.setIndexable(false);
    sys_send_mysearch.setRenderableOrder(0);
    sys_send_mysearch.setSearchableOrder(0);
    sys_send_mysearch.setListableOrder(0);
    sys_send_mysearch.setInsertable(false);
    sys_send_mysearch.setRenderable(false);
    sys_send_mysearch.setSearchable(false);
    sys_send_mysearch.setListable(false);
    sys_send_mysearch.setAssociation(false);
    sys_send_mysearch.setDetailLink(false);
    sys_send_mysearch.setMinOccurences(0);
    sys_send_mysearch.setMaxOccurences(0);
    listaFieldBean.add(sys_send_mysearch);

    FieldBean sys_type = new FieldBean();
    sys_type.setName("sys_type");
    Map<String, String> extra_attrs = new HashMap<>();
    extra_attrs.put("join_template", "${causale}");
    sys_type.setExtraAttr(extra_attrs);
    listaFieldBean.add(sys_type);

    FieldBean defaultField = new FieldBean();
    defaultField.setName("causale");
    defaultField.setHtmlRender(RenderType.TEXT);
    defaultField.setInsertable(true);
    defaultField.setHtmlLabel("Causale");
    defaultField.setRegex("^.{1,1024}$");
    defaultField.setMinOccurences(1);
    defaultField.setMaxOccurences(1);
    defaultField.setHtmlClass("center");

    extra_attrs = new HashMap<>();
    extra_attrs.put("error_message",
        "Causale non valida. Campo non inserito oppure testo inserito troppo lungo.");
    extra_attrs.put("help_message", "Inserire la causale del pagamento.");
    defaultField.setExtraAttr(extra_attrs);
    listaFieldBean.add(defaultField);
    return listaFieldBean;
  }

  private void addDefaultCssClass(List<FieldBean> listaDefinizioniDovuto) {
    for (FieldBean fieldBean : listaDefinizioniDovuto) {
      if (StringUtils.isBlank(fieldBean.getHtmlClass())) {
        fieldBean.setHtmlClass("center");
      }
      if(fieldBean.getSubfields() != null) {
        addDefaultCssClass(fieldBean.getSubfields());
      }
    }
  }
}
