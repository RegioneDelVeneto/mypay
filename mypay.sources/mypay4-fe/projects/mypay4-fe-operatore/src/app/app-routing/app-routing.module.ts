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
import {
    LoggedComponent
} from 'projects/mypay4-fe-common/src/lib/components/logged/logged.component';
import { ToDoComponent } from 'projects/mypay4-fe-common/src/lib/components/to-do/to-do.component';
import { AccessGuard } from 'projects/mypay4-fe-common/src/public-api';

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CardsComponent as AdminCardsComponent } from '../components/admin/cards/cards.component';
import { AnagraficaComponent } from '../components/admin/enti/anagrafica/anagrafica.component';
import { EnteDetailsComponent } from '../components/admin/enti/ente-details/ente-details.component';
import { EnteListComponent } from '../components/admin/enti/ente-list/ente-list.component';
import { GiornaleComponent } from '../components/admin/giornale/giornale.component';
import { TassonomieComponent } from '../components/admin/tassonomie/tassonomie.component';
import {
    TipoAnagraficaComponent
} from '../components/admin/tipi-dovuto/tipo-anagrafica/tipo-anagrafica.component';
import {
    TipoDetailsComponent
} from '../components/admin/tipi-dovuto/tipo-details/tipo-details.component';
import {
    TipoEntiAnagraficaComponent
} from '../components/admin/tipi-dovuto/tipo-enti-anagrafica/tipo-enti-anagrafica.component';
import { TipoListComponent } from '../components/admin/tipi-dovuto/tipo-list/tipo-list.component';
import { UtenteComponent } from '../components/admin/utente/utente.component';
import { UtentiComponent } from '../components/admin/utenti/utenti.component';
import { CardsComponent } from '../components/cards/cards.component';
import { DovutiDetailsComponent } from '../components/dovuti-details/dovuti-details.component';
import { DovutiComponent } from '../components/dovuti/dovuti.component';
import { FlussiCardsComponent } from '../components/flussi-cards/flussi-cards.component';
import { FlussiExportComponent } from '../components/flussi-export/flussi-export.component';
import { FlussiImportComponent } from '../components/flussi-import/flussi-import.component';
import { FlussiSpcComponent } from '../components/flussi-spc/flussi-spc.component';
import { HomeComponent } from '../components/home/home.component';
import { NotAuthorizedComponent } from '../components/not-authorized/not-authorized.component';
import { ForcedMailValidationGuard } from './forced-mail-validation-guard';

export const routes: Routes = [
  {path: 'home', component: HomeComponent, data:{breadcrumbs:{home:true}}},
  {path: 'logged', component: LoggedComponent, data:{redirectTo:'cards'}},
  {path: 'cards', component: CardsComponent, data:{requiresLogin: true, menuItemId: 20, breadcrumb:{home:true}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'not-authorized', component: NotAuthorizedComponent, data:{needEnte:false, requiresLogin: true, breadcrumb:{label:"Errore"}}, canActivate: [ AccessGuard ]},
  {path: 'flussi', component: FlussiCardsComponent, data:{requiresLogin: true, menuItemId: 21, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'flussi-import', component: FlussiImportComponent, data:{requiresLogin: true, menuItemId: 31, breadcrumb:{label:"Importazione flussi"}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'flussi-export', component: FlussiExportComponent, data:{requiresLogin: true, menuItemId: 32, breadcrumb:{label:"Flussi RT"}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'flussi-spc/quadratura', component: FlussiSpcComponent, data:{requiresLogin: true, tipoFlusso: 'quadratura', menuItemId: 33, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'flussi-spc/rendicontazione', component: FlussiSpcComponent, data:{requiresLogin: true, tipoFlusso: 'rendicontazione', menuItemId: 34, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'dovuti', component: DovutiComponent, data:{requiresLogin: true, menuItemId: 22, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'dovutiDetails/:mode/:id', component: DovutiDetailsComponent, data:{requiresLogin: true, menuItemId: 22, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'revoche', component: ToDoComponent, data:{requiresLogin: true, menuItemId: 23, breadcrumb:{label:"Revoche"}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin', component: AdminCardsComponent, data:{requiresLogin: true, menuItemId: 40, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin/enti', component: EnteListComponent, data:{requiresLogin: true, menuItemId: 41, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin/utenti', component: UtentiComponent, data:{requiresLogin: true, menuItemId: 42, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin/tipiDovuto', component: TipoListComponent, data:{requiresLogin: true, menuItemId: 43, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin/tipiDovuto/tipo/:codTipo', component: TipoEntiAnagraficaComponent, data:{requiresLogin: true, menuItemId: 43, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin/tassonomie', component: TassonomieComponent, data:{requiresLogin: true, menuItemId: 44, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  //{path: 'admin/massive', component: MassiveComponent, data:{requiresLogin: true, menuItemId: 44}, canActivate: [ AccessGuard ]},
  {path: 'admin/utenti/:id', component: UtenteComponent, data:{requiresLogin: true, menuItemId: [42,41], breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin/enti/anagrafica/:mode/:id', component: AnagraficaComponent, data:{requiresLogin: true, menuItemId: 41, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin/enti/details/:enteId', component: EnteDetailsComponent, data:{requiresLogin: true, menuItemId: 41, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin/enti/tipo/details/:enteId/:tipoId', component: TipoDetailsComponent, data:{requiresLogin: true, menuItemId: [43, 41], breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin/enti/tipo/anagrafica/:enteId', component: TipoAnagraficaComponent, data:{requiresLogin: true, menuItemId: 43, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin/giornale/pa', component: GiornaleComponent, data:{requiresLogin: true, menuItemId: 46, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: 'admin/giornale/fesp', component: GiornaleComponent, data:{requiresLogin: true, menuItemId: 47, breadcrumb:{}}, canActivate: [ AccessGuard, ForcedMailValidationGuard ]},
  {path: '', redirectTo: 'cards', pathMatch: 'full'},
  {path: '**', redirectTo: 'cards', pathMatch: 'full'}
];
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
