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
import { BREAKPOINT } from '@angular/flex-layout';

export const BS4_BREAKPOINTS = [
    { alias: 'gt-bsmd', mediaQuery: 'screen and (min-width: 768px)', overlapping: false, priority: 1001 },
    { alias: 'lt-bsmd', mediaQuery: 'screen and (max-width: 767px)', overlapping: false, priority: 1001 },
    { alias: 'bsmd', mediaQuery: 'screen and (min-width: 768px) and (max-width: 959px)', overlapping: false, priority: 1001 },
];

export const CustomBreakPointsProvider = {
    provide: BREAKPOINT,
    useValue: [...BS4_BREAKPOINTS],
    multi: true,
};
