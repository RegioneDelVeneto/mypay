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
import { CookieService, manageError, UserService } from 'projects/mypay4-fe-common/src/public-api';

import { Component, OnInit } from '@angular/core';
import {
    faFacebookSquare, faInstagramSquare, faTwitterSquare, faYoutubeSquare
} from '@fortawesome/free-brands-svg-icons';
import { faEnvelope, faPhone } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

  iconPhone = faPhone;
  iconEnvelope = faEnvelope;

  iconFacebook = faFacebookSquare;
  iconInstagram = faInstagramSquare;
  iconYoutube = faYoutubeSquare;
  iconTwitter = faTwitterSquare;

  constructor(
    private userService: UserService,
    private toastr: ToastrService,
    private cookieService: CookieService) { }

  ngOnInit(): void {
  }

  showAppInfo(){
    this.userService.getAppInfoString().subscribe(appInfoString => {
      this.toastr.info(appInfoString, 'Versione applicazione',{
        disableTimeOut: true,
        enableHtml: true,
        tapToDismiss: false,
        toastClass: 'ngx-toastr toast-app-info',
      });
      return null;
    }, manageError('Errore recuperando i dati della versione', this.toastr) );
  }

  resetCookieConsent(){
    this.cookieService.resetCookieConsentBar();
  }

}
