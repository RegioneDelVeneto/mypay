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
import { ElementRef, Injectable } from '@angular/core';
import { ComponentPortal, ComponentType } from '@angular/cdk/portal';
import { timer, Subscription } from 'rxjs';
import { OverlaySpinnerContainerComponent } from './overlay-spinner-container.component';
import { DynamicOverlay } from './dynamic-overlay.service';
import { OverlayRef } from '@angular/cdk/overlay';

@Injectable()
export class OverlaySpinnerService {

  constructor(private dynamicOverlay: DynamicOverlay) { }

  public showProgress<T>(elRef: ElementRef, component: ComponentType<T> = null) {
    if (elRef) {
      const result: OverlaySpinnerRef = { subscription: null, overlayRef: null };
      result.subscription = timer(500)
        .subscribe(() => {
          this.dynamicOverlay.setContainerElement(elRef.nativeElement);
          const positionStrategy = this.dynamicOverlay.position().global().centerHorizontally().centerVertically();
          result.overlayRef = this.dynamicOverlay.create({
            positionStrategy: positionStrategy,
            hasBackdrop: true
          });
          if(component)
            result.overlayRef.attach(new ComponentPortal(component));
          else
            result.overlayRef.attach(new ComponentPortal(OverlaySpinnerContainerComponent));
        });
      return result;
    } else {
      return null;
    }
  }

  detach(result: OverlaySpinnerRef) {
    if (result) {
      result.subscription.unsubscribe();
      if (result.overlayRef) {
        result.overlayRef.detach();
      }
    }
  }
}

export declare type OverlaySpinnerRef = { subscription: Subscription, overlayRef: OverlayRef };
