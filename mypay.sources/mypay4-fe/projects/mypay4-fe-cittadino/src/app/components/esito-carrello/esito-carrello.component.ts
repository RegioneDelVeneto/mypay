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
import { Router } from '@angular/router';
import { faShoppingCart } from '@fortawesome/free-solid-svg-icons';

import { Esito } from '../../model/esito';

@Component({
  selector: 'app-esito-carrello',
  templateUrl: './esito-carrello.component.html',
  styleUrls: ['./esito-carrello.component.scss']
})
export class EsitoCarrelloComponent implements OnInit, WithTitle {

  get titleLabel(){ return "Esito pagamento: transazione conclusa" }
  get titleIcon(){ return faShoppingCart }

  private esitoLanding: Esito;
  esito: string;

  constructor(
    private router: Router,
  ) {
    this.esitoLanding = router.getCurrentNavigation()?.extras?.state?.esito; //FOR TESTS ONLY  || {esito:'OK', returnMsg:'bla bla bla'};
  }

  ngOnInit(): void {
    if(this.esitoLanding?.returnMsg)
      this.esito = this.esitoLanding.returnMsg;
    else if(this.esitoLanding.esito === 'OK')
      this.esito = 'Processo di pagamento eseguito correttamente, l\'esito della transazione sarà disponibile a breve. Controlla la tua casella di posta.';
    else
      this.esito = 'Processo di pagamento non completato correttamente, l\'esito della transazione sarà disponibile a breve. Controlla la tua casella di posta.'
  }

  backHome(): void {
    this.router.navigate(['home']);
  }
}
