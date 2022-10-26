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
import { PageStateService } from 'projects/mypay4-fe-common/src/public-api';

import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { faClone } from '@fortawesome/free-solid-svg-icons';

import { AdminEntiService } from '../../../../services/admin-enti.service';

@Component({
  selector: 'app-tipo-details',
  templateUrl: './tipo-details.component.html',
  styleUrls: ['./tipo-details.component.scss']
})
export class TipoDetailsComponent implements OnInit {

  get titleLabel(){ return "Dettaglio ente - tipo dovuto" }
  get titleIcon(){ return faClone }

  deNomeEnte: string;
  deTipo: string;
  thumbLogoEnte;
  tabIndex: number;
  selected = new FormControl(0);

  constructor(
    private route: ActivatedRoute,
    private location: Location,
    private adminEntiService: AdminEntiService,
    private pageStateService: PageStateService,
  ) { }

  goBack(){
    this.location.back();
  }

  tabOnClick(tab) {
    this.tabIndex = tab.index;
  }

  ngOnInit(): void {
    let tabIndex = this.pageStateService.getSavedStateByKey('tabIndex');
    if (tabIndex) {
      this.tabIndex = tabIndex;
      this.selected.setValue(tabIndex);
    }
  }

  setCodTipo(codTipo: string) {
    const enteId = Number(this.route.snapshot.params['enteId']);
    this.adminEntiService.getTipoDovutoByEnteAndCod(enteId, codTipo).subscribe(tipoDovuto => {
      this.deNomeEnte = tipoDovuto.deNomeEnte;
      this.deTipo = tipoDovuto.deTipo;
      this.thumbLogoEnte = tipoDovuto.thumbLogoEnte;
    });
  }
}
