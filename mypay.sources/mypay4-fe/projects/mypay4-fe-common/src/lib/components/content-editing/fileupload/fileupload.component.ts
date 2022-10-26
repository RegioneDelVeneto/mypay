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
import { Component, OnInit, Input, ViewChild, ElementRef, EventEmitter, Output, Optional } from '@angular/core';
import { NgControl } from '@angular/forms';

@Component({
  selector: 'file-upload',
  templateUrl: './fileupload.component.html',
  styleUrls: ['./fileupload.component.scss']
})
export class FileuploadComponent implements OnInit {

  ngOnInit(): void {
  }

    /**
   * The file name
   * 
   * @private
   * @type {string}
   * @memberOf FileUploadComponent
   */
  @Input()
  public name: string;
  
  /**
   * Reference to file input DOM element
   * 
   * @private
   * @type {ElementRef}
   * @memberOf FileUploadComponent
   */
  @ViewChild('inputFile')
  public inputFileElement: ElementRef;

  /**
   * Notify changes on file select
   * 
   * @private
   * @type {EventEmitter<any>}
   * @memberOf FileUploadComponent
   */
  @Output()
  public notifyChange: EventEmitter<any> = new EventEmitter();
  
  /**
   * The selected files
   * 
   * @private
   * @type {FileList}
   * @memberOf FileUploadComponent
   */
  files: FileList;

  /**
   * The change callback for parent notification
   * 
   * @private
   * 
   * @memberOf FileUploadComponent
   */
  private onChangeCallback: (_: any) => { };

  /**
   * Creates an instance of FileUploadComponent.
   * 
   * @param {NgControl} control
   * 
   * @memberOf FileUploadComponent
   */
  constructor(@Optional() control: NgControl) {
      if (control) {
          control.valueAccessor = this;
      }
  }

  /**
   * 
   * 
   * @param {*} value
   * 
   * @memberOf FileUploadComponent
   */
  public writeValue(value: any) {
      if(value) {
          this.files = value;
      }
  }

  /**
   * Register the onChange function
   * 
   * @param {any} fn
   * 
   * @memberOf FileUploadComponent
   */
  public registerOnChange(fn: (_: any) => {}) {
      this.onChangeCallback = fn;
  }

  /**
   * Register the onTouched function 
   * 
   * 
   * @memberOf FileUploadComponent
   */
  public registerOnTouched() {
      // Do nothing
  }

  /**
   * 
   * 
   * @param {Event} event
   * 
   * @memberOf FileUploadComponent
   */
  public onChange(event: any): void {
      this.files = this.inputFileElement.nativeElement.files;
      this.onChangeCallback(this.files);
  }

  public onReset(event?: Event): void {
      this.inputFileElement.nativeElement.value = '';
      this.files = null;
      this.onChangeCallback(this.files);
  }

  selectFileOnChange(files: FileList) {
    this.files = files;
    this.onChangeCallback(this.files);
  }
}
