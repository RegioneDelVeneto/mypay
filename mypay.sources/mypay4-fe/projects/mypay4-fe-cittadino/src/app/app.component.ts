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
import { ConfigurationService, CookieService, MenuItem, UserService } from 'projects/mypay4-fe-common/src/public-api';
import { delay, filter } from 'rxjs/operators';

import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, ViewChild } from '@angular/core';
import { MatSidenav } from '@angular/material/sidenav';
import { ActivatedRoute, ActivationEnd, NavigationEnd, Route, Router } from '@angular/router';
import { faSignOutAlt, faTimes, faUser } from '@fortawesome/free-solid-svg-icons';
import { MatomoTracker } from '@ngx-matomo/tracker';

import { Meta, Title } from '@angular/platform-browser';
import { environment } from '../environments/environment';
import { matomoTrackingEnabled } from './app.module';
import { CoreAppElementsActivation, CoreAppService } from './services/core-app.service';
import { MenuService } from './services/menu.service';
import { RecaptchaService } from './services/recaptcha.service';
import { SidenavService } from './services/sidenav.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements AfterViewInit {
  title = 'mypay4-fe-cittadino';
  @ViewChild('sidenav') public sidenav: MatSidenav;
  @ViewChild('mainContentContainer') mainContentContainer: ElementRef;

  iconUser = faUser;
  iconTimes = faTimes;
  iconSignOut = faSignOutAlt;

  captchaActivated = false;
  coreElemActivated = new CoreAppElementsActivation();
  missingNeededCookieConsent = false;


  constructor(
    private sidenavService: SidenavService,
    public menuService: MenuService,
    public userService: UserService,
    private recaptchaService: RecaptchaService,
    public cookieService: CookieService,
    private changeDetectorRef : ChangeDetectorRef,
    private coreAppService: CoreAppService,
    private motomoTrackerService: MatomoTracker,
    private router: Router,
    private titleService: Title,
    private metaService: Meta,
    conf: ConfigurationService
  ) {

    let routeConfig: Route;

    const dynamicTitleEnabled = conf.getProperty<boolean>('dynamicTitle',environment);
    const baseTitle = conf.getProperty<string>('baseTitle',environment)||'MyPay - Cittadino';
    const dynamicMetaDescriptionEnabled = conf.getProperty<boolean>('dynamicMetaDescription',environment);
    const baseMetaDescription = conf.getProperty<string>('baseMetaDescription',environment)||'MyPay, il Portale dei pagamenti';

    this.titleService.setTitle(`${baseTitle}`);
    this.metaService.updateTag({ name: 'description', content: `${baseMetaDescription}` });

    router.events.pipe(
        filter(event => event instanceof NavigationEnd || event instanceof ActivationEnd)
    ).subscribe(event => {

      if (event instanceof ActivationEnd) {
        routeConfig = event.snapshot.routeConfig;
      } else if (event instanceof NavigationEnd) {
        window.scrollTo({ top: 0, behavior: 'smooth' });
        //this.mainContentContainer.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start', inline: 'start' });

        let route: ActivatedRoute = this.router.routerState.root;
        while (route!.firstChild) {
          route = route.firstChild;
        }
        const pageTitle = routeConfig.component?.prototype?.titleLabel;
        if (pageTitle && dynamicTitleEnabled)
          this.titleService.setTitle(`${baseTitle} - ${pageTitle}`);
        else
          this.titleService.setTitle(`${baseTitle}`);

        if (pageTitle && dynamicMetaDescriptionEnabled)
          this.metaService.updateTag({ name: 'description', content: `${baseMetaDescription} - ${pageTitle}` });
        else
          this.metaService.updateTag({ name: 'description', content: `${baseMetaDescription}` });

      }
    });

    this.recaptchaService.isActiveObs().pipe(delay(0)).subscribe(isActive => this.captchaActivated = isActive);
    this.cookieService.getMissingNeededConsentObs().pipe(delay(0)).subscribe(missingNeededCookieConsent => {
      console.log("missingNeededCookieConsent: ", missingNeededCookieConsent);
      this.missingNeededCookieConsent = missingNeededCookieConsent;
      this.changeDetectorRef.detectChanges();
    });

    if (matomoTrackingEnabled())
      this.cookieService.getConsentStateObs().pipe(delay(0)).subscribe(consentState => {
        if (consentState.cookieAll || consentState.cookieTracking) {
          console.log("motomoTrackerService.rememberCookieConsentGiven");
          motomoTrackerService.rememberCookieConsentGiven();
        } else {
          console.log("motomoTrackerService.forgetCookieConsentGiven");
          motomoTrackerService.forgetCookieConsentGiven();
        }
      });
    this.coreAppService.getStateObs().pipe(delay(0)).subscribe(coreElemActivated => this.coreElemActivated = coreElemActivated);
  }

  ngAfterViewInit(): void {
    this.sidenavService.setSidenav(this.sidenav);
  }

  onClickFirstLevel(item: MenuItem) {
    this.sidenavService.close();
    this.menuService.onClickFirstLevel(item, this.menuService);
  }

  closeSidenav(){
    this.sidenavService.close();
  }

  openLoginForm() {
    this.sidenavService.close();
    this.userService.goToLogin();
  }

  logout() {
    this.sidenavService.close();
    this.userService.logout();
  }

  resetCookieConsent() {
    this.cookieService.resetCookieConsentBar();
  }

}
