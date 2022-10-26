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
import { FileSaverModule } from 'ngx-filesaver';
import { MAT_LUXON_DATE_ADAPTER_OPTIONS, MatLuxonDateModule } from 'ngx-material-luxon';
import { ToastContainerModule, ToastrModule } from 'ngx-toastr';
import {
    ConfirmDialogComponent
} from 'projects/mypay4-fe-common/src/lib/components/confirm-dialog/confirm-dialog.component';
import {
    HelpFieldComponent
} from 'projects/mypay4-fe-common/src/lib/components/help-field/help-field.component';
import { HelpComponent } from 'projects/mypay4-fe-common/src/lib/components/help/help.component';
import {
    MyPayTableDetailComponent
} from 'projects/mypay4-fe-common/src/lib/components/my-pay-table-detail/my-pay-table-detail.component';
import {
    MypSearchChipsComponent
} from 'projects/mypay4-fe-common/src/lib/components/myp-search-chips/myp-search-chips.component';
import {
    AccessGuard, AppConfirmDirective, ConfigurationService, ContentEditingPropertyComponent,
    CookieService, DecodeHtmlPipe, DetailFilterPipe, DynamicOverlay, DynamicOverlayContainer,
    DynamicPipe, FileSizePipe, getItalianPaginatorIntl, GlobalPipe, JoinPipe, MapPipe,
    MyPayBreadcrumbsComponent, OverlaySpinnerContainerComponent, OverlaySpinnerService,
    TabbingClickDirective, TokenInterceptor
} from 'projects/mypay4-fe-common/src/public-api';

import { OverlayModule } from '@angular/cdk/overlay';
import { CurrencyPipe, DatePipe, registerLocaleData, TitleCasePipe } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import localeItExtra from '@angular/common/locales/extra/it';
import localeIt from '@angular/common/locales/it';
import { APP_INITIALIZER, LOCALE_ID, NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MAT_DATE_FORMATS } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorIntl, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import {
    LoggedComponent
} from '../../../mypay4-fe-common/src/lib/components/logged/logged.component';
import { LoginComponent } from '../../../mypay4-fe-common/src/lib/components/login/login.component';
import { ToDoComponent } from '../../../mypay4-fe-common/src/lib/components/to-do/to-do.component';
import { AppRoutingModule } from './app-routing/app-routing.module';
import { ForcedMailValidationGuard } from './app-routing/forced-mail-validation-guard';
import { AppComponent } from './app.component';
import { CardsComponent as AdminCardsComponent } from './components/admin/cards/cards.component';
import {
    AnagraficaLogoComponent
} from './components/admin/enti/anagrafica-logo/anagrafica-logo.component';
import { AnagraficaComponent } from './components/admin/enti/anagrafica/anagrafica.component';
import { EnteDetailsComponent } from './components/admin/enti/ente-details/ente-details.component';
import { EnteListComponent } from './components/admin/enti/ente-list/ente-list.component';
import {
    FunzionalitaListComponent
} from './components/admin/enti/funzionalita-list/funzionalita-list.component';
import { GiornaleComponent } from './components/admin/giornale/giornale.component';
import { ModifyMailComponent } from './components/admin/modify-mail/modify-mail.component';
import { RegistroComponent } from './components/admin/registro/registro.component';
import { TassonomieComponent } from './components/admin/tassonomie/tassonomie.component';
import {
    TipoAnagraficaComponent
} from './components/admin/tipi-dovuto/tipo-anagrafica/tipo-anagrafica.component';
import {
    TipoDetailsComponent
} from './components/admin/tipi-dovuto/tipo-details/tipo-details.component';
import {
    TipoEntiAnagraficaComponent
} from './components/admin/tipi-dovuto/tipo-enti-anagrafica/tipo-enti-anagrafica.component';
import { TipoListComponent } from './components/admin/tipi-dovuto/tipo-list/tipo-list.component';
import { UtenteComponent } from './components/admin/utente/utente.component';
import { UtentiComponent } from './components/admin/utenti/utenti.component';
import { CardsComponent } from './components/cards/cards.component';
import { DovutiDetailsComponent } from './components/dovuti-details/dovuti-details.component';
import { DovutiComponent } from './components/dovuti/dovuti.component';
import { EnteOverlayComponent } from './components/ente-overlay/ente-overlay.component';
import { FlussiCardsComponent } from './components/flussi-cards/flussi-cards.component';
import {
    FlussiExportDialogComponent
} from './components/flussi-export-dialog/flussi-export-dialog.component';
import { FlussiExportComponent } from './components/flussi-export/flussi-export.component';
import { FlussiImportComponent } from './components/flussi-import/flussi-import.component';
import { FlussiSpcComponent } from './components/flussi-spc/flussi-spc.component';
import { FooterComponent } from './components/footer/footer.component';
import { HeaderComponent } from './components/header/header.component';
import { HomeComponent } from './components/home/home.component';
import {
    MyPayTableOperatoreComponent
} from './components/my-pay-table-operatore/my-pay-table-operatore.component';
import { NotAuthorizedComponent } from './components/not-authorized/not-authorized.component';
import { SidenavService } from './services/sidenav.service';

registerLocaleData(localeIt, localeItExtra);

export function bootstrapMyPayConfig(configurationService: ConfigurationService) {
  return () => configurationService.bootstrapConfig();
}

@NgModule({
  declarations: [
    // MassiveComponent,
    AdminCardsComponent,
    AnagraficaComponent,
    AnagraficaLogoComponent,
    AppComponent,
    AppConfirmDirective,
    CardsComponent,
    ConfirmDialogComponent,
    ContentEditingPropertyComponent,
    DecodeHtmlPipe,
    DetailFilterPipe,
    DovutiComponent,
    DovutiDetailsComponent,
    DynamicPipe,
    EnteDetailsComponent,
    EnteListComponent,
    EnteOverlayComponent,
    FileSizePipe,
    FlussiCardsComponent,
    FlussiExportComponent,
    FlussiExportDialogComponent,
    FlussiImportComponent,
    FlussiSpcComponent,
    FooterComponent,
    FunzionalitaListComponent,
    GlobalPipe,
    HeaderComponent,
    HelpComponent,
    HelpFieldComponent,
    HomeComponent,
    JoinPipe,
    LoggedComponent,
    LoginComponent,
    MapPipe,
    ModifyMailComponent,
    MyPayTableDetailComponent,
    MyPayTableOperatoreComponent,
    MyPayBreadcrumbsComponent,
    MypSearchChipsComponent,
    NotAuthorizedComponent,
    OverlaySpinnerContainerComponent,
    RegistroComponent,
    TabbingClickDirective,
    TassonomieComponent,
    TipoAnagraficaComponent,
    TipoDetailsComponent,
    TipoEntiAnagraficaComponent,
    TipoListComponent,
    ToDoComponent,
    UtenteComponent,
    UtentiComponent,
    GiornaleComponent,
  ],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    FileSaverModule,
    FlexLayoutModule,
    FontAwesomeModule,
    FormsModule,
    HttpClientModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDatepickerModule,
    MatDialogModule,
    MatDividerModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatGridListModule,
    MatInputModule,
    MatListModule,
    MatLuxonDateModule,
    MatMenuModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatSelectModule,
    MatSidenavModule,
    MatSlideToggleModule,
    MatSliderModule,
    MatSortModule,
    MatTableModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule,
    OverlayModule,
    ReactiveFormsModule,
    ToastContainerModule,
    ToastrModule.forRoot({
      timeOut: 5000,
      positionClass: 'toast-top-center',
      closeButton: true,
      progressBar: true,
    }),
  ],
  providers: [
    AccessGuard,
    ConfigurationService,
    CookieService,
    CurrencyPipe,
    DatePipe,
    DecodeHtmlPipe,
    DetailFilterPipe,
    DynamicOverlay,
    DynamicOverlayContainer,
    DynamicPipe,
    FileSizePipe,
    ForcedMailValidationGuard,
    GlobalPipe,
    JoinPipe,
    MapPipe,
    OverlaySpinnerService,
    SidenavService,
    TitleCasePipe,
    { provide: APP_INITIALIZER, useFactory: bootstrapMyPayConfig, multi: true, deps: [ConfigurationService] },
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true },
    { provide: LOCALE_ID, useValue: 'it-IT' },  // TODO: verify need for internationalization. Now italian is forced
    { provide: MatPaginatorIntl, useValue: getItalianPaginatorIntl() },
    { provide: MAT_LUXON_DATE_ADAPTER_OPTIONS, useValue: {
      firstDayOfWeek: (locale: string) => {return 1;} // 0 = Sunday, 1 = Monday, etc
    } },
  { provide: MAT_DATE_FORMATS, useValue: {
      parse: {
        dateInput: 'dd/MM/yyyy',
      },
      display: {
        dateInput: 'dd/MM/yyyy',
        monthYearLabel: 'MMM/yyyy',
        dateA11yLabel: 'dd/MM/yyyy',
        monthYearA11yLabel: 'MMM/yyyy',
      },
    } },
  ],
  entryComponents: [
    HelpComponent,
    HelpFieldComponent,
    LoginComponent,
    OverlaySpinnerContainerComponent,
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
