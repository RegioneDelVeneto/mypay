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
import { DateTime } from 'luxon';
import { FileSaverService } from 'ngx-filesaver';
import { ToastrService } from 'ngx-toastr';
import {
  ConfirmDialogComponent
} from 'projects/mypay4-fe-common/src/lib/components/confirm-dialog/confirm-dialog.component';
import {
  ApiInvokerService, blank2Null, Comune, ConfigurationService, controlToUppercase, LocationService, manageError,
  Nazione, OverlaySpinnerService, PATTERNS, ProcessHTTPMsgService, Provincia, TableAction,
  TableColumn, UserService, validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { forkJoin, Observable, of, Subject, Subscription } from 'rxjs';
import { delay, first, map } from 'rxjs/operators';

import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { CdkStepper, StepperSelectionEvent } from '@angular/cdk/stepper';
import { CurrencyPipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, Renderer2, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatButtonToggleChange } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatStepper } from '@angular/material/stepper';
import { Router } from '@angular/router';
import {
  faAddressCard, faCheck, faEnvelope, faEuroSign, faMoneyBillWave, faPen, faReceipt,
  faShoppingCart, faTrash
} from '@fortawesome/free-solid-svg-icons';

import { Attualizzazione } from '../../model/altri-interface';
import { Carrello } from '../../model/carrello';
import { Debito } from '../../model/debito';
import { Esito } from '../../model/esito';
import { ItemCarrello } from '../../model/item-carrello';
import { MailValidationRequest, MailValidationResponse } from '../../model/mail-validation';
import { Person } from '../../model/person';
import { Spontaneo } from '../../model/spontaneo';
import { AvvisoService } from '../../services/avviso.service';
import { CarrelloService } from '../../services/carrello.service';
import { MailValidationService } from '../../services/mail-validation.service';
import { RecaptchaService } from '../../services/recaptcha.service';
import { SpontaneoService } from '../../services/spontaneo.service';

@Component({
  selector: 'app-carrello',
  templateUrl: './carrello.component.html',
  styleUrls: ['./carrello.component.scss'],
  providers: [{ provide: CdkStepper, useExisting: CarrelloComponent }]
})
export class CarrelloComponent implements OnInit, OnDestroy, WithTitle {

  @ViewChild('stepper') stepper: MatStepper;

  get titleLabel(){ return "Carrello" }
  get titleIcon(){ return faShoppingCart }

  iconRemoveCart = faTrash;
  iconReceipt = faReceipt;
  iconAddressCard = faAddressCard;
  iconMoneyBillWave = faMoneyBillWave;
  iconCheck = faCheck;
  iconEuroSign = faEuroSign;
  iconPen = faPen;
  iconEnvelope = faEnvelope;

  formGroup: FormGroup;
  formGroupMailValidation: FormGroup;

  //sbuscriptions (keep track for unsubscribe)
  private loggedUserSub: Subscription;
  private formSub: Subscription;
  private formMailValidationSub: Subscription;
  private carrelloSub: Subscription;
  private codFiscaleUppercaseSub: Subscription;
  verticalStepper: Observable<boolean>;

  itemCarrelloByIntestatari: {id:string, info:Person, content:ItemCarrello[]}[];
  empty: boolean = true;
  versante: Person;
  importoTotale: number;
  dovutiEntiSecondari: any;
  logged: boolean = false;
  isStepCompleted = {};
  isStepEditable = {};
  stepsInfo:any = {};
  paymentUrl: string = null;
  validationEmail: string = null;
  backUrl: string = null;
  idSession: string = null;
  tipoCarrello: string = null;
  triggerEsternoAnonimo: boolean = false;
  disablePayButton: boolean = false;
  private modelloUnico: boolean;

  formErrors = { };
  private formMailValidationValidationFun;

  private formValidationMessages = {
    'verificationCode': {
      'pattern':  'Inserire 6 cifre',
    }
  };

  private controlNames = [
    'indirizzo',
    'civico',
    'cap',
    'nazione',
    'provincia',
    'comune'];

  codiceIdentificativoUnivocoDetails = {
    F: {
      label: 'Codice fiscale',
      validators: [Validators.required, Validators.pattern(PATTERNS.codiceFiscale)]
    },
    G: {
      label: 'Partita IVA',
      validators: [Validators.required, Validators.pattern(PATTERNS.partitaIva)]
    }
  }

  private controlNamesAnonymous = [
    {name: 'anagrafica', initial: null, validators: Validators.required},
    {name: 'tipoSoggetto', initial: 'F', validators: [Validators.required, Validators.pattern(PATTERNS.tipoSoggetto)]},
    {name: 'codiceIdentificativoUnivoco', initial: null, validators: this.codiceIdentificativoUnivocoDetails.F.validators},
    {name: 'cfAnonimo', initial: false},
    {name: 'email', initial: null, validators: [Validators.required, Validators.email]}
  ];

  constructor(
    private userService: UserService,
    private carrelloService: CarrelloService,
    private avvisoService: AvvisoService,
    private locationService: LocationService,
    private spontaneoService: SpontaneoService,
    private mailValidationService: MailValidationService,
    private fileSaverService: FileSaverService,
    private router: Router,
    private toastrService: ToastrService,
    private elementRef: ElementRef,
    private overlaySpinnerService: OverlaySpinnerService,
    private renderer: Renderer2,
    private recaptchaService: RecaptchaService,
    private dialog: MatDialog,
    private breakpointObserver: BreakpointObserver,
    private conf: ConfigurationService,
  ) {
    this.modelloUnico = conf.getBackendProperty<boolean>('modelloUnico', false);

    const versante:Person = this.router.getCurrentNavigation()?.extras?.state?.versante;
    if(versante){
      this.carrelloService.preloaded.versante = versante;
      console.log('preloading versante with values: ',versante);
      this.versante = versante;
    } else if(this.carrelloService.preloaded.versante){
      this.versante = this.carrelloService.preloaded.versante;
    }
    const idSession = this.router.getCurrentNavigation()?.extras?.state?.idSession;
    if(idSession){
      this.carrelloService.preloaded.idSession = idSession;
      console.log('preloading idSession with value: '+idSession);
      this.idSession = idSession;
    }else if(this.carrelloService.preloaded.idSession){
      this.idSession = this.carrelloService.preloaded.idSession;
    }
    const backUrl:string = blank2Null(this.router.getCurrentNavigation()?.extras?.state?.backUrl);
    if(backUrl){
      this.carrelloService.preloaded.backUrl = backUrl;
      console.log('preloading backUrl with value: '+backUrl);
      this.backUrl = backUrl;
    }else if(this.carrelloService.preloaded.backUrl){
      this.backUrl = this.carrelloService.preloaded.backUrl;
    }
    const tipoCarrello = this.router.getCurrentNavigation()?.extras?.state?.tipoCarrello;
    if(tipoCarrello){
      this.carrelloService.preloaded.tipoCarrello = tipoCarrello;
      console.log('preloading tipoCarrello with value: '+tipoCarrello);
      this.tipoCarrello = tipoCarrello;
    }else if(this.carrelloService.preloaded.tipoCarrello){
      this.tipoCarrello = this.carrelloService.preloaded.tipoCarrello;
    }
    this.triggerEsternoAnonimo = CarrelloService.isEsternoAnonimo(this.tipoCarrello);

    const dovutiEntiSecondari = this.router.getCurrentNavigation()?.extras?.state?.dovutiEntiSecondari ?? this.carrelloService.preloaded.dovutiEntiSecondari;
    if(dovutiEntiSecondari){
      this.carrelloService.preloaded.dovutiEntiSecondari = dovutiEntiSecondari;
      this.dovutiEntiSecondari = dovutiEntiSecondari;
    }

    //make stepper responsive
    this.verticalStepper = this.breakpointObserver.observe([
      Breakpoints.XSmall,
      Breakpoints.Small
    ]).pipe(map(result => result.matches));
  }

  ngOnInit(): void {
    this.formGroupMailValidation = new FormGroup({});
    this.formMailValidationValidationFun = validateFormFun(this.formGroupMailValidation, this.formErrors, this.formValidationMessages);
    this.formMailValidationSub = this.formGroupMailValidation.valueChanges.subscribe(this.formMailValidationValidationFun);
    this.formGroupMailValidation.addControl('verificationCode',new FormControl(null, [Validators.required,Validators.pattern(PATTERNS.fixedDigit(6))]));

    this.formGroup = new FormGroup({});
    this.formSub = this.formGroup.valueChanges.subscribe(validateFormFun(this.formGroup, this.formErrors, this.formValidationMessages));
    this.controlNames.forEach(controlName => {
      this.formGroup.addControl('versante_'+controlName,new FormControl());
    });

    //subscribe to carrello
    this.carrelloSub = this.carrelloService.get().subscribe(carrello => this.updateCarrello(carrello, this));

    this.loadNazioni();
    this.loadProvince();

    //subscribe to logged user for versante
    this.loggedUserSub = this.userService.getLoggedUserObs().subscribe(loggedUser => {
      if(loggedUser){
        this.versante = new Person();
        this.logged = true;
        this.versante.anagrafica = loggedUser.nome+' '+loggedUser.cognome;
        this.versante.cap = loggedUser.cap;
        this.versante.civico = loggedUser.civico;
        this.versante.codiceIdentificativoUnivoco = loggedUser.codiceFiscale;
        this.versante.email = loggedUser.email;
        this.versante.indirizzo = loggedUser.indirizzo;
        this.versante.localitaId = loggedUser.comuneId;
        this.versante.provinciaId = loggedUser.provinciaId;
        this.versante.nazioneId = loggedUser.nazioneId;
      } else {
        if(this.logged)
          this.emptyCarrello();
        this.versante = this.versante || new Person();
        this.logged = false;
        this.recaptchaService.init(this.renderer);
        this.controlNamesAnonymous.forEach(controlName => {
          const initialValue = this.versante?.[controlName.name] || controlName.initial;
          this.formGroup.addControl('versante_'+controlName.name,new FormControl(initialValue,controlName.validators));
        });
        //cod fiscale uppercase
        this.codFiscaleUppercaseSub = controlToUppercase(this.formGroup.get('versante_codiceIdentificativoUnivoco'));
      }
      this.setUserData('versante',this.versante);

      //initialize step editable state
      this.onStepChange(null);
    });

    //resest special state "spontaneo to download"
    this.spontaneoToDownloadAvviso = null;

    if(this.carrelloService.size() > 0)
      this.carrelloService.triggerRefresh();

    this.disablePayButton = false;
  }

  ngOnDestroy(){
    this.loggedUserSub?.unsubscribe();
    this.formSub?.unsubscribe();
    this.formMailValidationSub?.unsubscribe();
    this.carrelloSub?.unsubscribe();
    this.codFiscaleUppercaseSub?.unsubscribe();
    this.recaptchaService.deactivate();
  }

  get anagraficaLabel(){
    return this.formGroup.get('versante_tipoSoggetto').value=='F'?'Nome e Cognome':"Denominazione";
  }

  get anagraficaPlaceholder(){
    return this.formGroup.get('versante_tipoSoggetto').value=='F'?'Inserire nome e cognome':"Inserire denominazione";
  }
  onChangeTipoPersona(event: MatButtonToggleChange) {
    const control = this.formGroup.get('versante_codiceIdentificativoUnivoco');
    control.setValidators(this.codiceIdentificativoUnivocoDetails[event?.value].validators);
    control.setValue(control.value); //force re-validation of field
  }

  get codiceIdentificativoUnivocoLabel() {
    return this.codiceIdentificativoUnivocoDetails[this.formGroup.get('versante_tipoSoggetto').value].label;
  }

  onStepChange(event: StepperSelectionEvent){
    const newIndex = event?.selectedIndex || 0;
    if(this.logged){
      this.stepsInfo = {
        dataInsert: {
          index: 0,
          editable: newIndex<2,
          completed: newIndex>=0,
          icon: this.iconPen
        },
        dataRecap: {
          index: 1,
          editable: newIndex==2,
          completed: newIndex>=1,
          icon: this.iconCheck
        },
        pay: {
          index: 2,
          editable: false,
          completed: newIndex>=2,
          icon: this.iconEuroSign
        }
      };
    } else {
      this.stepsInfo = {
        dataInsert: {
          index: 0,
          editable: newIndex<3,
          completed: newIndex>=0,
          icon: this.iconPen
        },
        mailVerify: {
          index: 1,
          editable: newIndex<3,
          completed: newIndex>=1,
          icon: this.iconEnvelope
        },
        dataRecap: {
          index: 2,
          editable: newIndex==3,
          completed: newIndex>=2,
          icon: this.iconCheck
        },
        pay: {
          index: 3,
          editable: false,
          completed: newIndex>=3,
          icon: this.iconEuroSign
        }
      };
    }
    const icons = [];
    Object.keys(this.stepsInfo).forEach(key => icons[this.stepsInfo[key].index]=this.stepsInfo[key].icon);
    this.stepsInfo.icons = icons;
  }

  private setUserData(subject: string, person: Person){
    this.formGroup.get(subject+'_indirizzo').setValue(person.indirizzo);
    this.formGroup.get(subject+'_civico').setValue(person.civico);
    this.formGroup.get(subject+'_cap').setValue(person.cap);
    this.formGroup.get(subject+'_nazione').setValue(person.nazioneId);
    if(person.nazioneId)
      this.nazioneOnChange(subject, person.nazioneId);
    this.formGroup.get(subject+'_provincia').setValue(person.provinciaId);
    const customComune = person.localita && (!person.localitaId || person.localitaId === Number.MIN_SAFE_INTEGER);
    if(person.provinciaId)
      this.provinciaOnChange(subject, person.provinciaId, customComune ? person.localita : null).subscribe(()=>{
        this.formGroup.get(subject+'_comune').setValue(customComune ? Number.MIN_SAFE_INTEGER : person.localitaId);
        if(person.localitaId && person.localitaId !== Number.MIN_SAFE_INTEGER)
          this.comuneOnChange(subject, person.localitaId);
      });
  }

  private updateCarrello(carrello: ItemCarrello[], thisRef: CarrelloComponent){
    thisRef.empty = !carrello?.length;
    console.log('empty: ', thisRef.empty);
    const newIntestatari: Map<string, {id: string, anonimo: boolean, info:Person, content:ItemCarrello[]}> = new Map();
    thisRef.importoTotale = 0;
    carrello.forEach(itemCarrello => {
      thisRef.importoTotale += itemCarrello.importo;
      if(Person.isPersonAnonimo(itemCarrello.intestatario))
        itemCarrello.intestatario = Person.normalizeAnonimoDetails(itemCarrello.intestatario);
      const idIntestatario = Person.idIntestatario(itemCarrello.intestatario);
      if(!newIntestatari.has(idIntestatario))
        newIntestatari.set(idIntestatario, {
          id: idIntestatario,
          anonimo: Person.isPersonAnonimo(itemCarrello.intestatario),
          info: itemCarrello.intestatario,
          content:[]});
      newIntestatari.get(idIntestatario).content.push(itemCarrello);
    });

    const alreadyExistingSubjects = new Set();
    for (const [controlName, control] of Object.entries(thisRef.formGroup.controls)) {
      const subject = controlName.substring(0, controlName.lastIndexOf('_'));
      if(subject!=='versante' && !newIntestatari.has(subject))
      thisRef.formGroup.removeControl(controlName);
      else
        alreadyExistingSubjects.add(subject);
    }

    newIntestatari.forEach(intestatario => {
      if(!alreadyExistingSubjects.has(intestatario.id)) {
        thisRef.controlNames.forEach(controlName => {
          thisRef.formGroup.addControl(intestatario.id+'_'+controlName,new FormControl());
        });
        thisRef.setUserData(intestatario.id, intestatario.info);
      }
    });
    thisRef.itemCarrelloByIntestatari = [...newIntestatari.values()];
  }

  tableColumnsStep2: TableColumn[] = [
    new TableColumn('thumbEnte', null, {type:'img64', ariaLabel:'Logo'}),
    new TableColumn('deEnte', 'Beneficiario', {sortable: false}),
    new TableColumn('deTipoDovuto', 'Tipo dovuto', {sortable: false}),
    new TableColumn('causale', 'Causale del versamento', {sortable: false}),
    new TableColumn('importo','Importo', { sortable:false, pipe: CurrencyPipe, pipeArgs:['EUR', 'symbol'] } ) ];
  tableColumnsStep1: TableColumn[] = [
    ...this.tableColumnsStep2,
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Tramite il menu azioni, cliccando sui tre puntini, potrai ad esempio scaricare l\'avviso di pagamento o rimuovere il dovuto dal carrello',
    actions: [
      new TableAction(this.iconReceipt, this.downloadAvviso, this.downloadAvvisoEnabled, 'Scarica avviso'),
      new TableAction(this.iconRemoveCart, this.removeFromCarrello, () => !this.triggerEsternoAnonimo, 'Rimuovi dal carrello')
      ] } ) ];

  spontaneoToDownloadAvviso:Spontaneo = null;
  downloadAvviso(elementRef: ItemCarrello, thisRef: CarrelloComponent, eventRef: any) {
    if(thisRef.triggerEsternoAnonimo) {
      thisRef.toastrService.error('Non è possibile effettuare la stampa per di pagamenti iniziati presso ente esterno, si prega di procedere con il pagamento','Stampa avviso');
      return;
    }
    if(eventRef)
      eventRef.stopPropagation();

    if(ItemCarrello.isSpontaneo(elementRef)){
      thisRef.setOptionalDataFromForm(elementRef.intestatario);

      if(_.isEmpty(_.trim(elementRef.intestatario.email))){
        const versanteEmail = thisRef.logged ? thisRef.versante.email : thisRef.formGroup.get('versante_email').value;
        if(_.isEmpty(_.trim(versanteEmail))){
          thisRef.toastrService.error('Poichè l\'intestatario del dovuto non ha un indirizzo email, è necessario indicare un indirizzo email per il soggetto versante','Validazione mail');
          return;
        } else {
          elementRef.versanteEmail = versanteEmail;
        }
      }
      //manage generate avviso
      if(thisRef.logged){
        //generate avviso
        thisRef.downloadAvvisoForSpontaneo(elementRef, thisRef);
      } else {
        ['versante_anagrafica','versante_tipoSoggetto','versante_codiceIdentificativoUnivoco', 'versante_email']
          .forEach(fieldName => thisRef.formGroup.get(fieldName).disable() );
        const isInvalid = thisRef.formGroup.invalid;
        ['versante_anagrafica','versante_tipoSoggetto','versante_codiceIdentificativoUnivoco', 'versante_email']
          .forEach(fieldName => thisRef.formGroup.get(fieldName).enable() );
        thisRef.versanteCfAnonimoOnChange(thisRef.formGroup.get('versante_cfAnonimo').value);
        if(isInvalid){
          thisRef.toastrService.error('È necessario compilare i dati obbligatori del form per procedere','Scarica avviso',{disableTimeOut: true});
        } else {
          thisRef.spontaneoToDownloadAvviso = elementRef;
          thisRef.onDataInsert(thisRef.stepper);
        }
      }
    }
    if(ItemCarrello.isDebito(elementRef)){
      thisRef.recaptchaService.submitWithRecaptchaHandling<any>('downloadAvviso', 
        recaptchaToken => thisRef.avvisoService.downloadAvvisoAnonymous(elementRef, recaptchaToken),
        () => thisRef.avvisoService.downloadAvviso(elementRef)
      ).subscribe(response => {
        const contentDisposition = response.headers.get('content-disposition');
        const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? 'mypay4_avviso_'+elementRef.id+'.pdf';
        const contentType = response.headers.get('content-type') ?? 'application/pdf; charset=utf-8';
        const blob:any = new Blob([response.body], { type: contentType });
        thisRef.fileSaverService.save(blob, fileName);
      }, manageError('Errore scaricando l\'avviso di pagamento', thisRef.toastrService) );
    }
  }

  downloadAvvisoEnabled(elementRef: ItemCarrello, thisRef: CarrelloComponent) {
    return !thisRef.triggerEsternoAnonimo &&
      (ItemCarrello.isDebito(elementRef) && elementRef.codStato === 'INSERIMENTO_DOVUTO' && !_.isNil(elementRef.codIuv) || ItemCarrello.isSpontaneo(elementRef));
  }

  downloadAvvisoForSpontaneo(spontaneo: Spontaneo, thisRef: CarrelloComponent) {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);

    thisRef.recaptchaService.submitWithRecaptchaHandling<Debito>('prepareAvviso', 
      recaptchaToken => {
        spontaneo.mailValidationToken = thisRef.mailValidationResponse.validatedToken;
        return thisRef.spontaneoService.prepareAvvisoAnonymous(spontaneo, recaptchaToken);
      },
      () => thisRef.spontaneoService.prepareAvviso(spontaneo)
    ).subscribe(debito => {
      Debito.setDetails(debito);
      thisRef.carrelloService.remove(spontaneo);
      thisRef.carrelloService.add(debito);
      thisRef.recaptchaService.submitWithRecaptchaHandling<any>('downloadAvviso',
        recaptchaToken => thisRef.avvisoService.downloadAvvisoAnonymous(debito, recaptchaToken),
        () => thisRef.avvisoService.downloadAvviso(debito)
      ).subscribe(response => {
        thisRef.overlaySpinnerService.detach(spinner);
        const contentDisposition = response.headers.get('content-disposition');
        const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? 'mypay4_avviso_'+debito.id+'.pdf';
        const contentType = response.headers.get('content-type') ?? 'application/pdf; charset=utf-8';
        const blob:any = new Blob([response.body], { type: contentType });
        thisRef.fileSaverService.save(blob, fileName);
		  }, manageError('Errore scaricando l\'avviso di pagamento', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
    }, manageError('Errore creando l\'avviso di pagamento', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) )
  }

  setOptionalDataFromForm(person: Person, personId: string = null){
    if(!personId)
      personId = Person.idIntestatario(person);
    person.indirizzo = this.formGroup.get(personId + '_indirizzo').value;
    person.civico = this.formGroup.get(personId + '_civico').value;
    person.cap = this.formGroup.get(personId + '_cap').value;
    person.nazioneId = this.formGroup.get(personId + '_nazione').value;
    person.provinciaId = this.formGroup.get(personId + '_provincia').value;
    person.localitaId = this.formGroup.get(personId + '_comune').value;
    if(person.localitaId === Number.MIN_SAFE_INTEGER)
      delete person.localitaId;
  }

  removeFromCarrello(elementRef: ItemCarrello, thisRef: CarrelloComponent, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    const removeError = thisRef.carrelloService.remove(elementRef);
    if(removeError)
      thisRef.toastrService.error(removeError,'Errore rimuovendo dal carrello',{disableTimeOut: true});
  else
    thisRef.toastrService.info('Elemento rimosso dal carrello');
  }

  onImportUpdate(stepper: MatStepper) {
    const items = this.itemCarrelloByIntestatari.map(obj => obj.content).reduce((a, b) => a.concat(b), []);
    if (items.find(item => item.urlNotificaPnd)) {
      this.dialog.open(ConfirmDialogComponent, { autoFocus: false, data: { message: 'Attenzione: l\'importo potrebbe variare in base alle notifiche ricevute' } })
        .afterClosed().pipe(first()).subscribe(result => {
          if (result === 'true') {
            //aggiorno importi
            const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
            let request = items.filter(item => item.urlNotificaPnd)
              .map(item => this.recaptchaService.submitWithRecaptchaHandling<any>('updateImporto',
                recaptchaToken => this.carrelloService.updateBasketPndAnonymous(item.codIpaEnte, item.codTipoDovuto, item.codIuv, recaptchaToken),
                () => this.carrelloService.updateBasketPnd(item.codIpaEnte, item.codTipoDovuto, item.codIuv)
              ));
            forkJoin(request).subscribe((response: Attualizzazione[]) => {
              items.filter(item => item.urlNotificaPnd).forEach((item, index) => {
                if (item.importo !== response[index].importoPosizione) {
                  item.importoAtt = true;
                  item.importo = response[index].importoPosizione;
                  if (response[index].bilancio) {
                    item.bilancio = response[index].bilancio;
                  }
                }
              });
              this.importoTotale = 0;
              items.forEach(item => this.importoTotale += item.importo)
              this.overlaySpinnerService.detach(spinner);
              this.onDataInsert(stepper);
            }, manageError('Errore aggiornando l\'importo', this.toastrService, () => { this.overlaySpinnerService.detach(spinner) }))
          } else
            return;
        })
    } else {
      this.onDataInsert(stepper);
    }
  }

  onDataInsert(stepper: MatStepper){
    if(!this.logged){
      this.versante.anagrafica = this.formGroup.get('versante_anagrafica').value;
      this.versante.tipoIdentificativoUnivoco = this.formGroup.get('versante_tipoSoggetto').value;
      if(this.formGroup.get('versante_cfAnonimo').value)
        this.versante.codiceIdentificativoUnivoco = Person.CF_ANONIMO;
      else
        this.versante.codiceIdentificativoUnivoco = this.formGroup.get('versante_codiceIdentificativoUnivoco').value;
      this.versante.email = this.formGroup.get('versante_email').value;
      this.setOptionalDataFromForm(this.versante, 'versante');

      this.validationEmail = [this.spontaneoToDownloadAvviso?.intestatario.email, this.versante.email].find(x => !_.isEmpty(_.trim(x)));

      if(!this.validationEmail){
        this.toastrService.error('Poichè l\'intestatario del dovuto non ha un indirizzo email, è necessario indicare un indirizzo email per il soggetto versante','Validazione mail');
        return;
      }

      if(this.mailValidationResponse?.validationStatus==='VALID' &&
          this.mailValidationResponse?.expiration > DateTime.now() &&
          this.mailValidationResponse?.email===this.validationEmail){
        //mail already validated, skip this step
        console.log('mail already validated, skip the mail validation step');
        if(this.spontaneoToDownloadAvviso){
          //generate avviso
          const spontaneoToDownloadAvviso = this.spontaneoToDownloadAvviso;
          this.spontaneoToDownloadAvviso = null;
          this.downloadAvvisoForSpontaneo(spontaneoToDownloadAvviso, this);
        } else {
          stepper.next();
          setTimeout(()=>{stepper.next()});
          this.toastrService.info('L\'indirizzo email "'+this.validationEmail+'" risulta già correttamente validato','Validazione mail');
        }
      } else {
        //init mail verification
        this.recaptchaService.submitWithRecaptchaHandling<MailValidationRequest>('requestMailValidation', 
          recaptchaToken => this.mailValidationService.requestMailValidation(this.validationEmail, recaptchaToken)
        ).subscribe(mailValidationRequest => {
          this.mailValidationRequest = mailValidationRequest;
          this.validPin = mailValidationRequest.pin;
        }, manageError('Errore validando l\'email', this.toastrService));
        stepper.next();
      }
    } else {
      this.setOptionalDataFromForm(this.versante, 'versante');

      stepper.next();
    }
  }

  mailValidationRequest: MailValidationRequest;
  mailValidationResponse: MailValidationResponse;
  validPin: string; //remove when real mail sent

  onMailValidationBack(stepper: MatStepper){
    this.spontaneoToDownloadAvviso = null;
    this.stepper.previous();
  }

  onMailValidation(stepper: MatStepper){
    //verify mail
    const pin = this.formGroupMailValidation.get('verificationCode').value;
    let mailValidationFun;
    if(this.mailValidationRequest){
      this.mailValidationRequest.pin = pin;
      //validate code on server
      mailValidationFun = this.recaptchaService.submitWithRecaptchaHandling<MailValidationResponse>('verifyMailValidation', 
        recaptchaToken => this.mailValidationService.verifyMailValidation(this.mailValidationRequest, recaptchaToken)
      );
    } else {
      //case when we had problem sending mail validation request: in this case use
      // a fake response with INVALID validation status
      const mailValidationResponse = new MailValidationResponse();
      mailValidationResponse.validationStatus = 'INVALID';
      mailValidationFun = of(mailValidationResponse);
    }
    mailValidationFun.subscribe(mailValidationResponse => {
      let errorMessage;
      switch (mailValidationResponse.validationStatus) {
        case 'WRONG_PIN': errorMessage = 'Codice verifica errato'; break;
        case 'INVALID': errorMessage = 'Errore generico verificando la mail'; break;
        case 'EXPIRED': errorMessage = 'Codice verifica scaduto'; break;
        case 'CAPTCHA': errorMessage = 'Errore verifica captcha'; break;
        case 'VALID': break;
        default: errorMessage = 'Errore di sistema';
      }
      if(errorMessage) {
        this.toastrService.error(errorMessage,'Errore validando l\'email',{disableTimeOut: true});
        this.formGroupMailValidation.get('verificationCode').setErrors({invalid: true});
        this.formMailValidationValidationFun({verificationCode:pin});
      } else {
        this.mailValidationResponse = mailValidationResponse;
        this.formGroupMailValidation.get('verificationCode').setErrors(null);
        this.formMailValidationValidationFun({verificationCode:pin});
        if(this.spontaneoToDownloadAvviso){
          //generate avviso
          const spontaneoToDownloadAvviso = this.spontaneoToDownloadAvviso;
          this.spontaneoToDownloadAvviso = null;
          this.downloadAvvisoForSpontaneo(spontaneoToDownloadAvviso, this);
          //go to page where generate avviso request started
          stepper.previous();
        } else {
          stepper.next();
        }
      }
    }, manageError('Errore validando l\'email', this.toastrService));
  }

  onBackToDataInsert(stepper: MatStepper){
    stepper.steps.first.select();
  }

  emptyCarrello(){
    this.carrelloService.empty();
    this.versante = null;
    this.spontaneoToDownloadAvviso = null;
    if(this.backUrl){
      setTimeout(() => {
        console.log('redirecting to url: '+this.backUrl);
        window.location.href = this.backUrl;
      }, 100);
    }else{
      this.router.navigate(['cards']);
    }
  }


  nazioni: Nazione[]=[];
  province: Provincia[]=[];
  _comuni: Comune[][] = [];

  getDescNazione(subject:string) {
    const nazioneId = this.formGroup.get(subject+'_nazione')?.value;
    if(!nazioneId) return;
    return this.nazioni?.find(nazione => nazione.nazioneId === nazioneId)?.nomeNazione;
  }

  getDescProvincia(subject:string) {
    const provinciaId = this.formGroup.get(subject+'_provincia')?.value;
    if(!provinciaId) return;
    return this.province?.find(provincia => provincia.provinciaId === provinciaId)?.provincia;
  }

  getDescLocalita(subject:string) {
    const comuneId = this.formGroup.get(subject+'_comune')?.value;
    if(!comuneId) return;
    return this._comuni?.[subject]?.find(comune => comune.comuneId === comuneId)?.comune;
  }

  private loadNazioni() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.locationService.getNazioni().subscribe(
      nazioni => {
       this.overlaySpinnerService.detach(spinner);
        this.nazioni = nazioni;
      }, manageError('Errore recuperando le nazioni', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  private loadProvince() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.locationService.getProvince().subscribe(
      province => {
       this.overlaySpinnerService.detach(spinner);
        this.province = province;
      }, manageError('Errore recuperando le province', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  private loadComuni(subject:string, provincia: Provincia, customComune?: string):Observable<void> {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    const resp = new Subject<void>();
    this.locationService.getComuni(provincia).pipe(delay(0)).subscribe(
      comuni => {
        this.overlaySpinnerService.detach(spinner);
        this._comuni[subject] = comuni;
        if(customComune){
          const c = new Comune(Number.MIN_SAFE_INTEGER);
          c.comune = customComune;
          c.provinciaId = provincia.provinciaId;
          this._comuni[subject]?.unshift(c);
        }
        resp.next();
        resp.complete();
      }, manageError('Errore recuperando i comuni', this.toastrService, () => {resp.complete(); this.overlaySpinnerService.detach(spinner)}) );
    return resp;
  }

  nazioneOnChange(subject:string, nazioneId: number) {
    this.formGroup.get(subject+'_provincia').setValue(null);
    this.formGroup.get(subject+'_comune').setValue(null);
    this.formGroup.get(subject+'_provincia').disable();
    this.formGroup.get(subject+'_comune').disable();
    if (nazioneId === 0) {
      this.formGroup.get(subject+'_nazione').setValue(null);
    } else if (nazioneId === 1) {
      this.formGroup.get(subject+'_provincia').enable();
    }
  }

  provinciaOnChange(subject:string, provinciaId: number, customComune?: string):Observable<void> {
    this.formGroup.get(subject+'_comune').setValue(null);
    this._comuni[subject] = null;
    if (provinciaId === 0) {
      this.formGroup.get(subject+'_comune').disable();
      this.formGroup.get(subject+'_provincia').setValue(null);
    } else {
      this.formGroup.get(subject+'_comune').enable();
    }
    return this.loadComuni(subject, new Provincia(provinciaId), customComune);
  }

  comuneOnChange(subject:string, comuneId: number) {
    if (comuneId === 0) {
      this.formGroup.get(subject+'_comune').setValue(null);
    }
  }

  comuni(subject:string) {
    return this._comuni?.[subject];
  }

  checkout(overrideReplicaPaymentCheck: boolean = false){
    this.disablePayButton = true;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    const basket = new Carrello();
    let items = this.itemCarrelloByIntestatari.map(obj => obj.content).reduce((a,b)=> a.concat(b),[]);
    basket.totalAmount = this.importoTotale;
    basket.idSession = this.idSession;
    basket.backUrlInviaEsito = this.backUrl;
    basket.items=[];
    basket.versante = this.versante;
    basket.tipoCarrello = this.tipoCarrello;
    basket.dovutiEntiSecondari =this.dovutiEntiSecondari;
    items.forEach(item => {
      this.setOptionalDataFromForm(item.intestatario);
      //clone and remove useless fields
      const itemToAdd = _.cloneDeep(item);
      delete itemToAdd.details;
      delete itemToAdd.thumbEnte;
      Object.keys(itemToAdd).filter(key => key.startsWith('__cellClickable')).forEach(key => delete itemToAdd[key]);
      basket.items.push(itemToAdd);
    });

    if(!this.userService.isLogged())
      basket.mailValidationToken = this.mailValidationResponse.validatedToken;
    this.recaptchaService.submitWithRecaptchaHandling<Esito>('checkoutCarrello', 
      recaptchaToken => this.carrelloService.processOrderAnonymous(basket, overrideReplicaPaymentCheck, recaptchaToken),
      () => this.carrelloService.processOrder(basket, overrideReplicaPaymentCheck)
    ).subscribe( data => {
      if(data.esito === 'OK'){
        this.stepper.next();
        this.paymentUrl = data.url;
        this.overlaySpinnerService.detach(spinner);
        console.log('redirecting to pagopa');
        setTimeout(() => {
          window.location.href = this.paymentUrl;
        }, 1000);
      } else if(data.esito === 'KO_REPLICA'){
        this.overlaySpinnerService.detach(spinner);
        let msg:string;
        if(data.returnMsg === 'REPLICA_DOVUTO')
          msg = 'ATTENZIONE: hai un altro pagamento con la stessa causale in attesa dell\'esito.';
        else
          msg = 'ATTENZIONE: hai già eseguito un pagamento con la stessa causale nelle ultime 24 ore.';
        msg += '\nConfermi di voler procedere con il pagamento?';
        this.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
        .afterClosed().pipe(first()).subscribe(result => {
          if(result==="true")
            this.checkout(true);
          else
            this.disablePayButton = false;
        });
      } else if(data.esito === 'KO_MANAGED'){
        manageError("Errore nell'invio della richiesta di pagamento",
          this.toastrService, () => {this.overlaySpinnerService.detach(spinner)})
        (ProcessHTTPMsgService.trimMessage(data.returnMsg));
        this.disablePayButton = false;
      } else {
        manageError("Errore nell'invio della richiesta di pagamento",
          this.toastrService, () => {this.overlaySpinnerService.detach(spinner)})
        (ProcessHTTPMsgService.trimMessage("Errore di sistema: ", data.returnMsg, data.errorUid));
        this.disablePayButton = false;
      }

    }, manageError("Errore nell'invio della richiesta di pagamento", this.toastrService, () => {this.overlaySpinnerService.detach(spinner); this.disablePayButton=false;}) );
  }

  versanteCfAnonimoOnChange(checked: boolean){
    const field = this.formGroup.get('versante_codiceIdentificativoUnivoco');
    if(checked){
      this.formGroup.get('versante_tipoSoggetto').setValue('F');
      setTimeout(()=>field.disable(),0);
    } else
      field.enable();
  }

  externalDownloadAvviso() {
    let msg = 'Poiché è presente più di un avviso nel carrello, la stampa dei singoli avvisi va effettuata dal pulsante azioni, cliccando sui <b>tre puntini</b> a fianco del singolo pagamento.'

    if(this.triggerEsternoAnonimo)
      this.dialog.open(ConfirmDialogComponent, { autoFocus: false, data:
        { message: 'Non è possibile effettuare la stampa per di pagamenti iniziati presso ente esterno, si prega di procedere con il pagamento', invalid: true, cancelLabel: 'OK', titleLabel: 'Stampa avviso' } });

    if (this.itemCarrelloByIntestatari.length == 0)
      return;

    if (this.itemCarrelloByIntestatari.length == 1) {

      let dovuti: ItemCarrello[] = this.itemCarrelloByIntestatari[0].content
      if (dovuti.length == 1) {

        let elementRef: ItemCarrello = dovuti[0];

        this.downloadAvviso(elementRef, this, null);
      }
      else {
        this.dialog.open(ConfirmDialogComponent, { autoFocus: false, data: { message: msg, invalid: true, cancelLabel: 'OK', titleLabel: 'Stampa avviso' } });
      }
    } else {
      this.dialog.open(ConfirmDialogComponent, { autoFocus: false, data: { message: msg, invalid: true, cancelLabel: 'OK', titleLabel: 'Stampa avviso' } });
    }

  }

}
