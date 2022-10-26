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
package it.regioneveneto.mygov.payment.mypay4.mapper;

import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMappers;
import org.jdbi.v3.sqlobject.config.Configurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RegisterRowMapperWithPrefixImpl implements Configurer {
    @Override
    public void configureForMethod(ConfigRegistry registry,
                                   Annotation annotation,
                                   Class<?> sqlObjectType,
                                   Method method) {
        configure(registry, (RegisterRowMapperWithPrefix) annotation);
    }

    @Override
    public void configureForType(ConfigRegistry registry,
                                 Annotation annotation,
                                 Class<?> sqlObjectType) {
        configure(registry, (RegisterRowMapperWithPrefix) annotation);
    }

    private void configure(ConfigRegistry registry,
                           RegisterRowMapperWithPrefix registerRowMapper) {
        try {
            RowMapperWithPrefix mapper = registerRowMapper.value().getConstructor().newInstance();
            mapper.setPrefix(registerRowMapper.prefix());
            registry.get(RowMappers.class).register(mapper);
        } catch (NoSuchMethodException|IllegalAccessException|InstantiationException|InvocationTargetException e) {
            throw new RuntimeException("Cannot construct " + registerRowMapper.value() + " with prefix " + registerRowMapper.prefix(), e);
        }
    }
}
