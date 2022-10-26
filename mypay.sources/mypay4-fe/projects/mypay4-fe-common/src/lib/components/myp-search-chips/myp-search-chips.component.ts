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
import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup } from '@angular/forms';
import { faTimes } from '@fortawesome/free-solid-svg-icons';

import { SearchFilter, SearchFilterDef } from '../../model/search-filter';

@Component({
  selector: 'myp-search-chips',
  templateUrl: './myp-search-chips.component.html',
  styleUrls: ['./myp-search-chips.component.scss']
})
export class MypSearchChipsComponent implements OnInit {

  searchOpenState: boolean = true;
  chips: SearchFilter[] = [];
  //iconTrash = faTrash;
  iconTrash = faTimes;

  @Input() formDef: { [key: string]: SearchFilterDef; };
  @Input() form: FormGroup;
  @Input() parentRef: any;
  @Input() removeFilterCallback: (any) => void;

  constructor() { }

  ngOnInit(): void {
  }

  setSearchPanelState(opened: boolean){
    this.searchOpenState = opened;
    if(!opened){
      const data = this.form.value;
      this.chips = [];
      Object.keys(data).forEach(field => {
        const def = this.formDef[field];
        if(!def)
          return;
        const removable: boolean = def.removable!=null ? def.removable : !this.isFieldRequired(this.form.get(field));
        const value = (def.valueConv || this.defaultValueConv)(data[field], data);
        if(value!=null)
          this.chips.push(new SearchFilter(field, def.label, value, removable));
      });
    }
  }
  removeSearchFilter(chip: SearchFilter){
    this.form.get(chip.field).setValue(this.formDef[chip.field].value);
    this.chips.splice(this.chips.indexOf(chip),1);

    this.removeFilterCallback(this.parentRef);
  }
  isFieldRequired(formControl: AbstractControl): boolean {
    let errors: any = formControl.validator && formControl.validator(new FormControl());
    return errors?.required;
  }
  defaultValueConv(value: any): string {
    if(typeof value === 'undefined' || value===null)
      return null;
    else {
      const valueString = ''+value;
      return valueString.length>0 ? valueString : null;
    }
  }

  doNothing():void {
    //just needed to enable keyboard tab navigation (using TabbingClickDirective)
  }

}
