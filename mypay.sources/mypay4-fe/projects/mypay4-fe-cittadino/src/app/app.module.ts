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
import * as _ from 'lodash';
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
    AccessGuard, AppConfirmDirective, ConfigurationFactory, ConfigurationService,
    ContentEditingPropertyComponent, CookieService, CustomBreakPointsProvider,
    CustomLayoutDirective, DetailFilterPipe, DynamicOverlay, DynamicOverlayContainer, DynamicPipe,
    getItalianPaginatorIntl, GlobalPipe, JoinPipe, MapPipe, MyPayBreadcrumbsComponent, OrderByPipe,
    OverlaySpinnerContainerComponent, OverlaySpinnerService, RenderableItemsFilterPipe,
    TabbingClickDirective, TokenInterceptor, TrimDirective
} from 'projects/mypay4-fe-common/src/public-api';

import { OverlayModule } from '@angular/cdk/overlay';
import { CurrencyPipe, DatePipe, registerLocaleData, TitleCasePipe } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import localeItExtra from '@angular/common/locales/extra/it';
import localeIt from '@angular/common/locales/it';
import { APP_INITIALIZER, DEFAULT_CURRENCY_CODE, LOCALE_ID, NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DATE_FORMATS } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
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
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgxMatomoRouterModule } from '@ngx-matomo/router';
import {
    MATOMO_CONFIGURATION, MatomoConsentMode, NgxMatomoTrackerModule
} from '@ngx-matomo/tracker';

import {
    LoggedComponent
} from '../../../mypay4-fe-common/src/lib/components/logged/logged.component';
import { LoginComponent } from '../../../mypay4-fe-common/src/lib/components/login/login.component';
import {
    MyPayTableDetailComponent
} from '../../../mypay4-fe-common/src/lib/components/my-pay-table-detail/my-pay-table-detail.component';
import { ToDoComponent } from '../../../mypay4-fe-common/src/lib/components/to-do/to-do.component';
import { AppRoutingModule } from './app-routing/app-routing.module';
import { ForcedMailValidationGuard } from './app-routing/forced-mail-validation-guard';
import { AppComponent } from './app.component';
import { AvvisiComponent } from './components/avvisi/avvisi.component';
import { CardsComponent } from './components/cards/cards.component';
import { CarrelloComponent } from './components/carrello/carrello.component';
import { DatiUtenteComponent } from './components/dati-utente/dati-utente.component';
import { DebitiComponent } from './components/debiti/debiti.component';
import { EsitoCarrelloComponent } from './components/esito-carrello/esito-carrello.component';
import { FooterComponent } from './components/footer/footer.component';
import { HeaderComponent } from './components/header/header.component';
import { HomeEnteComponent } from './components/home-ente/home-ente.component';
import { HomeComponent } from './components/home/home.component';
import { LandingComponent } from './components/landing/landing.component';
import {
    MyPayTableCittadinoComponent
} from './components/my-pay-table-cittadino/my-pay-table-cittadino.component';
import { PagatiComponent } from './components/pagati/pagati.component';
import { SpontaneoDynamoComponent } from './components/spontaneo-dynamo/spontaneo-dynamo.component';
import { SpontaneoComponent } from './components/spontaneo/spontaneo.component';
import { SidenavService } from './services/sidenav.service';

registerLocaleData(localeIt, localeItExtra);

export function bootstrapMyPayConfig(configurationService: ConfigurationService) {
  return () => ConfigurationFactory.get().init().then( () => configurationService.bootstrapConfig() );
}

export function matomoTrackingEnabled() {
  const matomo1Id = ConfigurationFactory.get().getBackendProperty('matomo1SiteId');
  const matomo2Id = ConfigurationFactory.get().getBackendProperty('matomo2SiteId');
  return !_.isNil(matomo1Id) || !_.isNil(matomo2Id);
}

export function matomoConfigFactory() {
  const matomo1Id = ConfigurationFactory.get().getBackendProperty('matomo1SiteId');
  const matomo2Id = ConfigurationFactory.get().getBackendProperty('matomo2SiteId');
  const trackers = [];
  if(matomo1Id){
    trackers.push({
      siteId: matomo1Id,
      trackerUrl: ConfigurationFactory.get().getBackendProperty('matomo1TrackerUrl'),
      trackerUrlSuffix: ConfigurationFactory.get().getBackendProperty('matomo1Suffix')
    });
  }
  if(matomo2Id){
    trackers.push({
      siteId: matomo2Id,
      trackerUrl: ConfigurationFactory.get().getBackendProperty('matomo2TrackerUrl'),
      trackerUrlSuffix: ConfigurationFactory.get().getBackendProperty('matomo2Suffix')
    });
  }
  const trackingDisabled:boolean = trackers.length===0;
  console.log("matomo tracking disabled: "+trackingDisabled);
  return {
    disabled: trackingDisabled,
    acceptDoNotTrack: true,
    requireConsent: MatomoConsentMode.COOKIE,
    trackers: trackers
  };
}

@NgModule({
  declarations: [
    AppComponent,
    AppConfirmDirective,
    AvvisiComponent,
    CardsComponent,
    CarrelloComponent,
    CarrelloComponent,
    ConfirmDialogComponent,
    ContentEditingPropertyComponent,
    CustomLayoutDirective,
    DebitiComponent,
    DetailFilterPipe,
    DynamicPipe,
    EsitoCarrelloComponent,
    FooterComponent,
    GlobalPipe,
    HeaderComponent,
    HelpComponent,
    HelpFieldComponent,
    HomeComponent,
    JoinPipe,
    LandingComponent,
    LoggedComponent,
    LoginComponent,
    MapPipe,
    MyPayTableCittadinoComponent,
    MyPayTableDetailComponent,
    MyPayBreadcrumbsComponent,
    OrderByPipe,
    OverlaySpinnerContainerComponent,
    PagatiComponent,
    RenderableItemsFilterPipe,
    SpontaneoComponent,
    SpontaneoDynamoComponent,
    TabbingClickDirective,
    ToDoComponent,
    TrimDirective,
    DatiUtenteComponent,
    HomeEnteComponent,
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
    MatButtonToggleModule,
    MatCardModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatDialogModule,
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
    MatStepperModule,
    MatTableModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule,
    NgxMatomoTrackerModule,
    NgxMatomoRouterModule,
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
    ForcedMailValidationGuard,
    ConfigurationService,
    CookieService,
    CurrencyPipe,
    CustomBreakPointsProvider,
    DatePipe,
    DetailFilterPipe,
    DynamicOverlay,
    DynamicOverlayContainer,
    DynamicPipe,
    GlobalPipe,
    JoinPipe,
    MapPipe,
    OrderByPipe,
    OverlaySpinnerService,
    RenderableItemsFilterPipe,
    SidenavService,
    TitleCasePipe,
    { provide: APP_INITIALIZER, useFactory: bootstrapMyPayConfig, multi: true, deps: [ConfigurationService] },
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true },
    { provide: LOCALE_ID, useValue: 'it-IT' },  // TODO: verify need for internationalization. Now italian is forced
    { provide: DEFAULT_CURRENCY_CODE, useValue: '€' },
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
    { provide: MATOMO_CONFIGURATION, useFactory: matomoConfigFactory },
  ],
  entryComponents: [
    HelpComponent,
    HelpFieldComponent,
    LoginComponent,
    MyPayTableDetailComponent,
    OverlaySpinnerContainerComponent,
  ],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule { }
