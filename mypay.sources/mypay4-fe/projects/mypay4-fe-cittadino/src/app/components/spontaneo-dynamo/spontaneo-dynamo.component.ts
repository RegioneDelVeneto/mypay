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
import { ToastrService } from 'ngx-toastr';
import {
    ContentEditingPropertyComponent, controlToUppercase, DynamicFunctionUtils, Ente, FieldBean,
    formettedAmountToNumberString, manageError, numberToFormattedAmount, OverlaySpinnerService,
    PATTERNS, TipoDovuto, UserService, validateFormFun
} from 'projects/mypay4-fe-common/src/public-api';
import { Subscription } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import {
    Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, SimpleChange
} from '@angular/core';
import {
    AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, ValidatorFn, Validators
} from '@angular/forms';
import { MatButtonToggleChange } from '@angular/material/button-toggle';
import { Router } from '@angular/router';

import { Person } from '../../model/person';
import { Spontaneo } from '../../model/spontaneo';
import { DefinitionDovuto, SpontaneoForm } from '../../model/spontaneo-form';
import { CarrelloService } from '../../services/carrello.service';
import { RecaptchaService } from '../../services/recaptcha.service';
import { SpontaneoService } from '../../services/spontaneo.service';

@Component({
  selector: 'spontaneo-dynamo',
  templateUrl: './spontaneo-dynamo.component.html',
  styleUrls: ['./spontaneo-dynamo.component.scss']
})
export class SpontaneoDynamoComponent implements OnInit, OnDestroy {

  @Output("reload") reloadEmitter = new EventEmitter<void>();

  constructor(
    protected toastrService: ToastrService,
    private carrelloService: CarrelloService,
    private spontaneoService: SpontaneoService,
    private overlaySpinnerService: OverlaySpinnerService,
    private userService: UserService,
    private recaptchaService: RecaptchaService,
    private elementRef: ElementRef,
    private router: Router,
    private formBuilder: FormBuilder,
  ) { }

  ngOnInit(): void {
  }

  ngOnDestroy(){
    this.codFiscaleUppercaseSub?.unsubscribe();
    this.validateFormSub?.unsubscribe();
    this.recaptchaService.deactivate();
    this.formChangesSub?.unsubscribe();
  }

  addedToCart: boolean = false;

  cfAnonimoSelected = false;
  cfAnonimoDisabled = false;
  private _currentTipoDovuto: TipoDovuto;
  @Input() private set currentTipoDovuto(tipoDovuto: TipoDovuto){
    this._currentTipoDovuto = tipoDovuto;
    this.codiceIdentificativoUnivocoDetails = {
      F: {
        label: 'Codice fiscale',
        validators: [Validators.pattern(PATTERNS.codiceFiscale)]
      },
      G: {
        label: 'Partita IVA',
        validators: [Validators.pattern(PATTERNS.partitaIva)]
      }
    };
    this.codiceIdentificativoUnivocoDetails.F.validators.push(Validators.required);
    this.codiceIdentificativoUnivocoDetails.G.validators.push(Validators.required);
    if(!tipoDovuto.flgCfAnonimo){
      this.cfAnonimoDisabled = true;
      this.form?.get('sys_intestatario_cfAnonimo')?.setValue(false);
      this.form?.get('sys_intestatario_cfAnonimo')?.disable();
    } else {
      this.cfAnonimoDisabled = false;
      this.form?.get('sys_intestatario_cfAnonimo')?.enable();
    }

  }
  private get currentTipoDovuto(){
    return this._currentTipoDovuto;
  }

  @Input()
  private currentEnte: Ente;

  @Input()
  private importoPrefissato: number;

  @Input()
  public fieldBeans: Array<FieldBean>;

  private codiceIdentificativoUnivocoDetails: {[key:string]:{'label':string, 'validators':ValidatorFn[]}};

  /**
   * The content type schema items
   * @private
   * @memberOf ContentEditingModalComponent
   */
  contentTypeSchemaItems: Array<FieldBean>;

  // true qualora il totale è incluso nell'xsd, false se va aggiunto
  totalIncluded: boolean;

  /**
   * The form for content editing
   *
   * @type {FormGroup}
   * @memberOf ContentEditingModalComponent
   */
  form:FormGroup;
  formErrors = { };
  private formChangesSub:Subscription;
  private originalFormValue = {};

  thisformFields = {
    'totalImporto': '',
    'sys_intestatario_anagrafica': '',
    'sys_intestatario_tipoSoggetto': '',
    'sys_intestatario_codiceIdentificativoUnivoco': '',
    'sys_intestatario_cfAnonimo': '',
    'sys_intestatario_email': '',
  }
  private codFiscaleUppercaseSub: Subscription;
  private validateFormSub: Subscription;

  thisformOnChange(fieldName: string) {
      const control = this.form.get(fieldName)
      if(control && control.dirty && !control.valid){
        if (control.errors.hasOwnProperty('required'))
          this.thisformFields[fieldName] = 'Dato obbligatorio';
        else if (control.errors.hasOwnProperty('pattern'))
          this.thisformFields[fieldName] = 'Formato del dato errato';
      } else {
          this.thisformFields[fieldName] = '';
      }
  }

  ngOnChanges(changes: { [propName: string]: SimpleChange }) {
      //if (changes['contentTypeSchemaItems'] && changes['contentTypeSchemaItems'].currentValue) {
      if (changes['fieldBeans'] && changes['fieldBeans'].currentValue) {
        let totalIncluded = false;
        let totalIncludedName = '';
        this.contentTypeSchemaItems = changes['fieldBeans'].currentValue
          .map(bean => {
            if (bean.extraAttr) {
              const totIncluded: string = bean.extraAttr['total_included'];
              if (totIncluded != null && totIncluded.length > 0) {
                // recupero l'informazione che mi dice se il totale è incluso negli items o va aggiunto esternamente
                // qualora sia incluso il valore sarà il nome stesso del campo
                totalIncluded = true;
                totalIncludedName = totIncluded;
              }
              // qualora trovi definizioni specifiche per mypay sovrascrivo quelle generiche
              const errMsg: string = bean.extraAttr['error_message'];
              if (errMsg && errMsg.length > 0) {
                bean.errorMessage = errMsg;
              }
              const helpMsg: string = bean.extraAttr['help_message'];
              if (helpMsg && helpMsg.length > 0) {
                bean.helpMessage = helpMsg;
              }
              const optional: string = bean.extraAttr['optional'];
              if (optional && optional.length > 0) {
                bean.required = optional.toLowerCase() === 'false';
              }
            }
            return bean;
          })
          .filter(bean => {
            return !bean.name.toLowerCase().startsWith('sys_');
          });

        if (this.totalIncluded != totalIncluded) {
          this.totalIncluded = totalIncluded;
        }
        this._gestisciStatoFieldPerImportoTotale(totalIncluded, totalIncludedName, this.importoPrefissato, this.fieldBeans);
        this.handleOnChangeContentTypeSchemaItems();
        this.addedToCart = false;
        this.originalFormValue = _.cloneDeep(this.form.value);
      }
      // if(changes['form'] && changes['form'].currentValue){
      //     this.handleOnChangeForm(changes['form'].currentValue);
      // }
  }

  async onReloadXXX(): Promise<boolean> {
    await this.router.navigateByUrl('.', { skipLocationChange: true });
    return this.router.navigateByUrl('/spontaneo');
  }

  onReload(): void {
    this.reloadEmitter.emit();
  }

  onReset(): void {
    this.addedToCart = false;
    this.form.reset(this.originalFormValue);

    if(this.fieldBeans[0])
      this.fieldBeans[0]['_trigger_on_change']=_.random(100000,999999);
  }

  /*
  * Qualora ci sia il totale Incluso ma venga specificato un importo allora devo utilizzare quell'importo come valore
  * non modificabile da mostrare all'utente
  */
  private _gestisciStatoFieldPerImportoTotale(totalIncluded: boolean, totalIncludedName: string, importoPredefinito: number, items: Array<FieldBean>) {
    if (totalIncluded && importoPredefinito && totalIncludedName && items && items.length > 0) {
      const item: FieldBean = this._getItemByName(items, totalIncludedName);
      item.defaultValue = numberToFormattedAmount(importoPredefinito);
      item.insertable = false;
    }
  }

  private _getItemByName(items: Array<FieldBean>, name: string): FieldBean {
    let item: FieldBean;
    items.some((curr: FieldBean) => {
      if (curr.name === name) {
        item = curr;
        return true;
      }
      return false;
    });
    return item;
  }



  onChangeTipoPersona(event: MatButtonToggleChange) {
    const control = this.form.get('sys_intestatario_codiceIdentificativoUnivoco');
    control.setValidators(this.codiceIdentificativoUnivocoDetails[event?.value].validators);
    control.setValue(control.value); //force re-validation of field
  }

  get codiceIdentificativoUnivocoLabel() {
    return this.codiceIdentificativoUnivocoDetails[this.form.get('sys_intestatario_tipoSoggetto').value].label;
  }

  get anagraficaLabel(){
    return this.form.get('sys_intestatario_tipoSoggetto').value=='F'?'Nome e Cognome':"Denominazione";
  }

  get anagraficaPlaceholder(){
    return this.form.get('sys_intestatario_tipoSoggetto').value=='F'?'Inserire nome e cognome':"Inserire denominazione";
  }

  private handleOnChangeContentTypeSchemaItems(): void {
    console.log('handleOnChangeContentTypeSchemaItems');

      let group: {[key: string]:FormControl} = {};
      this.buildFormGroup(this.contentTypeSchemaItems, group);
      if (!this.totalIncluded) {
        group['totalImporto'] = new FormControl(numberToFormattedAmount(this.importoPrefissato), [Validators.required, Validators.pattern(PATTERNS.importoNonZero)]);
      }

      group['sys_intestatario_anagrafica']=new FormControl(this.userService.getLoggedUserString(), [Validators.required,,Validators.maxLength(70)]);
      group['sys_intestatario_tipoSoggetto']=new FormControl('F', [Validators.required, Validators.pattern(PATTERNS.tipoSoggetto)]);
      group['sys_intestatario_codiceIdentificativoUnivoco']=new FormControl(
        this.userService.getLoggedUser()?.codiceFiscale,
        this.codiceIdentificativoUnivocoDetails.F.validators);
      group['sys_intestatario_cfAnonimo']=new FormControl();
      if(!this.currentTipoDovuto.flgCfAnonimo){
        group['sys_intestatario_cfAnonimo'].disable();
      } else {
        group['sys_intestatario_codiceIdentificativoUnivoco']=new FormControl(
          this.userService.getLoggedUser()?.codiceFiscale,
          this.codiceIdentificativoUnivocoDetails.F.validators);
      }
      group['sys_intestatario_email']=new FormControl(this.userService.getLoggedUser()?.email, [Validators.email]);
      this.form = this.formBuilder.group(group);

      setTimeout(()=>this.formChangesSub = this.form.valueChanges.subscribe(validateFormFun(this.form, this.formErrors)),0);

      this.codFiscaleUppercaseSub = controlToUppercase(this.form.get('sys_intestatario_codiceIdentificativoUnivoco'));
  }

  /**
   * @private
   * @param {Array<Model.ContentSchemaItem>} contentTypeSchemaItems
   * @param {*} group
   *
   * @memberOf ContentEditingModalComponent
   */
  private buildFormGroup(contentTypeSchemaItems: Array<FieldBean>, group: any): any {
      contentTypeSchemaItems
          .filter(item => {  return item.htmlRender !== 'NONE'; })
          .forEach(item => {
              //Adjusted//if (item['isArray'])
              if (item.maxOccurences < 0)
                  group[item.name] = new FormArray([item.htmlRender in ContentEditingPropertyComponent.multiElement ? this.getMultiField(item) : this.getField(item)]);
              else
                  group[item.name] = item.htmlRender in ContentEditingPropertyComponent.multiElement ? this.getMultiField(item) : this.getField(item);
          });
  }

  /**
   * Generate a sub-form group
   *
   * @private
   * @param {Model.ContentSchemaItem} item
   * @returns {FormGroup}
   *
   * @memberOf ContentEditingModalComponent
   */
  private getMultiField(item: FieldBean): FormGroup {
      let subGroup: any = {};
      this.buildFormGroup(item.subfields, subGroup);
      const fg: FormGroup = new FormGroup(subGroup);

      if (item.validDependsOn) {
          const validDependsOnUidsArray: string[] = (item.validDependsOnUids)
              ? item.validDependsOnUids.replace(/\s/g, '').split(',')
              : new Array<string>();

          const validate = (c: AbstractControl): Promise<{ [key: string]: any }> => {
              return new Promise(resolve => {
                  var valid: boolean = true;
                  var msg:string = '';
                  if (c instanceof FormGroup) {
                      const params: any[] = [];
                      const paramsUid: string[] = [];
                      // i parametri per la funzione sono esplicitati attraverso un path
                      // relativo al campo di destinazione della funzione separando con '.'
                      // ogni livello.
                      // Per convenzione utilizziamo come nome delle variabili utilizzati nelle
                      // funzioni l'ultimo nome indicato dopo l'ultimo '.'
                      validDependsOnUidsArray.forEach(
                          uid => {
                              const fc: FormControl = DynamicFunctionUtils.getControlFromForm(c, uid);
                              params.push(fc.value);
                              const paramNameIdx = uid.lastIndexOf('.');
                              paramsUid.push(uid.substring(paramNameIdx + 1));
                          }
                      );

                      const esitoValidazione: { ok: boolean, errCode: string, msg: string, invalidFields: string[] } =
                          DynamicFunctionUtils.executeFunction(item.validDependsOn, params, paramsUid);
                      valid = (esitoValidazione && esitoValidazione.ok);
                      msg = esitoValidazione.msg;
                  }
                  // la validazione è su un gruppo quindi non dovrei mai arrivare in questo ramo
                  else {
                      console.log('Validazione non prevista');
                  }
                  if (valid) {
                      resolve(null)
                  }
                  else {
                      resolve({dynamicValidation: msg });
                  }
              });
          }

          // uso il validatore asincrono per sganciare il calcolo della funzione che potrebbe
          // essere oneroso
          fg.setAsyncValidators(validate);
      }
      return fg;
  }

  /**
   * Generate a single form control
   *
   * @private
   * @param {Model.ContentSchemaItem} item
   * @returns {FormControl}
   *
   * @memberOf ContentEditingModalComponent
   */
  private getField(item: FieldBean): FormControl {
      let validators: Array<ValidatorFn> = new Array<ValidatorFn>();

      if (item.required)
          validators.push(Validators.required);

      if (item.regex)
          switch (item.htmlRender) {
              case 'DATE':
              case 'DATETIME':
                  break;
              default:
                  validators.push(Validators.pattern(item.regex));
          }
      const isDisabled: boolean = !item.insertable || item.htmlRender === 'LABEL' || item.htmlRender === 'CURRENCY_LABEL';
      const defVal: string = item.defaultValue;
      var formControl: FormControl = new FormControl({ value: defVal, disabled: isDisabled }, validators);
      return formControl;
  }

  /**
   * Creates a new filed on user action
   *
   * @private
   * @param {Model.ContentSchemaItem} item
   *
   * @memberOf ContentEditingModalComponent
   */
  private addFieldButtonClicked(item: FieldBean): void {
      let fieldArray: FormArray = <FormArray>this.form.controls[item.name];
      fieldArray.push(item.htmlRender in ContentEditingPropertyComponent.multiElement ? this.getMultiField(item) : this.getField(item));
  }

  /**
   * Removes an existing fied on user action
   *
   * @private
   * @param {Model.ContentSchemaItem} item
   * @param {number} index
   *
   * @memberOf ContentEditingModalComponent
   */
  private removeFieldButtonClicked(item: FieldBean, index: number): void {
      (<FormArray>this.form.controls[item.name]).removeAt(index);
  }

  addToCarrello() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    const spontaneoForm = new SpontaneoForm();
    spontaneoForm.fieldBeans = this.fieldBeans;
    spontaneoForm.importo = formettedAmountToNumberString(this.form.get('totalImporto')?.value);
    spontaneoForm.intestatario = new Person();
    spontaneoForm.intestatario.anagrafica = this.form.get('sys_intestatario_anagrafica')?.value;
    spontaneoForm.intestatario.tipoIdentificativoUnivoco = this.form.get('sys_intestatario_tipoSoggetto')?.value;
    spontaneoForm.intestatario.codiceIdentificativoUnivoco = this.form.get('sys_intestatario_codiceIdentificativoUnivoco')?.value;
    spontaneoForm.intestatario.email = this.form.get('sys_intestatario_email')?.value;
    spontaneoForm.definitionDovuto = this._getDefinzioniDovuto(this.form.value);

    const loggedUser = this.userService.getLoggedUser();
    if(loggedUser && spontaneoForm.intestatario.codiceIdentificativoUnivoco?.toUpperCase() === loggedUser?.codiceFiscale?.toUpperCase()){
      spontaneoForm.intestatario.nazioneId = loggedUser.nazioneId;
      spontaneoForm.intestatario.provinciaId = loggedUser.provinciaId;
      spontaneoForm.intestatario.localitaId = loggedUser.comuneId;
      spontaneoForm.intestatario.cap = loggedUser.cap;
      spontaneoForm.intestatario.indirizzo = loggedUser.indirizzo;
      spontaneoForm.intestatario.civico = loggedUser.civico;
    }

    let validateFormFun;
    if(this.userService.isLogged())
      validateFormFun = this.spontaneoService.validateForm(this.currentEnte, this.currentTipoDovuto, spontaneoForm);
    else
    validateFormFun = this.recaptchaService.submitToken('validateForm').pipe(
        flatMap(token => this.spontaneoService.validateFormAnonymous(this.currentEnte, this.currentTipoDovuto, spontaneoForm, token))
      );

    this.validateFormSub = validateFormFun.subscribe( (spontaneo:Spontaneo) => {
      let addError = spontaneo.errorMsg;
      if (addError) {
        this.toastrService.error(addError, 'Errore aggiungendo al carrello',{disableTimeOut: true});
      } else {
        //Case: No error in validateForm;
        Spontaneo.setDetails(spontaneo);
        const addError = this.carrelloService.add(spontaneo);
        if (addError)
          this.toastrService.error(addError, 'Errore aggiungendo al carrello',{disableTimeOut: true});
        else {
          this.toastrService.info('Elemento aggiunto al carrello');
          this.addedToCart = true;
        }
      }
      this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore aggiungendo al carrello', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}));
  }

  private _getDefinzioniDovuto(obj: any, def: Array<DefinitionDovuto> = new Array()): Array<DefinitionDovuto> {
    Object.keys(obj).filter(key => !Object.keys(this.thisformFields).includes(key)).forEach(key => {
      const child: any = obj[key];
      const childSons: Array<string> = Object.keys(child);
      // gli array vanno considerati un valore da spedire e non un altro livello da iterare
      if (child instanceof Date) {
        def.push(new DefinitionDovuto(key, DateTime.fromJSDate(child).toFormat('dd/MM/yyyy')));
      } else if (child instanceof DateTime) {
        def.push(new DefinitionDovuto(key, child.toFormat('dd/MM/yyyy')));
      } else if (Array.isArray(child) || typeof child !== 'object' || (childSons == null || childSons.length === 0)) {
        def.push(new DefinitionDovuto(key, child.toString()));
      } else {
        this._getDefinzioniDovuto(child, def);
      }
    });
    return def;
  }

  cfAnonimoOnChange(checked){
    const field = this.form.get('sys_intestatario_codiceIdentificativoUnivoco');
    if(checked){
      field.setValue(null);
      field.disable();
    } else{
      field.enable();
    }
    setTimeout(()=>this.cfAnonimoSelected = checked, 0);
  }
}
