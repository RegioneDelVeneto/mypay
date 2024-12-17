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
  blank2Null, formettedAmountToNumber, manageError, numberToFormattedAmount,
  OverlaySpinnerService, PATTERNS, UserService, validateFormFunAsync
} from 'projects/mypay4-fe-common/src/public-api';
import {
  AnagraficaTipoDovuto
} from 'projects/mypay4-fe-operatore/src/app/model/anagrafica-tipo-dovuto';
import { TassonomiaCodDesc } from 'projects/mypay4-fe-operatore/src/app/model/tassonomia-cod-desc';
import { Subscription } from 'rxjs';

import { Location } from '@angular/common';
import {
  Component, ElementRef, EventEmitter, OnDestroy, OnInit, Output, ViewChild
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { faTimes } from '@fortawesome/free-solid-svg-icons';

import { Ente } from '../../../../model/ente';
import { AdminEntiService } from '../../../../services/admin-enti.service';

@Component({
  selector: 'tipo-anagrafica',
  templateUrl: './tipo-anagrafica.component.html',
  styleUrls: ['./tipo-anagrafica.component.scss']
})
export class TipoAnagraficaComponent implements OnInit, OnDestroy {

  @ViewChild('sForm') searchFormDirective;

  get titleLabel(){ return "Inserimento nuovo tipo dovuto" }
  get titleIcon(){ return null }
  iconTimes = faTimes;
  modeAnag: string;

  mygovEnteTipoDovutoId: number;
  mygovEnteId: number;


  ente: Ente;
  anagTipoDovuto: AnagraficaTipoDovuto;
  @Output() codTipoOutput = new EventEmitter<string>();
  formAnag: FormGroup;
  formAnagModified: boolean = false;
  formAnagErrors:Object = {};
  blockingError: boolean = false;

  flagHasDovutoNoScadenza = false;
  macroAreas: TassonomiaCodDesc[];
  tipoServizios: TassonomiaCodDesc[];
  motivoRiscossiones: TassonomiaCodDesc[];
  codTassonomicos: TassonomiaCodDesc[];
  private valueChangesSub:Subscription;
  private userSub: Subscription;
  isUserAdmin: boolean = false;
  isUserAdminEnte: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private location: Location,
    private adminEntiService: AdminEntiService,
    private toastrService: ToastrService,
    private elementRef: ElementRef,
    private overlaySpinnerService: OverlaySpinnerService,
    private userService:UserService,
    private dialog: MatDialog,
  ) {
      this.userSub = userService.getLoggedUserObs().subscribe(() => {
        this.isUserAdmin = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ROLE);
        this.isUserAdminEnte = userService.isRoleAuthorized(UserService.BACK_OFFICE_ADMIN_ENTE_ROLE);
      });
  }

  ngOnInit(): void {
    //load params from url
    const params = this.route.snapshot.params;
    this.mygovEnteId = +params['enteId']; // (+) converts string 'id' to a number
    this.mygovEnteTipoDovutoId = +params['tipoId']; // (+) converts string 'id' to a number
    let mode = this.mygovEnteTipoDovutoId ? 'view' : 'insert';

    this.formAnag = this.formBuilder.group({
      codTipo: ['', [Validators.required]],
      deTipo: ['', [Validators.required]],
      ibanAccreditoPi: [''],
      ibanAccreditoPsp: [''],
      codXsdCausale: [''],
      importo: ['', [Validators.pattern(PATTERNS.importoNonZero)]],
      spontaneo: [false],
      flgCfAnonimo: [false],
      flgScadenzaObbligatoria: [false],
      flgStampaDataScadenza: [false],
      deUrlPagamentoDovuto: [''],
      deBilancioDefault: [''],
      deSettoreEnte: [''],
      deIntestatarioCCPostale: [''],
      flgNotificaIo: [false],
      flgDisabilitaStampaAvviso: [false],
      codiceContestoPagamento: [''],
      flgNotificaEsitoPush: [false],
      maxTentativiInoltroEsito: [''],
      nomeApplicativo: [''],
      deUrlInoltroEsitoPagamentoPush: ['', [Validators.pattern(PATTERNS.url)]],
      flgJwtAttivo: [false],
      codServiceAccountJwtUscitaClientId: [''],
      deServiceAccountJwtUscitaClientMail: ['', [Validators.email]],
      codServiceAccountJwtUscitaSecretKeyId: [''],
      codServiceAccountJwtUscitaSecretKey: [''],
      macroArea: [null, [Validators.required]],
      tipoServizio: [null, [Validators.required]],
      motivoRiscossione: [null, [Validators.required]],
      codTassonomico: [null, [Validators.required]],
      urlNotificaPnd:['', [Validators.pattern(PATTERNS.url)]],
      userPnd:[''],
      pswPnd:[''],
      urlNotificaAttualizzazionePnd:['', [Validators.pattern(PATTERNS.url)]]
    });

    this.valueChangesSub = this.formAnag.valueChanges.subscribe(
      validateFormFunAsync(this.formAnag, this.formAnagErrors, this.checkFormModified(this)));

    this.setMode(mode);
    this.initializeFields();
  }

  ngOnDestroy():void {
    this.valueChangesSub?.unsubscribe();
    this.userSub?.unsubscribe();
  }

  private setForm(anagTipoDovuto: AnagraficaTipoDovuto) {
    this.formAnag.get('nomeApplicativo').setValue(anagTipoDovuto.nomeApplicativo);
    this.formAnag.get('deUrlInoltroEsitoPagamentoPush').setValue(anagTipoDovuto.deUrlInoltroEsitoPagamentoPush);
    this.formAnag.get('flgJwtAttivo').setValue(anagTipoDovuto.flgJwtAttivo);
    this.formAnag.get('codServiceAccountJwtUscitaClientId').setValue(anagTipoDovuto.codServiceAccountJwtUscitaClientId);
    this.formAnag.get('deServiceAccountJwtUscitaClientMail').setValue(anagTipoDovuto.deServiceAccountJwtUscitaClientMail);
    this.formAnag.get('codServiceAccountJwtUscitaSecretKeyId').setValue(anagTipoDovuto.codServiceAccountJwtUscitaSecretKeyId);
    this.formAnag.get('codServiceAccountJwtUscitaSecretKey').setValue(anagTipoDovuto.codServiceAccountJwtUscitaSecretKey);
    this.formAnag.get('codTipo').setValue(anagTipoDovuto.codTipo);
    this.formAnag.get('deTipo').setValue(anagTipoDovuto.deTipo);
    this.formAnag.get('ibanAccreditoPi').setValue(anagTipoDovuto.ibanAccreditoPi);
    this.formAnag.get('ibanAccreditoPsp').setValue(anagTipoDovuto.ibanAccreditoPsp);
    this.formAnag.get('codXsdCausale').setValue(anagTipoDovuto.codXsdCausale);
    this.formAnag.get('importo').setValue(numberToFormattedAmount(anagTipoDovuto.importo, this.modeAnag === 'edit'));
    this.formAnag.get('spontaneo').setValue(anagTipoDovuto.spontaneo);
    this.formAnag.get('flgCfAnonimo').setValue(anagTipoDovuto.flgCfAnonimo);
    this.formAnag.get('flgScadenzaObbligatoria').setValue(anagTipoDovuto.flgScadenzaObbligatoria);
    this.formAnag.get('flgStampaDataScadenza').setValue(anagTipoDovuto.flgStampaDataScadenza);
    this.formAnag.get('deUrlPagamentoDovuto').setValue(anagTipoDovuto.deUrlPagamentoDovuto);
    this.formAnag.get('deBilancioDefault').setValue(anagTipoDovuto.deBilancioDefault);
    this.formAnag.get('deSettoreEnte').setValue(anagTipoDovuto.deSettoreEnte);
    this.formAnag.get('deIntestatarioCCPostale').setValue(anagTipoDovuto.deIntestatarioCcPostale);
    this.formAnag.get('flgNotificaIo').setValue(anagTipoDovuto.flgNotificaIo);
    this.formAnag.get('flgDisabilitaStampaAvviso').setValue(anagTipoDovuto.flgDisabilitaStampaAvviso);
    this.formAnag.get('codiceContestoPagamento').setValue(anagTipoDovuto.codiceContestoPagamento);
    this.formAnag.get('flgNotificaEsitoPush').setValue(anagTipoDovuto.flgNotificaEsitoPush);
    this.formAnag.get('maxTentativiInoltroEsito').setValue(anagTipoDovuto.maxTentativiInoltroEsito);
    this.formAnag.get('nomeApplicativo').setValue(anagTipoDovuto.nomeApplicativo);
    this.formAnag.get('deUrlInoltroEsitoPagamentoPush').setValue(anagTipoDovuto.deUrlInoltroEsitoPagamentoPush);
    this.formAnag.get('flgJwtAttivo').setValue(anagTipoDovuto.flgJwtAttivo);
    this.formAnag.get('codServiceAccountJwtUscitaClientId').setValue(anagTipoDovuto.codServiceAccountJwtUscitaClientId);
    this.formAnag.get('deServiceAccountJwtUscitaClientMail').setValue(anagTipoDovuto.deServiceAccountJwtUscitaClientMail);
    this.formAnag.get('codServiceAccountJwtUscitaSecretKeyId').setValue(anagTipoDovuto.codServiceAccountJwtUscitaSecretKeyId);
    this.formAnag.get('codServiceAccountJwtUscitaSecretKey').setValue(anagTipoDovuto.codServiceAccountJwtUscitaSecretKey);

    this.formAnag.get('urlNotificaPnd').setValue(anagTipoDovuto.urlNotificaPnd);
    this.formAnag.get('userPnd').setValue(anagTipoDovuto.userPnd);
    this.formAnag.get('pswPnd').setValue(anagTipoDovuto.pswPnd);
    this.formAnag.get('urlNotificaAttualizzazionePnd').setValue(anagTipoDovuto.urlNotificaAttualizzazionePnd);

        /* Can remove the part below after every tipo dovuto has macroArea. */
        this.formAnag.get('macroArea').setValue(null);
        this.formAnag.get('tipoServizio').setValue(null);
        this.formAnag.get('motivoRiscossione').setValue(null);
        this.formAnag.get('codTassonomico').setValue(null);
        this.tipoServizios = [];
        this.motivoRiscossiones = [];
        this.codTassonomicos = [];
        /* ---------------------------------------------------------------- */

        if (anagTipoDovuto.macroArea) {
          const codTipoEnte = this.ente.codTipoEnte;
          const macroArea = this.macroAreas.find(m => m.code == anagTipoDovuto.macroArea);
          if(!macroArea)
            return this.manageErrorCodTassonomico('Macro area non valida');
          
          this.formAnag.get('macroArea').setValue(macroArea);
          this.adminEntiService.getTipoServizio(codTipoEnte, macroArea.code).subscribe(
            tipoServizios => {
              this.tipoServizios = tipoServizios;
              if (anagTipoDovuto.tipoServizio) {
                const tipoServizio = tipoServizios.find(t => t.code == anagTipoDovuto.tipoServizio);
                if(!tipoServizio)
                  return this.manageErrorCodTassonomico('Tipo servizio non valido');
                this.formAnag.get('tipoServizio').setValue(tipoServizio);
                this.adminEntiService.getMotivoRiscossione(codTipoEnte, anagTipoDovuto.macroArea, anagTipoDovuto.tipoServizio).subscribe(
                  motivoRiscossiones => {
                    this.motivoRiscossiones = motivoRiscossiones;
                    if (anagTipoDovuto.motivoRiscossione) {
                      const motivoRiscossione = motivoRiscossiones.find(m => m.code == anagTipoDovuto.motivoRiscossione);
                      if(!motivoRiscossione)
                        return this.manageErrorCodTassonomico('Motivo riscossione non valido');

                      this.formAnag.get('motivoRiscossione').setValue(motivoRiscossione);
                      this.adminEntiService.getCodTassonomico(codTipoEnte,  anagTipoDovuto.macroArea, anagTipoDovuto.tipoServizio, anagTipoDovuto.motivoRiscossione).subscribe(
                        codTassonomicos => {
                          this.codTassonomicos = codTassonomicos;
                          if (anagTipoDovuto.codTassonomico) {
                            const codTassonomico = codTassonomicos.find(c => c.code == anagTipoDovuto.codTassonomico);
                            if(!codTassonomico)
                              return this.manageErrorCodTassonomico('Codice tassonomico non valido');
                            this.formAnag.get('codTassonomico').setValue(codTassonomico);
                          } else {
                            if(codTassonomicos?.length == 1)
                              this.formAnag.get('codTassonomico').setValue(codTassonomicos[0]);
                          }
                        }, manageError('Errore recuperando lista cod tassonomico', this.toastrService) );
                    }
                  }, manageError('Errore recuperando lista motivo riscossione', this.toastrService) );
              }
            }, manageError('Errore recuperando lista tipo servizio', this.toastrService) );
        }
  }

  private manageErrorCodTassonomico(errorMsg: string):void {
    manageError('Errore sui dati del codice tassonomico', this.toastrService)(errorMsg);
    this.formAnag.get('macroArea').setValue(null);
    this.formAnag.get('tipoServizio').setValue(null);
    this.formAnag.get('motivoRiscossione').setValue(null);
    this.formAnag.get('codTassonomico').setValue(null);
  }

  private initializeFields() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.adminEntiService.getEnteById(this.mygovEnteId).subscribe( ente => {
      this.ente = ente;
      this.adminEntiService.getMacroArea(ente.codTipoEnte).subscribe(
        macroAreas => {
          this.overlaySpinnerService.detach(spinner);
          this.macroAreas = macroAreas;
          if (this.mygovEnteTipoDovutoId) {
            const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
            this.adminEntiService.getTipoDovutoById(this.mygovEnteTipoDovutoId).subscribe( data => {
              this.anagTipoDovuto = data;
              this.codTipoOutput.emit(data.codTipo);
              this.setForm(data);
              this.overlaySpinnerService.detach(spinner);
              this.adminEntiService.hasDovutoNoScadenza(ente.mygovEnteId, data.codTipo).subscribe( flag => {
                this.flagHasDovutoNoScadenza = flag;
              }, manageError('Errore recuperando la presenza di dovuti aventi scadenza NULL', this.toastrService, () => {this.blockingError=true; this.overlaySpinnerService.detach(spinner)}) );
            }, manageError('Errore recuperando il dettaglio Tipo dovuto', this.toastrService, () => {this.blockingError=true; this.overlaySpinnerService.detach(spinner)}) );
          }
        }, manageError('Errore recuperando lista macro area', this.toastrService, () => {this.blockingError=true; this.overlaySpinnerService.detach(spinner)}) );
    }, manageError('Errore recuperando ente', this.toastrService, () => {this.blockingError=true; this.overlaySpinnerService.detach(spinner)}) );
  }

  private loadTipoServizios(ente: Ente, macroArea: TassonomiaCodDesc) {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.adminEntiService.getTipoServizio(ente.codTipoEnte, macroArea.code).subscribe(
      tipoServizios => {
        this.overlaySpinnerService.detach(spinner);
        this.tipoServizios = tipoServizios;
      }, manageError('Errore recuperando lista tipo servizio', this.toastrService, () => {this.blockingError=true; this.overlaySpinnerService.detach(spinner)}) );
  }

  private loadMotivoRiscossiones(ente: Ente, macroArea: TassonomiaCodDesc, tipoServizio: TassonomiaCodDesc) {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.adminEntiService.getMotivoRiscossione(ente.codTipoEnte, macroArea.code, tipoServizio.code).subscribe(
      motivoRiscossiones => {
        this.overlaySpinnerService.detach(spinner);
        this.motivoRiscossiones = motivoRiscossiones;
      }, manageError('Errore recuperando lista motivo riscossione', this.toastrService, () => {this.blockingError=true; this.overlaySpinnerService.detach(spinner)}) );
  }

  private loadCodTassonomico(ente: Ente, macroArea: TassonomiaCodDesc, tipoServizio: TassonomiaCodDesc, motivoRiscossione: TassonomiaCodDesc) {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.adminEntiService.getCodTassonomico(ente.codTipoEnte, macroArea.code, tipoServizio.code, motivoRiscossione.code).subscribe(
      codTassonomicos => {
        this.overlaySpinnerService.detach(spinner);
        this.codTassonomicos = codTassonomicos;
        if(codTassonomicos?.length == 1)
          this.formAnag.get('codTassonomico').setValue(codTassonomicos[0]);
      }, manageError('Errore recuperando lista cod tassonomico', this.toastrService, () => {this.blockingError=true; this.overlaySpinnerService.detach(spinner)}) );
  }

  spontaneoOnChange(isSpontaneo: boolean) {
    if (this.flagHasDovutoNoScadenza) {
      this.formAnag.get('flgScadenzaObbligatoria').setValue(false);
      this.formAnag.get('flgStampaDataScadenza').setValue(false);
    } else {
      this.formAnag.get('flgScadenzaObbligatoria').setValue(!isSpontaneo);
      this.formAnag.get('flgStampaDataScadenza').setValue(!isSpontaneo);
    }
    if(isSpontaneo) {
      this.formAnag.get('flgStampaDataScadenza').disable(); 
      this.formAnag.get('flgScadenzaObbligatoria').disable(); 
    } else {
      this.formAnag.get('flgScadenzaObbligatoria').enable(); 
    }
  }

  checkIfFlagHasDovutoNoScadenza(alertTemplateRef) {
    if (this.modeAnag == 'edit' && this.flagHasDovutoNoScadenza) {
      this.dialog.open(alertTemplateRef);
      this.formAnag.get('flgScadenzaObbligatoria').setValue(false);
      this.formAnag.get('flgStampaDataScadenza').setValue(false);
    }
  }

  flgScadenzaObbligatoriaOnChange(flgDataScadenza: boolean) {
    if (flgDataScadenza) {
        this.formAnag.get('flgStampaDataScadenza').setValue(true);
        this.formAnag.get('flgStampaDataScadenza').disable();  
    } else {
      this.formAnag.get('flgStampaDataScadenza').enable();
    }
  }

  flgNotificaEsitoPushOnChange(flgNotificaEsitoPush: boolean) {
    //if (flgNotificaEsitoPush)
    //  this.formAnag.get('flgJwtAttivo').enable();
    //else
    //  this.formAnag.get('flgJwtAttivo').disable();
  }

  compareMacroArea(o1: TassonomiaCodDesc, o2: TassonomiaCodDesc) {
    return o1.code === o2?.code;
  }

  compareTipoServizio(o1: TassonomiaCodDesc, o2: TassonomiaCodDesc) {
    return o1.code === o2?.code;
  }

  compareMotivoRiscossione(o1: TassonomiaCodDesc, o2: TassonomiaCodDesc) {
    return o1.code === o2?.code;
  }

  compareCodTassonomico(o1: TassonomiaCodDesc, o2: TassonomiaCodDesc) {
    return o1.code === o2?.code;
  }

  macroAreaOnChange(macroArea: TassonomiaCodDesc) {
    this.formAnag.get('tipoServizio').setValue(null);
    this.formAnag.get('motivoRiscossione').setValue(null);
    this.formAnag.get('codTassonomico').setValue(null);
    this.tipoServizios = [];
    this.motivoRiscossiones = [];
    this.codTassonomicos = [];
    this.loadTipoServizios(this.ente, macroArea);
    this.formAnag.get('tipoServizio').enable();
    this.formAnag.get('motivoRiscossione').disable();
    this.formAnag.get('codTassonomico').disable();
  }

  tipoServizioOnChange(tipoServizio: TassonomiaCodDesc) {
    this.formAnag.get('motivoRiscossione').setValue(null);
    this.formAnag.get('codTassonomico').setValue(null);
    this.motivoRiscossiones = [];
    this.codTassonomicos = [];
    let macroArea = this.formAnag.get('macroArea').value;
    this.loadMotivoRiscossiones(this.ente, macroArea, tipoServizio);
    this.formAnag.get('motivoRiscossione').enable();
    this.formAnag.get('codTassonomico').disable();
  }

  motivoRiscossioneOnChange(motivoRiscossione: TassonomiaCodDesc) {
    this.formAnag.get('codTassonomico').setValue(null);
    this.codTassonomicos = [];
    let macroArea = this.formAnag.get('macroArea').value;
    let tipoServizio = this.formAnag.get('tipoServizio').value;
    this.loadCodTassonomico(this.ente, macroArea, tipoServizio, motivoRiscossione);
    this.formAnag.get('codTassonomico').enable();
  }

  codTassonomicoOnChange(codTassonomico: TassonomiaCodDesc) {
    // Currently nothing to do.
  }

  private setMode(mode: string) {
    this.modeAnag = mode;
    this.formAnagModified = mode === 'insert';
    if (mode === 'view') {
      this.formAnag.disable();
    }
    if (mode !== 'view' && this.isUserAdmin) {
      this.formAnag.get('macroArea').enable();
      this.formAnag.get('tipoServizio').enable();
      this.formAnag.get('motivoRiscossione').enable();
      this.formAnag.get('codTassonomico').enable();
      this.formAnag.get('codXsdCausale').enable();
    }
    if (mode === 'insert'){
      this.formAnag.get('codTipo').enable();
      this.formAnag.get('deTipo').enable();
    }
    if (mode === 'edit' && this.isUserAdmin) {
      this.formAnag.get('deTipo').enable();
      this.formAnag.get('deUrlPagamentoDovuto').enable();
      this.formAnag.get('deSettoreEnte').enable();
      this.formAnag.get('deIntestatarioCCPostale').enable();
      this.formAnag.get('flgDisabilitaStampaAvviso').enable();
      this.formAnag.get('codiceContestoPagamento').enable();
      this.formAnag.get('flgNotificaEsitoPush').enable();
      this.formAnag.get('maxTentativiInoltroEsito').enable();
      this.formAnag.get('nomeApplicativo').enable();
      this.formAnag.get('deUrlInoltroEsitoPagamentoPush').enable();
      this.formAnag.get('flgJwtAttivo').enable();
      this.formAnag.get('codServiceAccountJwtUscitaClientId').enable();
      this.formAnag.get('deServiceAccountJwtUscitaClientMail').enable();
      this.formAnag.get('codServiceAccountJwtUscitaSecretKeyId').enable();
      this.formAnag.get('codServiceAccountJwtUscitaSecretKey').enable();
      this.formAnag.get('urlNotificaPnd').enable();
      this.formAnag.get('userPnd').enable();
      this.formAnag.get('pswPnd').enable();
      this.formAnag.get('urlNotificaAttualizzazionePnd').enable();
    }
    if (mode === 'edit') {
      this.formAnag.get('ibanAccreditoPi').enable();
      this.formAnag.get('ibanAccreditoPsp').enable();
      this.formAnag.get('flgCfAnonimo').enable();
      this.formAnag.get('importo').enable();
      this.formAnag.get('importo').setValue(numberToFormattedAmount(formettedAmountToNumber(this.formAnag.get('importo').value), true));
      this.formAnag.get('deBilancioDefault').enable();
      this.formAnag.get('spontaneo').enable();
      if (!this.formAnag.get('spontaneo').value) {
        if(!this.flagHasDovutoNoScadenza) {
          this.formAnag.get('flgScadenzaObbligatoria').enable();
          if (!this.formAnag.get('flgScadenzaObbligatoria').value)
            this.formAnag.get('flgStampaDataScadenza').enable();
        }
      }
      this.formAnag.get('flgNotificaIo').enable();
      this.formAnag.markAllAsTouched();
    }
  }

  goBack(){
    this.location.back();
  }

  onReset(): void {
    this.setMode('view');
    this.setForm(this.anagTipoDovuto);
  }

  enableEdit(): void {
    this.setMode('edit');
  }

  private isFieldModified(field: string){
    if (['macroArea', 'tipoServizio', 'motivoRiscossione', 'codTassonomico'].includes(field)){
      return this.anagTipoDovuto[field] !== (this.formAnag.get(field).value as TassonomiaCodDesc)?.code;
    } else if(['importo'].includes(field)){
      return (this.anagTipoDovuto[field] || '') !== (formettedAmountToNumber(this.formAnag.get(field).value) || '');
    } else {
      return (this.anagTipoDovuto[field] || '') !== (this.formAnag.get(field).value || '');
    }
  }

  private checkFormModified(thisRef: TipoAnagraficaComponent): (data: Object) => void {
    return function(data: Object){
      thisRef.formAnagModified = thisRef.modeAnag === 'insert' ||
          thisRef.isFieldModified('deInformazioniEnte');
    }
  }

  saveConfirmMsg(thisRef: TipoAnagraficaComponent): string | {message: string, invalid: boolean} {
    if(thisRef.modeAnag === 'insert'){
      return "Confermi l'inserimento del nuovo tipo dovuto?";
    } else {
      const msg = ['codTipo', 'deTipo', 'ibanAccreditoPi', 'ibanAccreditoPsp', 'codXsdCausale', 'importo', 'spontaneo',
       'flgCfAnonimo', 'flgScadenzaObbligatoria', 'flgStampaDataScadenza', 'deUrlPagamentoDovuto', 'deBilancioDefault',
       'deSettoreEnte', 'deIntestatarioCCPostale', 'flgNotificaIo', 'flgDisabilitaStampaAvviso', 'codiceContestoPagamento',
       'flgNotificaEsitoPush', 'maxTentativiInoltroEsito', 'nomeApplicativo', 'deUrlInoltroEsitoPagamentoPush', 'flgJwtAttivo',
       'codServiceAccountJwtUscitaClientId', 'deServiceAccountJwtUscitaClientMail', 'codServiceAccountJwtUscitaSecretKeyId',
       'codServiceAccountJwtUscitaSecretKey', 'macroArea', 'tipoServizio', 'motivoRiscossione', 'codTassonomico',
       'urlNotificaPnd','userPnd','pswPnd','urlNotificaAttualizzazionePnd']
        .filter(field => thisRef.isFieldModified(field))
        .map(field => this.createMsgByField(thisRef, field));
      if(msg.length==0){
        return {message:"Nessun campo modificato", invalid: true};
      } else {
        msg.unshift("Confermi la modifica dei seguenti campi?<br>");
        return msg.join("<br>");
      }
    }
  }

  private createMsgByField(thisRef: TipoAnagraficaComponent, field: string): string {
    if (['macroArea', 'tipoServizio', 'motivoRiscossione', 'codTassonomico'].includes(field)){
      return `${field}: ${thisRef.anagTipoDovuto[field] || ''} -> ${(thisRef.formAnag.get(field).value as TassonomiaCodDesc)?.code}`;
    } else if (['spontaneo', 'flgCfAnonimo', 'flgScadenzaObbligatoria', 'flgStampaDataScadenza', 'flgNotificaIo', 'flgDisabilitaStampaAvviso', 'flgNotificaEsitoPush', 'flgJwtAttivo'].includes(field)) {
      return `${field}: ${thisRef.anagTipoDovuto[field] || 'false'} -> ${thisRef.formAnag.get(field).value || 'false'}`;
    } else if (['importo'].includes(field)) {
      return `${field}: ${numberToFormattedAmount(thisRef.anagTipoDovuto[field]) || ''} -> ${numberToFormattedAmount(formettedAmountToNumber(thisRef.formAnag.get(field).value)) || ''}`;
    } else {
      return `${field}: ${thisRef.anagTipoDovuto[field] || ''} -> ${thisRef.formAnag.get(field).value || ''}`;
    }
  }

  onSubmit(){
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);

    let anagTipoDovuto = new AnagraficaTipoDovuto();

    anagTipoDovuto.mygovEnteTipoDovutoId = this.mygovEnteTipoDovutoId;
    anagTipoDovuto.mygovEnteId = this.ente.mygovEnteId;
    anagTipoDovuto.codIpaEnte = this.ente.codIpaEnte;
    anagTipoDovuto.nomeApplicativo = blank2Null(this.formAnag.get('nomeApplicativo').value);
    anagTipoDovuto.deUrlInoltroEsitoPagamentoPush = blank2Null(this.formAnag.get('deUrlInoltroEsitoPagamentoPush').value);
    anagTipoDovuto.flgJwtAttivo = this.formAnag.get('flgJwtAttivo').value;
    anagTipoDovuto.codServiceAccountJwtUscitaClientId = blank2Null(this.formAnag.get('codServiceAccountJwtUscitaClientId').value);
    anagTipoDovuto.deServiceAccountJwtUscitaClientMail = blank2Null(this.formAnag.get('deServiceAccountJwtUscitaClientMail').value);
    anagTipoDovuto.codServiceAccountJwtUscitaSecretKeyId = blank2Null(this.formAnag.get('codServiceAccountJwtUscitaSecretKeyId').value);
    anagTipoDovuto.codServiceAccountJwtUscitaSecretKey = blank2Null(this.formAnag.get('codServiceAccountJwtUscitaSecretKey').value);
    anagTipoDovuto.codTipo = this.formAnag.get('codTipo').value;
    anagTipoDovuto.deTipo = this.formAnag.get('deTipo').value;
    anagTipoDovuto.ibanAccreditoPi = blank2Null(this.formAnag.get('ibanAccreditoPi').value);
    anagTipoDovuto.ibanAccreditoPsp = blank2Null(this.formAnag.get('ibanAccreditoPsp').value);
    anagTipoDovuto.codXsdCausale = this.formAnag.get('codXsdCausale').value;
    anagTipoDovuto.importo = formettedAmountToNumber(this.formAnag.get('importo')?.value);
    anagTipoDovuto.spontaneo = this.formAnag.get('spontaneo').value;
    anagTipoDovuto.flgCfAnonimo = this.formAnag.get('flgCfAnonimo').value;
    anagTipoDovuto.flgScadenzaObbligatoria = this.formAnag.get('flgScadenzaObbligatoria').value;
    anagTipoDovuto.flgStampaDataScadenza = this.formAnag.get('flgStampaDataScadenza').value;
    anagTipoDovuto.deUrlPagamentoDovuto = blank2Null(this.formAnag.get('deUrlPagamentoDovuto').value);
    anagTipoDovuto.deBilancioDefault = blank2Null(this.formAnag.get('deBilancioDefault').value);
    anagTipoDovuto.deSettoreEnte = blank2Null(this.formAnag.get('deSettoreEnte').value);
    anagTipoDovuto.deIntestatarioCcPostale = blank2Null(this.formAnag.get('deIntestatarioCCPostale').value);
    anagTipoDovuto.flgNotificaIo = this.formAnag.get('flgNotificaIo').value;
    anagTipoDovuto.flgDisabilitaStampaAvviso = this.formAnag.get('flgDisabilitaStampaAvviso').value;
    anagTipoDovuto.codiceContestoPagamento = blank2Null(this.formAnag.get('codiceContestoPagamento').value);
    anagTipoDovuto.flgNotificaEsitoPush = this.formAnag.get('flgNotificaEsitoPush').value;
    anagTipoDovuto.maxTentativiInoltroEsito = this.formAnag.get('maxTentativiInoltroEsito').value;
    anagTipoDovuto.macroArea = (this.formAnag.get('macroArea').value as TassonomiaCodDesc)?.code;
    anagTipoDovuto.tipoServizio = (this.formAnag.get('tipoServizio').value as TassonomiaCodDesc)?.code;
    anagTipoDovuto.motivoRiscossione = (this.formAnag.get('motivoRiscossione').value as TassonomiaCodDesc)?.code;
    anagTipoDovuto.codTassonomico = (this.formAnag.get('codTassonomico').value as TassonomiaCodDesc)?.code;

    anagTipoDovuto.urlNotificaPnd = blank2Null(this.formAnag.get('urlNotificaPnd').value);
    anagTipoDovuto.userPnd = blank2Null(this.formAnag.get('userPnd').value);
    anagTipoDovuto.pswPnd = blank2Null(this.formAnag.get('pswPnd').value);
    anagTipoDovuto.urlNotificaAttualizzazionePnd = blank2Null(this.formAnag.get('urlNotificaAttualizzazionePnd').value);

    this.adminEntiService.upsertTipoDovuto(this.modeAnag, anagTipoDovuto).subscribe( data => {

      this.anagTipoDovuto = data;
      this.mygovEnteTipoDovutoId = data.mygovEnteTipoDovutoId;
      this.toastrService.success('Tipo dovuto '+(this.modeAnag==='edit'?'aggiornato':'inserito')+' correttamente.');
      this.setMode('view');
      this.setForm(data);
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore aggiornando il tipo dovuto', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
  }
}
