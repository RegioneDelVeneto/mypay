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
import * as _ from 'lodash';
import { FileSaverService } from 'ngx-filesaver';
import { ToastrService } from 'ngx-toastr';
import {
    ApiInvokerService, controlToUppercase, CookieService, Ente, manageError, OverlaySpinnerService,
    PATTERNS, TableAction, TableColumn, TipoDovuto, UserService, validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { combineLatest, Observable, ReplaySubject, Subscription } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { animate, state, style, transition, trigger } from '@angular/animations';
import { CurrencyPipe, DatePipe, TitleCasePipe } from '@angular/common';
import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatButtonToggleChange } from '@angular/material/button-toggle';
import { ActivatedRoute } from '@angular/router';
import {
    faCartPlus, faDownload, faFileInvoice, faPrint, faReceipt, faTrash
} from '@fortawesome/free-solid-svg-icons';

import { Debito } from '../../model/debito';
import { Pagato } from '../../model/pagato';
import { AvvisoService } from '../../services/avviso.service';
import { CarrelloService } from '../../services/carrello.service';
import { PagatoService } from '../../services/pagato.service';
import { RecaptchaService } from '../../services/recaptcha.service';

@Component({
  selector: 'app-avvisi',
  templateUrl: './avvisi.component.html',
  styleUrls: ['./avvisi.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class AvvisiComponent implements OnInit,AfterViewInit,OnDestroy, WithTitle {

  get titleLabel(){ return "Avvisi di pagamento" }
  get titleIcon(){ return faFileInvoice }

  //@ViewChild('sForm') searchFormDirective;

  iconPrint = faPrint;
  iconAddCart = faCartPlus;
  iconRemoveCart = faTrash;
  iconReceipt = faReceipt;
  iconDownload = faDownload;

  enteOptions: Ente[];
  enteFilteredOptions: Observable<Ente[]>;

  tipoDovutoOptionsMap: Map<String, TipoDovuto[]>;
  tipoDovutoOptions: TipoDovuto[];
  tipoDovutoFilteredOptions: Observable<TipoDovuto[]>;
  previousEnte: Ente;

  hasSearched: boolean = false;
  blockingError: boolean = false;
  isWating: boolean = false;
  private waitForSearchOnInit = new ReplaySubject<boolean>();

  logged: boolean;
  loggedUserName: string;

  form: FormGroup;
  formErrors = {};
  captchaError: boolean = false;
  cfAnonimoChecked: boolean = false;
  hasConsent: boolean = true;

  tableColumns: TableColumn[] = [
    new TableColumn('deEnte', 'Beneficiario'),
    new TableColumn('deTipoDovuto', 'Tipo dovuto'),
    new TableColumn('causale', 'Causale del versamento'),
    new TableColumn('importo','Importo', { pipe: CurrencyPipe, pipeArgs:['EUR', 'symbol'] } ),
    new TableColumn('dataScadenza', 'Data scadenza', { sortable: (item: Debito) => item.dataScadenza?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy'] } ),
    new TableColumn('deStato', 'Stato', { pipe: TitleCasePipe }),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(this.iconReceipt, this.downloadAvviso, this.downloadAvvisoEnabled, 'Scarica avviso'),
      new TableAction(this.iconReceipt, this.downloadRt, this.downloadRtEnabled, 'Scarica RT'),
      new TableAction(this.iconAddCart, this.addToCarrello, this.addToCarrelloEnabled, 'Aggiungi al carrello'),
      new TableAction(this.iconRemoveCart, this.removeFromCarrello, this.removeFromCarrelloEnabled, 'Rimuovi dal carrello')
      ] } ) ];
  tableData: Debito[];
  rowStyleFun = (elem:Debito) => elem?.deStato === 'PAGAMENTO INIZIATO' ? 'background-color: lightyellow' : null;

  constructor(
    protected userService: UserService,
    protected formBuilder: FormBuilder,
    protected avvisoService: AvvisoService,
    protected pagatoService: PagatoService,
    protected carrelloService: CarrelloService,
    protected toastrService: ToastrService,
    protected overlaySpinnerService: OverlaySpinnerService,
    protected elementRef: ElementRef,
    protected fileSaverService: FileSaverService,
    private renderer: Renderer2,
    private recaptchaService: RecaptchaService,
    private cookieService: CookieService,
    private route: ActivatedRoute) {
  }

  private codFiscaleUppercaseSub: Subscription;
  private formChangesSub: Subscription;
  private loggedUserAndCookieConsentSub: Subscription;


  ngOnInit(): void {
    //check if query params was passed to url to init search
    const params = this.route.snapshot.queryParams;
    const numeroAvviso = params?.['numeroAvviso'];
    const codIdUnivoco = params?.['codIdUnivoco'];
    const searchOnInit = !_.isNil(numeroAvviso) && !_.isNil(codIdUnivoco);
    if(!searchOnInit)
      this.waitForSearchOnInit.next(false);

    this.form = this.formBuilder.group({
      numeroAvviso: ['', [Validators.required]],
      tipoPersona: [''],
      codIdUnivoco: ['', [Validators.required, Validators.pattern(PATTERNS.codiceFiscaleOPartitaIva)]],
      cfAnonimo: [false],
      anagrafica: ['', [Validators.required]]
    });
    this.codFiscaleUppercaseSub = controlToUppercase(this.form.get('codIdUnivoco'));
    this.formChangesSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors));

    this.loggedUserAndCookieConsentSub = combineLatest([this.userService.getLoggedUserObs(), this.cookieService.getConsentStateObs()])
    .subscribe( ([loggedUser,cookieConsent]) => {
      this.loggedUserName = this.userService.getLoggedUserString();
      if(loggedUser){
        this.logged = true;
        this.hasConsent = true;
        this.cookieService.unsetMissingNeededConsent();
        this.form.get('tipoPersona').setValue('logged');
        this.form.get('codIdUnivoco').disable();
        this.form.get('cfAnonimo').disable();
      } else {
        this.logged = false;
        this.form.get('tipoPersona').setValue('other');
        console.log("cookie consent state on avviso:"+JSON.stringify(cookieConsent));
        if(cookieConsent.cookieAll || cookieConsent.cookieThirdParty){
          this.hasConsent = true;
          this.cookieService.unsetMissingNeededConsent();
          this.recaptchaService.init(this.renderer);
        } else {
          this.cookieService.setMissingNeededConsent();
          this.hasConsent = false;
        }
      }
      this.form.get('anagrafica').disable();
      if(searchOnInit){
        this.form.get('numeroAvviso').setValue(numeroAvviso);
        this.form.get('tipoPersona').setValue('other');
        this.form.get('codIdUnivoco').enable();
        this.form.get('codIdUnivoco').setValue(codIdUnivoco);
        this.form.markAllAsTouched();
        this.waitForSearchOnInit.next(true);
      }
    });
  }

  ngAfterViewInit(){
    this.waitForSearchOnInit.subscribe(searchOnInit => {
      if(searchOnInit && this.form.valid)
        this.onSubmit();
    })
  }

  ngOnDestroy():void {
    this.codFiscaleUppercaseSub?.unsubscribe();
    this.formChangesSub?.unsubscribe();
    this.loggedUserAndCookieConsentSub?.unsubscribe();
    this.recaptchaService.deactivate();
  }

  enteDisplayFn(ente: Ente): string {
    return ente ? ente.deNomeEnte : '';
  }

  tipoDovutoDisplayFn(tipoDovuto: TipoDovuto): string {
    return tipoDovuto ? tipoDovuto.deTipo : '';
  }

  onChangeTipoPersona(event: MatButtonToggleChange) {
    if (event?.value==='logged') {
      this.form.get('codIdUnivoco').setValue(null);
      this.form.get('codIdUnivoco').disable();
      this.form.get('cfAnonimo').setValue(false);
      this.form.get('cfAnonimo').disable();
      this.form.get('anagrafica').setValue(null);
      this.form.get('anagrafica').disable();
    } else {
      this.form.get('codIdUnivoco').enable();
      this.form.get('cfAnonimo').enable();
    }
  }

  cfAnonimoOnChange(checked: boolean) {
    const idUnivocoField = this.form.get('codIdUnivoco');
    const anagraficaField = this.form.get('anagrafica');
    this.cfAnonimoChecked = checked;
    if(checked) {
      anagraficaField.enable();
      idUnivocoField.setValue(null);
      idUnivocoField.disable();
    } else {
      idUnivocoField.enable();
      anagraficaField.setValue(null);
      anagraficaField.disable();
    }
  }

  onReset(){
    this.form.reset({
      numeroAvviso: '',
      tipoPersona: '',
      codIdUnivoco: '',
      cfAnonimo: false,
      anagrafica: '',
    });
    this.cfAnonimoOnChange(false);
  }

  onSubmit(){
    const i = this.form.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    let searchAvvisiFun:Observable<any>;
    if(this.logged)
      searchAvvisiFun = this.avvisoService.searchAvvisi(i.numeroAvviso, i.tipoPersona === 'logged', i.codIdUnivoco, i.anagrafica);
    else
      searchAvvisiFun = this.recaptchaService.submitToken('searchAvviso').pipe(
        flatMap(token => this.avvisoService.searchAvvisiAnonymous(i.numeroAvviso, i.codIdUnivoco, i.anagrafica, token))
      );

    searchAvvisiFun
      .subscribe(response => {
        this.hasSearched = true;
        const dovuti = <Debito[]> response.debiti;
        const pagati = <Pagato[]> response.pagati;
        dovuti.forEach(element => {
          element.details = [
            {key:'Oggetto del pagamento', value:element.causaleVisualizzata},
            {key:'Numero avviso', value:element.numeroAvviso},
            {key:'Intestatario avviso', value:element.intestatarioAvviso},
          ];
          this.carrelloService.updateState(element);
        })
        pagati.forEach(element => {
          const dovuto = new Debito();
          dovuto.dovutoElaborato = element;
          dovuto.deEnte = element.enteDeNome;
          dovuto.deTipoDovuto = element.deTipoDovuto;
          dovuto.causale = element.causale;
          dovuto.importo = element.importo;
          dovuto.dataScadenza = element.dataScadenza;
          dovuto.codStato = element.codStato;
          dovuto.deStato = element.statoComplessivo;
          dovuto.details = [
            {key:'Causale del versamento', value:element.causale},
            {key:'Numero avviso', value:element.codIuv},
            {key:'Intestatario avviso', value:element.intestatario},
            //{key:'Stato dettaglio', value:element.stato}
          ];
          dovuti.push(dovuto);
        })
        this.tableData = dovuti;
        this.overlaySpinnerService.detach(spinner);
      }, manageError('Errore effettuando la ricerca', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  removeSpaces(formControlName: any) {
    const field = this.form.get(formControlName.toString());
    field.setValue(field.value.replace(/\s/g, ""));
  }

  downloadAvviso(elementRef: Debito, thisRef: AvvisiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    let downloadAvvisoFun:Observable<any>;
    if(thisRef.logged)
      downloadAvvisoFun = thisRef.avvisoService.downloadAvviso(elementRef);
    else
      downloadAvvisoFun = thisRef.recaptchaService.submitToken('downloadAvviso').pipe(
        flatMap(token => thisRef.avvisoService.downloadAvvisoAnonymous(elementRef, token))
      );
    downloadAvvisoFun.subscribe(response => {
      const contentDisposition = response.headers.get('content-disposition');
      const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? 'mypay4_avviso_'+elementRef.id+'.pdf';
      const contentType = response.headers.get('content-type') ?? 'application/pdf; charset=utf-8';
      const blob:any = new Blob([response.body], { type: contentType });
      thisRef.fileSaverService.save(blob, fileName);
    }, manageError('Errore scaricando l\'avviso di pagamento', thisRef.toastrService) );
  }

  downloadAvvisoEnabled(elementRef: Debito, thisRef: AvvisiComponent) {
    return elementRef.codStato === 'INSERIMENTO_DOVUTO' && !_.isNil(elementRef.codIuv);
  }

  downloadRt(elementRef: Debito, thisRef: AvvisiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    let downloadRtFun:Observable<any>;
    if(thisRef.logged)
      downloadRtFun = thisRef.pagatoService.downloadRt(elementRef.dovutoElaborato);
    else
      downloadRtFun = thisRef.recaptchaService.submitToken('downloadRt').pipe(
        flatMap(token => thisRef.pagatoService.downloadRtAnonymous(elementRef.dovutoElaborato,token))
      );
    downloadRtFun.subscribe(response => {
      const contentDisposition = response.headers.get('content-disposition');
      const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? 'mypay4_ricevuta_'+elementRef.dovutoElaborato.id+'.pdf';
      const contentType = response.headers.get('content-type') ?? 'application/pdf; charset=utf-8';
      const blob:any = new Blob([response.body], { type: contentType });
      thisRef.fileSaverService.save(blob, fileName);
    }, manageError('Errore scaricando la ricevuta di pagamento', thisRef.toastrService) );
  }

  downloadRtEnabled(elementRef: Debito, thisRef: AvvisiComponent) {
    return elementRef.dovutoElaborato?.stato === 'COMPLETATO';
  }

  addToCarrello(elementRef: Debito, thisRef: AvvisiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();

    const addError = thisRef.carrelloService.add(elementRef);

    if(addError)
      thisRef.toastrService.error(addError,'Errore aggiungendo al carrello',{disableTimeOut: true});
    else
      thisRef.toastrService.info('Elemento aggiunto al carrello');
  }

  addToCarrelloEnabled(elementRef: Debito, thisRef: AvvisiComponent){
    return thisRef.carrelloService.canAdd(elementRef);
  }

  removeFromCarrello(elementRef: Debito, thisRef: AvvisiComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    const removeError = thisRef.carrelloService.remove(elementRef);
    if(removeError)
      thisRef.toastrService.error(removeError,'Errore rimuovendo dal carrello',{disableTimeOut: true});
    else
      thisRef.toastrService.info('Elemento rimosso dal carrello',null,{disableTimeOut: true});
  }

  removeFromCarrelloEnabled(elementRef: Debito, thisRef: AvvisiComponent){
    return thisRef.carrelloService.canRemove(elementRef);
  }

}
