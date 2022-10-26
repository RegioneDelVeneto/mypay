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
package it.regioneveneto.mygov.payment.mypay4.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import static it.regioneveneto.mygov.payment.mypay4.config.DateTimeConfig.DATE_FORMAT;
import static it.regioneveneto.mygov.payment.mypay4.config.DateTimeConfig.DATE_TIME_FORMAT;

@Slf4j
@Configuration
public class JacksonMapperCustomizer {
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    return builder -> builder
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)))
        .serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
        .serializers(new DateSerializer(null, new SimpleDateFormat(DATE_TIME_FORMAT)))
        .deserializers(new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)))
        .deserializers(new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
        // all strings are trimmed
        .deserializers(new StringDeserializer(){
          @Override
          public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String originalValue = super.deserialize(p, ctxt);
            if(originalValue==null)
              return null;
            String newValue = originalValue.trim();
            if(log.isInfoEnabled() && newValue.length()!=originalValue.length())
              log.info("TRIM {}: [{}] -> [{}]", this.getFullAttributeName(p.getParsingContext()), originalValue, newValue);
            return newValue;
          }

          private String getFullAttributeName(JsonStreamContext parsingContext) {
            String name = parsingContext.getCurrentName();
            if(parsingContext.inRoot() && !parsingContext.hasCurrentName())
              name = "/";
            if(parsingContext.inArray())
              name += "["+parsingContext.getCurrentIndex()+"]";
            JsonStreamContext parent = parsingContext.getParent();
            return parent==null ? name : (getFullAttributeName(parent)+"."+name);
          }
        });
  }
}
