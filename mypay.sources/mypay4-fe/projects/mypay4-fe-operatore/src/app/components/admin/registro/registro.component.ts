/*
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
import {
    MyPayBaseTableComponent
} from 'projects/mypay4-fe-common/src/lib/components/my-pay-table/my-pay-table.component';
import { MapPipe, PaginatorData, TableColumn } from 'projects/mypay4-fe-common/src/public-api';

import { DatePipe } from '@angular/common';
import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { faTimes } from '@fortawesome/free-solid-svg-icons';

import { RegistroOperazione } from '../../../model/registro-operazione';

@Component({
  selector: 'app-registro',
  templateUrl: './registro.component.html',
  styleUrls: ['./registro.component.scss']
})
export class RegistroComponent implements OnInit {

  iconTimes = faTimes;
  title: string;


  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<RegistroOperazione>;
  tableColumns: TableColumn[];
  tableData: RegistroOperazione[];
  paginatorData: PaginatorData;

  constructor(
    @Inject(MAT_DIALOG_DATA) private data: any,
  ) {
    this.tableData = data.tableData;
    this.title = data.title;
    const mapValue = data.mapValue || {true: 'Abilitato', false: 'Disabilitato'};

    this.tableColumns = [
      new TableColumn('username', 'Operazione eseguita da (ID utente)'),
      new TableColumn('fullName', 'Operazione eseguita da'),
      new TableColumn('dtOperazione', 'Data operazione',
            { sortable: (item: RegistroOperazione) => item.dtOperazione?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] }),
      new TableColumn('statoDa', 'Stato da', { pipe: MapPipe, pipeArgs: [mapValue]}) ,
      new TableColumn('statoA', 'Stato a', { pipe: MapPipe, pipeArgs: [mapValue]}) ,
    ];
  }

  ngOnInit(): void {
  }

}
