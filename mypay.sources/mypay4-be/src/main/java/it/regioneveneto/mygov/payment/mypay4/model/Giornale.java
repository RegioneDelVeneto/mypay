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
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder(toBuilder=true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygovGiornaleId")
public class Giornale extends it.regioneveneto.mygov.payment.mypay4.model.common.Giornale {
  public static final String ALIAS = "Giornale";
  public static final String FIELDS = ""+ALIAS+".mygov_giornale_id as Giornale_mygovGiornaleId,"+ALIAS+".version as Giornale_version"+
      ","+ALIAS+".data_ora_evento as Giornale_dataOraEvento,"+ALIAS+".identificativo_dominio as Giornale_identificativoDominio"+
      ","+ALIAS+".identificativo_univoco_versamento as Giornale_identificativoUnivocoVersamento"+
      ","+ALIAS+".codice_contesto_pagamento as Giornale_codiceContestoPagamento"+
      ","+ALIAS+".identificativo_prestatore_servizi_pagamento as Giornale_identificativoPrestatoreServiziPagamento"+
      ","+ALIAS+".tipo_versamento as Giornale_tipoVersamento,"+ALIAS+".componente as Giornale_componente"+
      ","+ALIAS+".categoria_evento as Giornale_categoriaEvento,"+ALIAS+".tipo_evento as Giornale_tipoEvento"+
      ","+ALIAS+".sotto_tipo_evento as Giornale_sottoTipoEvento"+
      ","+ALIAS+".identificativo_fruitore as Giornale_identificativoFruitore"+
      ","+ALIAS+".identificativo_erogatore as Giornale_identificativoErogatore"+
      ","+ALIAS+".identificativo_stazione_intermediario_pa as Giornale_identificativoStazioneIntermediarioPa"+
      ","+ALIAS+".canale_pagamento as Giornale_canalePagamento"+
      ","+ALIAS+".parametri_specifici_interfaccia as Giornale_parametriSpecificiInterfaccia,"+ALIAS+".esito as Giornale_esito";

}
