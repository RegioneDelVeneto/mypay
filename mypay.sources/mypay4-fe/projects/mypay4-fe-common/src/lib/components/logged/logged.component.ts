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
import { OverlaySpinnerService } from 'projects/mypay4-fe-common/src/public-api';
import { first, take } from 'rxjs/operators';

import { Component, ElementRef, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';

import { EMAIL_SOURCE_TYPE } from '../../model/user';
import { ConfigurationService } from '../../services/configuration.service';
import { UserService } from '../../services/user.service';
import { manageError } from '../../utils/manage-errors';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-logged',
  templateUrl: './logged.component.html',
  styleUrls: ['./logged.component.scss']
})
export class LoggedComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private toastrService: ToastrService,
    private overlaySpinnerService: OverlaySpinnerService,
    private elementRef: ElementRef,
    private dialog: MatDialog,
    private configurationService: ConfigurationService,
  ) { }

  ngOnInit(): void {

    this.route.queryParams.pipe(first()).subscribe(params => {
      const loginToken = params['login_token'];
      const spinner = this.overlaySpinnerService.showProgress(this.elementRef);
      this.userService.loginToken(loginToken, true).subscribe( user => {
        this.overlaySpinnerService.detach(spinner);
        const welcomeMessage = "Autenticato come "+user.nome+" "+user.cognome+" ("+user.codiceFiscale+")";
        this.toastrService.success(welcomeMessage);

        //in case email was set by backoffice admin, ask user if he wants to change it
        if(this.configurationService.getMyPayAppModuleName()==='cittadino' && user.emailSourceType===EMAIL_SOURCE_TYPE.B){
          this.dialog.open(ConfirmDialogComponent,{autoFocus:false, data: {
            message: `L'indirizzo email dell'utente (${user.email}) è stato impostato dall'amministratore di sistema.<br>Vuoi confermarlo (in caso, potrai modificare in seguito l\'indirizzo email dalla pagina \'Dati personali\') o modificarlo?`,
            confirmLabel: "Modifica", cancelLabel: "Conferma", titleLabel: "Conferma indirizzo email", checkboxLabel: "Non mostrare più questo messaggio"}})
          .afterClosed().pipe(first()).subscribe(result => {
            if(result==="true"){
              this.router.navigate(['utente']);
            } else if(result==="false_checked"){
              this.userService.changeEmailSourceFromBackofficeToValidated().subscribe( () => {
                this.redirectLogged();
              }, manageError("Errore finalizzando l\'autenticazione", this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}));
            } else {
              this.redirectLogged();
            }
          });
        } else {
          this.redirectLogged();
        }

      }, manageError("Errore finalizzando l\'autenticazione", this.toastrService, () => {this.overlaySpinnerService.detach(spinner)}) );
    });
  }

  private redirectLogged(){
    this.route.data.pipe(take(1)).subscribe(data => {
      const redirectTo = data?.redirectTo ?? 'cards';
      console.log('logged, redirect to: '+redirectTo);
      this.router.navigate([redirectTo]);
    })
  }

}
