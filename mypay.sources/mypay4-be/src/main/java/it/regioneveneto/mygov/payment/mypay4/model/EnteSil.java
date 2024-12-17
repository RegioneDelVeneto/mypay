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
package it.regioneveneto.mygov.payment.mypay4.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.Nested;

@Data
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovEnteSilId")
public class EnteSil extends BaseEntity {

  public static final String ALIAS = "EnteSil";
  public static final String FIELDS = ""+ALIAS+".mygov_ente_sil_id as EnteSil_mygovEnteSilId,"+ALIAS+".mygov_ente_id as EnteSil_mygovEnteId"+
      ","+ALIAS+".nome_applicativo as EnteSil_nomeApplicativo"+
      ","+ALIAS+".de_url_inoltro_esito_pagamento_push as EnteSil_deUrlInoltroEsitoPagamentoPush"+
      ","+ALIAS+".cod_service_account_jwt_entrata_client_id as EnteSil_codServiceAccountJwtEntrataClientId"+
      ","+ALIAS+".de_service_account_jwt_entrata_client_mail as EnteSil_deServiceAccountJwtEntrataClientMail"+
      ","+ALIAS+".cod_service_account_jwt_entrata_secret_key_id as EnteSil_codServiceAccountJwtEntrataSecretKeyId"+
      ","+ALIAS+".cod_service_account_jwt_entrata_secret_key as EnteSil_codServiceAccountJwtEntrataSecretKey"+
      ","+ALIAS+".cod_service_account_jwt_uscita_client_id as EnteSil_codServiceAccountJwtUscitaClientId"+
      ","+ALIAS+".de_service_account_jwt_uscita_client_mail as EnteSil_deServiceAccountJwtUscitaClientMail"+
      ","+ALIAS+".cod_service_account_jwt_uscita_secret_key_id as EnteSil_codServiceAccountJwtUscitaSecretKeyId"+
      ","+ALIAS+".cod_service_account_jwt_uscita_secret_key as EnteSil_codServiceAccountJwtUscitaSecretKey"+
      ","+ALIAS+".flg_jwt_attivo as EnteSil_flgJwtAttivo,"+ALIAS+".mygov_ente_tipo_dovuto_id as EnteSil_mygovEnteTipoDovutoId";

  private Long mygovEnteSilId;
  @Nested(Ente.ALIAS)
  private Ente mygovEnteId;
  private String nomeApplicativo;
  private String deUrlInoltroEsitoPagamentoPush;
  private String codServiceAccountJwtEntrataClientId;
  private String deServiceAccountJwtEntrataClientMail;
  private String codServiceAccountJwtEntrataSecretKeyId;
  private String codServiceAccountJwtEntrataSecretKey;
  private String codServiceAccountJwtUscitaClientId;
  private String deServiceAccountJwtUscitaClientMail;
  private String codServiceAccountJwtUscitaSecretKeyId;
  private String codServiceAccountJwtUscitaSecretKey;
  private boolean flgJwtAttivo;
  @Nested(EnteTipoDovuto.ALIAS)
  private EnteTipoDovuto mygovEnteTipoDovutoId;
}
