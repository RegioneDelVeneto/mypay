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
import { ToastContainerDirective, ToastrService } from 'ngx-toastr';
import { first } from 'rxjs/operators';

import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { faTimes } from '@fortawesome/free-solid-svg-icons';

import { environment } from '../../environments/environment';
import { EMAIL_SOURCE_TYPE } from '../../model/user';
import { ConfigurationService } from '../../services/configuration.service';
import { UserService } from '../../services/user.service';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  iconTimes = faTimes;

  @ViewChild(ToastContainerDirective, { static: true })
  toastContainer: ToastContainerDirective;

  user = {username: '', password: '', remember: false};
  errorMessage = '';

  constructor(
    private toastrService: ToastrService,
    public dialogRef: MatDialogRef<LoginComponent>,
    private userService: UserService,
    private router: Router,
    private dialog: MatDialog,
    private configurationService: ConfigurationService,
  ) { }

  ngOnInit(): void {
    this.toastrService.overlayContainer = this.toastContainer;
    this.user.username = this.configurationService.getProperty('fakeAuthUser', environment, '');
    this.user.password = this.configurationService.getProperty('fakeAuthPassword', environment, '');
  }

  onSubmit() {
    this.userService.loginPassword(this.user.username, this.user.password, this.user.remember)
      .subscribe( () => {
          const user = this.userService.getLoggedUser();
          //success
          console.log("logged user:", user);
          this.dialogRef.close();
          this.toastrService.overlayContainer = null;
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
                  this.router.navigate(['cards']);
                }, error => {
                  this.toastrService.error(error, null, {disableTimeOut: true});
                });
              } else {
                this.router.navigate(['cards']);
              }
            });
          } else {
            this.router.navigate(['cards']);
          }
      }, error => {
        this.toastrService.error(error, null, {disableTimeOut: true, positionClass:'toast-inline'});
      });
  }

}
