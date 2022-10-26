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
    ConfirmDialogComponent
} from 'projects/mypay4-fe-common/src/lib/components/confirm-dialog/confirm-dialog.component';
import {
    MyPayBaseTableComponent
} from 'projects/mypay4-fe-common/src/lib/components/my-pay-table/my-pay-table.component';
import {
    ConfigurationService, Ente, manageError, MapPipe, OverlaySpinnerService, PageStateService,
    PaginatorData, TableAction, TableColumn, UserService, validateFormFun, validateFormFunAsync,
    WithActions, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';
import { Observable, Subscription } from 'rxjs';
import { first, map } from 'rxjs/operators';

import { DatePipe } from '@angular/common';
import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import {
    AbstractControl, AsyncValidatorFn, FormBuilder, FormGroup, Validators
} from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { faBook, faSearch, faUsers } from '@fortawesome/free-solid-svg-icons';

import { Operatore } from '../../../model/operatore';
import { AdminUtenteService } from '../../../services/admin-utente.service';
import { ManageMode } from '../manage-mode';
import { ModifyMailComponent } from '../modify-mail/modify-mail.component';
import { RegistroComponent } from '../registro/registro.component';

@Component({
  selector: 'app-utenti',
  templateUrl: './utenti.component.html',
  styleUrls: ['./utenti.component.scss']
})
export class UtentiComponent implements OnInit, OnDestroy, WithTitle {

  @Input() ente: Ente;

  get titleLabel(){ return "Gestione utenti" }
  get titleIcon(){ return faUsers }

  json = JSON;

  externalProfileEnabled: boolean;
  insertUserEnabled: boolean;

  manageMode: ManageMode;
  opeForm: FormGroup;
  opeFormErrors = {};
  opeFormAnagErrorMsg = {
    usernameNotExists: 'Questo username non esiste',
    usernameAlreadyAssociated: 'Questo username è già associato'
  };
  private opeFormChangesSub:Subscription;
  form: FormGroup;
  formErrors = {};
  private formChangesSub:Subscription;

  private allAssociatedOperatori: string[];
  private mygovEnteId: number;
  private mygovTipoId: number;

  @ViewChild('myPayTable') mypayTableComponent: MyPayBaseTableComponent<Operatore>;
  tableColumns: TableColumn[] = [
    new TableColumn('username', 'ID utente'),
    new TableColumn('cognome', 'Cognome', {htlmId: ()=> this.manageMode !== ManageMode.TipoDovuto ? 'cognomeLungo' : null}),
    new TableColumn('nome', 'Nome', {htlmId: ()=> this.manageMode !== ManageMode.TipoDovuto ? 'nomeLungo' : null}),
    new TableColumn('email', 'Email'),
    new TableColumn('lastLogin', 'Ultimo login', {
      dispCondition: ()=>this.manageMode === ManageMode.Utente,
      sortable: (item: Operatore) => item.lastLogin?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] }),
    new TableColumn('amministratore', 'Amministratore', { pipe: MapPipe, pipeArgs: [{true: 'Si', false: 'No'}], dispCondition: ()=>this.manageMode === ManageMode.Ente }),
    new TableColumn('statoAssociazione', 'Stato', { dispCondition: ()=>this.manageMode === ManageMode.TipoDovuto }),
    new TableColumn('dtUltimaAbilitazione', 'Data ultima abilitazione', { dispCondition: ()=>this.manageMode === ManageMode.TipoDovuto,
      sortable: (item: Operatore) => item.dtUltimaAbilitazione?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] }),
    new TableColumn('dtUltimaDisabilitazione', 'Data ultima disabilitazione', { dispCondition: ()=>this.manageMode === ManageMode.TipoDovuto,
      sortable: (item: Operatore) => item.dtUltimaDisabilitazione?.valueOf(), pipe: DatePipe, pipeArgs: ['dd/MM/yyyy HH:mm:ss'] }),
    new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [
      new TableAction(faSearch, this.gotoDetails, null, 'Dettaglio operatore'),
      new TableAction(faSearch, this.decoupleEnte, this.decoupleEnteEnabled, 'Rimuovi associazione con ente'),
      new TableAction(faSearch, this.coupleEnte, this.coupleEnteEnabled, 'Abilita a tutti i dovuti'),
      new TableAction(faSearch, this.decoupleTipiDovutoEnte, this.decoupleTipiDovutoEnteEnabled, 'Disabilita da tutti i dovuti'),
      new TableAction(faSearch, this.decoupleTipo, this.decoupleTipoEnabled, 'Disabilita'),
      new TableAction(faSearch, this.coupleTipo, this.coupleTipoEnabled, 'Abilita'),
      new TableAction(faSearch, this.assignAdmin, this.assignAdminEnabled, 'Rendi amministratore'),
      new TableAction(faSearch, this.removeAdmin, this.removeAdminEnabled, 'Rimuovi ruolo amministratore'),
      new TableAction(faSearch, this.changeEmail, this.changeEmailEnabled, 'Modifica email per ente'),
      new TableAction(faBook, this.getRegistroTipoDovuto, this.getRegistroTipoDovutoEnabled, 'Registro cambio stato'),
      ] } ) ];
  tableData: Operatore[];
  paginatorData: PaginatorData;

  hasSearched: boolean = false;
  blockingError: boolean = false;

  private userSub: Subscription;
  isUserAdmin: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private pageStateService: PageStateService,
    private router: Router,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private adminUtenteService: AdminUtenteService,
    private toastrService: ToastrService,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private conf: ConfigurationService,
    userService: UserService,
  ) {
    this.userSub = userService.getLoggedUserObs().subscribe(() => {
      this.isUserAdmin = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ROLE);
      this.insertUserEnabled = this.isUserAdmin || this.conf.getBackendProperty('adminEnteEditUserEnabled', false);
    });
    this.form = this.formBuilder.group({
      username: [''],
      cognome: [''],
      nome: [''],
      onlyOper: [true],
      flgAssociato: ['']
    });
    this.externalProfileEnabled = this.conf.getBackendProperty('externalProfileEnabled');

    this.formChangesSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors));
    this.opeForm = this.formBuilder.group({
      operatore: [null, [Validators.required], [this.usernameValidator()]],
      email: [null, [Validators.required, Validators.email]],
      withTipiDovuto: [false]
    }, {updateOn:'blur'});
    this.opeFormChangesSub = this.opeForm.statusChanges.subscribe(
      validateFormFunAsync(this.opeForm, this.opeFormErrors, this.opeFormAnagErrorMsg) );
  }

  ngOnInit(): void {
    const params = this.route.snapshot.params;
    if (params['tipoId'])
      this.manageMode = ManageMode.TipoDovuto;
    else if (params['enteId'])
      this.manageMode = ManageMode.Ente;
    else
      this.manageMode = ManageMode.Utente;
    this.mygovEnteId = Number(params['enteId']);
    this.mygovTipoId = Number(params['tipoId']);

    let doSearch:boolean = this.manageMode === ManageMode.Ente || this.manageMode === ManageMode.TipoDovuto;
    //retrieve page state data if navigating back
    if(this.pageStateService.isNavigatingBack()){
      const pageState = this.pageStateService.restoreState();
      if(pageState){
        if(pageState.createdUsername)
          this.opeForm.get('operatore').setValue(pageState.createdUsername);
        doSearch = !pageState.reloadData;
        this.form.setValue(pageState.formData);
        setTimeout(()=>{
          if(pageState.reloadData){
            this.onSubmit();
          } else {
            this.tableData = pageState.tableData;
            this.paginatorData = pageState.paginatorData;
          }
        });
      }
    }

    if(doSearch)
      setTimeout(()=>this.onSubmit());

    //init allAssociatedOperatori
    if(this.manageMode === ManageMode.Ente){
      setTimeout(()=>{
        if(!this.allAssociatedOperatori)
          this.adminUtenteService.searchOperatoriForEnte(this.mygovEnteId, null, null, null).subscribe(data => {
            this.allAssociatedOperatori = data.map(oper => oper.username);
          });
      },3000);
    }
  }

  ngOnDestroy():void {
    this.formChangesSub?.unsubscribe();
    this.opeFormChangesSub?.unsubscribe();
  }

  private static LIST_STATI_ASSOCIATO = [{code:'', label:'Tutti'}, {code:true, label:'Associato'}, {code:false, label:'Disassociato'}];
  private static LIST_STATI_ABILITATO = [{code:'', label:'Tutti'}, {code:true, label:'Abilitato'}, {code:false, label:'Disabilitato'}];

  get listStati() {
    return this.manageMode === ManageMode.TipoDovuto ? UtentiComponent.LIST_STATI_ABILITATO : UtentiComponent.LIST_STATI_ASSOCIATO;
  }

  addOperatore() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    const i = this.opeForm.value;
    const newOpe = new Operatore();
    newOpe.username = i.operatore;
    newOpe.email = i.email.trim();
    const couplingMode = (i.withTipiDovuto || false) ? 'ENTE_DOVUTI' : 'ENTE';
    this.adminUtenteService.coupleEnte(newOpe, this.mygovEnteId, couplingMode).subscribe( ente => {
      this.allAssociatedOperatori.push(newOpe.username);
      this.opeForm.reset();
      this.onSubmit();
      this.overlaySpinnerService.detach(spinner);
      this.toastrService.success('L\'operatore aggiunto correttamente.' );
    }, manageError('Errore aggiungendo il nuovo operatore.', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  private usernameValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      return this.adminUtenteService.checkUsernameExists(control.value)
        .pipe(
          map(exists => {
            if(exists){
              if(this.allAssociatedOperatori?.includes(control.value))
                return { 'usernameAlreadyAssociated': true };
            } else {
              // return error (key: value)
              return { 'usernameNotExists': true };
            }
          })
        );
    };
  }

  onSubmit(){
    const i = this.form.value;
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    let searchOperatoriFun;
    let updateAllAssociatedOperatori = false;
    if (this.manageMode == ManageMode.Utente)
      searchOperatoriFun = this.adminUtenteService.searchOperatori
          .bind(this.adminUtenteService, i.username, i.cognome, i.nome, i.onlyOper);
    else if (this.manageMode == ManageMode.Ente){
      searchOperatoriFun = this.adminUtenteService.searchOperatoriForEnte
          .bind(this.adminUtenteService, this.mygovEnteId, i.username, i.cognome, i.nome);
      updateAllAssociatedOperatori = !i.username && !i.cognome && !i.nome;
    }
    else if (this.manageMode == ManageMode.TipoDovuto)
      searchOperatoriFun = this.adminUtenteService.searchOperatoriForEnteTipoDovuto
          .bind(this.adminUtenteService, this.mygovEnteId, this.mygovTipoId, i.username, i.cognome, i.nome, i.flgAssociato);

    searchOperatoriFun().subscribe(data => {
      if(updateAllAssociatedOperatori && !this.allAssociatedOperatori){
        this.allAssociatedOperatori = data.map(oper => oper.username);
      }
      this.hasSearched = true;
      this.tableData = data;
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando la ricerca', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }

  onReset(){
    this.form.reset({onlyOper: true});
    this.hasSearched = false;
    this.tableData = null;
  }

  gotoDetails(elementRef: Operatore, thisRef: UtentiComponent, eventRef: any){
    thisRef.navigateToUtentePage(elementRef.userId, 'view', thisRef, eventRef);
  }

  coupleEnteEnabled(elementRef: Operatore, thisRef: UtentiComponent) {
    return thisRef.manageMode === ManageMode.Ente;
  }

  coupleEnte(elementRef: Operatore, thisRef: UtentiComponent, eventRef: any) {
    const msg = 'Confermi di voler abilitare l\'operatore a tutti i suoi tipi dovuto per l\'ente "'+thisRef.ente.deNomeEnte+'"?';

    thisRef.dialog.open(ConfirmDialogComponent, { autoFocus: false, data: { message: msg } })
      .afterClosed().pipe(first()).subscribe(result => {
        if (result === "false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.adminUtenteService.coupleEnte(elementRef, thisRef.mygovEnteId, 'DOVUTI').subscribe(ente => {
          thisRef.allAssociatedOperatori.push(elementRef.username);
          thisRef.overlaySpinnerService.detach(spinner);
          thisRef.toastrService.success('Dovuti dell\'ente associati all\'operatore.');
        }, manageError('Errore associando l\'ente all\'operatore', thisRef.toastrService, () => { thisRef.overlaySpinnerService.detach(spinner) }));
      });
  }

  decoupleEnteEnabled(elementRef: Operatore, thisRef: UtentiComponent) {
    return !thisRef.externalProfileEnabled && thisRef.manageMode === ManageMode.Ente;
  }

  decoupleEnte(elementRef: Operatore, thisRef: UtentiComponent, eventRef: any) {
    const msg = `Confermi di voler disassociare l\'ente corrente dall\'operatore "${elementRef.username}"?`;

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.adminUtenteService.decoupleEnte(elementRef, thisRef.mygovEnteId, false).subscribe( ente => {
          const removedOperatoreIndex = thisRef.allAssociatedOperatori.indexOf(elementRef.username);
          if(removedOperatoreIndex!=-1)
            thisRef.allAssociatedOperatori.splice(removedOperatoreIndex, 1);
          thisRef.onSubmit();
          thisRef.overlaySpinnerService.detach(spinner);
          thisRef.toastrService.success('Ente disassociato dall\'operatore.' );
        }, manageError('Errore effettuando la disassociazione', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
    });
  }

  coupleTipo(elementRef: Operatore, thisRef: UtentiComponent, eventRef: any) {
    const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
    thisRef.adminUtenteService.coupleTipo(elementRef, thisRef.mygovTipoId).subscribe( ente => {
      elementRef.flgAssociato = true;
      elementRef.statoAssociazione = 'Abilitato';
      elementRef.dtUltimaAbilitazione = DateTime.now();
      WithActions.reset(elementRef);
      thisRef.overlaySpinnerService.detach(spinner);
      thisRef.toastrService.success('Operatore abilitato al tipo dovuto.' );
    }, manageError('Errore effettuando l\'abilitazione', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
  }

  coupleTipoEnabled(elementRef: Operatore, thisRef: UtentiComponent) {
    return thisRef.manageMode === ManageMode.TipoDovuto && !elementRef.flgAssociato;
  }


  decoupleTipiDovutoEnteEnabled(elementRef: Operatore, thisRef: UtentiComponent) {
    return thisRef.manageMode === ManageMode.Ente;
  }

  decoupleTipiDovutoEnte(elementRef: Operatore, thisRef: UtentiComponent, eventRef: any) {
    const msg = 'Confermi di voler disabilitare l\'operatore da tutti i suoi tipi dovuto per l\'ente "'+thisRef.ente.deNomeEnte+'"?';

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
    .afterClosed().pipe(first()).subscribe(result => {
      if(result==="true")
        thisRef.adminUtenteService.decoupleEnte(elementRef, thisRef.mygovEnteId, true).subscribe(() => {
          thisRef.toastrService.success('Tipi dovuto disabilitati dall\'operatore');
        }, manageError('Errore disabilitando i tipi dovuto dall\'operatore', thisRef.toastrService) );
    });
  }


  decoupleTipo(elementRef: Operatore, thisRef: UtentiComponent, eventRef: any) {
    const msg = `Confermi di voler disabilitare il tipo dovuto corrente dall\'operatore "${elementRef.username}"?`;

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.adminUtenteService.decoupleTipo(elementRef, thisRef.mygovTipoId).subscribe( ente => {
          elementRef.flgAssociato = false;
          elementRef.statoAssociazione = 'Disabilitato';
          elementRef.dtUltimaDisabilitazione = DateTime.now();
          WithActions.reset(elementRef);
          thisRef.overlaySpinnerService.detach(spinner);
          thisRef.toastrService.success('Operatore disabilitato dal tipo dovuto.' );
        }, manageError('Errore effettuando la disabilitazione', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
    });
  }

  decoupleTipoEnabled(elementRef: Operatore, thisRef: UtentiComponent) {
    return thisRef.manageMode === ManageMode.TipoDovuto && elementRef.flgAssociato;
  }

  assignAdmin(elementRef: Operatore, thisRef: UtentiComponent) {
    const msg = `Confermi di voler assegnare il ruolo di amministratore all\'operatore "${elementRef.username}"?`;

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.adminUtenteService.changeRuolo(elementRef, thisRef.mygovEnteId, true).subscribe( data => {
          elementRef.ruolo = 'ROLE_ADMIN';
          elementRef.amministratore = true;
          WithActions.reset(elementRef);
          thisRef.overlaySpinnerService.detach(spinner);
          thisRef.toastrService.success('Ruolo operatore aggiornato correttamente.' );
        }, manageError('Errore aggiornando il ruolo', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
    });
  }

  assignAdminEnabled(elementRef: Operatore, thisRef: UtentiComponent) {
    return !thisRef.externalProfileEnabled && thisRef.manageMode === ManageMode.Ente && elementRef.ruolo !== 'ROLE_ADMIN' && thisRef.isUserAdmin;
  }

  removeAdmin(elementRef: Operatore, thisRef: UtentiComponent) {
    const msg = `Confermi di voler rimuovere il ruolo di amministratore dall\'operatore "${elementRef.username}"?`;

    thisRef.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {message: msg}})
      .afterClosed().pipe(first()).subscribe(result => {
        if(result==="false") return;
        const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
        thisRef.adminUtenteService.changeRuolo(elementRef, thisRef.mygovEnteId, false).subscribe( data => {
          elementRef.ruolo = '';
          elementRef.amministratore = false;
          WithActions.reset(elementRef);
          thisRef.overlaySpinnerService.detach(spinner);
          thisRef.toastrService.success('Ruolo operatore aggiornato correttamente.' );
        }, manageError('Errore aggiornando il ruolo', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}) );
    });
  }

  removeAdminEnabled(elementRef: Operatore, thisRef: UtentiComponent) {
    return !thisRef.externalProfileEnabled && thisRef.manageMode === ManageMode.Ente && elementRef.ruolo === 'ROLE_ADMIN' && thisRef.isUserAdmin;
  }

  changeEmail(elementRef: Operatore, thisRef: UtentiComponent) {
    thisRef.dialog.open(ModifyMailComponent, {autoFocus:false, data: {
      operatore: elementRef,
      emailAddress: elementRef.email,
      mygovEnteId: thisRef.mygovEnteId,
      deNomeEnte: thisRef.ente?.deNomeEnte } } )
      .afterClosed().pipe(first()).subscribe(result => {
        if(result){
          elementRef.email = result.emailAddress;
        }
      });
  }

  changeEmailEnabled(elementRef: Operatore, thisRef: UtentiComponent) {
    return thisRef.manageMode === ManageMode.Ente && thisRef.isUserAdmin;
  }

  getRegistroTipoDovutoEnabled(elementRef: Operatore, thisRef: UtentiComponent) {
    return thisRef.manageMode === ManageMode.TipoDovuto && thisRef.isUserAdmin;
  }

  getRegistroTipoDovuto(elementRef: Operatore, thisRef: UtentiComponent) {
    thisRef.adminUtenteService.getRegistroTipoDovutoOperatore(elementRef, thisRef.mygovTipoId).subscribe(data => {
      //open detail panel
      thisRef.dialog.open(RegistroComponent, {panelClass: 'mypay4-registro-panel', autoFocus:false, data: {
        tableData: data,
        title: `cambio stato abilitazione - Utente: ${elementRef.username}`,
      } } );
    });
  }

  onInsert() {
    this.navigateToUtentePage(0, 'insert', this, null);
  }

  onInsertForAddOperatore() {
    this.navigateToUtentePage(0, 'insertForAddOperatore', this, null);
  }

  private navigateToUtentePage(userId: number, mode: string, thisRef: UtentiComponent, eventRef: any){
    if(eventRef)
      eventRef.stopPropagation();
    let tabIndex = thisRef.manageMode === ManageMode.Ente ? 2 : (thisRef.manageMode === ManageMode.TipoDovuto ? 1 : undefined);
    const navId = thisRef.pageStateService.saveState({
      tabIndex: tabIndex,
      formData: thisRef.form.value,
      tableData: thisRef.tableData,
      paginatorData: {
        pageSize: thisRef.mypayTableComponent.paginator.pageSize,
        pageIndex: thisRef.mypayTableComponent.paginator.pageIndex
      }
    });
    thisRef.router.navigate(['admin', 'utenti', userId],{state:{mode:mode,backNavId:navId}});
  }

}
