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
import { AccessGuard } from 'projects/mypay4-fe-common/src/public-api';

import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import {
    LoggedComponent
} from '../../../../mypay4-fe-common/src/lib/components/logged/logged.component';
import { AvvisiComponent } from '../components/avvisi/avvisi.component';
import { CardsComponent } from '../components/cards/cards.component';
import { CarrelloComponent } from '../components/carrello/carrello.component';
import { DatiUtenteComponent } from '../components/dati-utente/dati-utente.component';
import { DebitiComponent } from '../components/debiti/debiti.component';
import { EsitoCarrelloComponent } from '../components/esito-carrello/esito-carrello.component';
import { HomeEnteComponent } from '../components/home-ente/home-ente.component';
import { HomeComponent } from '../components/home/home.component';
import { LandingComponent } from '../components/landing/landing.component';
import { PagatiComponent } from '../components/pagati/pagati.component';
import { SpontaneoComponent } from '../components/spontaneo/spontaneo.component';
import { ForcedMailValidationGuard } from './forced-mail-validation-guard';

const routes = [
  {path: 'home', component: HomeComponent, data:{menuItemId: 20, breadcrumb:{home:true}}},
  {path: 'logged', component: LoggedComponent, data:{redirectTo: 'cards'}},
  {path: 'landing/:type', component: LandingComponent},
  {path: 'cards', component: CardsComponent, data:{menuItemId: 21, requiresLogin: true, breadcrumb:{home: true}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'utente', component: DatiUtenteComponent, data:{menuItemId: 28, requiresLogin: true, breadcrumb:{}}, canActivate: [ AccessGuard ]},
  {path: 'debiti', component: DebitiComponent, data:{menuItemId: 22, requiresLogin: true, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'pagati', component: PagatiComponent, data:{menuItemId: 23, requiresLogin: true, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'avvisi', component: AvvisiComponent, data:{menuItemId: 24, breadcrumb:{}}, canActivate: [ ForcedMailValidationGuard ]},
  {path: 'spontaneo', component: SpontaneoComponent, data:{menuItemId: 32, breadcrumb:{}}, canActivate: [ ForcedMailValidationGuard ]},
  {path: 'ente/:codIpaEnte', component: HomeEnteComponent, data:{breadcrumb:{}}, canActivate: [ ForcedMailValidationGuard ]},
  {path: 'carrello', component: CarrelloComponent, data:{menuItemId: 26, breadcrumb:{}}, canActivate: [ ForcedMailValidationGuard ]},
  {path: 'esito-carrello', component: EsitoCarrelloComponent, data:{menuItemId: 27, breadcrumb:{label:"Esito pagamento"}}},
  {path: '', redirectTo: 'cards', pathMatch: 'full'},
  {path: '**', redirectTo: 'cards', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
