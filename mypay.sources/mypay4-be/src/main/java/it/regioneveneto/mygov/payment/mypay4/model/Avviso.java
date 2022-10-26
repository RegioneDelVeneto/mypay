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
import org.jdbi.v3.core.mapper.PropagateNull;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovAvvisoId")
public class Avviso extends BaseEntity {

  public final static String ALIAS = "Avviso";
  public final static String FIELDS = ""+ALIAS+".mygov_avviso_id as Avviso_mygovAvvisoId,"+ALIAS+".version as Avviso_version"+
      ","+ALIAS+".dt_creazione as Avviso_dtCreazione,"+ALIAS+".dt_ultima_modifica as Avviso_dtUltimaModifica"+
      ","+ALIAS+".mygov_ente_id as Avviso_mygovEnteId,"+ALIAS+".cod_iuv as Avviso_codIuv"+
      ","+ALIAS+".cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco as Avviso_codRpSoggPagIdUnivPagTipoIdUnivoco"+
      ","+ALIAS+".cod_rp_sogg_pag_id_univ_pag_codice_id_univoco as Avviso_codRpSoggPagIdUnivPagCodiceIdUnivoco"+
      ","+ALIAS+".de_rp_sogg_pag_anagrafica_pagatore as Avviso_deRpSoggPagAnagraficaPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_indirizzo_pagatore as Avviso_deRpSoggPagIndirizzoPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_civico_pagatore as Avviso_deRpSoggPagCivicoPagatore"+
      ","+ALIAS+".cod_rp_sogg_pag_cap_pagatore as Avviso_codRpSoggPagCapPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_localita_pagatore as Avviso_deRpSoggPagLocalitaPagatore"+
      ","+ALIAS+".de_rp_sogg_pag_provincia_pagatore as Avviso_deRpSoggPagProvinciaPagatore"+
      ","+ALIAS+".cod_rp_sogg_pag_nazione_pagatore as Avviso_codRpSoggPagNazionePagatore"+
      ","+ALIAS+".de_rp_sogg_pag_email_pagatore as Avviso_deRpSoggPagEmailPagatore";


  @PropagateNull
  private Long mygovAvvisoId;
  private int version;
  private Date dtCreazione;
  private Date dtUltimaModifica;
  @Nested(Ente.ALIAS)
  private Ente mygovEnteId;
  private String codIuv;
  private String codRpSoggPagIdUnivPagTipoIdUnivoco;
  private String codRpSoggPagIdUnivPagCodiceIdUnivoco;
  private String deRpSoggPagAnagraficaPagatore;
  private String deRpSoggPagIndirizzoPagatore;
  private String deRpSoggPagCivicoPagatore;
  private String codRpSoggPagCapPagatore;
  private String deRpSoggPagLocalitaPagatore;
  private String deRpSoggPagProvinciaPagatore;
  private String codRpSoggPagNazionePagatore;
  private String deRpSoggPagEmailPagatore;
}
