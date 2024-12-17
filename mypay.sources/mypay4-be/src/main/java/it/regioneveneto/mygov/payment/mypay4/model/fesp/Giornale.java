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
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder(toBuilder=true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovGiornaleId")
public class Giornale extends it.regioneveneto.mygov.payment.mypay4.model.common.Giornale {

  public static final String ALIAS = "FESP_Giornale";
  public static final String FIELDS = ""+ALIAS+".mygov_giornale_id as FESP_Giornale_mygovGiornaleId,"+ALIAS+".version as FESP_Giornale_version"+
      ","+ALIAS+".data_ora_evento as FESP_Giornale_dataOraEvento"+
      ","+ALIAS+".identificativo_dominio as FESP_Giornale_identificativoDominio"+
      ","+ALIAS+".identificativo_univoco_versamento as FESP_Giornale_identificativoUnivocoVersamento"+
      ","+ALIAS+".codice_contesto_pagamento as FESP_Giornale_codiceContestoPagamento"+
      ","+ALIAS+".identificativo_prestatore_servizi_pagamento as FESP_Giornale_identificativoPrestatoreServiziPagamento"+
      ","+ALIAS+".tipo_versamento as FESP_Giornale_tipoVersamento,"+ALIAS+".componente as FESP_Giornale_componente"+
      ","+ALIAS+".categoria_evento as FESP_Giornale_categoriaEvento,"+ALIAS+".tipo_evento as FESP_Giornale_tipoEvento"+
      ","+ALIAS+".sotto_tipo_evento as FESP_Giornale_sottoTipoEvento"+
      ","+ALIAS+".identificativo_fruitore as FESP_Giornale_identificativoFruitore"+
      ","+ALIAS+".identificativo_erogatore as FESP_Giornale_identificativoErogatore"+
      ","+ALIAS+".identificativo_stazione_intermediario_pa as FESP_Giornale_identificativoStazioneIntermediarioPa"+
      ","+ALIAS+".canale_pagamento as FESP_Giornale_canalePagamento"+
      ","+ALIAS+".parametri_specifici_interfaccia as FESP_Giornale_parametriSpecificiInterfaccia"+
      ","+ALIAS+".esito as FESP_Giornale_esito";


}
