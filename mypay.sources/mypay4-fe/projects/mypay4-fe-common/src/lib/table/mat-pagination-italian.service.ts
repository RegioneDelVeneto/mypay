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
import { MatPaginatorIntl } from '@angular/material/paginator';

const italianRangeLabel = (page: number, pageSize: number, length: number) => {
  if (length == 0 || pageSize == 0) { return `0 di ${length}`; }

  length = Math.max(length, 0);

  const startIndex = page * pageSize;

  // If the start index exceeds the list length, do not try and fix the end index to the end.
  const endIndex = startIndex < length ?
      Math.min(startIndex + pageSize, length) :
      startIndex + pageSize;

  return `${startIndex + 1} - ${endIndex} di ${length}`;
}


export function getItalianPaginatorIntl() {
  const paginatorIntl = new MatPaginatorIntl();

  paginatorIntl.firstPageLabel='Prima pagina';
  paginatorIntl.itemsPerPageLabel = 'Elementi per pagina:';
  paginatorIntl.lastPageLabel='Ultima pagina';
  paginatorIntl.nextPageLabel = 'Pagina successiva';
  paginatorIntl.previousPageLabel = 'Pagina precedente';
  paginatorIntl.getRangeLabel = italianRangeLabel;

  return paginatorIntl;
}
