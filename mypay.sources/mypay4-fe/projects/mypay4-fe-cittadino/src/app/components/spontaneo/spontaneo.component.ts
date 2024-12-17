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
  CookieService, Ente, FieldBean, manageError, OverlaySpinnerService, StorageService, TipoDovuto, UserService,
  validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { combineLatest, Observable, Subscription } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

import {
  Component, ElementRef, Input, OnDestroy, OnInit, Renderer2, ViewChild
} from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { faTags } from '@fortawesome/free-solid-svg-icons';

import * as _ from 'lodash';
import { ExtAppSpontaneousPayment } from '../../model/ext-app';
import { SpontaneoForm } from '../../model/spontaneo-form';
import { RecaptchaService } from '../../services/recaptcha.service';
import { SpontaneoService } from '../../services/spontaneo.service';

@Component({
  selector: 'app-spontaneo',
  templateUrl: './spontaneo.component.html',
  styleUrls: ['./spontaneo.component.scss']
})
export class SpontaneoComponent implements OnInit, OnDestroy, WithTitle {

  get titleLabel(){ return "Altre tipologie di pagamento" }
  get titleIcon(){ return faTags }

  @ViewChild('enteTipoDirective') enteTipoDirective;
  @ViewChild('insertFormDirective') insertFormDirective;

  @Input("cod-ipa-ente")
  private codIpaEnteInput: string;

  hidden: boolean = true;

  enteOptions: Ente[];
  enteFilteredOptions: Observable<Ente[]>;

  tipoDovutoOptionsMap: Map<String, TipoDovuto[]>;
  tipoDovutoOptions: TipoDovuto[];
  tipoDovutoFilteredOptions: Observable<TipoDovuto[]>;
  previousEnte: Ente;

  codIpaEnteLanding: string;
  private codTipoDovutoLanding: string;
  extAppFlow: boolean = false;
  private extAppToken: string;
  private extAppSpontaneousPayment: ExtAppSpontaneousPayment;
  
  hasSearched: boolean = false;
  blockingError: boolean = false;

  enteTipo: FormGroup;
  enteTipoErrors = {};

  private formChangesSub: Subscription;
  private enteTipoChangeSub: Subscription;
  private loggedUserAndCookieConsentSub: Subscription;

  hasConsent: boolean = true;

  constructor(
    private formBuilder: FormBuilder,
    private spontaneoService: SpontaneoService,
    private userService: UserService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private renderer: Renderer2,
    private recaptchaService: RecaptchaService,
    private cookieService: CookieService,
    private storageService: StorageService,
    router: Router,
  ) {
    this.enteTipo = this.formBuilder.group({
      ente: [null, [Validators.required, this.enteValidator]],
      tipoDovuto: [null, [Validators.required, this.tipoDovutoValidator]],
    });

    this.formChangesSub = this.enteTipo.valueChanges.subscribe(validateFormFun(this.enteTipo, this.enteTipoErrors));

    this.codIpaEnteLanding = router.getCurrentNavigation()?.extras?.state?.codIpaEnte;
    this.codTipoDovutoLanding = router.getCurrentNavigation()?.extras?.state?.codTipoDovuto;
    this.extAppToken = router.getCurrentNavigation()?.extras?.state?.extAppToken;
    this.extAppFlow = ! _.isNil(router.getCurrentNavigation()?.extras?.state?.extAppToken);
  }

  private extAppError(msg: string){
    this.blockingError = true;
    if(!this.extAppSpontaneousPayment.callbackUrl){
      this.toastrService.error('Errore interno [callbackUrl null]');
      return;
    }
    const redirectUrl = new URL(this.extAppSpontaneousPayment.callbackUrl);
    redirectUrl.searchParams.append('esito', 'ERR');
    redirectUrl.searchParams.append('msg', msg);
    setTimeout(() => {
      console.log('redirecting to url: '+redirectUrl);
      window.location.href = redirectUrl.toString();
    }, 100);
  }

  get placeholderTipoDovuto() {
    return this.enteTipo.get('ente').value?null:"Selezionare un ente";
  }
  placeholderEnte:string;


  ngOnInit(): void {
    if(this.codIpaEnteInput){
      console.log('setting codIpaEnte to:'+this.codIpaEnteInput);
      this.codIpaEnteLanding = this.codIpaEnteInput;
    }

    //init recaptcha if anonymous
    this.loggedUserAndCookieConsentSub = combineLatest([this.userService.getLoggedUserObs(), this.cookieService.getConsentStateObs()])
    .subscribe( ([loggedUser,cookieConsent]) => {
      if(loggedUser){
        this.cookieService.unsetMissingNeededConsent();
        this.hasConsent = true;
        this.viewInit();
      } else {
        console.log("cookie consent state on spontaneo:"+JSON.stringify(cookieConsent));
        if(!this.recaptchaService.isEnabled() || cookieConsent.cookieAll || cookieConsent.cookieThirdParty){
          this.recaptchaService.init(this.renderer);
          this.cookieService.unsetMissingNeededConsent();

          if(this.extAppFlow){
            this.storageService.getObject<ExtAppSpontaneousPayment>(this.extAppToken).subscribe(extAppSpontaneousPayment => {
              extAppSpontaneousPayment.token = this.extAppToken;
              this.extAppSpontaneousPayment = extAppSpontaneousPayment;
              this.codIpaEnteLanding = extAppSpontaneousPayment.codiceEnte;
              this.codTipoDovutoLanding = extAppSpontaneousPayment.codiceTipoDovuto;
              if(!this.codIpaEnteLanding)
                this.extAppError('ente null');
              else if(!this.codTipoDovutoLanding)
                this.extAppError('tipoDovuto null');
              this.hidden = false;
              this.onSubmit(this.codIpaEnteLanding, this.codTipoDovutoLanding);
            }, manageError('Errore recuperando i dati di pagamento', this.toastrService, ()=>{this.blockingError=true}));
          } else {
            this.viewInit();
          }
          this.hasConsent = true;
        } else {
          this.cookieService.setMissingNeededConsent();
          this.hasConsent = false;
          this.hidden = true;
        }
      }
    });
  }

  private viewInit():void {
    //load enti from backend
    this.enteTipo.get('ente').disable();
    this.placeholderEnte = "Attendere - Caricamento elenco enti in corso..";
    let downloadEntiFun;
    if (this.codIpaEnteLanding) {
      downloadEntiFun = this.spontaneoService.getEnteSpontaneoByCodIpa(this.codIpaEnteLanding);
    } else {
      downloadEntiFun = this.spontaneoService.getAllEntiSpontanei();
    }
    downloadEntiFun.subscribe(enti => {
      this.enteTipo.get('ente').enable();
      this.placeholderEnte = null;
      this.enteOptions = enti;
      //init autocomplete feature of ente field
      this.enteFilteredOptions = this.enteTipo.get('ente').valueChanges
        .pipe(
          startWith(''),
          map(value => typeof value === 'string' || !value ? value : value.deNomeEnte),
          map(deNomeEnte => deNomeEnte ? this._enteFilter(deNomeEnte) : this.enteOptions.slice())
        );
      this.hidden = false;

      //Set ente, tipoDovuto by the query params if exist.
      if (this.codIpaEnteLanding) {
        const ente = this.enteOptions.find(e => e.codIpaEnte === this.codIpaEnteLanding);
        if (ente) {
          this.enteTipo.get('ente').setValue(ente);
          if (!this.tipoDovutoOptionsMap.has(ente.codIpaEnte)) {
            this.spontaneoService.getListTipoDovutoByEnte(ente).subscribe(tipiDovuto => {
              this.tipoDovutoOptionsMap.set(ente.codIpaEnte, tipiDovuto);
              this.tipoDovutoOptions = this.tipoDovutoOptionsMap.get(ente.codIpaEnte);
              if (this.codTipoDovutoLanding) {
                const tipoDovuto = this.tipoDovutoOptions.find(tipo => tipo.codTipo === this.codTipoDovutoLanding);
                if (tipoDovuto) {
                  this.enteTipo.get('tipoDovuto').setValue(tipoDovuto);
                  this.onSubmit();                
                }
              }
            }, manageError('Errore caricando l\'elenco dei tipi dovuto', this.toastrService, ()=>{this.blockingError=true}) );
          }
        }
      }
    }, 
    error => {
      if(this.codIpaEnteLanding && _.startsWith(error, 'Risorsa non trovata')){
        this.toastrService.info('Questo ente non ha alcun servizio di pagamento spontaneo attivo.');
        this.hidden = true;
      } else
        manageError('Errore caricando l\'elenco degli enti', this.toastrService, ()=>{this.blockingError=true})(error);
    } );

    this.enteTipo.get('tipoDovuto').disable();
    this.tipoDovutoOptionsMap = new Map();
    this.enteTipoChangeSub = this.enteTipo.get('ente').valueChanges.subscribe(value => {
      if(value && value.mygovEnteId){
        this.enteTipo.get('tipoDovuto').enable();
        this.enteTipo.get('tipoDovuto').setValue(null);
        if(!this.tipoDovutoOptionsMap.has(value.codIpaEnte)){
          this.spontaneoService.getListTipoDovutoByEnte(value).subscribe(tipiDovuto => {
            this.tipoDovutoOptionsMap.set(value.codIpaEnte, tipiDovuto);
            this.tipoDovutoOptions = this.tipoDovutoOptionsMap.get(value.codIpaEnte);
            this.tipoDovutoFilteredOptions = this.enteTipo.get('tipoDovuto').valueChanges
            .pipe(
              startWith(''),
              map(value => typeof value === 'string' || !value ? value : value.deTipo),
              map(deTipoDovuto => deTipoDovuto ? this._tipoDovutoFilter(deTipoDovuto) : this.tipoDovutoOptions.slice())
            );
          }, manageError('Errore caricando l\'elenco dei tipi dovuto', this.toastrService, ()=>{this.blockingError=true}) );
        } else {
          this.tipoDovutoOptions = this.tipoDovutoOptionsMap.get(value.codIpaEnte);
          this.tipoDovutoFilteredOptions = this.enteTipo.get('tipoDovuto').valueChanges
          .pipe(
            startWith(''),
            map(value => typeof value === 'string' || !value ? value : value.deTipo),
            map(deTipoDovuto => deTipoDovuto ? this._tipoDovutoFilter(deTipoDovuto) : this.tipoDovutoOptions.slice())
          );
        }
      } else {
        this.tipoDovutoOptions = [];
        this.enteTipo.get('tipoDovuto').disable();
        this.enteTipo.get('tipoDovuto').setValue(null);
      }
    });
  }

  ngOnDestroy():void {
    this.formChangesSub?.unsubscribe();
    this.enteTipoChangeSub?.unsubscribe();
    this.loggedUserAndCookieConsentSub?.unsubscribe();
    this.recaptchaService.deactivate();
    this.cookieService.unsetMissingNeededConsent();
  }

  enteDisplayFn(ente: Ente): string {
    return ente ? ente.deNomeEnte : '';
  }

  private _enteFilter(name: string): Ente[] {
    const filterValue = name.toLowerCase();
    return this.enteOptions.filter(option => option.deNomeEnte.toLowerCase().indexOf(filterValue) !== -1);
  }

  private enteValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.mygovEnteId != null ) ? null : {'invalid': true};
  };

  tipoDovutoDisplayFn(tipoDovuto: TipoDovuto): string {
    return tipoDovuto ? tipoDovuto.deTipo : '';
  }

  private _tipoDovutoFilter(name: string): TipoDovuto[] {
    const filterValue = name.toLowerCase();
    return this.tipoDovutoOptions.filter(option => option.deTipo.toLowerCase().indexOf(filterValue) !== -1);
  }

  private tipoDovutoValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.mygovEnteTipoDovutoId != null ) ? null : {'invalid': true};
  };

  onReset(){
    this.enteTipo.get("tipoDovuto").setValue(null);
    this.hasSearched = false;
  }

  onSubmit(codEnte?: string, codTipoDovuto?: string) {

    const i = this.enteTipo.value;
    let ente: Ente, tipoDovuto: TipoDovuto;
    if(codTipoDovuto){
      ente = new Ente();
      ente.codIpaEnte = codEnte;
      tipoDovuto = new TipoDovuto();
      tipoDovuto.codTipo = codTipoDovuto;
    } else {
      ente = i.ente;
      tipoDovuto = i.tipoDovuto;
      let redirectLink = tipoDovuto.deUrlPagamentoDovuto?.trim();
      if (redirectLink && redirectLink.length > 0) {
        window.open(redirectLink);
        return;
      }
    }
   
    this.hasSearched = true;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);

    this.recaptchaService.submitWithRecaptchaHandling<SpontaneoForm>('initializeForm', 
      recaptchaToken => this.spontaneoService.initializeFormAnonymous(ente, tipoDovuto, recaptchaToken),
      () => this.spontaneoService.initializeForm(ente, tipoDovuto)
    ).subscribe( data => {
      this.currentEnte = ente;
      this.currentTipoDovuto = tipoDovuto;
      this.fieldBeans = data.fieldBeans;
      this.importoPrefissato = data.importo ? Number(data.importo) : null;
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore inizializzando il form', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );

  }

  /* -- Members for spontaneo-dynamo(my-dynamo) -- */
  currentTipoDovuto: TipoDovuto;
  currentEnte: Ente;
  fieldBeans: FieldBean[];
  importoPrefissato: number;

  public isLoaded: boolean;


  /* !-- Members for spontaneo-dynamo(my-dynamo) -- */

}

