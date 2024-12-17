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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygov_dovuto_preload_id")
public class DovutoPreload extends BaseEntity {

    public static final String ALIAS = "DovutoPreload";
    public static final String FIELDS = ""
            + ALIAS + ".mygovDovutoPreloadId as mygovDovutoPreloadId,"
            + ALIAS + ".mygovDovutoId as mygovDovutoId,"
            + ALIAS + ".gpdIupd as gpdIupd,"
            + ALIAS + ".gpdStatus as gpdStatus,"
            + ALIAS + ".nuovoGpdStatus as nuovoGpdStatus,"
            + ALIAS + ".mygovEnteId as mygovEnteId,"
            + ALIAS + ".dt_ultima_modifica as dtUltimaModifica";

    private Long mygovDovutoPreloadId;
    private Long mygovDovutoId;
    private String gpdIupd;
    private Character gpdStatus;
    private Character nuovoGpdStatus;
    private Long mygovEnteId;
    private Date dtUltimaModifica;
}