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
package it.regioneveneto.mygov.payment.mypay4.dao;

import it.regioneveneto.mygov.payment.mypay4.model.EsitoAvvisoDigitale;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface EsitoAvvisoDigitaleDao extends BaseDao {

  @SqlUpdate(
      "inset into mygov_esito_avviso_digitale (" +
          "   mygov_esito_avviso_digitale_id" +
          " , version" +
          " , mygov_avviso_digitale_id" +
          " , num_e_ad_esito_av_tipo_canale_esito" +
          " , cod_e_ad_esito_av_id_canale_esito" +
          " , dt_e_ad_esito_av_data_esito" +
          " , num_e_ad_esito_av_codice_esito" +
          " , de_e_ad_esito_av_desc_esito" +
          " ) values (" +
          " nextval('')" +
          " , :d.version" +
          " , :d.mygovAvvisoDigitaleId.mygovAvvisoDigitaleId" +
          " , :d.numEAdEsitoAvTipoCanaleEsito" +
          " , :d.codEAdEsitoAvIdCanaleEsito" +
          " , :d.dtEAdEsitoAvDataEsito" +
          " , :d.numEAdEsitoAvCodiceEsito" +
          " , :d.deEAdEsitoAvDescEsito)"
  )
  @GetGeneratedKeys("mygov_esito_avviso_digitale_id")
  long insert(@BindBean("d") EsitoAvvisoDigitale d);

}
