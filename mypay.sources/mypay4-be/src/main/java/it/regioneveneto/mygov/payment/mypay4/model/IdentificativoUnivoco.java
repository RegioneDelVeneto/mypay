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

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygov_identificativo_univoco_id")
public class IdentificativoUnivoco extends BaseEntity {

  public final static String ALIAS = "IdentificativoUnivoco";
  public final static String FIELDS = ""+ALIAS+".mygov_identificativo_univoco_id as IdentificativoUnivoco_mygovIdentificativoUnivocoId"+
      ","+ALIAS+".version as IdentificativoUnivoco_version,"+ALIAS+".mygov_ente_id as IdentificativoUnivoco_mygovEnteId"+
      ","+ALIAS+".mygov_flusso_id as IdentificativoUnivoco_mygovFlussoId"+
      ","+ALIAS+".cod_tipo_identificativo as IdentificativoUnivoco_codTipoIdentificativo"+
      ","+ALIAS+".identificativo as IdentificativoUnivoco_identificativo"+
      ","+ALIAS+".dt_inserimento as IdentificativoUnivoco_dtInserimento";

  private Long mygovIdentificativoUnivocoId;
  private int version;
  private Long mygovEnteId;
  private Long mygovFlussoId;
  private String codTipoIdentificativo;
  private String identificativo;
  private Date dtInserimento;
}
