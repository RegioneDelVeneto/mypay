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
import { Directive, ElementRef, HostListener, Inject, Optional, Renderer2 } from '@angular/core';
import { COMPOSITION_BUFFER_MODE, ControlValueAccessor } from '@angular/forms';

@Directive()
export abstract class AbstractTextOperationDirective implements ControlValueAccessor {


  protected abstract eventValue(): string;
  protected abstract processValue(value: string): string;

  private get _type(): string {
    return this._sourceElementRef.nativeElement.type || "text";
  }


  /**
   * Keep the value of input element in a cache.
   *
   * @type {string}
   * @private
   */
  private _value: string;

  // Source services to modify elements.
  private _sourceRenderer: Renderer2;
  private _sourceElementRef: ElementRef;


  /**
   * Updates the value on the blur event.
   */
  @HostListener("blur", ["$event.type", "$event.target.value"])
  onBlur(event: string, value: string): void {
    this.updateValue(event, this.processValue(value));
    this.onTouched();
  }

  /**
   * Updates the value on the input event.
   */
  @HostListener("input", ["$event.type", "$event.target.value"])
  onInput(event: string, value: string): void {
    this.updateValue(event, value);
  }

  onChange = (_: any) => {
    //This is intentionally empty
  };

  onTouched = () => {
    //This is intentionally empty
  };

  constructor(
    @Inject(Renderer2) renderer: Renderer2,
    @Inject(ElementRef) elementRef: ElementRef,
    @Optional() @Inject(COMPOSITION_BUFFER_MODE) _compositionMode: boolean
  ) {
    this._sourceRenderer = renderer;
    this._sourceElementRef = elementRef;
  }

  registerOnChange(fn: (_: any) => void): void { this.onChange = fn; }

  registerOnTouched(fn: () => void): void { this.onTouched = fn; }

  /**
   * Writes a new value to the element based on the type of input element.
   *
   * @param {any} value - new value
   */
  public writeValue(value: any): void {
    //
    // The Template Driven Form doesn't automatically convert undefined values to null. We will do,
    // keeping an empty string as string because the condition `'' || null` returns null what
    // could change the initial state of a model.
    // The Reactive Form does it automatically during initialization.
    //
    // SEE: https://github.com/anein/angular2-trim-directive/issues/18
    //
    this._value = value === "" ? "" : value || null;

    this._sourceRenderer.setProperty(this._sourceElementRef.nativeElement, "value", this._value);

    // a dirty trick (or magic) goes here:
    // it updates the element value if `setProperty` doesn't set a new value for some reason.
    //
    // SEE: https://github.com/anein/angular2-trim-directive/issues/9
    //
    if (this._type !== "text") {
      this._sourceRenderer.setAttribute(this._sourceElementRef.nativeElement, "value", this._value);
    }
  }

  setDisabledState(isDisabled: boolean): void {
    this._sourceRenderer.setProperty(this._sourceElementRef.nativeElement, 'disabled', isDisabled);
  }

  /**
   * Writes the cursor position in safari
   *
   * @param cursorPosition - the cursor current position
   * @param hasTypedSymbol
   */
  private setCursorPointer(cursorPosition: any, hasTypedSymbol: boolean): void {
    // move the cursor to the stored position (Safari usually moves the cursor to the end)
    // setSelectionRange method apply only to inputs of types text, search, URL, tel and password
    if (hasTypedSymbol && ["text", "search", "url", "tel", "password"].indexOf(this._type) >= 0) {
      // Ok, for some reason in the tests the type changed is not being catch and because of that
      // this line is executed and causes an error of DOMException, it pass the text without problem
      // But it should be a better way to validate that type change
      this._sourceElementRef.nativeElement.setSelectionRange(cursorPosition, cursorPosition);
    }
  }

  /**
   * Trims an input value, and sets it to the model and element.
   *
   * @param {string} value - input value
   * @param {string} event - input event
   */
  private updateValue(event: string, value: string): void {
    // check if the user has set an optional attribute, and Trimmmm!!! Uhahahaha!
    value = this.eventValue() !== "" && event !== this.eventValue() ? value : this.processValue(value);

    const previous = this._value;

    // store the cursor position
    const cursorPosition = this._sourceElementRef.nativeElement.selectionStart;

    // write value to the element.
    this.writeValue(value);

    // Update the model only on getting new value, and prevent firing
    // the `dirty` state when click on empty fields.
    //
    // SEE:
    //    https://github.com/anein/angular2-trim-directive/issues/17
    //    https://github.com/anein/angular2-trim-directive/issues/35
    //    https://github.com/anein/angular2-trim-directive/issues/39
    //
    if ((this._value || previous) && this.processValue(this._value) !== previous) {
      this.onChange(this._value);
    }

    // check that non-null value is being changed
    const hasTypedSymbol = value && previous && value !== previous;

    // write the cursor position
    this.setCursorPointer(cursorPosition, hasTypedSymbol);
  }

}
