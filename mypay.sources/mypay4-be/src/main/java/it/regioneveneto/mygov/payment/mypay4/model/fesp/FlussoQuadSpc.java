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
package it.regioneveneto.mygov.payment.mypay4.model.fesp;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import it.regioneveneto.mygov.payment.mypay4.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovFlussoQuadSpcId")
public class FlussoQuadSpc extends BaseEntity {

  public static final String ALIAS = "FESP_FlussoQuadSpc";
  public static final String FIELDS = ""+ALIAS+".mygov_flusso_quad_spc_id as FESP_FlussoQuadSpc_mygovFlussoQuadSpcId"+
      ","+ALIAS+".version as FESP_FlussoQuadSpc_version,"+ALIAS+".cod_ipa_ente as FESP_FlussoQuadSpc_codIpaEnte"+
      ","+ALIAS+".cod_identificativo_flusso as FESP_FlussoQuadSpc_codIdentificativoFlusso"+
      ","+ALIAS+".dt_data_ora_flusso as FESP_FlussoQuadSpc_dtDataOraFlusso"+
      ","+ALIAS+".de_nome_file_scaricato as FESP_FlussoQuadSpc_deNomeFileScaricato"+
      ","+ALIAS+".num_dimensione_file_scaricato as FESP_FlussoQuadSpc_numDimensioneFileScaricato"+
      ","+ALIAS+".dt_creazione as FESP_FlussoQuadSpc_dtCreazione"+
      ","+ALIAS+".dt_ultima_modifica as FESP_FlussoQuadSpc_dtUltimaModifica,"+ALIAS+".cod_stato as FESP_FlussoQuadSpc_codStato";

  private Long mygovFlussoQuadSpcId;
  private int version;
  private String codIpaEnte;
  private String codIdentificativoFlusso;
  private Timestamp dtDataOraFlusso;
  private String deNomeFileScaricato;
  private Long numDimensioneFileScaricato;
  private Timestamp dtCreazione;
  private Timestamp dtUltimaModifica;
  private String codStato;
}
