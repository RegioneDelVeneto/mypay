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
  ApiInvokerService, blank2Null, Comune, formettedAmountToNumber, formettedAmountToNumberString,
  LocationService, manageError, MyPayBreadcrumbsService, Nazione, numberToFormattedAmount,
  OverlaySpinnerService, PageStateService, PATTERNS, Provincia, TipoDovuto, validateFormFun,
  WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { combineLatest, Observable, Subscription } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

import { Location } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatAutocomplete } from '@angular/material/autocomplete';
import { ActivatedRoute, Router } from '@angular/router';
import { faClone, faReceipt } from '@fortawesome/free-solid-svg-icons';

import { Dovuto } from '../../model/dovuto';
import { DovutoMultibeneficiario } from '../../model/dovuto-multibeneficiario';
import { EnteExtended } from '../../model/ente-extended';
import { DovutoService } from '../../services/dovuto.service';
import { EnteService } from '../../services/ente.service';

@Component({
  selector: 'app-dovuti-details',
  templateUrl: './dovuti-details.component.html',
  styleUrls: ['./dovuti-details.component.scss']
})
export class DovutiDetailsComponent implements OnInit, OnDestroy, WithTitle {

  get titleLabel(){ return this.pageTitle ?? "Dettaglio dovuto" }
  get titleIcon(){ return faClone }

  iconReceipt = faReceipt;

  @ViewChild('sForm') searchFormDirective;


  mode = 'view';
  private pageTitle;

  mygovDovutoId: number;

  private dovuto: Dovuto;
  form: FormGroup;

  //Declaration of form dovuto multibeneficiario
  private dovutoMultibeneficiario: DovutoMultibeneficiario;
  formDovutoMultiBen: FormGroup;


  //Autocomplete Ente
  @ViewChild(MatAutocomplete) autocompleteEnteExtended: MatAutocomplete;
  enteOptions: EnteExtended[];
  enteExtendedFilteredOptions: Observable<EnteExtended[]>;

  //expandAccordion
  expandedAccordion: boolean = false;
  validFormMultiben: boolean;

  tipiSoggetto = {"F": "Soggetto fisico", "G": "Soggetto giuridico"};

  tipiDovuto: TipoDovuto[];
  nazioni: Nazione[];
  nazioniMB: Nazione[];
  province: Provincia[];
  provincieMB: Provincia[];
  comuni: Comune[];
  existIuv: boolean = false;

  formErrors:Object = {};
  private valueChangesSub:Subscription;
  private enteChangesSub:Subscription;
  private formValidationMessages = {
    'tipoDovuto': {
      'tipoDovutoValidator': '"Tipo dovuto" non valido.',
    },
    'importo': {
      'importoValidator': '"Importo" può essere 0 solo nel caso di dovuti multi-beneficiario.',
    }
  };

  private valueChangesSubMultiben: Subscription;

  private previousPageNavId: number;

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private location: Location,
    private enteService: EnteService,
    private locationService: LocationService,
    private dovutoService: DovutoService,
    private fileSaverService: FileSaverService,
    private toastrService: ToastrService,
    private elementRef: ElementRef,
    private overlaySpinnerService: OverlaySpinnerService,
    private mypayBreadcrumbsService: MyPayBreadcrumbsService,
    private pageStateService: PageStateService,
    router: Router,
  ) {
    this.previousPageNavId = router.getCurrentNavigation()?.extras?.state?.backNavId;
  }

  private tipoDovutoValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.mygovEnteTipoDovutoId != null ) ? null : {'tipoDovutoValidator': true};
  };

  private importoValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( control.value!=null && !this.expandedAccordion && formettedAmountToNumber(control.value)==0 ) ? {'importoValidator': true} : null;
  };

  get tipoDovutoAsReadonlyField(){
    return this.form.get('tipoDovuto').value?.deTipo ?? '';
  }
  get tipoSoggettoAsReadonlyField(){
    return this.tipiSoggetto[this.form.get('tipoSoggetto').value] ?? '';
  }
  get nazioneAsReadonlyField(){
    return this.dovuto?.nazione?.nomeNazione ?? '';
  }
  get provAsReadonlyField(){
    return this.dovuto?.prov?.provincia ?? '';
  }
  get comuneAsReadonlyField(){
    return this.dovuto?.comune?.comune ?? '';
  }

  ngOnInit(): void {
    //load params from url
    const params = this.route.snapshot.params;
    this.mode = params['mode'];
    this.mygovDovutoId = +params['id']; // (+) converts string 'id' to a number

    console.log('mode', this.mode);
    this.form = this.formBuilder.group({
      ente: [''],
      tipoDovuto: [{value: null, disabled: this.mode!=='insert'}, [Validators.required, this.tipoDovutoValidator]],
      iud: [],
      iuf: [],
      anagrafica: ['', [Validators.required, Validators.maxLength(70)]],
      tipoSoggetto: [''],
      flgAnagraficaAnonima: new FormControl({value: false, disabled: true}),
      codFiscale: ['', [Validators.required, Validators.pattern(PATTERNS.codiceFiscaleOPartitaIva)]],
      email: ['', [Validators.email]],
      indirizzo: [''],
      numCiv: [''],
      cap: ['', [Validators.pattern(PATTERNS.cap)]],
      nazione: [{value:null}],
      prov: [{value:null, disabled: true}],
      comune: [{value:null, disabled: true}],
      comuneCustomFlag: [{value:false, disabled: true}],
      comuneCustom: [{value:null, disabled: true}],
      importo: ['', [Validators.required, Validators.pattern(PATTERNS.importo), this.importoValidator]],
      dataScadenza: [''],
      causale: ['', [Validators.required]],
      iuv: [''],
      stato: [''], // Don't save me!
      flgGenerateIuv: new FormControl(false),
      flgMultibeneficiario: new FormControl(false)
    });

    //initialaize formDovutoMultiBen
    this.formDovutoMultiBen = this.formBuilder.group({
      enteMultibeneficiario: [''],
      denominazioneBeneficiario: ['', [Validators.required]],
      codiceIdentificativoUnivoco: ['', [Validators.required, Validators.pattern(PATTERNS.codiceFiscaleOPartitaIva)]],
      ibanAddebito: ['', [Validators.required]],
      indirizzoBeneficiario: [''],
      civicoBeneficiario: [''],
      capBeneficiario: ['', [Validators.pattern(PATTERNS.cap)]],
      importoSecondario: ['', [Validators.required, Validators.pattern(PATTERNS.importoNonZero)]],
      nazioneBeneficiario: [{value:'', disabled: this.mode=='view'}],
      provinciaBeneficiario: [{value:'', disabled: this.mode=='view'}],
      localitaBeneficiario: [''],
      causaleMB: ['', [Validators.maxLength(130)]],
      datiSpecificiRiscossione: ['', [Validators.required, Validators.pattern(PATTERNS.datiSpecificiRiscossione)]]
    });

    this.valueChangesSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors, this.formValidationMessages));
    this.valueChangesSubMultiben = this.formDovutoMultiBen.valueChanges.subscribe(validateFormFun(this.formDovutoMultiBen, this.formErrors, this.formValidationMessages));

    if(this.mode !== 'view')
      this.loadNazioni();

    //Search for secondary PA when the dovuto is multi-beneficiary or not, setting up the PA and
    //managing the change of the PA
    this.getEntiSecondariByOperatoreId(null);

    this.enteChangesSub = this.enteService.getCurrentEnteObs().subscribe(ente => {
        this.loadDovuto();
    });

    if (this.mode === 'edit') {
      this.pageTitle = 'Modifica dovuto';
      this.mypayBreadcrumbsService.updateCurrentBreadcrumb(this.pageTitle);
    } else if (this.mode === 'insert') {
      this.pageTitle = 'Inserimento dovuto';
      this.mypayBreadcrumbsService.updateCurrentBreadcrumb(this.pageTitle);
    } else {
      this.pageTitle = null;
      this.mypayBreadcrumbsService.resetCurrentBreadcrumb();
    }
  }

  ngOnDestroy():void {
    this.valueChangesSub?.unsubscribe();
    this.valueChangesSubMultiben?.unsubscribe();
    this.enteChangesSub?.unsubscribe();
  }


  public get flgScadenzaObbligatoria() : string {
    return this.form.get('tipoDovuto').value?.flgScadenzaObbligatoria;
  }


  private setForm(dovuto: Dovuto, tipoDovuto: TipoDovuto) {
    this.form.get('tipoDovuto').setValue(tipoDovuto);
    this.form.get('iud').setValue(dovuto.iud);
    this.form.get('iuf').setValue(dovuto.iuf);
    this.form.get('stato').setValue(dovuto.stato);
    this.form.get('anagrafica').setValue(dovuto.anagrafica);
    this.form.get('tipoSoggetto').setValue(dovuto.tipoSoggetto);
    this.form.get('codFiscale').setValue(dovuto.codFiscale);
    if (tipoDovuto?.flgCfAnonimo) {
      this.form.get('flgAnagraficaAnonima').enable();
      if (dovuto.codFiscale?.trim().toLowerCase() === Dovuto.CF_ANONIMO.toLowerCase()) {
        this.form.get('codFiscale').disable();
        this.form.get('flgAnagraficaAnonima').setValue(true);
      }
    }
    this.form.get('email').setValue(dovuto.email);
    this.form.get('indirizzo').setValue(dovuto.indirizzo);
    this.form.get('numCiv').setValue(dovuto.numCiv);
    this.form.get('cap').setValue(dovuto.cap);
    this.form.get('nazione').setValue(dovuto.nazione);
    if (dovuto.nazione?.nazioneId === 1) {
      if(this.mode !== 'view')
        this.loadProvince();
      this.form.get('prov').enable();
      this.form.get('prov').setValue(dovuto.prov);
      if (dovuto?.prov) {
        if(this.mode !== 'view')
          this.loadComuni(dovuto?.prov);
        this.form.get('comuneCustomFlag').enable();
        if(dovuto.comune?.comune && dovuto.comune?.comuneId==null){
          //custom comune (no combo box)
          this.form.get('comune').disable();
          this.form.get('comuneCustom').enable();
          this.form.get('comuneCustomFlag').setValue(true);
          this.form.get('comune').setValue(null);
          this.form.get('comuneCustom').setValue(dovuto?.comune?.comune);
        } else {
          this.form.get('comune').enable();
          this.form.get('comuneCustom').disable();
          this.form.get('comuneCustomFlag').setValue(false);
          this.form.get('comune').setValue(dovuto?.comune);
          this.form.get('comuneCustom').setValue(null);
        }
      }
    } else {
      this.form.get('prov').disable();
      this.form.get('comune').disable();
      this.form.get('comuneCustomFlag').disable();
      this.form.get('comuneCustom').disable();
    }

    const importoField = this.form.get('importo');
    if (!_.isNil(tipoDovuto.importo)) {
      importoField.setValue(numberToFormattedAmount(tipoDovuto.importo, this.mode === 'edit'));
      importoField.disable();
    } else {
      importoField.setValue(numberToFormattedAmount(dovuto.importo, this.mode === 'edit'));
      importoField.enable();
    }
    this.form.get('dataScadenza').setValue(dovuto.dataScadenza);
    this.form.get('causale').setValue(dovuto.causaleToShow);
    this.form.get('iuv').setValue(dovuto.iuv);
    this.existIuv = dovuto.iuv && dovuto.iuv.trim() !== '';

    //setting formDovutoMultiBen
    if (dovuto.dovutoMultibeneficiario != null) {
      this.loadNazioniMB();
      this.loadProvinceMB();

      //Search for secondary PA when the dovuto is multi-beneficiary, setting up the PA and
      //managing the change of the PA
      this.getEntiSecondariByOperatoreId(dovuto);

      this.form.get('flgMultibeneficiario').setValue(true);
      this.expandedAccordion = true;
      this.form.get('importo').updateValueAndValidity();

      this.formDovutoMultiBen.get('denominazioneBeneficiario').setValue(dovuto.dovutoMultibeneficiario.denominazioneBeneficiario);
      this.formDovutoMultiBen.get('codiceIdentificativoUnivoco').setValue(dovuto.dovutoMultibeneficiario.codiceIdentificativoUnivoco);
      this.formDovutoMultiBen.get('ibanAddebito').setValue(dovuto.dovutoMultibeneficiario.ibanAddebito);
      this.formDovutoMultiBen.get('indirizzoBeneficiario').setValue(dovuto.dovutoMultibeneficiario.indirizzoBeneficiario);
      this.formDovutoMultiBen.get('civicoBeneficiario').setValue(dovuto.dovutoMultibeneficiario.civicoBeneficiario);
      this.formDovutoMultiBen.get('capBeneficiario').setValue(dovuto.dovutoMultibeneficiario.capBeneficiario);
      this.formDovutoMultiBen.get('nazioneBeneficiario').setValue(dovuto.dovutoMultibeneficiario.nazioneBeneficiario);
      this.formDovutoMultiBen.get('provinciaBeneficiario').setValue(dovuto.dovutoMultibeneficiario.provinciaBeneficiario);
      this.formDovutoMultiBen.get('localitaBeneficiario').setValue(dovuto.dovutoMultibeneficiario.localitaBeneficiario);
      this.formDovutoMultiBen.get('importoSecondario').setValue(numberToFormattedAmount(dovuto.dovutoMultibeneficiario.importoSecondario));
      this.formDovutoMultiBen.get('datiSpecificiRiscossione').setValue(dovuto.dovutoMultibeneficiario.datiSpecificiRiscossione);
      this.formDovutoMultiBen.get('causaleMB').setValue(dovuto.dovutoMultibeneficiario.causaleMB);

    }else {
      this.form.get('flgMultibeneficiario').setValue(false);
    }
  }

  private loadDovuto(){
    if(!this.enteService.getCurrentEnte()) return;

    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    if (!this.mygovDovutoId)
      this.enteService.getListTipoDovutoByEnteAsOperatore(this.enteService.getCurrentEnte())
      .subscribe(
        data =>  {
          this.form.get('ente').setValue(this.enteService.getCurrentEnte().deNomeEnte);
          this.tipiDovuto = data.filter(item => item.codTipo !== 'MARCA_BOLLO_DIGITALE');
          this.overlaySpinnerService.detach(spinner);
        }, manageError('Errore recuperando i tipi dovuto', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
    else
      combineLatest([
        this.dovutoService.getDetailDebitoOperatore(this.enteService.getCurrentEnte(), this.mygovDovutoId),
        this.enteService.getListTipoDovutoByEnteAsOperatore(this.enteService.getCurrentEnte())
      ]).subscribe( ([data, tipiDovuto]) => {
          if(!data){
            this.toastrService.warning('Il dovuto non è presente nel sistema (verificare che non sia passato nell\'archivio).' );
            this.goBack();
            return;
          }
          this.tipiDovuto = tipiDovuto;
          const tipoDovuto = tipiDovuto.find(tipoDovuto => tipoDovuto.mygovEnteTipoDovutoId === data.tipoDovuto?.mygovEnteTipoDovutoId);
          //console.log(`loadDovuto() response: ${JSON.stringify(data)}`)
          this.dovuto = data;
          this.setForm(data, tipoDovuto);
          this.overlaySpinnerService.detach(spinner);
        }, manageError('Errore recuperando il dettaglio dovuto', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  private loadNazioni() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.locationService.getNazioni().subscribe(
      nazioni => {
       this.overlaySpinnerService.detach(spinner);
        this.nazioni = nazioni;
      }, manageError('Errore recuperando le nazioni', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  private loadNazioniMB() {
    this.locationService.getNazioni().subscribe(n=> {
      this.nazioniMB = n;
    })
  }

  private loadProvince() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.locationService.getProvince().subscribe(
      province => {
       this.overlaySpinnerService.detach(spinner);
        this.province = province;
      }, manageError('Errore recuperando le province', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  private loadProvinceMB() {
    this.locationService.getProvince().subscribe(p=> {
      this.provincieMB = p;
    })
  }

  private loadComuni(provincia: Provincia) {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    return this.locationService.getComuni(provincia).subscribe(
      comuni => {
        this.overlaySpinnerService.detach(spinner);
        this.comuni = comuni;
      }, manageError('Errore recuperando i comuni', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  tipoDovutoOnChange(tipoDovuto: TipoDovuto) {
    if (tipoDovuto.flgCfAnonimo)
      this.form.get('flgAnagraficaAnonima').enable();
    else{
      this.form.get('flgAnagraficaAnonima').setValue(false);
      this.flgAnagraficaAnonimaOnChange(false);
      this.form.get('flgAnagraficaAnonima').disable();
    }


    if (tipoDovuto.importo && parseFloat(tipoDovuto.importo) > 0) {
      this.form.get('importo').setValue(numberToFormattedAmount(tipoDovuto.importo));
      this.form.get('importo').disable();
    } else {
      this.form.get('importo').setValue(null);
      this.form.get('importo').enable();
    }
  }

  flgAnagraficaAnonimaOnChange(flgAnagraficaAnonima: boolean) {
    if (flgAnagraficaAnonima){
      this.form.get('codFiscale').disable();
      this.form.get('codFiscale').setValue(null);
    } else
      this.form.get('codFiscale').enable();

  }

  compareNazione(o1: Nazione, o2: Nazione) {
    return o1.nazioneId === o2?.nazioneId;
  }

  compareProvincia(o1: Provincia, o2: Provincia) {
    return o1.provinciaId === o2?.provinciaId;
  }

  compareComune(o1: Comune, o2: Comune) {
    return o1.comuneId === o2?.comuneId;
  }

  nazioneOnChange(nazione: Nazione) {
    this.form.get('prov').setValue(null);
    this.form.get('comune').setValue(null);
    this.province = [];
    this.comuni = [];
    this.form.get('prov').disable();
    this.form.get('comune').disable();
    if (nazione.nazioneId === 0) {
      this.form.get('nazione').setValue(null);
    } else if (nazione.nazioneId === 1) {
      this.loadProvince();
      this.form.get('prov').enable();
    }
  }

  provinciaOnChange(provincia: Provincia) {
    this.form.get('comune').setValue(null);
    this.comuni = [];
    if (provincia.provinciaId === 0) {
      this.form.get('comune').disable();
      this.form.get('prov').setValue(null);
    } else {
      this.form.get('comune').enable();
    }
    this.loadComuni(provincia);
  }

  comuneOnChange(comune: Comune) {
    if (comune.comuneId === 0) {
      this.form.get('comune').setValue(null);
    }
  }

  downloadAvviso() {
    let dovuto = new Dovuto();
    dovuto.id = this.mygovDovutoId;
    this.dovutoService.downloadAvviso(dovuto).subscribe(response => {
      const contentDisposition = response.headers.get('content-disposition');
      const fileName = ApiInvokerService.extractFilenameFromContentDisposition(contentDisposition)  ?? 'mypay4_avviso_'+this.mygovDovutoId+'.pdf';
      const contentType = response.headers.get('content-type') ?? 'application/pdf; charset=utf-8';
      const blob:any = new Blob([response.body], { type: contentType });
      this.fileSaverService.save(blob, fileName);
		}, manageError('Errore scaricando l\'avviso di pagamento', this.toastrService) );
  }

  goBack(){
    this.location.back();
  }

  onSubmit(){
    if(!this.enteService.getCurrentEnte())
      return;

    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);

    const dovutoToUpsert = this.dovuto ? _.cloneDeep(this.dovuto) : new Dovuto();

    let comuneToUpsert;
    if(this.form.get('comuneCustomFlag').value){
      comuneToUpsert = new Comune(null);
      comuneToUpsert.comune = this.form.get('comuneCustom').value;
    } else {
      comuneToUpsert = this.form.get('comune').value;
    }

    dovutoToUpsert.tipoDovuto = this.form.get('tipoDovuto').value;
    dovutoToUpsert.anagrafica = blank2Null(this.form.get('anagrafica').value);
    dovutoToUpsert.tipoSoggetto = blank2Null(this.form.get('tipoSoggetto').value);
    dovutoToUpsert.flgAnagraficaAnonima = this.form.get('flgAnagraficaAnonima').value;
    dovutoToUpsert.codFiscale = blank2Null(this.form.get('codFiscale').value);
    dovutoToUpsert.email = blank2Null(this.form.get('email').value);
    dovutoToUpsert.indirizzo = blank2Null(this.form.get('indirizzo').value);
    dovutoToUpsert.numCiv = blank2Null(this.form.get('numCiv').value);
    dovutoToUpsert.cap = blank2Null(this.form.get('cap').value);
    dovutoToUpsert.nazione = this.form.get('nazione').value;
    dovutoToUpsert.prov = this.form.get('prov').value;
    dovutoToUpsert.comune = comuneToUpsert;
    dovutoToUpsert.importo = blank2Null(formettedAmountToNumberString(this.form.get('importo').value));
    dovutoToUpsert.dataScadenza = this.form.get('dataScadenza').value;
    dovutoToUpsert.causale = blank2Null(this.form.get('causale').value);
    dovutoToUpsert.iuv = blank2Null(this.form.get('iuv').value);
    dovutoToUpsert.flgGenerateIuv = this.form.get('flgGenerateIuv').value;
    dovutoToUpsert.flgMultibeneficiario = this.form.get('flgMultibeneficiario').value;

    //dovutoMultibeneficiario
    if(this.expandAccordion) {
      const dovutoMultibeneToUpsert = this.dovutoMultibeneficiario ?  _.cloneDeep(this.dovutoMultibeneficiario) : new DovutoMultibeneficiario();
      dovutoMultibeneToUpsert.denominazioneBeneficiario = this.formDovutoMultiBen.get('denominazioneBeneficiario').value;
      dovutoMultibeneToUpsert.codiceIdentificativoUnivoco = this.formDovutoMultiBen.get('codiceIdentificativoUnivoco').value;
      dovutoMultibeneToUpsert.ibanAddebito = this.formDovutoMultiBen.get('ibanAddebito').value;
      dovutoMultibeneToUpsert.indirizzoBeneficiario = blank2Null(this.formDovutoMultiBen.get('indirizzoBeneficiario').value);
      dovutoMultibeneToUpsert.civicoBeneficiario = blank2Null(this.formDovutoMultiBen.get('civicoBeneficiario').value);
      dovutoMultibeneToUpsert.capBeneficiario = blank2Null(this.formDovutoMultiBen.get('capBeneficiario').value);
      dovutoMultibeneToUpsert.nazioneBeneficiario = blank2Null(this.formDovutoMultiBen.get('nazioneBeneficiario').value);
      dovutoMultibeneToUpsert.provinciaBeneficiario = blank2Null(this.formDovutoMultiBen.get('provinciaBeneficiario').value);
      dovutoMultibeneToUpsert.localitaBeneficiario = blank2Null(this.formDovutoMultiBen.get('localitaBeneficiario').value);
      dovutoMultibeneToUpsert.importoSecondario = blank2Null(formettedAmountToNumberString(this.formDovutoMultiBen.get('importoSecondario').value));
      dovutoMultibeneToUpsert.datiSpecificiRiscossione = blank2Null(this.formDovutoMultiBen.get('datiSpecificiRiscossione').value);
      dovutoMultibeneToUpsert.causaleMB = blank2Null(this.formDovutoMultiBen.get('causaleMB').value)


      //set dovutoMultibeneToUpsert into dovutoToUpsert
      dovutoToUpsert.dovutoMultibeneficiario = dovutoMultibeneToUpsert;
    }


    this.dovutoService.upsertDovuto(this.mode, this.enteService.getCurrentEnte(), this.mygovDovutoId, dovutoToUpsert)
      .subscribe( data => {
        if (!data.invalidDesc) {
          this.dovuto = data;
          const tipoDovuto = this.tipiDovuto.find(tipoDovuto => tipoDovuto.mygovEnteTipoDovutoId === this.dovuto.tipoDovuto?.mygovEnteTipoDovutoId);
          this.setForm(this.dovuto, tipoDovuto);
          this.form.markAsUntouched();
          this.toastrService.success('Dovuto '+(this.mode==='edit'?'modificato':'inserito')+' correttamente.');
          this.mode = 'view';
          this.setForm(this.dovuto, tipoDovuto);
          this.form.markAsUntouched();
          this.pageTitle = null;
          this.mypayBreadcrumbsService.resetCurrentBreadcrumb();
          this.mygovDovutoId = this.dovuto.id; // For downloadAvviso.
          this.pageStateService.addToSavedState(this.previousPageNavId, 'reloadData', true);
        } else {
          this.toastrService.error(data.invalidDesc);
        }

        this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore salvando il dettaglio dovuto', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  comuneCustomFlagOnChange(checked: boolean) {
    const comuneField = this.form.get('comune');
    const comuneCustomField = this.form.get('comuneCustom');
    if(checked) {
      comuneField.disable();
      comuneCustomField.enable();
      comuneCustomField.setValue(null);
    } else {
      comuneField.enable();
      comuneCustomField.disable();
    }

  }

  /**
   * Show Ente in mat autocomplete
   * @param ente
   * @returns
   */
  enteExtendedDisplayFn(ente: EnteExtended): string {
    return ente ? ente.deNomeEnte : '';
  }

  private _enteFilter(name: string): EnteExtended[] {
    const filterValue = name;
    return this.enteOptions.filter(option => option.deNomeEnte.toLowerCase().indexOf(filterValue.toLowerCase()) !== -1);
  }

  private getEntiSecondariByOperatoreId(dovuto) {
    this.enteService.getEnteExtendedUserId().subscribe(enti => {
      this.enteOptions = enti;
      // rempve ente selected from enteOption
      let currentEnte = this.enteService.getCurrentEnte();
      let indexCurrentEnte = this.enteOptions.findIndex(e=> e.deNomeEnte === currentEnte.deNomeEnte);
      this.enteOptions.splice(indexCurrentEnte,1)
      //init autocomplete feature of ente field
      this.enteExtendedFilteredOptions = this.formDovutoMultiBen.get('enteMultibeneficiario').valueChanges
        .pipe(
          startWith(''),
          map(value => typeof value === 'string' || !value ? value : value.deNomeEnte),
          map(deNomeEnte => deNomeEnte ? this._enteFilter(deNomeEnte) : this.enteOptions.slice())
        );

        if (dovuto!==null && dovuto.dovutoMultibeneficiario!==null) {
          const enteSecondario = this.enteOptions.find(ente=> ente.deNomeEnte === dovuto.dovutoMultibeneficiario.denominazioneBeneficiario);
          if (enteSecondario)
            this.formDovutoMultiBen.get('enteMultibeneficiario').setValue(enteSecondario);
        }
    });
  }

  enteSelected(ente: EnteExtended){
    this.fillformDovutoMultiBen(ente);
    if(ente){
      this.formDovutoMultiBen.get('nazioneBeneficiario').disable();
      this.formDovutoMultiBen.get('provinciaBeneficiario').disable();
    } else {
      this.formDovutoMultiBen.get('nazioneBeneficiario').enable();
      this.formDovutoMultiBen.get('provinciaBeneficiario').enable();
    }
  }

  private fillformDovutoMultiBen(ente: EnteExtended) {
    const chosenEnte = ente;
    this.formDovutoMultiBen.get('denominazioneBeneficiario').setValue(chosenEnte.deNomeEnte);
    this.formDovutoMultiBen.get('codiceIdentificativoUnivoco').setValue(chosenEnte.codiceFiscaleEnte);
    this.formDovutoMultiBen.get('ibanAddebito').setValue(chosenEnte.ibanAccrerdito);
    this.formDovutoMultiBen.get('indirizzoBeneficiario').setValue(chosenEnte.indirizzoBeneficiario);
    this.formDovutoMultiBen.get('civicoBeneficiario').setValue(chosenEnte.civicoBeneficiario);
    this.formDovutoMultiBen.get('capBeneficiario').setValue(chosenEnte.capBeneficiario);
    this.formDovutoMultiBen.get('nazioneBeneficiario').setValue(chosenEnte.nazioneBeneficiario);
    this.formDovutoMultiBen.get('provinciaBeneficiario').setValue(chosenEnte.provicniaBeneficiario);
    this.formDovutoMultiBen.get('localitaBeneficiario').setValue(chosenEnte.localitaBeneficiario);
    this.formDovutoMultiBen.get('importoSecondario').setValue(chosenEnte.importoSecondario);
  }

  private previousFlgGenerateIuvValue:boolean;
  public expandAccordion() {
    this.expandedAccordion = !this.expandedAccordion;
    this.form.get('importo').updateValueAndValidity();


    //if is not expanded, clean the formDovutoMultiBen
    const flgGenerateIuvControl = this.form.get('flgGenerateIuv');
    this.previousFlgGenerateIuvValue = flgGenerateIuvControl.value;
    if (!this.expandedAccordion && (this.mode == 'insert' || this.mode == 'edit')) {
      this.formDovutoMultiBen.reset()
      flgGenerateIuvControl.setValue(this.previousFlgGenerateIuvValue || false);
      flgGenerateIuvControl.enable();
    } else if(this.expandedAccordion && (this.mode == 'insert' ||  this.mode == 'edit') ) {
      this.previousFlgGenerateIuvValue = flgGenerateIuvControl.value;
      flgGenerateIuvControl.setValue(true);
      flgGenerateIuvControl.disable();
    }


    if (this.nazioniMB == null || this.nazioniMB.length == 0)
      this.loadNazioniMB();

    if (this.provincieMB == null || this.provincieMB.length == 0)
      this.loadProvinceMB();
  }

  public resetFormDovutoMB(event, isResetButton: boolean) {
    if (isResetButton) {
      this.formDovutoMultiBen.reset();
    } else {
      if (event.key === 'Backspace' && this.formDovutoMultiBen.get('enteMultibeneficiario').value == '') {
        this.formDovutoMultiBen.reset();
        this.formDovutoMultiBen.get('enteMultibeneficiario').setValue('');
      }
    }
  }


}
