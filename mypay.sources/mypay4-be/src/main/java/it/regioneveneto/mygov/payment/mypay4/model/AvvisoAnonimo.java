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

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovAvvisoAnonimoId")
public class AvvisoAnonimo extends BaseEntity {

  public final static String ALIAS = "AvvisoAnonimo";
  public final static String FIELDS = ""+ALIAS+".mygov_avviso_anonimo_id as AvvisoAnonimo_mygovAvvisoAnonimoId"+
      ","+ALIAS+".cod_ipa_ente as AvvisoAnonimo_codIpaEnte,"+ALIAS+".cod_iuv as AvvisoAnonimo_codIuv"+
      ","+ALIAS+".id_session as AvvisoAnonimo_idSession,"+ALIAS+".de_email_address as AvvisoAnonimo_deEmailAddress"+
      ","+ALIAS+".email_verificata as AvvisoAnonimo_emailVerificata"+
      ","+ALIAS+".mygov_carrello_id as AvvisoAnonimo_mygovCarrelloId,"+ALIAS+".dt_creazione as AvvisoAnonimo_dtCreazione"+
      ","+ALIAS+".ente_sil_invia_risposta_pagamento_url as AvvisoAnonimo_enteSilInviaRispostaPagamentoUrl";

  private Long mygovAvvisoAnonimoId;
  private String codIpaEnte;
  private String codIuv;
  private String idSession;
  private String deEmailAddress;
  private boolean emailVerificata;
  @Nested(Carrello.ALIAS)
  private Carrello mygovCarrelloId;
  private Date dtCreazione;
  private String enteSilInviaRispostaPagamentoUrl;
}
