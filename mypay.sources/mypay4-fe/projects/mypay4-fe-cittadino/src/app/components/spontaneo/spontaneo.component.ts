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
    CookieService, Ente, FieldBean, manageError, OverlaySpinnerService, TipoDovuto, UserService,
    validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { combineLatest, Observable, Subscription } from 'rxjs';
import { flatMap, map, startWith } from 'rxjs/operators';

import {
    Component, ElementRef, Input, OnDestroy, OnInit, Renderer2, ViewChild
} from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { faTags } from '@fortawesome/free-solid-svg-icons';

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

  enteOptions: Ente[];
  enteFilteredOptions: Observable<Ente[]>;

  tipoDovutoOptionsMap: Map<String, TipoDovuto[]>;
  tipoDovutoOptions: TipoDovuto[];
  tipoDovutoFilteredOptions: Observable<TipoDovuto[]>;
  previousEnte: Ente;

  codIpaEnteLanding: string;
  private codTipoDovutoLanding: string;

  hasSearched: boolean = false;
  blockingError: boolean = false;

  enteTipo: FormGroup;
  enteTipoErrors = {};
  private logged:boolean;

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
    router: Router,
    private route:ActivatedRoute,
  ) {
    this.enteTipo = this.formBuilder.group({
      ente: [null, [Validators.required, this.enteValidator]],
      tipoDovuto: [null, [Validators.required, this.tipoDovutoValidator]],
    });

    this.formChangesSub = this.enteTipo.valueChanges.subscribe(validateFormFun(this.enteTipo, this.enteTipoErrors));

    this.codIpaEnteLanding = router.getCurrentNavigation()?.extras?.state?.codIpaEnte;
    this.codTipoDovutoLanding = router.getCurrentNavigation()?.extras?.state?.codTipoDovuto;
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
        this.logged = true;
        this.cookieService.unsetMissingNeededConsent();
        this.hasConsent = true;
        this.viewInit();
      } else {
        this.logged = false;
        console.log("cookie consent state on spontaneo:"+JSON.stringify(cookieConsent));
        if(cookieConsent.cookieAll || cookieConsent.cookieThirdParty){
          this.recaptchaService.init(this.renderer);
          this.cookieService.unsetMissingNeededConsent();
          this.viewInit();
          this.hasConsent = true;
        } else {
          this.cookieService.setMissingNeededConsent();
          this.hasConsent = false;
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
                  //timeout needed to allow for complete form initialization
                  this.onSubmit();
                }
              }
            }, manageError('Errore caricando l\'elenco dei tipi dovuto', this.toastrService, ()=>{this.blockingError=true}) );
          }
        }
      }
    }, manageError('Errore caricando l\'elenco degli enti', this.toastrService, ()=>{this.blockingError=true}) );

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

  onSubmit() {

    if(!this.logged){
      if(!this.recaptchaService.isActive()){
        this.toastrService.error
      }
    }


    const i = this.enteTipo.value;
    let redirectLink = (i.tipoDovuto as TipoDovuto).deUrlPagamentoDovuto?.trim();
    if (redirectLink && redirectLink.length > 0) {
      window.open(redirectLink);
    } else {
      this.hasSearched = true;
      const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
      let initializeFormFun;
      if(this.logged)
        initializeFormFun = this.spontaneoService.initializeForm(i.ente, i.tipoDovuto);
      else
        initializeFormFun = this.recaptchaService.submitToken('initializeForm').pipe(
          flatMap(token => this.spontaneoService.initializeFormAnonymous(i.ente, i.tipoDovuto, token))
        );

      initializeFormFun.subscribe(data => {
        this.currentEnte = i.ente as Ente;
        this.currentTipoDovuto = i.tipoDovuto as TipoDovuto;
        this.fieldBeans = data.fieldBeans;
        this.importoPrefissato = data.importo ? Number(data.importo) : null;
        this.overlaySpinnerService.detach(spinner);
      }, manageError('Errore inizializzando il form', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
    }
  }

  /* -- Members for spontaneo-dynamo(my-dynamo) -- */
  currentTipoDovuto: TipoDovuto;
  currentEnte: Ente;
  fieldBeans: FieldBean[];
  importoPrefissato: number;

  public isLoaded: boolean;


  /* !-- Members for spontaneo-dynamo(my-dynamo) -- */

}

