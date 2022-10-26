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
import { ToastrService } from 'ngx-toastr';
import {
    Comune, EMAIL_SOURCE_TYPE, fieldsMatchValidator, LocationService, manageError, Nazione,
    OverlaySpinnerService, Provincia, User, UserService, validateFormFun, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { forkJoin, Observable, Subject, Subscription } from 'rxjs';
import { delay, first, map } from 'rxjs/operators';

import { Component, ElementRef, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { faUserCog } from '@fortawesome/free-solid-svg-icons';

import { UserMailValidation } from '../../model/user-mail-validation';
import { MailValidationService } from '../../services/mail-validation.service';
import { UtenteService } from '../../services/utente.service';

@Component({
  selector: 'app-dati-utente',
  templateUrl: './dati-utente.component.html',
  styleUrls: ['./dati-utente.component.scss']
})
export class DatiUtenteComponent implements OnInit, OnDestroy, WithTitle {

  private addressControlNames = [
    'indirizzo',
    'civico',
    'cap',
    'nazione',
    'provincia',
    'comune'];

  constructor(
    private toastrService: ToastrService,
    private elementRef: ElementRef,
    private overlaySpinnerService: OverlaySpinnerService,
    private locationService: LocationService,
    private userService: UserService,
    private utenteService: UtenteService,
    private formBuilder: FormBuilder,
    private mailValidationService: MailValidationService,
  ) {
    this.formEmail = this.formBuilder.group({
      email: [null, []],
      emailNew: [null, [Validators.required, Validators.email]],
      emailConfirm: [null, []], //validators added later since need reference to form
      emailCode: [null, [Validators.required, Validators.pattern("^[0-9]{6}$")]]
    });
    this.formEmail.get('emailConfirm').setValidators([Validators.required, Validators.email, fieldsMatchValidator(this.formEmail.get('emailNew'))]);
    this.formEmailChangesSub = this.formEmail.valueChanges.subscribe(validateFormFun(this.formEmail, this.formEmailErrors));

    this.formAddress = new FormGroup({});
    this.addressControlNames.forEach(controlName => {
      this.formAddress.addControl(controlName,new FormControl());
    });

    this.formAddress.valueChanges.subscribe( values => {
      this.requiredAddress = _.sum(this.addressControlNames.map( control => values?.[control] ? 1 : 0)) > 0;
    });
    this.formAddressChangesSub = this.formAddress.valueChanges.subscribe(validateFormFun(this.formAddress, this.formAddressErrors));
  }
  get titleLabel(){ return "Dati personali" }
  get titleIcon(){ return faUserCog }

  private _loggedUser: User;

  EMAIL_SOURCE_TYPE = EMAIL_SOURCE_TYPE;

  nazione: number;
  provincia: number;
  comune: number;
  cap: string;
  indirizzo: string;
  civico: string;

  nazioni: Nazione[]=[];
  province: Provincia[]=[];
  comuni: Comune[] = [];

  formEmail: FormGroup;
  formEmailErrors = {};
  formAddress: FormGroup;
  formAddressErrors = {};
  private formAddressChangesSub: Subscription;
  private formEmailChangesSub: Subscription;
  addressViewMode = true;
  private _requiredAddress = false;

  lastLoginFormatted: string;
  emailMode = 'view';
  private userMailValidation: UserMailValidation;
  private forcedMailValidationWarningSub: Subscription;

  private manageErrorUserMailValidationOutcome(userMailValidation: UserMailValidation){
    if(!userMailValidation.outcome || userMailValidation.outcome=='OK'){
      return false;
    }
    if(userMailValidation.outcome=='EXPIRED'){
      this.toastrService.warning('Indirizzo mail non validato in tempo utile. È necessario ricominciare da capo la procedura di validazione.','Validazione indirizzo mail');
      this.loggedUser.emailNew = null;
      this.refreshEmailFields();
      this.emailMode = 'view';
    } else if(userMailValidation.outcome=='MAX_TRIES'){
      this.toastrService.warning('Numero massimo di tentativi superato. È necessario ricominciare da capo la procedura di validazione.','Validazione indirizzo mail');
      this.loggedUser.emailNew = null;
      this.refreshEmailFields();
      this.emailMode = 'view';
    } else if(userMailValidation.outcome=='WRONG_CODE'){
      this.toastrService.warning('Codice inserito errato. Restano a disposizione '+userMailValidation.remainingTries+' tentativi.','Validazione indirizzo mail');
    } else {
      this.toastrService.error('Errore: '+userMailValidation.outcome,'Validazione indirizzo mail');
    }
    return true;
  }

  ngOnInit(): void {
    this.loggedUser = this.userService.getLoggedUser();
    if(this.loggedUser.emailNew){
      const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
      this.mailValidationService.userMailCheckValidationStatus().subscribe( userMailValidation => {
        this.userMailValidation = userMailValidation;
        this.overlaySpinnerService.detach(spinner);
        if(!this.manageErrorUserMailValidationOutcome(userMailValidation)) {
          this.emailMode = 'enterCode';
          this.toastrService.info('Inserire il codice di validazione ricevuto nella propria casella email. Tentativi a disposizione: '+userMailValidation.remainingTries,'Validazione indirizzo mail');
        }
      }, manageError('Errore leggendo i dati utente', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
    } else if(!this.loggedUser.email) {
      this.toastrService.warning('È necessario inserire e validare un indirizzo email per poter utilizzare tutte le funzionalità.','Validazione indirizzo mail');
    };
    forkJoin([
      this.loadNazioni(),
      this.loadProvince() ]).pipe(first()).subscribe( () =>
      this.setUserData(this.loggedUser)
    );
    this.forcedMailValidationWarningSub = this.mailValidationService.getForcedMailValidationWarningObservable().subscribe(() => {
      this.toastrService.warning('Non è possibile accedere alle altre funzionalità finchè non si è validato un indirizzo email.','Validazione indirizzo mail');
    } );
  }

  ngOnDestroy():void {
    this.formAddressChangesSub?.unsubscribe();
    this.formEmailChangesSub?.unsubscribe();
    this.forcedMailValidationWarningSub?.unsubscribe();
  }

  get loggedUser(){
    return this._loggedUser;
  }

  set loggedUser(loggedUser: User){
    this._loggedUser = loggedUser;
    this.refreshEmailFields();
  }

  private refreshEmailFields(){
    this.lastLoginFormatted = this.loggedUser.lastLogin?.toFormat('dd/LL/yyyy HH:mm:ss') || '';
    this.formEmail.get('email').setValue(this.loggedUser.email);
    this.formEmail.get('emailNew').setValue(this.loggedUser.emailNew);
  }

  get requiredAddress(){
    return this._requiredAddress;
  }

  get emailSourceTypeFormatted(){
    let code;
    if(this.loggedUser.emailNew)
      code = 'O';
    else if(!this.loggedUser.email)
      code = 'M';
    else
      code = this.loggedUser.emailSourceType;
    return User.EMAIL_SOURCE_TYPE_DESCR?.[code] || 'Sconosciuto';
  }

  set requiredAddress(requiredAddress:boolean){
    if(requiredAddress !== this._requiredAddress){
      this._requiredAddress = requiredAddress;
      this.addressControlNames.forEach(control => {
        if(requiredAddress)
          this.formAddress.get(control).setValidators(Validators.required);
        else
          this.formAddress.get(control).clearValidators();
        setTimeout(()=>this.formAddress.get(control).updateValueAndValidity({onlySelf: true, emitEvent: false}),0);
      });
      setTimeout(()=>this.formAddress.updateValueAndValidity({onlySelf: true, emitEvent: false}),0);
    }
  }

  get nazioneAsReadonlyField(){
    const nazioneId = this.formAddress.get('nazione').value;
    return nazioneId ? (this.nazioni.find(nazione => nazione.nazioneId == nazioneId)?.nomeNazione ?? '') : '';
  }

  get provinciaAsReadonlyField(){
    const provinciaId = this.formAddress.get('provincia').value;
    return provinciaId ? (this.province.find(provincia => provincia.provinciaId == provinciaId)?.provincia ?? '') : '';
  }

  get comuneAsReadonlyField(){
    const comuneId = this.formAddress.get('comune').value;
    return comuneId ? (this.comuni.find(comune => comune.comuneId == comuneId)?.comune ?? '') : '';
  }

  private loadNazioni() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    return this.locationService.getNazioni().pipe( map(
      nazioni => {
       this.overlaySpinnerService.detach(spinner);
        this.nazioni = nazioni;
      }, manageError('Errore recuperando le nazioni', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) )
    );
  }

  private loadProvince() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    return this.locationService.getProvince().pipe( map(
      province => {
       this.overlaySpinnerService.detach(spinner);
        this.province = province;
      }, manageError('Errore recuperando le province', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) )
    );
  }

  private loadComuni(provincia: Provincia):Observable<void> {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    const resp = new Subject<void>();
    this.locationService.getComuni(provincia).pipe(delay(0)).subscribe(
      comuni => {
        this.overlaySpinnerService.detach(spinner);
        this.comuni = comuni;
        resp.next(null);
        resp.complete();
      }, manageError('Errore recuperando i comuni', this.toastrService, () => {resp.complete(); this.overlaySpinnerService.detach(spinner)}) );
    return resp;
  }

  nazioneOnChange(nazioneId: number) {
    this.formAddress.get('provincia').setValue(null);
    this.formAddress.get('comune').setValue(null);
    this.formAddress.get('provincia').disable();
    this.formAddress.get('comune').disable();
    if (nazioneId === 0) {
      this.formAddress.get('nazione').setValue(null);
    } else if (nazioneId === 1) {
      this.formAddress.get('provincia').enable();
    }
  }

  provinciaOnChange(provinciaId: number):Observable<void> {
    this.formAddress.get('comune').setValue(null);
    this.comuni = null;
    if (provinciaId === 0) {
      this.formAddress.get('comune').disable();
      this.formAddress.get('provincia').setValue(null);
    } else {
      this.formAddress.get('comune').enable();
    }
    return this.loadComuni(new Provincia(provinciaId));
  }

  comuneOnChange(comuneId: number) {
    if (!comuneId || comuneId === 0) {
      this.formAddress.get('comune').setValue(null);
    }
  }

  private setUserData(person: User){
    this.formAddress.get('indirizzo').setValue(person.indirizzo);
    this.formAddress.get('civico').setValue(person.civico);
    this.formAddress.get('cap').setValue(person.cap);
    this.formAddress.get('nazione').setValue(person.nazioneId);
    if(person.nazioneId)
      this.nazioneOnChange(person.nazioneId);
    this.formAddress.get('provincia').setValue(person.provinciaId);
    this.comuneOnChange(null);
    if(person.provinciaId)
      this.provinciaOnChange(person.provinciaId).subscribe(()=>{
        this.formAddress.get('comune').setValue(person.comuneId);
        this.comuneOnChange(person.comuneId);
      });
  }

  onMailEdit(){
    this.emailMode = 'enterMail';
    this.formEmail.get('emailNew').setValue(null);
    this.formEmail.get('emailConfirm').setValue(null);
  }

  onResetEnterMail(){
    this.emailMode = 'view';
  }

  onResetEnterCode(){
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
      this.mailValidationService.userMailResetNewEmail().subscribe( () => {
        this.overlaySpinnerService.detach(spinner);
        this.loggedUser.emailNew = null;
        this.refreshEmailFields();
        this.emailMode = 'view';
      }, manageError('Errore resettando i dati di validazione email.', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
    this.emailMode = 'view';
  }

  onMailSave(){
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    const emailNew = this.formEmail.get('emailNew').value;
    this.mailValidationService.userMailSendNewEmail(emailNew).subscribe( userMailValidation => {
      this.loggedUser.emailNew = emailNew;
      this.refreshEmailFields();
      this.userMailValidation = userMailValidation;
      this.overlaySpinnerService.detach(spinner);
      this.emailMode = 'enterCode';
      this.formEmail.get('emailCode').setValue(null);
      this.toastrService.info('Inserire il codice di validazione ricevuto via email. Tentativi a disposizione: '+userMailValidation.remainingTries,'Validazione indirizzo mail');
    }, manageError('Errore salvando la nuova mail', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  onCodeSend(){
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    const code = this.formEmail.get('emailCode').value;
      this.mailValidationService.userMailSendCode(code).subscribe( userMailValidation => {
        this.overlaySpinnerService.detach(spinner);
        if(!this.manageErrorUserMailValidationOutcome(userMailValidation)) {
          this.toastrService.success('Email validata correttamente.','Validazione indirizzo mail');
          this.loggedUser.emailValidationNeeded = false;
          this.loggedUser.email = userMailValidation.emailAddress;
          this.loggedUser.emailNew = null;
          this.loggedUser.emailSourceType = EMAIL_SOURCE_TYPE.V;
          this.refreshEmailFields();
          this.emailMode = 'view';
        }
      }, manageError('Errore validando l\'email.', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  onEdit(){
    this.addressViewMode = false;
  }

  onCancelEdit(){
    this.addressViewMode = true;
    this.setUserData(this.loggedUser);
  }

  onEmpty(){
    this.setUserData(new User());
  }

  get isMailSaveInvalid(){
    return this.formEmail.get('emailNew').invalid || this.formEmail.get('emailConfirm').invalid;
  }

  get isMailSendCodeInvalid(){
    return this.formEmail.get('emailCode').invalid;
  }

  onSubmit(){
    const utente = new User();
    utente.nazioneId = this.formAddress.get('nazione').value;
    utente.provinciaId = this.formAddress.get('provincia').value;
    utente.comuneId = this.formAddress.get('comune').value;
    utente.cap = this.formAddress.get('cap').value;
    utente.indirizzo = this.formAddress.get('indirizzo').value;
    utente.civico = this.formAddress.get('civico').value;

    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.utenteService.update(utente)
      .subscribe(() => {
        this.userService.updateUserData(utente);
        this.loggedUser = this.userService.getLoggedUser();
        this.overlaySpinnerService.detach(spinner);
        this.toastrService.success('Dati utente aggiornati correttamente.');
      }, manageError('Errore aggiornando i dati utente', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }
}
