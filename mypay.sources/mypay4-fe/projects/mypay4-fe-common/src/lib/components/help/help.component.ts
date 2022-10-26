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
import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { faQuestionCircle, faTimes } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'my-pay-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss']
})
export class HelpComponent implements OnInit {

  iconTimes = faTimes;
  iconQuestionCircle = faQuestionCircle;

  @Input('template') templateRef: TemplateRef<any>;

  constructor(
    private dialog:MatDialog,
  ) {
  }

  ngOnInit(): void {
  }

  openHelp(templateHelpDialog: TemplateRef<any>): void {
    this.dialog.open(templateHelpDialog, {panelClass: 'mypay4-page-help-panel', autoFocus:false, data: {templateRef: this.templateRef}});
  }

}
