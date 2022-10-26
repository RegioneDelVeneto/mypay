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
import { manageError, OverlaySpinnerService } from 'projects/mypay4-fe-common/src/public-api';
import { AdminEntiService } from 'projects/mypay4-fe-operatore/src/app/services/admin-enti.service';
import { EnteService } from 'projects/mypay4-fe-operatore/src/app/services/ente.service';

import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';

import { FileSizePipe } from '../../../../../../../mypay4-fe-common/src/lib/pipes/filesize-pipe';

@Component({
  selector: 'anagrafica-logo-ente',
  templateUrl: './anagrafica-logo.component.html',
  styleUrls: ['./anagrafica-logo.component.scss']
})
export class AnagraficaLogoComponent implements OnInit {

  @Input() mygovEnteId: number;
  @Input() enabled: boolean;
  imgSrcLogoEnte: string;
  @ViewChild('fileForm', { read: ElementRef }) fileFormElement: ElementRef;
  @ViewChild('fileInput') fileInput: ElementRef;
  formData: FormData = null;
  fileLabel: string;

  constructor(
    private fileSizePipe: FileSizePipe,
    private enteService: EnteService,
    private adminEntiService: AdminEntiService,
    private toastrService: ToastrService,
    private elementRef: ElementRef,
    private overlaySpinnerService: OverlaySpinnerService) { }

  ngOnInit(): void {
    if(this.mygovEnteId){
      const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
      this.enteService.getEnte(this.mygovEnteId).subscribe(ente => {
        this.imgSrcLogoEnte = ente.deLogoEnte;
        this.overlaySpinnerService.detach(spinner);
      }, manageError('Errore effettuando il caricamento del logo', this.toastrService, ()=>{this.overlaySpinnerService.detach(spinner)}) );
    }
  }

  onResetFile(){
    this.formData = null;
    this.fileLabel = null;
    this.fileFormElement.nativeElement.reset();
  }

  selectFileOnChange(files: FileList) {
    if (files?.length > 0) {
      this.formData = new FormData();
      this.formData.append("file", files[0]);
      this.fileLabel = files[0].name + " ["+this.fileSizePipe.transform(files[0].size)+"]";
    } else {
      this.formData = null;
      this.fileLabel = null;
      this.fileFormElement.nativeElement.reset();
    }
  }

  saveLogo() {
    const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
    this.adminEntiService.updateLogo(this.mygovEnteId, this.formData) .subscribe(ente => {
        this.imgSrcLogoEnte = ente.deLogoEnte;
        this.toastrService.success('Logo aggiornato correttamente');
        this.selectFileOnChange(null);
        this.overlaySpinnerService.detach(spinner);
    }, manageError('Errore effettuando l\'aggionamento di Logo file', this.toastrService, ()=>{this.overlaySpinnerService.detach(spinner)}) );

  }
}
