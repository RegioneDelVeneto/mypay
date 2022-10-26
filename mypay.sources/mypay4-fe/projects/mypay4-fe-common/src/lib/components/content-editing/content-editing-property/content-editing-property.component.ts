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
import { FieldBean } from 'projects/mypay4-fe-common/src/public-api';

import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, FormGroup } from '@angular/forms';
import { faQuestionCircle } from '@fortawesome/free-solid-svg-icons';

import { DependentContent } from '../dependent-content';

@Component({
  selector: 'content-editing-property',
  templateUrl: './content-editing-property.component.html',
  styleUrls: ['./content-editing-property.component.scss']
})
export class ContentEditingPropertyComponent extends DependentContent implements OnInit {

  iconHelp = faQuestionCircle;

  /**
   * Il valore corrente degli attributi da cui questo componente dipende
   */
  private currValParams: any = {};

  /**
   * Il valore corrente degli attributi da cui questo componente dipende
   */
  private currValParamsEnabled: any = {};

  // constructor (){
  //     super();
  // }

  ngOnInit(): void {
      this.gestisciAggiornamenti();
  }

  gestisciAggiornamenti(): void {
      // Gestisco eventuali aggiornamenti dinamici dell'abilitazione
      const changeEnableCallback = (updatedValue: any) => {
          //console.log(updatedValue);
          this.schemaItem.insertable = updatedValue;
          if (updatedValue) {
              const enableAllInsertable = (parentControl: AbstractControl, schemaItem: FieldBean) => {
                  // non vengono aggiunti i control x Item con renderType = 'NONE'
                  if (schemaItem.htmlRender != 'NONE') {
                      const control: AbstractControl = parentControl.get(schemaItem.name);
                      if (control == null) {
                          console.warn('Control non trovato [' + schemaItem.name + ']')
                      } else {
                          if (schemaItem.insertable && control.disabled) {
                              //Adjusted//control.enable(true);
                              control.enable();
                          } else if(!schemaItem.insertable && control.enabled){
                              //Adjusted//control.disable(false);
                              control.disable();
                          }
                          if (schemaItem.subfields) {
                              schemaItem.subfields.forEach(
                                  field => {
                                      enableAllInsertable(control, field);
                                  }
                              );
                          } else{
                              control.updateValueAndValidity();
                              control.markAsDirty();
                          }
                      }
                  }
              }
              enableAllInsertable(this.form, this.schemaItem);
          }
          else {
              //Adjusted//this.form.get(this.key).disable(false);
              this.form.get(this.key).disable();
          }
      }



      const enableDependsOnUidsArray: string[] = (this.schemaItem.enabledDependsOnUids)
          ? this.schemaItem.enabledDependsOnUids.replace(/\s/g, '').split(',')
          : new Array<string>();

      this.gestisciAggiornamentoDaFunzione(
          this.schemaItem.enabledDependsOn,
          enableDependsOnUidsArray,
          this.form,
          this.currValParamsEnabled,
          changeEnableCallback);

      // Gestisco eventuali aggiornamenti dinamici dei valori
      const changeValueCallback = (updatedValue: any) => {
          this.form.controls[this.key].patchValue(updatedValue);
      }

      const valueDependsOnUidsArray: string[] = (this.schemaItem.valueDependsOnUids)
          ? this.schemaItem.valueDependsOnUids.replace(/\s/g, '').split(',')
          : new Array<string>();

      this.gestisciAggiornamentoDaFunzione(
          this.schemaItem.valueDependsOn,
          valueDependsOnUidsArray,
          this.form,
          this.currValParams,
          changeValueCallback);
  }

  /**
   * The form attached to this component
   *
   * @private
   * @type {FormGroup}
   * @memberOf ContentEditingPropertyComponent
   */
  @Input()
  public form: FormGroup;

  /**
   * The property key
   *
   * @private
   * @type {string}
   * @memberOf ContentEditingPropertyComponent
   */
  @Input()
  public key: string;

  /**
   *
   *
   * @private
   * @type {FormGroup}
   * @memberOf ContentEditingPropertyComponent
   */
  @Input()
  public subFieldGroup: FormGroup;

  /**
   * The edited schema item
   *
   * @private
   * @type {Model.Widget}
   * @memberOf ContentEditingPropertyComponent
   */
  private _schemaItem: FieldBean;

  public get schemaItem(): FieldBean {
      return this._schemaItem;
  }

  @Input('schemaItem')
  public set schemaItem(newSchemaItem: FieldBean) {
      this._schemaItem = newSchemaItem;
      this.isContainer = newSchemaItem.htmlRender in ContentEditingPropertyComponent.multiElement;
  }

  public static multiElement: any = {
      MULTIFIELD: true,
      TAB: true
  };

  isContainer: boolean;

  @Input()
  public inputClass: string = "";

  @Input()
  public labelClass: string = "";

  @Input()
  public readOnly: boolean = false;

  // private get labelClassCalc(): string {
  //     return (this.isContainer) ? '' : ('control-label ' + this.labelClass);
  // }

  //  [ngClass]="{'has-danger':!form.controls[key].valid && form.controls[key].touched, 'controls':!isContainer}" [className] = "isContainer?'':inputAndLabelClass"
  private get hasError(): boolean {
      return this.form.get(this.key).invalid && this.form.get(this.key).dirty && this.form.get(this.key).errors!=null;
  }

  // private get inputClassCalc(): string {
  //     const cls: string =
  //         (this.hasError ? ' has-danger ' : '')
  //             .concat((this.isContainer) ? '' : (' controls ' + this.inputClass))
  //         ;
  //     return cls;
  // }

  private get errors(): Array<string> {
      const errors: Array<string> = new Array<string>();
      if (this.form.invalid) {
          if (this.schemaItem.errorMessage != null && this.schemaItem.errorMessage.length > 0) {
              errors.push(this.schemaItem.errorMessage);
          } else {
              const control: AbstractControl = this.form.get(this.key);
              if (control.hasError('required')) {
                  errors.push('Dato obbligatorio');
              }
              else if (control.hasError('pattern')) {
                  errors.push('Formato del dato errato');
              }
              else if (control.hasError('dynamicValidation')) {
                  errors.push(control.getError('dynamicValidation'));
              }
              // Aggiungere altri errori da censire
              else if (control.errors) {
                  errors.push('Dato errato');
              } else {
                  // quando un input ha un errore tutti i form antenati sono in errore
                  // con errors vuoti, non voglio mostrare messaggi in quel caso
              }
          }
      }

      return errors;
  }
}
