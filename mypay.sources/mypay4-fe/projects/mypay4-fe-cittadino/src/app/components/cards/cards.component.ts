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
    manageError, OverlaySpinnerService, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';

import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import {
    faAngleRight, faCashRegister, faCreditCard, faFileInvoice, faHome, faInfoCircle, faTags
} from '@fortawesome/free-solid-svg-icons';

import { Debito } from '../../model/debito';
import { Pagato } from '../../model/pagato';
import { DebitoService } from '../../services/debito.service';
import { PagatoService } from '../../services/pagato.service';

@Component({
  selector: 'app-cards',
  templateUrl: './cards.component.html',
  styleUrls: ['./cards.component.scss']
})
export class CardsComponent implements OnInit, AfterViewInit, WithTitle {

  @ViewChild('cardPagati', { read: ElementRef }) cardPagati: ElementRef;
  @ViewChild('cardDebiti', { read: ElementRef }) cardDebiti: ElementRef;

  get titleLabel(){ return "Bacheca pagamenti" }
  get titleIcon(){ return faHome }

  iconAngleRight = faAngleRight;
  iconCashRegister = faCashRegister;
  iconCreditCard = faCreditCard;
  iconFileInvoice = faFileInvoice;
  iconTags = faTags;
  iconInfo = faInfoCircle;

  listPagati: Pagato[];
  listDebiti: Debito[];

  constructor(
    private pagatoService: PagatoService,
    private debitoService: DebitoService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService) {
  }

  ngOnInit(): void {

  }

  ngAfterViewInit() {
    //load "storico transazioni"
    const spinnerPagati = this.overlaySpinnerService.showProgress(this.cardPagati);
    this.pagatoService.searchLastPagati(3)
      .subscribe(data => {
        this.listPagati = data.map(p => {
          p.dataPagamentoDay = p.dataPagamento?.toFormat('dd');
          p.dataPagamentoMonth = p.dataPagamento?.toFormat('MMM');
          p.dataPagamentoYear = p.dataPagamento?.toFormat('yyyy');
          return p;
        });
        this.overlaySpinnerService.detach(spinnerPagati);
      }, manageError('Errore ricercando Storico Transazioni', this.toastrService, () => {this.overlaySpinnerService.detach(spinnerPagati)}) );
    //load "posizioni aperte"
    const spinnerDebiti = this.overlaySpinnerService.showProgress(this.cardDebiti);
    this.debitoService.searchLastDebiti(3)
      .subscribe(data => {
        this.listDebiti = data.map(d => {
          d.dataScadenzaDay = d.dataScadenza?.toFormat('dd');
          d.dataScadenzaMonth = d.dataScadenza?.toFormat('MMM');
          d.dataScadenzaYear = d.dataScadenza?.toFormat('yyyy');
          return d;
        });
        this.overlaySpinnerService.detach(spinnerDebiti);
      }, manageError('Errore ricercando Storico Transazioni', this.toastrService, () => {this.overlaySpinnerService.detach(spinnerDebiti)}) );
  }

}
