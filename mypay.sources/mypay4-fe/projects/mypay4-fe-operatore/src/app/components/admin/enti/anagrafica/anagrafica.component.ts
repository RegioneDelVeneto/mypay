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
import { DateTime } from 'luxon';
import { ToastrService } from 'ngx-toastr';
import {
    blank2Null, Comune, LocationService, manageError, Nazione, OverlaySpinnerService, PATTERNS,
    Provincia, UserService, validateFormFunAsync, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { LingueCodDesc } from 'projects/mypay4-fe-operatore/src/app/model/lingue-cod-desc';
import { combineLatest, Subscription } from 'rxjs';

import { Location } from '@angular/common';
import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { faEye, faEyeSlash } from '@fortawesome/free-solid-svg-icons';

import { AnagraficaStato } from '../../../../model/anagrafica-stato';
import { Ente } from '../../../../model/ente';
import { TassonomiaCodDesc } from '../../../../model/tassonomia-cod-desc';
import { AdminEntiService } from '../../../../services/admin-enti.service';

@Component({
  selector: 'ente-anagrafica',
  templateUrl: './anagrafica.component.html',
  styleUrls: ['./anagrafica.component.scss']
})
export class AnagraficaComponent implements OnInit, OnDestroy, WithTitle {

  @ViewChild('sForm') searchFormDirective;

  private _ente: Ente;
  @Input() set ente(ente: Ente){
    this._ente = ente;
    if(ente && this.initialized){
      if(!this.initializedEnte){
        this.initializedEnte = true;
        this.setForm(ente);
      }
    }
  }
  get ente(){
    return this._ente;
  }

  get titleLabel(){ return "Inserimento nuovo ente" }
  get titleIcon(){ return null }

  getPwdIcon(visible: boolean){
    return visible ? faEye : faEyeSlash;
  }
  getPwdTooltip(visible: boolean){
    return visible ? 'Nascondi' : 'Mostra';
  }

  modeAnag = 'view';
  private initialized = false;
  private initializedEnte = false;

  mygovEnteId: number;

  formAnag: FormGroup;
  formAnagModified: boolean = false;
  formAnagErrors:Object = {};
  blockingError: boolean = false;

  anagraficaStati: AnagraficaStato[];
  tipiEnte: TassonomiaCodDesc[];
  nazioni: Nazione[];
  province: Provincia[];
  comuni: Comune[];
  lingueAggiuntive: LingueCodDesc[];

  private valueChangesSub:Subscription;
  private userSub: Subscription;
  isUserAdmin: boolean = false;

  fields = {
    codIpaEnte:'Codice IPA',
    dtUltimaModifica:'Data ultima modifica',
    cdStatoEnte:'Stato',
    dePassword:'Pws web services',
    paaSILInviaCarrelloDovutiHash:'Password PaaSILInviaCarrelloDovuti',
    dtAvvio:'Data avvio',
    deNomeEnte:'Nome ente',
    applicationCode:'Codice segregazione',
    codiceFiscaleEnte:'Codice fiscale ente',
    codCodiceInterbancarioCbill:'Codice interbancario CBILL',
    emailAmministratore:'Email ricezione notifiche',
    enteSilInviaRispostaPagamentoUrl:'Invia risposta pagamento URL',
    codTipoEnte:'Codice tipo ente creditore',
    codRpDatiVersDatiSingVersIbanAccredito:'IBAN accredito',
    deRpEnteBenefDenominazioneBeneficiario:'Denominazione',
    deRpEnteBenefIndirizzoBeneficiario:'Indirizzo',
    deRpEnteBenefCivicoBeneficiario:'Civico',
    codRpEnteBenefCapBeneficiario:'CAP',
    nazione:'Nazione',
    prov:'Provincia',
    comune:'Località',
    deRpEnteBenefTelefonoBeneficiario:'Telefono',
    deRpEnteBenefSitoWebBeneficiario:'Sito web',
    deRpEnteBenefEmailBeneficiario:'Email',
    deAutorizzazione:'Autorizzazione poste',
    deInformazioniEnte:'Informazioni ente',
    linguaAggiuntiva:'Avviso multilingua',
    flagInsertDefaultSet: 'Inserimento Dovuti Standard'
  }

  constructor(
    private userService: UserService,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private location: Location,
    private locationService: LocationService,
    private adminEntiService: AdminEntiService,
    private toastrService: ToastrService,
    private elementRef: ElementRef,
    private overlaySpinnerService: OverlaySpinnerService) {

      this.userSub = userService.getLoggedUserObs().subscribe(() => {
        this.isUserAdmin = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ROLE);
      });
  }

  ngOnInit(): void {
    //load params from url
    const params = this.route.snapshot.params;
    let mode = params['mode'] || 'view';
    if(params['enteId'])
     this.mygovEnteId = +params['enteId']; // (+) converts string 'id' to a number

    this.formAnag = this.formBuilder.group({
      codIpaEnte: ['', [Validators.required]],
      dtUltimaModifica: [null],
      dePassword: [''],
      paaSILInviaCarrelloDovutiHash: [''],
      deNomeEnte: ['', [Validators.required]],
      cdStatoEnte: [null],
      dtAvvio: [null],
      codCodiceInterbancarioCbill: [''],
      emailAmministratore: ['', [Validators.required, Validators.email]],
      applicationCode: ['', [Validators.required]],
      codiceFiscaleEnte: ['', [Validators.required, Validators.pattern(PATTERNS.partitaIva)]],
      enteSilInviaRispostaPagamentoUrl: ['', [Validators.pattern(PATTERNS.url)]],
      codTipoEnte: [null, [Validators.required]],
      codRpDatiVersDatiSingVersIbanAccredito: [''],
      deRpEnteBenefDenominazioneBeneficiario: [''],
      deRpEnteBenefIndirizzoBeneficiario: [''],
      deRpEnteBenefCivicoBeneficiario: [''],
      codRpEnteBenefCapBeneficiario: ['', Validators.pattern(PATTERNS.cap)],
      nazione: [null],
      prov: [null],
      comune: [null],
      deRpEnteBenefTelefonoBeneficiario: [''],
      deRpEnteBenefSitoWebBeneficiario: ['', Validators.pattern(PATTERNS.url)],
      deRpEnteBenefEmailBeneficiario: ['', [Validators.email]],
      deAutorizzazione: [''],
      deInformazioniEnte: [''],
      linguaAggiuntiva: [null],
      flagInsertDefaultSet: [false]
    });

    this.valueChangesSub = this.formAnag.statusChanges.subscribe(
      validateFormFunAsync(this.formAnag, this.formAnagErrors, this.checkFormModified(this)));

    this.setMode(mode);
    this.initilizeFields();
  }

  ngOnDestroy():void {
    this.valueChangesSub?.unsubscribe();
    this.userSub?.unsubscribe();
  }

  get linguaAggiuntivaAsReadonlyField(){
    return this.formAnag.get('linguaAggiuntiva').value?.description ?? '';
  }

  get cdStatoEnteAsReadonlyField(){
    return this.formAnag.get('cdStatoEnte').value?.deStato ?? '';
  }
  get codTipoEnteAsReadonlyField(){
    return this.formAnag.get('codTipoEnte').value?.description ?? '';
  }
  get nazioneAsReadonlyField(){
    return this.formAnag.get('nazione').value?.nomeNazione ?? '';
  }
  get provAsReadonlyField(){
    return this.formAnag.get('prov').value?.provincia ?? '';
  }
  get comuneAsReadonlyField(){
    return this.formAnag.get('comune').value?.comune ?? '';
  }

  private setForm(ente: Ente) {
    this.formAnag.get('codIpaEnte').setValue(ente.codIpaEnte);
    this.formAnag.get('dtUltimaModifica').setValue(ente.dtUltimaModifica.toFormat('dd/MM/yyyy HH:mm:ss'));
    this.formAnag.get('dePassword').setValue(ente.dePassword);
    this.formAnag.get('paaSILInviaCarrelloDovutiHash').setValue(ente.paaSILInviaCarrelloDovutiHash);
    this.formAnag.get('deNomeEnte').setValue(ente.deNomeEnte);
    let stato = this.anagraficaStati.filter(s => s.mygovAnagraficaStatoId == ente.cdStatoEnte.mygovAnagraficaStatoId)[0];
    this.formAnag.get('cdStatoEnte').setValue(stato);
    this.formAnag.get('dtAvvio').setValue(ente.dtAvvio);
    this.formAnag.get('codCodiceInterbancarioCbill').setValue(ente.codCodiceInterbancarioCbill);
    this.formAnag.get('emailAmministratore').setValue(ente.emailAmministratore);
    this.formAnag.get('applicationCode').setValue(ente.applicationCode);
    this.formAnag.get('codiceFiscaleEnte').setValue(ente.codiceFiscaleEnte);
    this.formAnag.get('enteSilInviaRispostaPagamentoUrl').setValue(ente.enteSilInviaRispostaPagamentoUrl);
    if (ente.codTipoEnte) {
      let tipoEnte = this.tipiEnte.filter(t => t.code == ente.codTipoEnte)[0];
      this.formAnag.get('codTipoEnte').setValue(tipoEnte);
    }
    this.formAnag.get('codRpDatiVersDatiSingVersIbanAccredito').setValue(ente.codRpDatiVersDatiSingVersIbanAccredito);
    this.formAnag.get('deRpEnteBenefDenominazioneBeneficiario').setValue(ente.deRpEnteBenefDenominazioneBeneficiario);
    this.formAnag.get('deRpEnteBenefIndirizzoBeneficiario').setValue(ente.deRpEnteBenefIndirizzoBeneficiario);
    this.formAnag.get('deRpEnteBenefCivicoBeneficiario').setValue(ente.deRpEnteBenefCivicoBeneficiario);
    this.formAnag.get('codRpEnteBenefCapBeneficiario').setValue(ente.codRpEnteBenefCapBeneficiario);
    if (ente.codRpEnteBenefNazioneBeneficiario) {
      let nazione = this.nazioni.filter(n => n.codiceIsoAlpha2 == ente.codRpEnteBenefNazioneBeneficiario)[0];
      this.formAnag.get('nazione').setValue(nazione);
      if (nazione.nazioneId === 1) {
        this.locationService.getProvince().subscribe( province => {
          this.province = province;
          if (ente.deRpEnteBenefProvinciaBeneficiario) {
            let provincia = this.province.filter(f => f.sigla == ente.deRpEnteBenefProvinciaBeneficiario)[0];
            this.formAnag.get('prov').setValue(provincia);
            if(provincia)
              this.locationService.getComuni(provincia).subscribe(comuni => {
                  this.comuni = comuni;
                  if (ente.deRpEnteBenefLocalitaBeneficiario) {
                    let comune = this.comuni.filter(c => c.comune?.toUpperCase() == ente.deRpEnteBenefLocalitaBeneficiario.toUpperCase())[0];
                    this.formAnag.get('comune').setValue(comune);
                  }
              }, manageError('Errore recuperando i comuni', this.toastrService) );
          }
        }, manageError('Errore recuperando le province', this.toastrService) );
      }
    }
    this.formAnag.get('deRpEnteBenefTelefonoBeneficiario').setValue(ente.deRpEnteBenefTelefonoBeneficiario);
    this.formAnag.get('deRpEnteBenefSitoWebBeneficiario').setValue(ente.deRpEnteBenefSitoWebBeneficiario);
    this.formAnag.get('deRpEnteBenefEmailBeneficiario').setValue(ente.deRpEnteBenefEmailBeneficiario);
    this.formAnag.get('deAutorizzazione').setValue(ente.deAutorizzazione);
    this.formAnag.get('deInformazioniEnte').setValue(ente.deInformazioniEnte);
    let lingua = this.lingueAggiuntive.filter(l => l.code == ente.linguaAggiuntiva)[0];
    this.formAnag.get('linguaAggiuntiva').setValue(lingua);
    this.formAnag.get('flagInsertDefaultSet').setValue(ente.flagInsertDefaultSet);
  }

  private initilizeFields() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    combineLatest([
      this.adminEntiService.getTipiEnte(), // Combo Codice Tipo Ente
      this.adminEntiService.getAnagraficaStati(), // Combo Stato
      this.locationService.getNazioni(), // Combo Nazione
      this.adminEntiService.getLingueAggiuntive(), // Combo Lingue
    ]).subscribe( ([tipiEnte, anagraficaStati, nazioni, lingue]) => {
      this.tipiEnte = tipiEnte;
      this.anagraficaStati = anagraficaStati;
      this.nazioni = nazioni;
      this.lingueAggiuntive = lingue;
      let defaultlingua =  this.lingueAggiuntive.filter(s => s.description == 'No')[0];
      this.formAnag.get('linguaAggiuntiva').setValue(defaultlingua);
      let defaultStato = this.anagraficaStati.filter(s => s.codStato == 'INSERITO')[0];
      this.formAnag.get('cdStatoEnte').setValue(defaultStato);
      this.initialized = true;
      if(!this.initializedEnte && this.ente){
        this.initializedEnte = true;
        this.setForm(this.ente);
      }
      this.overlaySpinnerService.detach(spinner);
    }, manageError("Errore recuperando i dati", this.toastrService, () => {this.blockingError=true; this.overlaySpinnerService.detach(spinner)}) );
  }

  private loadProvince() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.locationService.getProvince().subscribe(
      province => {
       this.overlaySpinnerService.detach(spinner);
        this.province = province;
      }, manageError('Errore recuperando le province', this.toastrService, () => {this.blockingError=true; this.overlaySpinnerService.detach(spinner)}) );
  }

  private loadComuni(provincia: Provincia) {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.locationService.getComuni(provincia).subscribe(
      comuni => {
        this.overlaySpinnerService.detach(spinner);
        this.comuni = comuni;
      }, manageError('Errore recuperando i comuni', this.toastrService, () => {this.blockingError=true; this.overlaySpinnerService.detach(spinner)}) );
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
    this.formAnag.get('prov').setValue(null);
    this.formAnag.get('comune').setValue(null);
    this.province = [];
    this.comuni = [];
    this.formAnag.get('prov').disable();
    this.formAnag.get('comune').disable();
    if (nazione.nazioneId === 0) {
      this.formAnag.get('nazione').setValue(null);
    } else if (nazione.nazioneId === 1) {
      this.loadProvince();
      this.formAnag.get('prov').enable();
    }
  }

  provinciaOnChange(provincia: Provincia) {
    this.formAnag.get('comune').setValue(null);
    this.comuni = [];
    if (provincia.provinciaId === 0) {
      this.formAnag.get('comune').disable();
      this.formAnag.get('prov').setValue(null);
    } else {
      this.formAnag.get('comune').enable();
    }
    this.loadComuni(provincia);
  }

  comuneOnChange(comune: Comune) {
    if (comune.comuneId === 0) {
      this.formAnag.get('comune').setValue(null);
    }
  }

  private setMode(mode: string) {
    this.modeAnag = mode;
  }

  goBack(){
    this.location.back();
  }

  onReset(): void {
    this.setMode('view');
    this.setForm(this.ente);
  }

  enableEdit(): void {
    this.setMode('edit');
  }

  private isFieldModified(field: string){
    if (field === 'dtUltimaModifica')
      return false;
    if (field === 'cdStatoEnte')
      return this.ente.cdStatoEnte.mygovAnagraficaStatoId !== (this.formAnag.get('cdStatoEnte').value as AnagraficaStato)?.mygovAnagraficaStatoId;
    if (field === 'codTipoEnte')
      return this.ente.codTipoEnte !== (this.formAnag.get('codTipoEnte').value as TassonomiaCodDesc)?.code;
    if (field === 'nazione')
      return this.ente.codRpEnteBenefNazioneBeneficiario !== (this.formAnag.get('nazione').value as Nazione)?.codiceIsoAlpha2;
    if (field === 'prov')
      return this.ente.deRpEnteBenefProvinciaBeneficiario !== (this.formAnag.get('prov').value as Provincia)?.sigla;
    if (field === 'comune')
      return this.ente.deRpEnteBenefLocalitaBeneficiario !== (this.formAnag.get('comune').value as Comune)?.comune;
    if (field === 'linguaAggiuntiva')
      return this.ente.linguaAggiuntiva !== (this.formAnag.get('linguaAggiuntiva').value as LingueCodDesc)?.code;
    else
      return this.ente[field] !== this.formAnag.get(field).value;
  }

  private checkFormModified(thisRef: AnagraficaComponent): (data: Object) => void {
    return function(data: Object){
      thisRef.formAnagModified = thisRef.modeAnag === 'insert' ||
          thisRef.isFieldModified('codIpaEnte') || thisRef.isFieldModified('deNomeEnte') || thisRef.isFieldModified('dtAvvio') ||
          thisRef.isFieldModified('cdStatoEnte') || thisRef.isFieldModified('codCodiceInterbancarioCbill') ||
          thisRef.isFieldModified('emailAmministratore') || thisRef.isFieldModified('enteSilInviaRispostaPagamentoUrl') ||
          thisRef.isFieldModified('codTipoEnte') || thisRef.isFieldModified('codRpDatiVersDatiSingVersIbanAccredito') ||
          thisRef.isFieldModified('deRpEnteBenefDenominazioneBeneficiario') || thisRef.isFieldModified('deRpEnteBenefIndirizzoBeneficiario') ||
          thisRef.isFieldModified('deRpEnteBenefCivicoBeneficiario') || thisRef.isFieldModified('codRpEnteBenefCapBeneficiario') ||
          thisRef.isFieldModified('nazione') || thisRef.isFieldModified('prov') || thisRef.isFieldModified('comune') ||
          thisRef.isFieldModified('deRpEnteBenefTelefonoBeneficiario') || thisRef.isFieldModified('deRpEnteBenefSitoWebBeneficiario') ||
          thisRef.isFieldModified('deRpEnteBenefEmailBeneficiario') || thisRef.isFieldModified('deAutorizzazione') ||
          thisRef.isFieldModified('deInformazioniEnte') || thisRef.isFieldModified('linguaAggiuntiva') || thisRef.isFieldModified('flagInsertDefaultSet');
    }
  }

  saveConfirmMsg(thisRef: AnagraficaComponent): string | {message: string, invalid: boolean} {
    if(thisRef.modeAnag === 'insert'){
      return "Confermi l'inserimento del nuovo ente?";
    } else {
      const msg = Object.keys(this.fields)
        .filter(field => thisRef.isFieldModified(field))
        .map(field => this.createMsgByField(thisRef, field, this.fields[field]));
      if(msg.length==0){
        return {message:"Nessun campo modificato", invalid: true};
      } else {
        msg.unshift("Confermi la modifica dei seguenti campi?<br>");
        if(thisRef.isFieldModified('codTipoEnte')){
          msg.push('<br>Attenzione, si sta modificando il "Codice Tipo ente Creditore" e questo');
          msg.push('comporta che necessariamente tutti i codici tassonomici dei Tipo Dovuto ');
          msg.push('dell\'ente dovranno essere riconfigurati; si consiglia di annullare ');
          msg.push('l\'operazione se questo intervento non era stato considerato.');
        }
        return msg.join("<br>");
      }
    }
  }

  private createMsgByField(thisRef: AnagraficaComponent, field: string, label: string): string {
    if (field === 'cdStatoEnte')
      return `${label}: ${thisRef.ente.cdStatoEnte.deStato} -> ${(thisRef.formAnag.get(field).value as AnagraficaStato)?.deStato} `;
    if (field === 'codTipoEnte')
      return `${label}: ${thisRef.tipiEnte.find(t => t.code===thisRef.ente[field])?.description ?? '[vuoto]'} -> ${(thisRef.formAnag.get(field).value as TassonomiaCodDesc)?.description || '[vuoto]'}`;
    if (field === 'nazione')
      return `${label}: ${thisRef.ente.codRpEnteBenefNazioneBeneficiario || '[vuoto]'} -> ${(thisRef.formAnag.get(field).value as Nazione)?.codiceIsoAlpha2 || '[vuoto]'}`;
    if (field === 'prov')
      return `provincia: ${thisRef.ente.deRpEnteBenefProvinciaBeneficiario || '[vuoto]'} -> ${(thisRef.formAnag.get(field).value as Provincia)?.sigla || '[vuoto]'}`;
    if (field === 'comune')
      return `${label}: ${thisRef.ente.deRpEnteBenefLocalitaBeneficiario || '[vuoto]'} -> ${(thisRef.formAnag.get(field).value as Comune)?.comune || '[vuoto]'}`;
    if (field === 'dtAvvio')
      return `${label}: ${thisRef.ente.dtAvvio?.toFormat('dd/MM/yyyy') || '[vuoto]'} -> ${(thisRef.formAnag.get(field).value as DateTime)?.toFormat('dd/MM/yyyy') || '[vuoto]'} `;
    if (field === 'linguaAggiuntiva')
      return `${label}: ${thisRef.lingueAggiuntive.find(l => l.code===thisRef.ente[field])?.description || '[vuoto]'} -> ${(thisRef.formAnag.get(field).value as LingueCodDesc)?.description || '[vuoto]'}`;

    return `${label}: ${thisRef.ente[field] || '[vuoto]'} -> ${thisRef.formAnag.get(field).value || '[vuoto]'}`;
  }

  onSubmit(){
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);

    const newEnte = new Ente();

    newEnte.mygovEnteId = this.mygovEnteId;
    newEnte.codIpaEnte = this.formAnag.get('codIpaEnte').value;
    newEnte.deNomeEnte = this.formAnag.get('deNomeEnte').value;
    newEnte.cdStatoEnte = this.formAnag.get('cdStatoEnte').value;
    newEnte.dtAvvio = this.formAnag.get('dtAvvio').value;
    newEnte.codCodiceInterbancarioCbill = blank2Null(this.formAnag.get('codCodiceInterbancarioCbill').value);
    newEnte.emailAmministratore = this.formAnag.get('emailAmministratore').value;
    newEnte.applicationCode = this.formAnag.get('applicationCode').value;
    newEnte.codiceFiscaleEnte = this.formAnag.get('codiceFiscaleEnte').value;
    newEnte.enteSilInviaRispostaPagamentoUrl =  blank2Null(this.formAnag.get('enteSilInviaRispostaPagamentoUrl').value);
    newEnte.codTipoEnte = (this.formAnag.get('codTipoEnte').value as TassonomiaCodDesc).code;
    newEnte.codRpDatiVersDatiSingVersIbanAccredito = this.formAnag.get('codRpDatiVersDatiSingVersIbanAccredito').value;
    newEnte.codRpDatiVersDatiSingVersBicAccreditoSeller = false;
    newEnte.deRpEnteBenefDenominazioneBeneficiario = this.formAnag.get('deRpEnteBenefDenominazioneBeneficiario').value;
    newEnte.deRpEnteBenefIndirizzoBeneficiario = this.formAnag.get('deRpEnteBenefIndirizzoBeneficiario').value;
    newEnte.deRpEnteBenefCivicoBeneficiario = this.formAnag.get('deRpEnteBenefCivicoBeneficiario').value;
    newEnte.codRpEnteBenefCapBeneficiario = this.formAnag.get('codRpEnteBenefCapBeneficiario').value;
    newEnte.codRpEnteBenefNazioneBeneficiario = (this.formAnag.get('nazione').value as Nazione)?.codiceIsoAlpha2;
    newEnte.deRpEnteBenefProvinciaBeneficiario = (this.formAnag.get('prov').value as Provincia)?.sigla;
    newEnte.deRpEnteBenefLocalitaBeneficiario = (this.formAnag.get('comune').value as Comune)?.comune;
    newEnte.deRpEnteBenefTelefonoBeneficiario = this.formAnag.get('deRpEnteBenefTelefonoBeneficiario').value;
    newEnte.deRpEnteBenefSitoWebBeneficiario = this.formAnag.get('deRpEnteBenefSitoWebBeneficiario').value;
    newEnte.deRpEnteBenefEmailBeneficiario =  blank2Null(this.formAnag.get('deRpEnteBenefEmailBeneficiario').value);
    newEnte.deAutorizzazione =  blank2Null(this.formAnag.get('deAutorizzazione').value);
    newEnte.deInformazioniEnte = blank2Null(this.formAnag.get('deInformazioniEnte').value);
    newEnte.linguaAggiuntiva =  blank2Null((this.formAnag.get('linguaAggiuntiva').value as LingueCodDesc).code);
    newEnte.flagInsertDefaultSet = this.formAnag.get('flagInsertDefaultSet').value;

    this.adminEntiService.upsertEnte(this.modeAnag, newEnte).subscribe( data => {
      this.ente = data;
      this.mygovEnteId = data.mygovEnteId;
      this.setForm(data);
      this.toastrService.success('Ente '+(this.modeAnag==='edit'?'aggiornato':'inserito')+' correttamente.');
      this.setMode('view');
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore recuperando il dettaglio dovuto', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }
}
