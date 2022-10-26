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
import {Overlay, OverlayKeyboardDispatcher, OverlayPositionBuilder, ScrollStrategyOptions} from '@angular/cdk/overlay';
import {ComponentFactoryResolver, Inject, Injectable, Injector, NgZone, Renderer2, RendererFactory2} from '@angular/core';
import {DynamicOverlayContainer} from './dynamic-overlay-container.service';
import {Directionality} from '@angular/cdk/bidi';
import {DOCUMENT} from '@angular/common';

@Injectable()
export class DynamicOverlay extends Overlay {

  private readonly _dynamicOverlayContainer: DynamicOverlayContainer;
  private renderer: Renderer2;

  constructor(scrollStrategies: ScrollStrategyOptions,
              _overlayContainer: DynamicOverlayContainer,
              _componentFactoryResolver: ComponentFactoryResolver,
              _positionBuilder: OverlayPositionBuilder,
              _keyboardDispatcher: OverlayKeyboardDispatcher,
              _injector: Injector, _ngZone: NgZone, @Inject(DOCUMENT) _document: any, _directionality: Directionality, rendererFactory: RendererFactory2) {
    super(scrollStrategies, _overlayContainer, _componentFactoryResolver, _positionBuilder, _keyboardDispatcher, _injector, _ngZone, _document, _directionality);
    this.renderer = rendererFactory.createRenderer(null, null);

    this._dynamicOverlayContainer = _overlayContainer;
  }

  public setContainerElement(containerElement: HTMLElement): void {
    this.renderer.setStyle(containerElement, 'transform', 'translateZ(0)');
    this._dynamicOverlayContainer.setContainerElement(containerElement);
  }
}
