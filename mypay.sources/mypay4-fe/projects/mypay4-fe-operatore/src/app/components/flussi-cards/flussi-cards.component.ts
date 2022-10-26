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
import { WithTitle } from 'projects/mypay4-fe-common/src/public-api';

import { Component, OnInit } from '@angular/core';
import {
    faAngleRight, faCloudDownloadAlt, faCloudUploadAlt, faRandom, faReceipt
} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-flussi-cards',
  templateUrl: './flussi-cards.component.html',
  styleUrls: ['./flussi-cards.component.scss']
})
export class FlussiCardsComponent implements OnInit, WithTitle {

  get titleLabel(){ return "Gestione flussi" }
  get titleIcon(){ return faRandom }

  iconImport = faCloudUploadAlt;
  iconExport = faCloudDownloadAlt;
  iconSpc = faReceipt;
  iconAngleRight = faAngleRight;

  constructor() { }

  ngOnInit(): void {
  }

}
