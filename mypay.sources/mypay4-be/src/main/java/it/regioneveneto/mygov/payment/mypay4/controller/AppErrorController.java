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
package it.regioneveneto.mygov.payment.mypay4.controller;

import it.regioneveneto.mygov.payment.mypay4.service.common.AppErrorService;
import it.regioneveneto.mygov.payment.mypay4.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@Slf4j
@ConditionalOnWebApplication
@EnableConfigurationProperties({ ServerProperties.class })
@RequestMapping("${server.error.path:${error.path:/error}}")
public class AppErrorController extends AbstractErrorController {

  private final ErrorProperties errorProperties;

  @Value("${app.fe.cittadino.absolute-path}")
  private String homepage;

  @Autowired
  private AppErrorService appErrorService;

  public AppErrorController(ServerProperties serverProperties, ErrorAttributes errorAttributes,
                            ObjectProvider<ErrorViewResolver> errorViewResolvers) {
    super(errorAttributes, errorViewResolvers.orderedStream().collect(Collectors.toList()));
    Assert.notNull(serverProperties.getError(), "ErrorProperties must not be null");
    this.errorProperties = serverProperties.getError();
  }

  @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
    Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    String view, title, subtitle, detail;
    if(Objects.equals(statusCode, HttpStatus.NOT_FOUND.value()) ||
      Objects.equals(statusCode, HttpStatus.I_AM_A_TEAPOT.value()) ){
      view = "error";
      title = "Risorsa non trovata";
      subtitle = "La risorsa richiesta non è disponibile";
      detail = "";
    } else if(Objects.equals(statusCode, HttpStatus.UNAUTHORIZED.value())){
      view = "error";
      title = "Non autorizzato";
      subtitle = "Non si dispone dei permessi per accedere alla risorsa richiesta";
      detail = "";
    } else {
      // generic system error
      view = "error";
      title = "Errore di sistema";
      subtitle = "Si è verificato un errore imprevisto";
      detail = "Si prega di riprovare in seguito";
    }
    //Exception e = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName(view);
    modelAndView.addObject("errorUid", handleErrorUid(request));
    modelAndView.addObject("homepagePath", homepage);
    modelAndView.addObject("title", title);
    modelAndView.addObject("subtitle", subtitle);
    modelAndView.addObject("detail", detail);
    return modelAndView;
  }

  @RequestMapping
  public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
    HttpStatus status = getStatus(request);
    if (status == HttpStatus.NO_CONTENT) {
      return new ResponseEntity<>(status);
    } else if (status == HttpStatus.NOT_FOUND) {
      request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.I_AM_A_TEAPOT.value());
    }

    String message = null;
    Map<String, Object> errorAttributes = getErrorAttributes(request, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.EXCEPTION, ErrorAttributeOptions.Include.MESSAGE));
    if(StringUtils.equals(errorAttributes.getOrDefault("exception","").toString(), MaxUploadSizeExceededException.class.getName())){
      request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.BAD_REQUEST.value());
      status = HttpStatus.BAD_REQUEST;
      try {
        Pattern pattern = Pattern.compile(".*?\\((\\d+)\\).*?\\((\\d+)\\)");
        Matcher matcher = pattern.matcher(errorAttributes.getOrDefault("message", "").toString());
        if (matcher.find() && matcher.groupCount()==2)
            message = String.format("dimensione file (%s) superiore a quella consentita (%s)",
              Optional.ofNullable(matcher.group(1)).map(Long::parseLong).map(Utilities::humanReadableByteCountBin).orElse(null),
              Optional.ofNullable(matcher.group(2)).map(Long::parseLong).map(Utilities::humanReadableByteCountBin).orElse(null));
        else {
          pattern = Pattern.compile(".*?\\s(\\d+)\\s.*?");
          matcher = pattern.matcher(errorAttributes.getOrDefault("message", "").toString());
          if (matcher.find() && matcher.groupCount()==1)
            message = String.format("dimensione file superiore a quella consentita (%s)",
              Optional.ofNullable(matcher.group(1)).map(Long::parseLong).map(Utilities::humanReadableByteCountBin).orElse(null));
        }
      }catch(Exception e){
        //ignore this exception
        log.debug("error extracting MaxUploadSizeExceededException info", e);
      }
    }
    Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
    if(StringUtils.isNotBlank(message))
      body.put("message", message);

    //add errorUID
    body.put("errorUID", handleErrorUid(request));

    return new ResponseEntity<>(body, status);
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<String> mediaTypeNotAcceptable(HttpServletRequest request) {
    HttpStatus status = getStatus(request);
    return ResponseEntity.status(status).build();
  }

  private String handleErrorUid(HttpServletRequest request){
    Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    String url = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
    Pair<String, String> nowStringAndErrorUid = appErrorService.generateNowStringAndErrorUid();
    log.error("errorUID[{}] now[{}] code[{}] url[{}]", nowStringAndErrorUid.getRight(), nowStringAndErrorUid.getLeft(), statusCode, url);
    return nowStringAndErrorUid.getRight();
  }

  protected ErrorAttributeOptions getErrorAttributeOptions(HttpServletRequest request, MediaType mediaType) {
    ErrorAttributeOptions options = ErrorAttributeOptions.defaults();

    if (this.errorProperties.isIncludeException()) {
      options = options.including(ErrorAttributeOptions.Include.EXCEPTION);
    }
    if (isIncludeStackTrace(request, mediaType)) {
      options = options.including(ErrorAttributeOptions.Include.STACK_TRACE);
    }
    if (isIncludeMessage(request, mediaType)) {
      options = options.including(ErrorAttributeOptions.Include.MESSAGE);
    }
    if (isIncludeBindingErrors(request, mediaType)) {
      options = options.including(ErrorAttributeOptions.Include.BINDING_ERRORS);
    }
    return options;
  }

  /**
   * Determine if the stacktrace attribute should be included.
   * @param request the source request
   * @param produces the media type produced (or {@code MediaType.ALL})
   * @return if the stacktrace attribute should be included
   */
  protected boolean isIncludeStackTrace(HttpServletRequest request, MediaType produces) {
    switch (getErrorProperties().getIncludeStacktrace()) {
      case ALWAYS:
        return true;
      case ON_PARAM:
        return getTraceParameter(request);
      default:
        return false;
    }
  }

  /**
   * Determine if the message attribute should be included.
   * @param request the source request
   * @param produces the media type produced (or {@code MediaType.ALL})
   * @return if the message attribute should be included
   */
  protected boolean isIncludeMessage(HttpServletRequest request, MediaType produces) {
    switch (getErrorProperties().getIncludeMessage()) {
      case ALWAYS:
        return true;
      case ON_PARAM:
        return getMessageParameter(request);
      default:
        return false;
    }
  }

  /**
   * Determine if the errors attribute should be included.
   * @param request the source request
   * @param produces the media type produced (or {@code MediaType.ALL})
   * @return if the errors attribute should be included
   */
  protected boolean isIncludeBindingErrors(HttpServletRequest request, MediaType produces) {
    switch (getErrorProperties().getIncludeBindingErrors()) {
      case ALWAYS:
        return true;
      case ON_PARAM:
        return getErrorsParameter(request);
      default:
        return false;
    }
  }

  /**
   * Provide access to the error properties.
   * @return the error properties
   */
  protected ErrorProperties getErrorProperties() {
    return this.errorProperties;
  }
}
