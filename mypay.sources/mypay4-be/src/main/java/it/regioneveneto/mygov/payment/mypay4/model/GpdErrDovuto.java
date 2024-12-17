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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygov_gpd_err_dovuto_id")
public class GpdErrDovuto extends BaseEntity {


    public static final String ALIAS = "GpdErrDovuto";
    public static final String FIELDS = ""
            + ALIAS + ".mygov_gpd_err_dovuto_id as mygov_gpd_err_dovuto_id," + ","
            + ALIAS + ".cod_errore_http as cod_errore_http,"
            + ALIAS + ".gpd_iupd as gpd_iupd,"
            + ALIAS + ".descrizione as descrizione,"
            + ALIAS + ".azione as azione,"
            + ALIAS + ".dt_ultima_modifica as dtUltimaModifica"
            + ALIAS + ".mygov_ente_id as mygovEnteId";

    private Long mygovGpdErrDovutoId;
    private String gpdIupd;
    private String codErroreHttp;
    private String descrizione;
    private String azione;
    private Long mygovEnteId;
    private Date dtUltimaModifica;
}
