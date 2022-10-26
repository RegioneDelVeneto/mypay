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
    Ente, manageError, MyPayBreadcrumbsService, WithTitle
} from 'projects/mypay4-fe-common/src/public-api';

import { Component, OnInit, SecurityContext } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { faInfoCircle, faLandmark } from '@fortawesome/free-solid-svg-icons';

import { EnteService } from '../../services/ente.service';

@Component({
  selector: 'app-home-ente',
  templateUrl: './home-ente.component.html',
  styleUrls: ['./home-ente.component.scss']
})
export class HomeEnteComponent implements OnInit, WithTitle {

  get titleLabel(){ return "Home ente" }
  get titleIcon(){ return faLandmark }

  ente: Ente;
  deInformazioniEnte: string;
  iconInfo = faInfoCircle;

  constructor(
    private route:ActivatedRoute,
    private enteService: EnteService,
    private toastrService: ToastrService,
    private breadcrumbsService: MyPayBreadcrumbsService,
    private domSanitizer: DomSanitizer,
  ) { }

  ngOnInit(): void {
    const codIpaEnte = this.route.snapshot.paramMap.get('codIpaEnte');
    console.log("homeEnte: setting ente to "+codIpaEnte);
    this.enteService.getEnte(codIpaEnte).subscribe(ente => {
      if(ente){
        this.ente = ente;
        this.deInformazioniEnte = this.domSanitizer.sanitize(SecurityContext.HTML, ente?.deInformazioniEnte);
        this.breadcrumbsService.updateCurrentBreadcrumb("Home ente "+ente.deNomeEnte);
      } else {
        manageError("Errore di sistema", this.toastrService)("ente non esistente");
      }
    });
  }

}
