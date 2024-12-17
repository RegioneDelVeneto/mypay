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

import it.regioneveneto.mygov.payment.mypay4.dao.GpdErrDovutoDao;
import it.regioneveneto.mygov.payment.mypay4.model.GpdErrDovuto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(propagation = Propagation.REQUIRED)
public class GpdErrDovutoService {

    @Autowired
    private GpdErrDovutoDao gpdErrDovutoDao;

    public long insert(GpdErrDovuto d) {
        return gpdErrDovutoDao.insert(d);
    }

    public GpdErrDovuto getByIupd(String gpdIupd) {
        return gpdErrDovutoDao.getByIupd(gpdIupd);
    }

    public int delete(Long enteID, List<String> iupdList ) {
        return gpdErrDovutoDao.delete(enteID, iupdList);
    }
}
