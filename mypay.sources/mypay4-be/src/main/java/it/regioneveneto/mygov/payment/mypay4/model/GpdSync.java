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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "mygov_gpd_sync_id")
public class GpdSync extends BaseEntity {

    public static final String ALIAS = "GpdSync";
    public static final String FIELDS = ""
            + ALIAS + ".mygovGpdSyncId as mygovGpdSyncId,"
            + ALIAS + ".version as version,"
            + ALIAS + ".mygovFlussoId as mygovFlussoId,"
            + ALIAS + ".mygovEnteId as mygovEnteId,"
            + ALIAS + ".filename as filename,"
            + ALIAS + ".fileId as fileId,"
            + ALIAS + ".codStato as codStato,"
            + ALIAS + ".numDovutiInviati as numDovutiInviati,"
            + ALIAS + ".numDovutiElaborati as numDovutiElaborati,"
            + ALIAS + ".num_rich_elaborazione as numRichElaborazione,"
            + ALIAS + ".dt_creazione as dtCreazione,"
            + ALIAS + ".dt_ultima_modifica as dtUltimaModifica";

    private Long mygovGpdSyncId;
    private int version;
    private Long mygovFlussoId;
    private Long mygovEnteId;
    private String filename;
    private String fileId;
    private String codStato;
    private int numDovutiInviati;
    private int numDovutiElaborati;
    private int numRichElaborazione;
    private Date dtCreazione;
    private Date dtUltimaModifica;
}
