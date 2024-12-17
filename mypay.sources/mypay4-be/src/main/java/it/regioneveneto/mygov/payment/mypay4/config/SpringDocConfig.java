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

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

  @Autowired
  private BuildProperties buildProperties;

  @Value("${jwt.use-header-auth.enabled:false}")
  private boolean useHeaderAuth;

  @Bean
  public GroupedOpenApi externalAppOpenApi() {
    String[] paths = {"/a2a/app/**"};
    return GroupedOpenApi.builder().group("ExternalApp").pathsToMatch(paths)
      .build();
  }

  @Bean
  public GroupedOpenApi myPayOpenApi() {
    String[] paths = {"/a2a/**"};
    return GroupedOpenApi.builder().group("MyPayWebapp").pathsToExclude(paths)
      .build();
  }

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
      .info(new Info()
        .title("MyPay")
        .version(buildProperties.getVersion())
        .description("MyPay API"))
      .addSecurityItem(new SecurityRequirement()
        .addList("MyPaySecurity")).components(new Components()
        .addSecuritySchemes(
          "MyPaySecurity", new SecurityScheme()
            .name("MyPaySecurity")
            .type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .in(useHeaderAuth ? SecurityScheme.In.HEADER : SecurityScheme.In.COOKIE)
            .scheme("bearer")
        )
      );
  }
}
