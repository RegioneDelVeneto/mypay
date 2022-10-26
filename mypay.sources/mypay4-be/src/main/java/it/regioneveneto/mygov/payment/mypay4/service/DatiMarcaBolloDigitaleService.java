/**
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
package it.regioneveneto.mygov.payment.mypay4.service;

import it.regioneveneto.mygov.payment.mypay4.dao.DatiMarcaBolloDigitaleDao;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import it.regioneveneto.mygov.payment.mypay4.model.DatiMarcaBolloDigitale;
import it.veneto.regione.schemas._2012.pagamenti.ente.CtDatiMarcaBolloDigitale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@Slf4j
public class DatiMarcaBolloDigitaleService {

	@Autowired
	DatiMarcaBolloDigitaleDao datiMarcaBolloDigitaleDao;

	public Long insert(DatiMarcaBolloDigitale bolloDigitale) {
		return datiMarcaBolloDigitaleDao.insert(bolloDigitale);
	}

	public DatiMarcaBolloDigitale getById(Long id) {
		return datiMarcaBolloDigitaleDao.getById(id);
	}

	public void remove(Long idDatiMarcaBolloDigitale) {
		Assert.notNull(idDatiMarcaBolloDigitale, "Identificativo Marca Bollo Digitale null");
		DatiMarcaBolloDigitale marcaBolloDigitale = getById(idDatiMarcaBolloDigitale);
		Assert.notNull(marcaBolloDigitale, "Nessuna marca da bollo digitale per id [ " + idDatiMarcaBolloDigitale + " ]");

		int deletedRec = datiMarcaBolloDigitaleDao.remove(idDatiMarcaBolloDigitale);
		if (deletedRec != 1) {
			throw new MyPayException("DatiMarcaBolloDigitale delete internal error");
		}
		log.info("DatiMarcaBolloDigitale whit: {} is now deleted", idDatiMarcaBolloDigitale);
	}

	public DatiMarcaBolloDigitale mapCtDatiMarcaBolloDigitaleToModel(CtDatiMarcaBolloDigitale ctDatiMarcaBolloDigitale){
		return ctDatiMarcaBolloDigitale == null ? null : DatiMarcaBolloDigitale.builder()
				.hashDocumento(ctDatiMarcaBolloDigitale.getHashDocumento())
				.provinciaResidenza(ctDatiMarcaBolloDigitale.getProvinciaResidenza())
				.tipoBollo(ctDatiMarcaBolloDigitale.getTipoBollo())
				.build();
	}
}