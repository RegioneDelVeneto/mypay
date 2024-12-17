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

import com.google.gson.Gson;
import it.regioneveneto.mygov.payment.mypay4.dto.DefinitionDovuto;
import it.regioneveneto.mygov.payment.mypay4.dto.DovutoDipendente;
import it.regioneveneto.mygov.payment.mypay4.dto.SpontaneoFormTo;
import it.regioneveneto.mygov.payment.mypay4.dto.SpontaneoTo;
import it.regioneveneto.mygov.payment.mypay4.exception.DefinitionDovutoException;
import it.regioneveneto.mygov.payment.mypay4.model.EnteTipoDovuto;
import it.regioneveneto.mygov.payment.mypay4.util.Constants;
import it.regioneveneto.mygov.payment.mypay4.util.NamespaceFilter;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import it.regioneveneto.mygov.payment.mypay4.util.render.FieldBean;
import it.regioneveneto.mygov.payment.mypay4.util.render.RenderType;
import it.veneto.regione.schemas._2012.pagamenti.ente.bilanciodefault.Bilancio;
import it.veneto.regione.schemas._2012.pagamenti.ente.bilanciodefault.CtAccertamentoDefault;
import it.veneto.regione.schemas._2012.pagamenti.ente.bilanciodefault.CtCapitoloDefault;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.xml.XmlException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.regioneveneto.mygov.payment.mypay4.util.Constants.TIPO_FUNZIONE;

@Service
@Slf4j
public class DefinitionDovutoService {

  SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  private class ValidDependsOnResult {
    private boolean ok;
    private String msg;
    private String errCode;
    private String invalidFields;
    private List<String> invalidFieldsList = new ArrayList<>();
  }

  private final XMLFilter filter = new NamespaceFilter("http://www.regione.veneto.it/schemas/2012/Pagamenti/Ente/BilancioDefault/");
  private final ScriptEngineManager factory = new ScriptEngineManager();
  private final ScriptEngine engine = factory.getEngineByName("rhino");

  private final List<String> unacceptedCharsList = Arrays.asList("#");

  @Autowired
  MessageSource messageSource;

  public String calculateBilancio(boolean semplice, String deBilancioDefault,
                                  String causale, BigDecimal importoTotale, String codCapitolo) throws Exception {

    if (StringUtils.isBlank(deBilancioDefault))
      return null;
    log.debug("Chiamata al metodo calculateBilancio" + (semplice ? "Semplice" : ""));

    if (!semplice)
      Assert.notNull(causale, "Parametro [ causale ] nullo");
    Assert.notNull(importoTotale, "Parametro [ importoTotale ] nullo");

    String bilancio;
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Bilancio.class);

      // Set the parent XMLReader on the XMLFilter
      SAXParserFactory spf = SAXParserFactory.newInstance();
      SAXParser sp = spf.newSAXParser();
      XMLReader xr = sp.getXMLReader();
      filter.setParent(xr);

      // Set UnmarshallerHandler as ContentHandler on XMLFilter
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      UnmarshallerHandler unmarshallerHandler = unmarshaller .getUnmarshallerHandler();
      filter.setContentHandler(unmarshallerHandler);

      // Parse XML from String.
      filter.parse(new InputSource(new StringReader(deBilancioDefault)));
      Bilancio document = (Bilancio) unmarshallerHandler.getResult();

      for (CtCapitoloDefault ctCapitoloDefault : document.getCapitolos()) {
        if (StringUtils.isNotEmpty(codCapitolo) && ctCapitoloDefault.getCodCapitolo().equals(Constants.BILANCIO_DEFAULT_PLACEHOLDER_CAPITOLO))
          ctCapitoloDefault.setCodCapitolo(codCapitolo);
        for (CtAccertamentoDefault ctAccertamentoDefault : ctCapitoloDefault.getAccertamentos()) {
          if (ctAccertamentoDefault.getImporto().equals(Constants.BILANCIO_DEFAULT_TOTALE)) {
            String importoTotaleString = Utilities.parseImportoString(importoTotale);
            String importoSenzaSeparatoreMigliaia = importoTotaleString.replaceAll("\\.", "");
            String importoConSeparatorePuntoDecimali = importoSenzaSeparatoreMigliaia.replaceAll(",", ".");
            ctAccertamentoDefault.setImporto(importoConSeparatorePuntoDecimali);
          } else if (!semplice && ctAccertamentoDefault.getImporto().contains(Constants.BILANCIO_DEFAULT_ESTRAI_IMPORTO)) {
            engine.eval(ctAccertamentoDefault.getImporto());
            Invocable invocable = (Invocable) engine;
            String result = String.valueOf(invocable.invokeFunction(Constants.BILANCIO_DEFAULT_ESTRAI_IMPORTO, causale));
            Double resultDouble = Double.valueOf(result);
            BigDecimal resultBigDecimal = BigDecimal.valueOf(resultDouble);
            String importoTotaleString = Utilities.parseImportoString(resultBigDecimal);
            String importoSenzaSeparatoreMigliaia = importoTotaleString.replaceAll("\\.", "");
            String importoConSeparatorePuntoDecimali = importoSenzaSeparatoreMigliaia.replaceAll(",", ".");
            ctAccertamentoDefault.setImporto(importoConSeparatorePuntoDecimali);
          } else if (ctAccertamentoDefault.getImporto().contains(Constants.BILANCIO_DEFAULT_CALCOLA_IMPORTO)) {
            engine.eval(ctAccertamentoDefault.getImporto());
            Invocable invocable = (Invocable) engine;
            String result = String.valueOf(
                invocable.invokeFunction(Constants.BILANCIO_DEFAULT_CALCOLA_IMPORTO, importoTotale));
            Double resultDouble = Double.valueOf(result);
            BigDecimal resultBigDecimal = BigDecimal.valueOf(resultDouble);
            String importoTotaleString = Utilities.parseImportoString(resultBigDecimal);
            String importoSenzaSeparatoreMigliaia = importoTotaleString.replaceAll("\\.", "");
            String importoConSeparatorePuntoDecimali = importoSenzaSeparatoreMigliaia.replaceAll(",", ".");
            ctAccertamentoDefault.setImporto(importoConSeparatorePuntoDecimali);
          }
        }
      }

      StringWriter sw = new StringWriter();
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // Remove the xml header from the output.
      marshaller.marshal(document, sw);

      bilancio = sw.toString();
      StringBuilder sb = new StringBuilder();
      Scanner scanner = new Scanner(bilancio);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String lineNoPrefix = line.replaceAll(" xmlns=\"[^\"]*\"", ""); // Remove the prefix from the output.
        sb.append(lineNoPrefix.trim());
      }
      scanner.close();
      bilancio = sb.toString();
      log.debug("Bilancio: " + bilancio);
    } catch (XmlException e) {
      String msg = "Errore durante il parsing della busta bilancio";
      log.error(msg, e);
      throw new DefinitionDovutoException(msg, e);
    }
    return bilancio;
  }

  public SpontaneoTo validation(EnteTipoDovuto enteTipoDovuto, SpontaneoFormTo container) throws Exception {

    if (CollectionUtils.isEmpty(container.getFieldBeans())) {
      throw new DefinitionDovutoException("Modello di definizione della form non trovato");
    }

    if (CollectionUtils.isEmpty(container.getDefinitionDovuto())) {
      throw new DefinitionDovutoException("La list dei valori del dovuto non trovata");
    }

    Map<String, String> mappaValori = mappaValoriIniziali(container.getFieldBeans());

    //Override mappaValori
    for (DefinitionDovuto definitionDovuto: container.getDefinitionDovuto()) {
      mappaValori.put(definitionDovuto.getName(), definitionDovuto.getValue());
    }

    mappaValori = calcoloValoriConFunzione(mappaValori, container.getFieldBeans());

    Set<DovutoDipendente> insiemeValoriConProprietaEnabled = estraiValori(container.getFieldBeans(), TIPO_FUNZIONE.ENABLED);

    // controllo abilitazione
    Set<String> insiemeNodiDisabilitati = new HashSet<String>();
    for (DovutoDipendente dipDto : insiemeValoriConProprietaEnabled) {
      List<String> listaDipendenze = getEnabledDependsOnDependenciesList(dipDto);
      String enabled = calculateEnabled(dipDto, listaDipendenze, mappaValori);
      if (!enabled.equalsIgnoreCase(Boolean.TRUE.toString())) {
        insiemeNodiDisabilitati.add(dipDto.getName());
      }
    }

    aggiungiFigliAiNodiDisabilitati(insiemeNodiDisabilitati, container.getFieldBeans());

    Set<DovutoDipendente> insiemeValoriConProprietaValid = estraiValori(container.getFieldBeans(), TIPO_FUNZIONE.VALID);

    // controllo valid_depends_on
    for (DovutoDipendente dipDto : insiemeValoriConProprietaValid) {
      if (!insiemeNodiDisabilitati.contains(dipDto.getName())) {
        List<String> listaDipendenze = getValidDependsOnDependenciesList(dipDto);
        ValidDependsOnResult result = calculateValid(dipDto, listaDipendenze, mappaValori);
        if (!result.isOk()) {
          String msg = result.getMsg();
          log.error(msg);
          throw new DefinitionDovutoException(msg);
        }
      }
    }

    // controlli sui campi abilitati
    verificaPatternEObbligatorieta(mappaValori, container.getFieldBeans(), insiemeNodiDisabilitati);
    String importo = calcolaImporto(container.getFieldBeans(), container.getImporto(), mappaValori);
    BigDecimal importoBig = Utilities.toBigDecimal(importo);
    String causale = calcolaCausale(false, container.getFieldBeans(), mappaValori);
    String causaleVisualizzata = calcolaCausale(true, container.getFieldBeans(), mappaValori);
    String codCapitolo = Utilities.getCodBilancioFromXSD(container.getFieldBeans(), mappaValori);
    String bilancio = calculateBilancio(false, enteTipoDovuto.getDeBilancioDefault(), causale, importoBig, codCapitolo);

    //TODO add checks on intestatario

    return SpontaneoTo.builder()
        .codIpaEnte(enteTipoDovuto.getMygovEnteId().getCodIpaEnte())
        .deEnte(enteTipoDovuto.getMygovEnteId().getDeNomeEnte())
        .codTipoDovuto(enteTipoDovuto.getCodTipo())
        .deTipoDovuto(enteTipoDovuto.getDeTipo())
        .importo(importoBig)
        .causale(causale)
        .causaleVisualizzata(causaleVisualizzata)
        .bilancio(bilancio)
        .intestatario(container.getIntestatario())
        .build();
  }

  private Map<String, String> mappaValoriIniziali(List<FieldBean> listaDefinizioniDovuto) {
    Map<String, String> initialValueMap = new HashMap<>();
    for (FieldBean fb : listaDefinizioniDovuto) {
      fillMapAndSetWithDefaultValue(fb, initialValueMap);
      if (!CollectionUtils.isEmpty(fb.getSubfields()))
        for (FieldBean fb2 : fb.getSubfields()) {
          calculateChildrenDefaultValue(fb2, initialValueMap);
        }
    }
    return initialValueMap;
  }

  private void fillMapAndSetWithDefaultValue(FieldBean fieldBean, Map<String, String> mappaValori) {
    String name = fieldBean.getName();
    if (name.startsWith("sys_"))
      return;
    if (StringUtils.isNotBlank(fieldBean.getDefaultValue())) {
      mappaValori.put(fieldBean.getName(), fieldBean.getDefaultValue());
    } else {
      mappaValori.put(fieldBean.getName(), "");
    }
  }

  private void calculateChildrenDefaultValue(FieldBean fieldBean, Map<String, String> mappaValori) {
    if (!CollectionUtils.isEmpty(fieldBean.getSubfields())) {
      for (FieldBean fb : fieldBean.getSubfields()) {
        calculateChildrenDefaultValue(fb, mappaValori);
      }
    }
    // caso base
    fillMapAndSetWithDefaultValue(fieldBean, mappaValori);
    return;
  }

  private Map<String, String> calcoloValoriConFunzione(Map<String, String> mappaValori, final List<FieldBean> listaDefinizioniDovuto) {

    Set<DovutoDipendente> insiemeDovutiConDipendenti = estraiValori(listaDefinizioniDovuto, TIPO_FUNZIONE.VALUE);
    Map<String, String> mappaValoriFinali = new HashMap<>();

    for (String chiave : mappaValori.keySet()) {
      if (dovutoNotContains(insiemeDovutiConDipendenti, chiave)) {
        // aggiungo gli elementi nella mappa dei valori finali solamente
        // se non hanno funzioni di valorizzazione
        mappaValoriFinali.put(chiave, mappaValori.get(chiave));
      }
    }

    while (org.apache.commons.collections.CollectionUtils.isNotEmpty(insiemeDovutiConDipendenti)) {
      boolean risolto = false;
      Iterator<DovutoDipendente> iterator = insiemeDovutiConDipendenti.iterator();
      while (iterator.hasNext() && !risolto) {
        DovutoDipendente dipDto = iterator.next();
        List<String> listaDipendenze = getDependenciesList(dipDto);
        boolean hasAllDependenciesResolved =
            org.apache.commons.collections.CollectionUtils.isSubCollection(listaDipendenze, mappaValoriFinali.keySet());
        if (hasAllDependenciesResolved) {
          // HA TUTTE LE DIPENDENZE RISOLTE QUINDI LA RISOLVO
          try {
            String value = calculateValue(dipDto, listaDipendenze, mappaValoriFinali);
            mappaValoriFinali.put(dipDto.getName(), value);
            insiemeDovutiConDipendenti.remove(dipDto);
            risolto = true;
          } catch (Exception e) {
            String msg = "Errore durante l'esecuzione della funzione javascript";
            log.error(msg);
            throw new DefinitionDovutoException(msg);
          }
        }
      }
      if (!risolto) {
        String msg = "Errore, dipendenza circolare nei valori con dipendenze";
        log.error(msg);
        throw new DefinitionDovutoException(msg);
      }
    }
    return mappaValoriFinali;
  }

  private boolean dovutoNotContains(Set<DovutoDipendente> insiemeDovutiConDipendenti, String chiave) {
    return !dovutoContains(insiemeDovutiConDipendenti, chiave);
  }

  private boolean dovutoContains(Set<DovutoDipendente> insiemeDovutiConDipendenti, String chiave) {
    for (DovutoDipendente dovutoDipendenteDto : insiemeDovutiConDipendenti) {
      if (dovutoDipendenteDto.getName().equals(chiave)) {
        return true;
      }
    }
    return false;
  }

  private List<String> getDependenciesList(DovutoDipendente dipDto) {
    String[] listaDipendenze = StringUtils.split(dipDto.getValueDependsOnUids(), ',');
    List<String> listaDipendenzeFinal = new ArrayList<String>();
    for (String dipendenza : listaDipendenze) {
      if (dipendenza.contains(".")) {
        String[] listaFields = StringUtils.split(dipendenza, '.');
        Assert.notEmpty(listaFields);
        String dipFinal = listaFields[listaFields.length - 1];
        listaDipendenzeFinal.add(dipFinal);
      } else {
        listaDipendenzeFinal.add(dipendenza);
      }
    }
    return listaDipendenzeFinal;
  }

  private String calculateValue(DovutoDipendente dipDto, List<String> listaDipendenze,
                                Map<String, String> mappaValori) throws ScriptException, NoSuchMethodException {
    List<String> listaValoriOrdinati = new ArrayList<String>();
    String dipendenze = "";
    for (String s : listaDipendenze) {
      dipendenze += s + ",";
      listaValoriOrdinati.add(mappaValori.get(s));
    }
    if (dipendenze.endsWith(","))
      dipendenze = dipendenze.substring(0, dipendenze.length() - 1);

    engine.eval("function test(" + dipendenze + "){ " + dipDto.getValueDependsOn() + "}");
    Invocable invocable = (Invocable) engine;
    return String.valueOf(invocable.invokeFunction("test", listaValoriOrdinati.toArray()));
  }

  private Set<DovutoDipendente> estraiValori(List<FieldBean> listaDefinizioniDovuto, TIPO_FUNZIONE tipoFunzione) {
    Set<DovutoDipendente> insiemeValori = new HashSet<>();
    estraiValori(listaDefinizioniDovuto, insiemeValori, tipoFunzione);
    return insiemeValori;
  }

  private void estraiValori(List<FieldBean> listaDefinizioniDovuto, Set<DovutoDipendente> insiemeValori, TIPO_FUNZIONE tipoFunzione) {
    for (FieldBean fb : listaDefinizioniDovuto) {
      boolean hasDependsOn;
      switch (tipoFunzione) {
        case VALID:
          hasDependsOn = StringUtils.isNotBlank(fb.getValidDependsOn());
          break;
        case CAUSALE:
          hasDependsOn = fb.getExtraAttr() != null && fb.getExtraAttr().containsKey("causale_function") &&
              StringUtils.isNotBlank(fb.getExtraAttr().get("causale_function"));
          break;
        case CAUSALE_VISUALIZZATA:
          hasDependsOn = fb.getExtraAttr() != null && fb.getExtraAttr().containsKey("causale_visualizzata_function") &&
              StringUtils.isNotBlank(fb.getExtraAttr().get("causale_visualizzata_function"));
          break;
        case ENABLED:
          hasDependsOn = StringUtils.isNotBlank(fb.getEnabledDependsOn());
          break;
        case VALUE:
          hasDependsOn = StringUtils.isNotBlank(fb.getValueDependsOn());
          break;
        default:
          String msg = "Tipo funzione non prevista [ " + tipoFunzione + " ]";
          log.error(msg);
          throw new DefinitionDovutoException(msg);
      }
      if (hasDependsOn) {
        DovutoDipendente dipDto = mapFieldBeanToDovutoDipendenteDto(fb);
        insiemeValori.add(dipDto);
      }
      if (!CollectionUtils.isEmpty(fb.getSubfields())) {
        estraiValori(fb.getSubfields(), insiemeValori, tipoFunzione);
      }
    }
  }

  private DovutoDipendente mapFieldBeanToDovutoDipendenteDto(FieldBean fieldBean) {
    DovutoDipendente dto = new DovutoDipendente();
    dto.setName(fieldBean.getName());
    dto.setValidDependsOn(fieldBean.getValidDependsOn());
    dto.setValidDependsOnUids(fieldBean.getValidDependsOnUids());
    dto.setValueDependsOn(fieldBean.getValueDependsOn());
    dto.setValueDependsOnUids(fieldBean.getValueDependsOnUids());
    dto.setEnabledDependsOn(fieldBean.getEnabledDependsOn());
    dto.setEnabledDependsOnUids(fieldBean.getEnabledDependsOnUids());
    dto.setMandatoryDependsOn(fieldBean.getMandatoryDependsOn());
    dto.setMandatoryDependsOnUids(fieldBean.getMandatoryDependsOnUids());
    dto.setHiddenDependsOn(fieldBean.getHiddenDependsOn());
    dto.setHiddenDependsOnUids(fieldBean.getHiddenDependsOnUids());
    dto.setRenderType(fieldBean.getHtmlRender().toString());

    if (fieldBean.getExtraAttr() != null) {
      if (fieldBean.getExtraAttr().containsKey("causale_function")) {
        dto.setCausaleFunction(fieldBean.getExtraAttr().get("causale_function"));
      }

      if (fieldBean.getExtraAttr().containsKey("causale_function_uids")) {
        dto.setCausaleFunctionUids(fieldBean.getExtraAttr().get("causale_function_uids"));
      }

      if (fieldBean.getExtraAttr().containsKey("causale_visualizzata_function")) {
        dto.setCausaleVisualizzataFunction(fieldBean.getExtraAttr().get("causale_visualizzata_function"));
      }

      if (fieldBean.getExtraAttr().containsKey("causale_visualizzata_function_uids")) {
        dto.setCausaleVisualizzataFunctionUids(
            fieldBean.getExtraAttr().get("causale_visualizzata_function_uids"));
      }
    }
    return dto;
  }

  private List<String> getEnabledDependsOnDependenciesList(DovutoDipendente dipDto) {
    String[] listaDipendenze = StringUtils.split(dipDto.getEnabledDependsOnUids(), ',');
    List<String> listaDipendenzeFinal = new ArrayList<String>();
    for (String dipendenza : listaDipendenze) {
      if (dipendenza.contains(".")) {
        String[] listaFields = StringUtils.split(dipendenza, '.');
        Assert.notEmpty(listaFields);
        String dipFinal = listaFields[listaFields.length - 1];
        listaDipendenzeFinal.add(dipFinal);
      } else {
        listaDipendenzeFinal.add(dipendenza);
      }
    }
    return listaDipendenzeFinal;
  }

  private String calculateEnabled(DovutoDipendente dipDto, List<String> listaDipendenze,
                                  Map<String, String> mappaValori) throws ScriptException, NoSuchMethodException {
    List<String> listaValoriOrdinati = new ArrayList<String>();
    String dipendenze = "";
    for (String s : listaDipendenze) {
      dipendenze += s + ",";
      listaValoriOrdinati.add(mappaValori.get(s));
    }
    if (dipendenze.endsWith(","))
      dipendenze = dipendenze.substring(0, dipendenze.length() - 1);

    engine.eval("function test(" + dipendenze + "){ " + dipDto.getEnabledDependsOn() + "}");
    Invocable invocable = (Invocable) engine;
    return String.valueOf(invocable.invokeFunction("test", listaValoriOrdinati.toArray()));
  }

  private void aggiungiFigliAiNodiDisabilitati(Set<String> insiemeNodiDisabilitati, List<FieldBean> listaDefinizioniDovuto) {
    for (FieldBean fieldBean : listaDefinizioniDovuto) {
      removeAllDisabledDependenciesRecursive(insiemeNodiDisabilitati, fieldBean, false);
    }
  }

  private void removeAllDisabledDependenciesRecursive(Set<String> insiemeNodiDisabilitati, FieldBean fieldBean, boolean remove) {
    boolean nodoDisabilitato = remove || insiemeNodiDisabilitati.contains(fieldBean.getName());
    if (nodoDisabilitato) {
      insiemeNodiDisabilitati.add(fieldBean.getName());
    }
    if (fieldBean.getSubfields() != null) {
      for (FieldBean fb : fieldBean.getSubfields()) {
        removeAllDisabledDependenciesRecursive(insiemeNodiDisabilitati, fb, nodoDisabilitato);
      }
    }
  }

  private List<String> getValidDependsOnDependenciesList(DovutoDipendente dipDto) {
    String[] listaDipendenze = StringUtils.split(dipDto.getValidDependsOnUids(), ',');
    List<String> listaDipendenzeFinal = new ArrayList<String>();
    for (String dipendenza : listaDipendenze) {
      if (dipendenza.contains(".")) {
        String[] listaFields = StringUtils.split(dipendenza, '.');
        Assert.notEmpty(listaFields);
        String dipFinal = listaFields[listaFields.length - 1];
        listaDipendenzeFinal.add(dipFinal);
      } else {
        listaDipendenzeFinal.add(dipendenza);
      }
    }
    return listaDipendenzeFinal;
  }

  private ValidDependsOnResult calculateValid(DovutoDipendente dipDto, List<String> listaDipendenze,
                                              Map<String, String> mappaValori) throws ScriptException, NoSuchMethodException, DefinitionDovutoException {
    List<String> listaValoriOrdinati = new ArrayList<String>();
    String dipendenze = "";
    for (String s : listaDipendenze) {
      dipendenze += s + ",";
      if (mappaValori.containsKey(s)) {
        listaValoriOrdinati.add(mappaValori.get(s));
      } else {
        listaValoriOrdinati.add("null");
      }
    }
    if (dipendenze.endsWith(","))
      dipendenze = dipendenze.substring(0, dipendenze.length() - 1);

    engine.eval("function test(" + dipendenze + "){ " + dipDto.getValidDependsOn() + "}");
    Invocable invocable = (Invocable) engine;
    Object obj = invocable.invokeFunction("test", listaValoriOrdinati.toArray());

    String func = "return JSON.stringify(value)";

    engine.eval("function test(value){ " + func + "}");
    invocable = (Invocable) engine;
    Object obj2 = invocable.invokeFunction("test", obj);
    String value = String.valueOf(obj2);

    ValidDependsOnResult valid = new Gson().fromJson(value, ValidDependsOnResult.class);
    return valid;
  }

  private void verificaPatternEObbligatorieta(Map<String, String> mappaValori, List<FieldBean> listaDefinizioni,
                                              Set<String> insiemeNodiDisabilitati) throws DefinitionDovutoException {
    Iterator<Map.Entry<String, String>> it = mappaValori.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, String> pair = it.next();
      String key = pair.getKey();
      if (!insiemeNodiDisabilitati.contains(key)) {
        FieldBean fieldBean = getFieldBeanByName(listaDefinizioni, key);
        if (fieldBean != null) {
          String value = pair.getValue();
          if (!(fieldBean.getHtmlRender().compareTo(RenderType.MULTIFIELD) == 0
              || fieldBean.getHtmlRender().compareTo(RenderType.TAB) == 0)) {
            if (StringUtils.isBlank(value) && isRequired(fieldBean)) {
              if (fieldBean.getExtraAttr() != null
                  && StringUtils.isNotBlank(fieldBean.getExtraAttr().get("error_message"))) {
                String errorMessage = fieldBean.getExtraAttr().get("error_message");
                log.error(errorMessage);
                throw new DefinitionDovutoException(errorMessage);
              } else {
                String msg = "Errore, il campo [ " + key + " ] è obbligatorio";
                log.error(msg);
                throw new DefinitionDovutoException(msg);
              }
            }

            checkUnacceptedChars(value, fieldBean.getHtmlLabel());

            if (StringUtils.isNotBlank(value)) {
              boolean isValid = true;
              if (fieldBean.getExtraAttr() != null) {
                String validationType = fieldBean.getExtraAttr().get("validation_type");
                String valMin = fieldBean.getExtraAttr().get("val_min");
                String valMax = fieldBean.getExtraAttr().get("val_max");
                if (StringUtils.isNotBlank(validationType)) {
                  isValid = Utilities.validateWithValidationType(validationType, value, valMin, valMax);
                } else {
                  String pattern = fieldBean.getRegex();
                  if (StringUtils.isNotBlank(pattern)) {
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(value);
                    isValid = m.find();
                  }
                }
              } else {
                String pattern = fieldBean.getRegex();
                if (StringUtils.isNotBlank(pattern)) {
                  Pattern r = Pattern.compile(pattern);
                  Matcher m = r.matcher(value);
                  isValid = m.find();
                }
              }

              if (!isValid) {
                String errorMessage = fieldBean.getExtraAttr().get("error_message");
                log.error(errorMessage);
                throw new DefinitionDovutoException(errorMessage);
              }
            }
            if (fieldBean.getExtraAttr() != null) {
              String validationType = fieldBean.getExtraAttr().get("validation_type");
              if (StringUtils.isNotBlank(validationType) && validationType.equals("id_univoco_FG")) {
                String cf = value;
                if (StringUtils.isNotBlank(cf))
                  cf = cf.toUpperCase();
                mappaValori.put(key, cf);
              }
            }
            if (fieldBean.getHtmlRender().compareTo(RenderType.DATE) == 0) {
              try {
                String valueDate = value;
                if(StringUtils.isNotBlank(fieldBean.getExtraAttr().get("dateFormat"))){
                  Date date = formatter.parse(value);
                  SimpleDateFormat extraDateFormat = new SimpleDateFormat(fieldBean.getExtraAttr().get("dateFormat"));
                  valueDate = extraDateFormat.format(date);
                }
                mappaValori.put(key, valueDate);
              } catch (ParseException e) {
                if (fieldBean.getExtraAttr() != null
                    && StringUtils.isNotBlank(fieldBean.getExtraAttr().get("error_message"))) {
                  String errorMessage = fieldBean.getExtraAttr().get("error_message");
                  log.error(errorMessage);
                  throw new DefinitionDovutoException(errorMessage);
                } else {
                  String msg = "Errore nella validazione della data";
                  log.error(msg);
                  throw new DefinitionDovutoException(msg);
                }
              }
            }
            if (fieldBean.getHtmlRender().compareTo(RenderType.SINGLESELECT) == 0
                && !fieldBean.getEnumerationList().contains(value)) {
              if (fieldBean.getExtraAttr() != null
                  && StringUtils.isNotBlank(fieldBean.getExtraAttr().get("error_message"))) {
                String errorMessage = fieldBean.getExtraAttr().get("error_message");
                log.error(errorMessage);
                throw new DefinitionDovutoException(errorMessage);
              } else {
                String msg = "Il campo [ " + key + " ] non è contenuto nella lista di valori ammessi";
                log.error(msg);
                throw new DefinitionDovutoException(msg);
              }
            }
          }
        } else {
          String msg = "Errore interno durante la validazione dei campi";
          log.error(msg);
          throw new DefinitionDovutoException(msg);
        }
      }
    }
  }

  private FieldBean getFieldBeanByName(List<FieldBean> listaDefinizioni, String name) {
    for (FieldBean fieldBean : listaDefinizioni) {
      if (fieldBean.getName().equals(name)) {
        return fieldBean;
      }
      if (fieldBean.getSubfields() != null) {
        FieldBean fb = getFieldBeanByName(fieldBean.getSubfields(), name);
        if (fb != null) {
          return fb;
        }
      }
    }
    return null;
  }

  private boolean isRequired(FieldBean fieldBean) {
    boolean obbligatorio = false;
    if (fieldBean.getExtraAttr() != null && fieldBean.getExtraAttr().containsKey("optional")
        && !fieldBean.getExtraAttr().get("optional").equalsIgnoreCase("true")) {
      obbligatorio = true;
    } else if (fieldBean.getExtraAttr() != null && fieldBean.getExtraAttr().containsKey("optional")
        && fieldBean.getExtraAttr().get("optional").equalsIgnoreCase("true")) {
      obbligatorio = false;
    } else if (fieldBean.getMinOccurences() > 0) {
      obbligatorio = true;
    }
    return obbligatorio;
  }

  private boolean checkUnacceptedChars(String value, String fieldName) throws DefinitionDovutoException {
    boolean isOk = true;
    for (int i = 0; i < unacceptedCharsList.size(); i++) {
      String unacceptedChar = unacceptedCharsList.get(i);

      if (!value.isEmpty() && value.contains(unacceptedChar)) {
        String errorMessage = "Il campo " + fieldName
            + " non \u00E8 valido. Non \u00E8 possibile inserire il carattere " + unacceptedChar + ".";
        log.error(errorMessage);
        throw new DefinitionDovutoException(errorMessage);
      }
    }
    return isOk;
  }

  private String calcolaImporto(List<FieldBean> fb, String importo, Map<String, String> mappaValori) throws DefinitionDovutoException {
    if (StringUtils.isBlank(importo)) {
      String importoCalcolato = getFinalValueByKey(mappaValori, Utilities.getTotaleInXSD(fb));
      try {
        Float.parseFloat(importoCalcolato);
        formatImporto(importoCalcolato);
        BigDecimal importoBD = Utilities.parseImportoString(formatImporto(importoCalcolato));
        String msg = Utilities.verificaImporto(importoBD);
        if (msg!=null) {
          log.error(msg);
          throw new DefinitionDovutoException(msg);
        }
        log.debug("importoCalcolato: "+importoCalcolato+ ", "+formatImporto(importoCalcolato));

        return formatImporto(importoCalcolato);
      } catch (Exception e) {
        String msg = messageSource.getMessage("pa.messages.invalidImporto", null, Locale.ITALY);
        log.error(msg);
        throw new DefinitionDovutoException(msg, e);
      }
    }

    BigDecimal importoBig = Utilities.toBigDecimal(importo);

    String msg = Utilities.verificaImporto(importoBig);
    if (msg!=null) {
      log.error(msg);
      throw new DefinitionDovutoException(msg);
    }
    String finalImporto = Utilities.parseImportoString(importoBig);
    log.debug("finalImporto: "+finalImporto);
    return finalImporto;
  }

  private String formatImporto(String importo) {
    importo = replaceLast(importo, ".", ",");
    if (!importo.contains(",")) {
      importo = importo + ",00";
    } else {
      if (importo.split(",")[1].length() == 1) {
        importo = importo + "0";
      }
    }
    return importo;
  }

  private String replaceLast(String string, String oldChar, String newChar) {
    int index = string.lastIndexOf(oldChar);
    if (index == -1)
      return string;
    return string.substring(0, index) + newChar + string.substring(index + oldChar.length());
  }

  private String getFinalValueByKey(Map<String, String> mappaValori, String name) {
    Assert.notEmpty(mappaValori);
    Iterator<Map.Entry<String, String>> it = mappaValori.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, String> pair = it.next();
      if (pair.getKey().equals(name))
        return pair.getValue();
    }
    return null;
  }

  private String calcolaCausale(boolean visual, List<FieldBean> listaDefinizioniDovuto, Map<String, String> mappaValori)
      throws DefinitionDovutoException, NoSuchMethodException, ScriptException {
    final TIPO_FUNZIONE TIPO = visual ? TIPO_FUNZIONE.CAUSALE_VISUALIZZATA : TIPO_FUNZIONE.CAUSALE;
    Set<DovutoDipendente> insiemeValoriConFunzioneCausale = estraiValori(listaDefinizioniDovuto, TIPO);

    String joinTemplate = recuperaJoinTemplate(visual, listaDefinizioniDovuto);

    if (visual && StringUtils.isBlank(joinTemplate))
      return "";

    Map<String, String> mappaValoriFinali = new HashMap<>(mappaValori);

    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(insiemeValoriConFunzioneCausale)) {
      for (DovutoDipendente dovutoDipendenteDto : insiemeValoriConFunzioneCausale) {
        List<String> listaDipendenze = getCausaleDependenciesList(visual, dovutoDipendenteDto);
        String value = calculateCausaleField(visual, dovutoDipendenteDto, listaDipendenze, mappaValori);
        mappaValoriFinali.put(dovutoDipendenteDto.getName(), value);
      }
    }

    StringSubstitutor sub = new StringSubstitutor(mappaValoriFinali);
    String causale = sub.replace(joinTemplate);
    return causale;
  }

  private String recuperaJoinTemplate(boolean visual, List<FieldBean> listaDefinizioniDovuto) throws DefinitionDovutoException {
    String template = visual ? "join_template_visual" : "join_template";
    for (FieldBean fb : listaDefinizioniDovuto) {
      if (fb.getExtraAttr() != null && StringUtils.isNotBlank(fb.getExtraAttr().get(template))) {
        return fb.getExtraAttr().get(template);
      } else if (!CollectionUtils.isEmpty(fb.getSubfields())) {
        return recuperaJoinTemplate(visual, fb.getSubfields());
      }
    }
    String msg = "Campo "+ template +" per la causale " + (visual ? "vsualizzata " : "") + "non presente";
    log.warn(msg);
    if (!visual)
      throw new DefinitionDovutoException(msg);
    return "";
  }

  private List<String> getCausaleDependenciesList(boolean visual, DovutoDipendente dipDto) {
    String causaleFunctionUids = visual ? dipDto.getCausaleVisualizzataFunctionUids() : dipDto.getCausaleFunctionUids();
    String[] listaDipendenze = StringUtils.split(causaleFunctionUids, ',');
    List<String> listaDipendenzeFinal = new ArrayList<String>();
    for (String dipendenza : listaDipendenze) {
      if (dipendenza.contains(".")) {
        String[] listaFields = StringUtils.split(dipendenza, '.');
        Assert.notEmpty(listaFields);
        String dipFinal = listaFields[listaFields.length - 1];
        listaDipendenzeFinal.add(dipFinal);
      } else {
        listaDipendenzeFinal.add(dipendenza);
      }
    }
    return listaDipendenzeFinal;
  }

  private String calculateCausaleField(boolean visual, DovutoDipendente dipDto, List<String> listaDipendenze,
                                       Map<String, String> mappaValori) throws ScriptException, NoSuchMethodException {
    List<String> listaValoriOrdinati = new ArrayList<>();
    String dipendenze = "";
    for (String s : listaDipendenze) {
      dipendenze += s + ",";
      listaValoriOrdinati.add(mappaValori.get(s));
    }
    if (dipendenze.endsWith(","))
      dipendenze = dipendenze.substring(0, dipendenze.length() - 1);

    String causaleFunction = visual ? dipDto.getCausaleVisualizzataFunction() : dipDto.getCausaleFunction();
    engine.eval("function test(" + dipendenze + "){ " + causaleFunction + "}");
    Invocable invocable = (Invocable) engine;
    return String.valueOf(invocable.invokeFunction("test", listaValoriOrdinati.toArray()).toString());
  }

  public <T> T handleScript(String argsSignature, String body, String argsValue) throws ScriptException, NoSuchMethodException {
    engine.eval("function test(" + argsSignature + "){ " + body + "}");
    Invocable invocable = (Invocable) engine;
    return (T) invocable.invokeFunction("test", argsValue);
  }
}
