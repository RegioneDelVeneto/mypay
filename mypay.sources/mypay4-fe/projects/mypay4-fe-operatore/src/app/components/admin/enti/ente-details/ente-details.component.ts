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
import { ToastrService } from 'ngx-toastr';
import {
    Ente, manageError, OverlaySpinnerService, PageStateService, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { AdminEntiService } from 'projects/mypay4-fe-operatore/src/app/services/admin-enti.service';

import { Location } from '@angular/common';
import { Component, ElementRef, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-ente-details',
  templateUrl: './ente-details.component.html',
  styleUrls: ['./ente-details.component.scss']
})
export class EnteDetailsComponent implements OnInit, WithTitle {

  get titleLabel(){ return "Dettaglio ente" }
  get titleIcon(){ return null }

  mygovEnteId: number;
  ente: Ente;
  tabIndex: number;
  selected = new FormControl(0);

  constructor(
    private route: ActivatedRoute,
    private location: Location,
    private adminEntiService: AdminEntiService,
    private pageStateService: PageStateService,
    private toastrService: ToastrService,
    private elementRef: ElementRef,
    private overlaySpinnerService: OverlaySpinnerService,
  ) { }

  goBack(){
    this.location.back();
  }

  tabOnClick(tab) {
    this.tabIndex = tab.index;
  }

  ngOnInit(): void {
    //load params from url
    const params = this.route.snapshot.params;
    if (params && Object.keys(params).length > 0) {
      this.mygovEnteId = +params['enteId']; // (+) converts string 'id' to a number
      const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
      this.adminEntiService.getEnteById(this.mygovEnteId).subscribe( ente => {
        this.ente = ente;
        this.overlaySpinnerService.detach(spinner);
      }, manageError("Errore recuperando i dati", this.toastrService, () => this.overlaySpinnerService.detach(spinner)) );
    }
    let tabIndex = this.pageStateService.getSavedStateByKey('tabIndex');
    if (tabIndex) {
      this.tabIndex = tabIndex;
      this.selected.setValue(tabIndex);
    }
  }

}
