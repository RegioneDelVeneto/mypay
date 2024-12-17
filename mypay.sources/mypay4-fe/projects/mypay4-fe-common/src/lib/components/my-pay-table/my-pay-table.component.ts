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
import { BehaviorSubject, combineLatest, Observable, of, Subject } from 'rxjs';
import { filter, first, map, takeUntil } from 'rxjs/operators';

import { animate, state, style, transition, trigger } from '@angular/animations';
import {
  Component, ElementRef, Injector, Input, OnChanges, OnDestroy, OnInit, PipeTransform, Renderer2,
  SimpleChanges, Type, ViewChild, ViewContainerRef
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { faCopy, faEllipsisH, faFile, faSearch } from '@fortawesome/free-solid-svg-icons';

import { OverlaySpinnerService } from '../../overlay-spinner/overlay-spinner.service';
import { PaginatorData } from '../../table/paginator-data';
import { TableAction } from '../../table/table-action';
import { TableColumn } from '../../table/table-column';
import { WithActions } from '../../table/with-actions';
import { manageError } from '../../utils/manage-errors';
import { getUniqueId } from '../../utils/string-utils';
import { getProp } from '../../utils/utils';
import { MyPayTableDetailComponent } from '../my-pay-table-detail/my-pay-table-detail.component';

@Component({
  selector: '',
  template: ''
})
export class MyPayBaseTableComponent<T> implements OnInit, OnChanges, OnDestroy {

  SECTION_ID = MyPayTableDetailComponent.SECTION_ID;
  NO_DETAIL = MyPayTableDetailComponent.NO_DETAIL;

  //static properties for concrete implementations (cittadino, operatore)
  static templateUrl = 'mypay4-fe-common/src/lib/components/my-pay-table/my-pay-table.component.html';
  static animations = [
      trigger('detailExpand', [
        state('collapsed', style({height: '0px', minHeight: '0'})),
        state('expanded', style({height: '*'})),
        transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
      ]),
    ];


  iconEllipsisH = faEllipsisH;

  iconCopy = faCopy;
  iconFile = faFile;

  getProp = getProp;

  @Input() parentRef: any;
  @Input() tableData: T;
  @Input('tableColumns') inputTableColumns:TableColumn[];
  @Input() paginatorData: PaginatorData;
  @Input() rowStyle: (T)=>string;
  @Input() onClickRowFun: (T, any)=>Observable<void>;
  @Input() hasDetail: boolean = true;
  @Input() detailFilterInclude: string[];
  @Input() detailFilterExclude: string[];
  @Input() showFilter: boolean = false;
  @Input() showTotalFooter: boolean = false;



  @ViewChild(MatPaginator, {static: false}) paginator: MatPaginator;
  @ViewChild(MatSort, {static: false}) sort: MatSort;
  @ViewChild('template', {static: true}) template;
  @ViewChild('tableElementRef', { read: ElementRef }) tableElementRef: ElementRef;

  tableId: string;
  showTable = false;
  tableColumnsName:string[];
  tableDataSource: MatTableDataSource<T> = new MatTableDataSource();
  expandedElement: T | null;
  tableDatailColumnsName = ['key','value'];
  tableColumns:TableColumn[];

  sortColumnId: string;

  //responsive table
  private onDestroy$ = new Subject<boolean>();
  private thead: HTMLTableSectionElement;
  private tbody: HTMLTableSectionElement;
  private theadChanged$ = new BehaviorSubject(true);
  private tbodyChanged$ = new Subject<boolean>();
  private theadObserver = new MutationObserver(() => this.theadChanged$.next(true));
  private tbodyObserver = new MutationObserver(() => this.tbodyChanged$.next(true));

  constructor(
    private viewContainerRef: ViewContainerRef,
    private renderer: Renderer2,
    public dialog: MatDialog,
    private injector: Injector,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    ) {
      this.tableId = getUniqueId(2);
      //console.log('creating table with id',this.tableId);
    }

  ngOnInit(): void {
    if(!this.rowStyle)
      this.rowStyle = () => '';

    this.changeColumnsToShow();

    this.viewContainerRef.createEmbeddedView(this.template);
    //initialize sorter
    this.tableDataSource.sortingDataAccessor = (item, property) => {
      const sortable = this.tableColumns.find(item => item.id == property).sortable;
      if(typeof sortable === 'boolean')
        return item[property];
      else
        return sortable(item);
    };
  }

  //responsive table
  ngOnDestroy(): void {
    this.theadObserver?.disconnect();
    this.tbodyObserver?.disconnect();
    this.onDestroy$.next(true);
  }

  ngOnChanges(changes: SimpleChanges){
    const tableDataChange = changes['tableData'];
    const paginatorDataChange = changes['paginatorData'];
    if(tableDataChange){
      //console.log('[myPayTable] first:'+tableDataChange.isFirstChange(), tableDataChange.currentValue);
      // setting the paginator before loading data dramatically improves performance of table
      // rendering in case of big data sources (https://stackoverflow.com/a/51296374/8745150)
      this.tableDataSource.data = [];
      this.tableDataSource.paginator = this.paginator;
      this.tableDataSource.sort = this.sort;
      this.showTable = tableDataChange.currentValue?.length>0;

      setTimeout(() => {
        /*
         * Responsive table management
         */
        if(this.showTable && !this.thead){
          this.thead = this.tableElementRef.nativeElement.querySelector('thead');
          this.tbody = this.tableElementRef.nativeElement.querySelector('tbody');
          this.theadObserver.observe(this.thead, { characterData: true, subtree: true });
          this.tbodyObserver.observe(this.tbody, { childList: true });
          // Set the "data-column-name" attribute for every body row cell, either on
          // thead row changes (e.g. language changes) or tbody rows changes (add, delete).
          combineLatest([this.theadChanged$, this.tbodyChanged$])
            .pipe(
              map(x => [this.thead.rows.item(0), this.tbody.rows]),
              filter(([headRow, bodyRows]) => headRow != null),
              map(
                ([headRow, bodyRows]: [HTMLTableRowElement, HTMLCollectionOf<HTMLTableRowElement>]) => {
                  // let headRowChildren = [];
                  // for(let i=0; i<headRow.children?.length; i++)
                  return [
                  [...(<any>headRow).children].map(headerCell => headerCell.textContent),
                  [...(<any>bodyRows)].map(row => [...row.children])
                ]}
              ),
              takeUntil(this.onDestroy$)
            )
            .subscribe(([columnNames, rows]: [string[], HTMLTableCellElement[][]]) =>
              rows.forEach(rowCells =>
                rowCells.forEach(cell => this.renderer.setAttribute(
                    cell,
                    'data-column-name',
                    columnNames[cell.cellIndex]
                  )
                )
              )
            );
        }

        // set data source data
        if(tableDataChange.currentValue){
          this.tableDataSource.data = tableDataChange.currentValue;
          //set sorting behaviour
          this.tableDataSource.sort = this.sort;
          //set paginator to first page
          this.tableDataSource.paginator?.firstPage();
        }
      });
    }
    if(paginatorDataChange){
      setTimeout(() => {
        const currentPaginatorData = paginatorDataChange.currentValue;
        if(currentPaginatorData){
          this.paginator.pageSize = currentPaginatorData.pageSize;
          this.paginator.pageIndex = currentPaginatorData.pageIndex;
          //hack to change page (see https://github.com/angular/components/issues/8417)
          this.paginator._changePageSize(this.paginator.pageSize);
        } else {
          this.paginator.firstPage();
        }
        this.tableDataSource.paginator = this.paginator;
      });
    }

    // for (const propName in changes) {
    //   const changedProp = changes[propName];
    //   const to = JSON.stringify(changedProp.currentValue);
    //   if (changedProp.isFirstChange()) {
    //     log.push(`Initial value of ${propName} set to ${to}`);
    //   } else {
    //     const from = JSON.stringify(changedProp.previousValue);
    //     log.push(`${propName} changed from ${from} to ${to}`);
    //   }
    // }
    // this.changeLog.push(log.join(', '));

  }

  onClickRow(element:any){
    if(!this.enableDetail(element))
      return; //in this case there is no expandable detail
      
    if(this.expandedElement === element)
      this.expandedElement = null;
    else {
      const obs = this.onClickRowFun?.(element, this.parentRef) || of(null).pipe(first());
      const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
      obs.pipe(takeUntil(this.onDestroy$)).subscribe( () => {
        this.overlaySpinnerService.detach(spinner);
        if(element.details)
          this.expandedElement = element;
      }, manageError('Errore recuperando il dettaglio', this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
    }
  }

  //const BASE_URL = new InjectionToken<string>('BaseUrl');

  rowDescription(element:any){
    const descrFields:string[] = [];
    this.tableColumns
      ?.filter(col => (!col.dispCondition || col.dispCondition(this.parentRef, col)) && col.label && !col.actions)
      .forEach(col => {
        let value = this.getProp(element,col.id);
        if(value && col.pipe){
          value = this.injector.get<PipeTransform>(col.pipe as Type<PipeTransform>).transform(value,...(col.pipeArgs ?? []));
        }
        if(value==null)
          value = '<VUOTO>';
        descrFields.push(col.label+': '+value);
      });
    return descrFields.join(", ");
  }

  changeColumnsToShow() {
    setTimeout(() => {
      this.tableColumns = this.inputTableColumns.filter(col => !col.dispCondition || col.dispCondition(this.parentRef, col));
      if(this.hasDetail){
        //add detail action
        let actionColumn = this.tableColumns.find(aColumn => aColumn.actions?.length >= 0);
        const detailAction = new TableAction(faSearch, this.openDetailPanelFun(this), this.enableOpenDetailPanelFun(this), 'Mostra altri campi', null, TableAction.ACTION_ID_DETTAGLIO);
        if(!actionColumn)
          actionColumn = new TableColumn('rowActions', 'Azioni', { sortable: false, tooltip: 'Azioni', actions: [ detailAction ] });
        else if(!actionColumn.actions.find(action => action.id === TableAction.ACTION_ID_DETTAGLIO))
          actionColumn.actions.unshift(detailAction);
        //add accordion status icon
        this.tableColumns = this.tableColumns.concat([new TableColumn('tableExpandColumn','',{sortable:false,ariaLabel:'Mostra altri campi'})]);
      }
      this.tableColumnsName = this.tableColumns.map( col => col.id );
    }, 0);
  }

  toggleFloat(element: WithActions, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
    WithActions.toggleFloatingButtons(this.tableId, element);
  }

  openActionMenu(element: WithActions, eventRef: any) {
    if(eventRef)
      eventRef.stopPropagation();
  }

  enableDetail(element:any){
    return this.hasDetail && !element[MyPayTableDetailComponent.NO_DETAIL] && (this.onClickRowFun || element.details);
  }

  enableOpenDetailPanelFun(thisRef: MyPayBaseTableComponent<T>){
    return (element: any) => {
      return thisRef.enableDetail(element);
    }
  }

  openDetailPanelFun(thisRef: MyPayBaseTableComponent<T>){
    return (element: any) => {
      if(!thisRef.enableDetail(element))
        return; //in this case there is no expandable detail
      const obs = thisRef.onClickRowFun?.(element, thisRef.parentRef) || of(null).pipe(first());
      const spinner = thisRef.overlaySpinnerService.showProgress(thisRef.elementRef);
      obs.pipe(takeUntil(thisRef.onDestroy$)).subscribe( () => {
        thisRef.overlaySpinnerService.detach(spinner);
        //if(element.details)
          //open detail panel
          thisRef.dialog.open(MyPayTableDetailComponent, {panelClass: 'mypay4-detail-panel', autoFocus:false,
          id: MyPayTableDetailComponent.DIALOG_ID,
          data: {
            element: element,
            tableId: thisRef.tableId,
            parentRef: thisRef.parentRef,
            tableColumnns: thisRef.tableColumns,
            detailFilterInclude: thisRef.detailFilterInclude,
            detailFilterExclude: thisRef.detailFilterExclude} } );
      }, manageError('Errore recuperando il dettaglio', thisRef.toastrService, () => {thisRef.overlaySpinnerService.detach(spinner)}));
    }
  }

  showFloatingButtons(tableId: string, element: WithActions){
    return element.showFloatingButtons instanceof Map && element.showFloatingButtons?.get(tableId);
  }

  isCellClickable(tableId:string, parentRef:any, row:any, tableColumn:TableColumn){
    if (tableColumn.cellClickableCondition && !tableColumn.cellClickableCondition(parentRef, row))
      return false;

    const clickableProp = '__cellClickable__prop__'+tableColumn.id;
    if(row['__cellClickable__table__']!==tableId){
      Object.keys(row)
        .filter(x => x.startsWith('__cellClickable__prop__'))
        .forEach(x => delete row[x]);
        row['__cellClickable__table__']=tableId;
    }
    if(row?.hasOwnProperty(clickableProp)){
      return row[clickableProp];
    } else {
      const clickable = tableColumn.cellClick?.(tableId, parentRef, row, tableColumn, true) || false;
      //console.log(tableId+"-"+tableColumn.id+"-"+clickable, row);
      if(row)
        row[clickableProp] = clickable;
      return clickable;
    }
  }

  cellClick(tableId:string, parentRef:any, row:any, tableColumn:TableColumn){
    this.isCellClickable(tableId, parentRef, row, tableColumn) && tableColumn.cellClick?.(tableId, parentRef, row, tableColumn);
  }

  getTotal(columnId: string) {
    return this.tableDataSource.data.map(t=>t[columnId] as number).reduce((acc, value) => acc + value, 0);
  }
}
