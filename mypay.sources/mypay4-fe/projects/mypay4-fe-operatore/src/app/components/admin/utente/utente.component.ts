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
    ConfirmDialogComponent
} from 'projects/mypay4-fe-common/src/lib/components/confirm-dialog/confirm-dialog.component';
import {
    MyPayBaseTableComponent
} from 'projects/mypay4-fe-common/src/lib/components/my-pay-table/my-pay-table.component';
import {
    ConfigurationService, Ente, JoinPipe, manageError, MyPayBreadcrumbsService,
    OverlaySpinnerService, PageStateService, PaginatorData, PATTERNS, TableAction, TableColumn,
    UserService, validateFormFunAsync, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { EMPTY, Observable, of, Subscription } from 'rxjs';
import { first, map, shareReplay, startWith } from 'rxjs/operators';

import { Location } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import {
    AbstractControl, AsyncValidatorFn, FormBuilder, FormGroup, Validators
} from '@angular/forms';
import { MatAutocomplete } from '@angular/material/autocomplete';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { faFolderMinus, faSearch, faUsers } from '@fortawesome/free-solid-svg-icons';

import { EnteRoles } from '../../../model/ente-roles';
import { Operatore } from '../../../model/operatore';
import { AdminUtenteService } from '../../../services/admin-utente.service';
import { EnteService } from '../../../services/ente.service';
import { ModifyMailComponent } from '../modify-mail/modify-mail.component';

@Component({
  selector: 'app-utente',
  templateUrl: './utente.component.html',
  styleUrls: ['./utente.component.scss']
})
export class UtenteComponent implements OnInit, OnDestroy, WithTitle {

  get titleLabel(){ return this.pageTitle ?? "Dettaglio utente" }
  get titleIcon(){ return faUsers }

  private pageTitle;

  externalProfileEnabled: boolean;

  private mygovUtenteId: number;
  private operatore: Operatore;
  private previousPageNavId: number;
  activeTabIndex: number = 0;
  modeAnag: string;
  private isInsertForOperatore: boolean = false;
  formAnag: FormGroup;
  formAnagErrors:Object = {};
  formAnagErrorMsg = {
    usernameExists: 'Questo username è già esistente'
  };
  formAnagModified: boolean = false;
  private valueChangesSub:Subscription;
  private valueChangesEnteSub:Subscription;
  private userSub:Subscription;
  private allEnti: Observable<Ente[]>;

  @ViewChild(MatAutocomplete) autocompleteEnte: MatAutocomplete;
  formEnte: FormGroup;
  formEnteErrors:Object = {};
  enteOptions: Ente[];
  enteFilteredOptions: Observable<Ente[]>;
  blockingError: boolean = false;
  modifyUserEnabled: boolean;

  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<EnteRoles>;
  tableColumns: TableColumn[] = [
    new TableColumn('thumbLogoEnte', null, {type:'img64', ariaLabel:'Logo'}),
    new TableColumn('deNomeEnte', 'Nome ente'),
    new TableColumn('codIpaEnte', 'Codice IPA ente'),
    new TableColumn('emailAddress', 'Email'),
    new TableColumn('roles', 'Ruoli', {pipe: JoinPipe, pipeArgs: [', ', {'ROLE_ADMIN':'Amministratore', 'ROLE_OPER':'Operatore'}]}),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(faSearch, this.gotoDetailsEnte, null, 'Dettaglio ente'),
      new TableAction(faFolderMinus, this.decoupleEnte, this.decoupleEnteEnabled, 'Disassocia da ente'),
      new TableAction(faSearch, this.coupleEnte, null, 'Abilita a tutti i dovuti'),
      new TableAction(faFolderMinus, this.decoupleTipiDovutoEnte, null, 'Disabilita da tutti i dovuti'),
      new TableAction(faSearch, this.changeEmail, ()=>true, 'Modifica email per ente'),
      ] } ) ];
  tableData: EnteRoles[];
  paginatorData: PaginatorData;


  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private adminUtenteService: AdminUtenteService,
    private toastrService: ToastrService,
    private elementRef: ElementRef,
    private overlaySpinnerService: OverlaySpinnerService,
    private location: Location,
    private pageStateService: PageStateService,
    private enteService: EnteService,
    private dialog: MatDialog,
    private conf: ConfigurationService,
    private mypayBreadcrumbsService: MyPayBreadcrumbsService,
    userService: UserService,
  ) {
    this.userSub = userService.getLoggedUserObs().subscribe(() => {
      const isUserAdmin = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ROLE);
      this.modifyUserEnabled = isUserAdmin || this.conf.getBackendProperty('adminEnteEditUserEnabled', false);
    });

    //load mode from navigation state
    this.setAnagMode(this.router.getCurrentNavigation()?.extras?.state?.mode);

    this.externalProfileEnabled = this.conf.getBackendProperty('externalProfileEnabled');

    this.previousPageNavId = this.router.getCurrentNavigation()?.extras?.state?.backNavId;
   }

  ngOnInit(): void {

    this.formAnag = this.formBuilder.group({
      username: ['', [Validators.required], [this.usernameValidator()]],
      nome: ['', [Validators.required]],
      cognome: ['', [Validators.required]],
      codiceFiscale: ['', [Validators.required, Validators.pattern(PATTERNS.codiceFiscale)]],
      email: ['', [Validators.required, Validators.email]],
    }, {updateOn:'blur'});

    this.formEnte = this.formBuilder.group({
      ente: ['', [Validators.required, this.enteValidator]],
      email: [null, [Validators.required, Validators.email]],
      coupleWithAllTipiDovuto: [false, []]
    });

    //retrieve page state data if navigating back
    let datailOperatoreFromPageState: Observable<Operatore>;
    let paginatorDataFromPageState: PaginatorData;
    let tableFilterData: string;
    if(this.pageStateService.isNavigatingBack()){
      const pageState = this.pageStateService.restoreState();
      if(pageState){
        this.modeAnag = pageState.modeAnag;
        this.activeTabIndex = pageState.activeTabIndex;
        this.formEnte.setValue(pageState.formEnteData);
        if(!pageState.relooadData){
          datailOperatoreFromPageState = of(pageState.operatore);
          paginatorDataFromPageState = pageState.paginatorData;
          tableFilterData = pageState.tableFilterData;
        }
      }
    }

    //load and cache allEnti
    this.allEnti = this.enteService.getAllEnti().pipe(shareReplay(1));

    if(this.modeAnag === 'insert'){
      this.formAnagModified = true;
    } else {
      //load user param from url
      const params = this.route.snapshot.params;
      this.mygovUtenteId = +params['id'];

      //load utente data
      const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
      (datailOperatoreFromPageState || this.adminUtenteService.detailOperatore(this.mygovUtenteId)).subscribe(operatore => {
        this.operatore = operatore;
        this.setFormValues(operatore);
        this.tableData = this.operatore.fullEntiRoles;
        this.checkLogoEnte();
        if(paginatorDataFromPageState || tableFilterData)
          setTimeout(()=>{
            this.paginatorData = paginatorDataFromPageState;
            console.log('setting filter to', tableFilterData);
            this.mypayTableComponent.tableDataSource.filter = tableFilterData;
          });
        this.overlaySpinnerService.detach(spinner);
        //load enti, filter the ones already associated
        this.loadEntiNotCoupled();
      }, manageError('Errore recuperando i dati dell\'utente', this.toastrService, () => {this.blockingError=true; this.overlaySpinnerService.detach(spinner)}) );
    }

    this.valueChangesSub = this.formAnag.statusChanges.subscribe(
      validateFormFunAsync(this.formAnag, this.formAnagErrors, this.formAnagErrorMsg, this.checkFormModified(this)) );

    this.valueChangesEnteSub = this.formEnte.statusChanges.subscribe(
      validateFormFunAsync(this.formEnte, this.formEnteErrors) );

  }

  ngOnDestroy():void {
    this.valueChangesSub?.unsubscribe();
    this.valueChangesEnteSub?.unsubscribe();
    this.userSub?.unsubscribe();
  }

  private setFormValues(operatore: Operatore){
    this.formAnag.get('username').setValue(operatore.username);
    this.formAnag.get('nome').setValue(operatore.nome);
    this.formAnag.get('cognome').setValue(operatore.cognome);
    this.formAnag.get('codiceFiscale').setValue(operatore.codiceFiscale);
    this.formAnag.get('email').setValue(operatore.email);
  }

  private setAnagMode(mode: string){
    if(mode === 'insert' || mode === 'insertForAddOperatore'){
      this.isInsertForOperatore = mode === 'insertForAddOperatore';
      this.modeAnag = 'insert';
      this.pageTitle = 'Inserimento utente';
      this.mypayBreadcrumbsService.updateCurrentBreadcrumb(this.pageTitle);

    }else{
      this.modeAnag = mode || 'view';
      this.pageTitle = null;
      this.mypayBreadcrumbsService.resetCurrentBreadcrumb();
    }
  }

  private setDisabled(control: AbstractControl, disabled: boolean){
    if(disabled)
      control.disable();
    else
      control.enable();
  }

  private usernameValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      return this.modeAnag !== 'insert' ? EMPTY : this.adminUtenteService.checkUsernameExists(control.value)
        .pipe(
          map(exists => {
            // if username is taken
            if (exists) {
              // return error (key: value)
              return { 'usernameExists': true};
            }
          })
        );
    };
  }

  private isFieldModified(field: string){
    return this.operatore[field] !== this.formAnag.get(field).value;
  }

  private checkFormModified(thisRef: UtenteComponent): (data: Object) => void {
    return function(data: Object){
      thisRef.formAnagModified = thisRef.modeAnag === 'insert' ||
        thisRef.isFieldModified('nome') || thisRef.isFieldModified('cognome') ||
        thisRef.isFieldModified('codiceFiscale') || thisRef.isFieldModified('email');
    }
  }

  saveConfirmMsg(thisRef: UtenteComponent): string {
    if(thisRef.modeAnag === 'insert'){
      return "Confermi l'inserimento del nuovo utente?";
    } else {
      let msg = ["Confermi la modifica dei seguenti campi?"];
      ['nome', 'cognome', 'codiceFiscale', 'email']
        .filter(field => thisRef.isFieldModified(field))
        .forEach(field => msg.push(field+": "+thisRef.operatore[field]+" -> "+thisRef.formAnag.get(field).value));
      return msg.join("<br>");
    }
  }

  onSave(): void {
    const thisRef = this;
    if(thisRef.modeAnag === 'insert'){
      const newOperatore = new Operatore();
      ['username', 'nome', 'cognome', 'codiceFiscale', 'email']
        .forEach(field => newOperatore[field]=thisRef.formAnag.get(field).value);
      const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
      thisRef.adminUtenteService.insertAnagraficaOperatore(newOperatore).subscribe(userId => {
        thisRef.overlaySpinnerService.detach(spinner);
        thisRef.toastrService.success('Anagrafica utente inserita correttamente.');
        if(thisRef.isInsertForOperatore){
          thisRef.pageStateService.addToSavedState(thisRef.previousPageNavId, 'createdUsername', newOperatore.username);
          thisRef.location.back();
        } else {
          newOperatore.fullEntiRoles = [];
          newOperatore.userId = userId;
          thisRef.operatore = newOperatore;
          thisRef.loadEntiNotCoupled();
          thisRef.setAnagMode('view');
          thisRef.router.navigate(['admin', 'utenti', userId],{replaceUrl: true});
        }
      }, manageError('Errore inserendo l\'anagrafica utente', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
    } else {
      const newOperatore = _.cloneDeep(thisRef.operatore);
      ['nome', 'cognome', 'codiceFiscale', 'email']
        .filter(field => thisRef.isFieldModified(field))
        .forEach(field => newOperatore[field]=thisRef.formAnag.get(field).value);
      const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
      thisRef.adminUtenteService.updateAnagraficaOperatore(newOperatore).subscribe(() => {
        thisRef.overlaySpinnerService.detach(spinner);
        thisRef.toastrService.success('Anagrafica utente aggiornata correttamente.');
        thisRef.operatore = newOperatore;
        thisRef.setAnagMode('view');
      }, manageError('Errore aggiornando l\'anagrafica utente', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
    }
    thisRef.pageStateService.addToSavedState(thisRef.previousPageNavId, 'reloadData', true);
  }

  onReset(): void {
    this.setAnagMode('view');
    this.setFormValues(this.operatore);
  }


  onResetInserting(){
    this.formAnag.reset();
  }

  enableEdit(): void {
    this.setAnagMode('edit');
  }

  goBack(){
    this.location.back();
  }

  gotoDetailsEnte(elementRef: EnteRoles, thisRef: UtenteComponent, eventRef: any){
    if(eventRef)
      eventRef.stopPropagation();
    thisRef.pageStateService.saveState({
      modeAnag: thisRef.modeAnag,
      activeTabIndex: thisRef.activeTabIndex,
      operatore: thisRef.operatore,
      formEnteData: thisRef.formEnte.value,
      tableFilterData: thisRef.mypayTableComponent.tableDataSource.filter,
      paginatorData: {
        pageSize: thisRef.mypayTableComponent.paginator.pageSize,
        pageIndex: thisRef.mypayTableComponent.paginator.pageIndex
      }
    });
    thisRef.router.navigate(['admin', 'enti', 'details', elementRef.mygovEnteId]);
  }

  detachEnte(elementRef: EnteRoles, thisRef: UtenteComponent, eventRef: any){
    console.log('detachEnte');
  }

  loadEntiNotCoupled() {
    //load enti from backend
    this.allEnti.subscribe(enti => {
      this.enteOptions = enti.filter(ente => ! this.operatore.fullEntiRoles.some(anEnte => anEnte.codIpaEnte === ente.codIpaEnte));
      //console.log(this.enteOptions);
      //init autocomplete feature of ente field
      this.enteFilteredOptions = this.formEnte.get('ente').valueChanges
        .pipe(
          startWith(''),
          map(value => typeof value === 'string' || !value ? value : value.deNomeEnte),
          map(deNomeEnte => deNomeEnte ? this._enteFilter(deNomeEnte) : this.enteOptions.slice())
        );
    }, error => {
      if(error?.status === 401){
        //if not authorized user is not an operatore -> go to not-authorized page
        this.router.navigate(['not-authorized']);
      } else
        manageError('Errore caricando l\'elenco degli enti', this.toastrService, ()=>{this.blockingError=true})(error);
    } );
  }

  private _enteFilter(name: string): Ente[] {
    const filterValue = name.toLowerCase();
    return this.enteOptions.filter(option => option.deNomeEnte.toLowerCase().indexOf(filterValue) !== -1);
  }

  private verifyEnte(){
    const currentValue = this.formEnte.get('ente').value;
    if(!currentValue || typeof currentValue==='string' && (<string>currentValue).trim().length===0){
      this.enteService.setCurrentEnte(null);
    } else if(currentValue.mygovEnteId != null) {
      this.enteService.setCurrentEnte(currentValue);
    }
    //reset input field to previous valid value when new value is invalid
    this.formEnte.get('ente').setValue(this.enteService.getCurrentEnte());
  }

  enteFocusout($ev){
    if(!this.autocompleteEnte.isOpen)
      this.verifyEnte();
  }

  enteSelected(ente: Ente){
    this.verifyEnte();
  }

  enteClosed(){
    this.verifyEnte();
  }

  private enteValidator = (control: AbstractControl):{[key: string]: boolean} | null => {
    return ( !control.value || control.value.mygovEnteId != null ) ? null : {'invalid': true};
  };

  enteDisplayFn(ente: Ente): string {
    return ente ? ente.deNomeEnte : '';
  }

  private checkLogoEnte(){
    this.allEnti.pipe(first()).subscribe(enti => {
      this.tableData.filter(enteRoles => !enteRoles.thumbLogoEnte).forEach(enteRoles => {
        enteRoles.thumbLogoEnte = enti.find(ente => ente.codIpaEnte === enteRoles.codIpaEnte)?.thumbLogoEnte;
      });
    });
  }

  coupleEnte(elementRef: EnteRoles, thisRef: UtenteComponent, eventRef: any) {
    if (thisRef) {
      const msg = 'Confermi di voler disassociare l\'operatore dall\'ente "' + elementRef.deNomeEnte + '"?';

      thisRef.dialog.open(ConfirmDialogComponent, { autoFocus: false, data: { message: msg } })
        .afterClosed().pipe(first()).subscribe(result => {
          if (result === "true") {
            const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
            thisRef.adminUtenteService.coupleEnte(thisRef.operatore, elementRef.mygovEnteId, 'DOVUTI').subscribe(enteRoles => {
              thisRef.overlaySpinnerService.detach(spinner);
              thisRef.toastrService.success('Dovuti dell\'ente associati all\'operatore.');
            }, manageError('Errore associando l\'ente all\'operatore', thisRef.toastrService, () => { thisRef.overlaySpinnerService.detach(spinner) }));
            thisRef.operatore.email = null;
          }
        });
    }
    else {
      const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
      const ente = this.formEnte.get('ente').value as Ente;
      const couplingMode = this.formEnte.get('coupleWithAllTipiDovuto').value ? 'ENTE_DOVUTI' : 'ENTE';

      this.operatore.email = this.formEnte.get('email').value.trim();

      this.adminUtenteService.coupleEnte(this.operatore, ente.mygovEnteId, couplingMode).subscribe(enteRoles => {
        //update list of enti of operatore
        this.operatore.fullEntiRoles = [enteRoles, ...this.operatore.fullEntiRoles];
        console.log('fullEntiRoles', this.operatore.fullEntiRoles);
        //..and table
        this.tableData = this.operatore.fullEntiRoles;
        this.checkLogoEnte();
        //reset field for associate new ente
        this.formEnte.reset();
        //recalculate list of ente for association
        this.loadEntiNotCoupled();
        this.overlaySpinnerService.detach(spinner);
        this.toastrService.success('Ente associato all\'operatore.');
      }, manageError('Errore associando l\'ente all\'operatore', this.toastrService, () => { this.overlaySpinnerService.detach(spinner) }));
      this.operatore.email = null;

    }
  }

  decoupleEnteEnabled(elementRef: EnteRoles, thisRef: UtenteComponent) {
    return !thisRef.externalProfileEnabled;
  }

  decoupleEnte(elementRef: EnteRoles, thisRef: UtenteComponent, eventRef: any) {
    const msg = 'Confermi di voler disassociare l\'operatore dall\'ente "'+elementRef.deNomeEnte+'"?';

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
    .afterClosed().pipe(first()).subscribe(result => {
      if(result==="true")
        thisRef.adminUtenteService.decoupleEnte(thisRef.operatore, elementRef.mygovEnteId, false).subscribe(() => {
          //update list of enti of operatore
          thisRef.operatore.fullEntiRoles = thisRef.operatore.fullEntiRoles.filter(e => e.mygovEnteId !== elementRef.mygovEnteId);
          //..and table
          thisRef.tableData = thisRef.operatore.fullEntiRoles;
          //recalculate list of ente for association
          thisRef.loadEntiNotCoupled();
          thisRef.toastrService.success('Ente disassociato dall\'operatore.');
        }, manageError('Errore disassociando l\'ente all\'operatore', thisRef.toastrService) );
    });
  }

  decoupleTipiDovutoEnte(elementRef: EnteRoles, thisRef: UtenteComponent, eventRef: any) {
    const msg = 'Confermi di voler disabilitare l\'operatore da tutti i suoi tipi dovuto per l\'ente "'+elementRef.deNomeEnte+'"?';

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
    .afterClosed().pipe(first()).subscribe(result => {
      if(result==="true")
        thisRef.adminUtenteService.decoupleEnte(thisRef.operatore, elementRef.mygovEnteId, true).subscribe(() => {
          thisRef.toastrService.success('Tipi dovuto disabilitati dall\'operatore');
        }, manageError('Errore disabilitando i tipi dovuto dall\'operatore', thisRef.toastrService) );
    });
  }


  changeEmail(elementRef: EnteRoles, thisRef: UtenteComponent) {
    thisRef.dialog.open(ModifyMailComponent, {autoFocus:false, data: {
      operatore: thisRef.operatore,
      emailAddress: elementRef.emailAddress,
      mygovEnteId: elementRef.mygovEnteId,
      deNomeEnte: elementRef.deNomeEnte} } )
      .afterClosed().pipe(first()).subscribe(result => {
        if(result){
          elementRef.emailAddress = result.emailAddress;
        }
      });
  }

}
