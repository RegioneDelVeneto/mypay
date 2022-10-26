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
import { TableAction } from 'projects/mypay4-fe-common/src/public-api';
import { BehaviorSubject, Subscription } from 'rxjs';

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { faTimes } from '@fortawesome/free-solid-svg-icons';

import { DynamicPipe } from '../../pipes/dynamic-pipe';
import { TableColumn } from '../../table/table-column';

export type KeyValue = { [key: string]: string };
export type UpdateDetailFun = (details:BehaviorSubject<KeyValue[]>)=>void;

@Component({
  selector: 'lib-my-pay-table-detail',
  templateUrl: './my-pay-table-detail.component.html',
  styleUrls: ['./my-pay-table-detail.component.scss']
})
export class MyPayTableDetailComponent implements OnInit, OnDestroy {

  public static DIALOG_ID = 'mypay4-table-detail-dialog';

  public static SECTION_ID = '__MYPAY_SECTION__';
  public static NO_DETAIL = '__NO_MYPAY_DETAIL__';

  public detailsGroups: any;
  public detailsGroupsLabel: any;
  public element: any;
  public tableId: string;
  public parentRef: any;
  public actionColumn: TableColumn;
  public detailFilterInclude: string[];
  public detailFilterExclude: string[];
  private detailSubjectSubscription: Subscription;

  iconTimes = faTimes;

  constructor(
    @Inject(MAT_DIALOG_DATA) private data: any,
    private dynamicPipe: DynamicPipe,
    private matDialog: MatDialog,
  ) {
    this.element = data.element;
    this.tableId = data.tableId;
    this.parentRef = data.parentRef;
    this.detailFilterInclude = data.detailFilterInclude;
    this.detailFilterExclude = data.detailFilterExclude;

    let detailSubject:BehaviorSubject<KeyValue[]>;
    if(this.element.details instanceof BehaviorSubject){
      detailSubject = this.element.details;
    } else {
      detailSubject = new BehaviorSubject(this.element.details);
    }

    this.detailSubjectSubscription = detailSubject.subscribe(newDetails => {

      const detailsGroups = [];
      const detailsGroupsLabel = [];
      let details = [];
      data.tableColumnns.forEach((aColumn:TableColumn) => {
        if(!aColumn.actions && !aColumn.isImg && aColumn.id!=="tableExpandColumn")
          details.push({key:aColumn.label, value: this.dynamicPipe.transform(this.element[aColumn.id], aColumn.pipe, aColumn.pipeArgs)})
        if(aColumn.actions)
          this.actionColumn = aColumn;
      });
      if(details){
        detailsGroupsLabel.push(null);
        detailsGroups.push(details);
      }

      newDetails?.forEach(element => {
        if(element.key === MyPayTableDetailComponent.SECTION_ID){
          detailsGroupsLabel.push(element.value);
          details = [];
          detailsGroups.push(details);
        } else {
          if(!details){
            detailsGroupsLabel.push(null);
            detailsGroups.push(details);
          }
          const alreadyPresentDetail = details.find(elem => elem.key === element.key);
          if(alreadyPresentDetail)
            alreadyPresentDetail.value = element.value;
          else
            details.push(element);
        }
      });

      this.detailsGroups = detailsGroups;
      this.detailsGroupsLabel = detailsGroupsLabel;
    });

  }

  clickAction(action: TableAction, eventRef: any):void {
    MyPayTableDetailComponent.close(this.matDialog);
    action.click(this.tableId, this.element, this.parentRef, eventRef);
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.detailSubjectSubscription?.unsubscribe();
  }

  static close(matDialog: MatDialog){
    matDialog.getDialogById(MyPayTableDetailComponent.DIALOG_ID)?.close();
  }

  public static markNoDetail(element: any){
    element.details = null;
    element[MyPayTableDetailComponent.NO_DETAIL] = true;
  }

}
