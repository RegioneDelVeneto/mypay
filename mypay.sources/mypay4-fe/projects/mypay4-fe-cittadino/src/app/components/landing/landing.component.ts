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
    ConfirmDialogComponent
} from 'projects/mypay4-fe-common/src/lib/components/confirm-dialog/confirm-dialog.component';
import {
    BaseUrlService, Ente, manageError, OverlaySpinnerService, StorageService, TipoDovuto,
    UserService
} from 'projects/mypay4-fe-common/src/public-api';
import { combineLatest, forkJoin, of } from 'rxjs';
import { first, map } from 'rxjs/operators';

import { Component, ElementRef, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';

import { Carrello } from '../../model/carrello';
import { Esito } from '../../model/esito';
import { CarrelloService } from '../../services/carrello.service';
import { CoreAppElementsActivation, CoreAppService } from '../../services/core-app.service';
import { EnteService } from '../../services/ente.service';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent implements OnInit {

  constructor(
    private route:ActivatedRoute,
    private router:Router,
    private storageService:StorageService,
    private toastrService: ToastrService,
    private elementRef: ElementRef,
    private overlaySpinnerService: OverlaySpinnerService,
    private carrelloService: CarrelloService,
    private enteService: EnteService,
    private dialog: MatDialog,
    private baseUrl: BaseUrlService,
    private coreAppService: CoreAppService,
    private userService: UserService) { }

  ngOnInit(): void {
    const landingType = this.route.snapshot.paramMap.get('type');
    if(['inviaDovuti','precaricato'].includes(landingType)){

      const id = this.route.snapshot.queryParamMap.get('id');
      const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
      combineLatest([
        this.storageService.getObject<Carrello>(id),
        this.enteService.getAllEnti()])
      .subscribe( ([carrello,enti]) => {
        this.overlaySpinnerService.detach(spinner);
        console.log('external carrello:',carrello);
        if(!(carrello?.items?.length>0))
          manageError("Errore processando il carrello", this.toastrService)("carrello vuoto");
        //empty carrello in case it contains something (in theory this should not be possible)
        this.carrelloService.empty();
        //add items to carrello
        const mapTipoDovuto = new Map<string, TipoDovuto[]>();

        forkJoin(carrello.items.map(item => {
          const ente: Ente = enti.find(ente => ente.codIpaEnte === item.codIpaEnte);
          item.deEnte = ente.deNomeEnte;
          const tipoDovuto = mapTipoDovuto.get(ente.codIpaEnte);
          return (tipoDovuto ? of(tipoDovuto) : this.enteService.getListTipoDovutoByEnte(ente))
            .pipe(map(tipiDovuto => {
              mapTipoDovuto.set(ente.codIpaEnte, tipiDovuto);
              const tipoDovuto: TipoDovuto = tipiDovuto.find(tipoDovuto => tipoDovuto.codTipo === item.codTipoDovuto);
              item.deTipoDovuto = tipoDovuto.deTipo;
              this.carrelloService.add(item);
            }));
        })).subscribe( () => this.router.navigate(['carrello'],{state: {
          versante: carrello.versante,
          idSession: carrello.idSession,
          backUrl: carrello.backUrlInviaEsito,
          tipoCarrello: carrello.tipoCarrello }}) );

      }, manageError("Errore processando il carrello", this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}))

    } else if(landingType === 'paymentReplica') {

      const basketId = this.route.snapshot.queryParamMap.get('id');
      const replicaType = this.route.snapshot.queryParamMap.get('type');
      let msg:string;
      if(replicaType === 'dovuto')
        msg = 'ATTENZIONE: hai un altro pagamento con la stessa causale in attesa dell\'esito. ' +
              'Se hai già tentato un pagamento controlla la tua casella email, verifica se hai la ricevuta con l\'esito del pagamento.';
      else
        msg = 'ATTENZIONE: hai già eseguito un pagamento con la stessa causale nelle ultime 24 ore.';
      msg += '\nConfermi di voler procedere con il pagamento?';
      this.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        console.log('redirecting to pagopa');
        let redirectUrl = this.baseUrl.getBaseUrlApi()+'public/landing/inviaDovuti?id='+basketId
          +'&overrideCheckReplicaPayments='+(result==='true' ? 'ok' : 'ko');
        setTimeout(() => {
          console.log('redirecting to url: '+redirectUrl);
          window.location.href = redirectUrl;
        }, 100);
      });

    } else if(landingType === 'ente') {

      let codIpaEnte = this.route.snapshot.queryParamMap.get('codIpaEnte');
      if(!codIpaEnte)
        codIpaEnte = this.route.snapshot.queryParamMap.get('enteToChange');

      const queryParams = {};
      const redirectUrl = this.route.snapshot.queryParamMap.get('redirectUrl');
      if(redirectUrl){
        const params = (new URL(redirectUrl, window.location.href)).searchParams;
        if(params.has('numeroAvviso'))
          queryParams['numeroAvviso'] = params.get('numeroAvviso');
        if(params.has('codIdUnivoco'))
          queryParams['codIdUnivoco'] = params.get('codIdUnivoco');
      }
      if(codIpaEnte){
        this.router.navigate(['ente', codIpaEnte], {queryParams: queryParams});
      } else {
        this.router.navigate(['home']);
      }

    } else if(landingType === 'spontaneo') {

      const codIpaEnte = this.route.snapshot.queryParamMap.get('codIpaEnte');
      const codTipoDovuto = this.route.snapshot.queryParamMap.get('codTipo');

      this.router.navigate(['spontaneo'],{state: {codIpaEnte: codIpaEnte, codTipoDovuto: codTipoDovuto}});

    } else if(landingType === 'charity') {

      const coreAppElements = new CoreAppElementsActivation();
      coreAppElements.sidenav = false;
      coreAppElements.header = false;
      this.coreAppService.setState(coreAppElements);

      const callbackUrl = this.route.snapshot.queryParamMap.get('callbackUrl');

      this.router.navigate(['spontaneo']);

    } else if(landingType === 'esitoPagamento') {

      const id = this.route.snapshot.queryParamMap.get('id');
      this.storageService.getObject<Esito>(id).subscribe(esito => {
        this.router.navigate(['esito-carrello'], {state: {esito: esito}});
      }, manageError('Errore recuperando la sessione del pagamento', this.toastrService));

    } else if(landingType === 'login') {

      if(this.userService.isLogged())
        this.router.navigate(['home']);
      else
        this.userService.goToLogin();

    } else {

      console.log('invalid landingType ['+landingType+'].. redirecting to home');
      this.router.navigate(['home']);

    }
  }

}
