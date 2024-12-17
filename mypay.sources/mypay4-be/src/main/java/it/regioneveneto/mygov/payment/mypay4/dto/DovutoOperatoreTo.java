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
package it.regioneveneto.mygov.payment.mypay4.dto;

import it.regioneveneto.mygov.payment.mypay4.dto.fesp.DovutoEntePrimarioTo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DovutoOperatoreTo extends BaseTo implements Serializable {
  private Long id;
  private String codFiscale;
  private String iud;
  private String iuv;
  private String causale;
  private String causaleVisualizzata;
  private String importo;
  private LocalDate dataScadenza;
  private String stato;
  private String codStato;
  private LocalDateTime dataStato;
  private boolean hasAvviso;
  private boolean hasRicevuta;


  //details
  private EnteTipoDovutoTo tipoDovuto;
  private String anagrafica;
  private String tipoSoggetto;
  private boolean flgAnagraficaAnonima;
  private boolean hasCodFiscale;
  private String email;
  private String indirizzo;
  private String numCiv;
  private String cap;
  private NazioneTo nazione;
  private ProvinciaTo prov;
  private ComuneTo comune;
  private boolean flgGenerateIuv;
  private String iuf;

  //datails dovutoElaborato
  private LocalDateTime dataInizioTransazione;
  private String identificativoTransazione;
  private String intestatario;
  private String pspScelto;

  private String dovutoType; // "debito" or "pagato"

  private String invalidDesc; // Message thrown by ValidatorException when insertion, update.
  
  private boolean flgMultibeneficiario;
  private boolean flgIuvVolatile;

  //Ente primario detail
  private DovutoEntePrimarioTo entePrimarioDetail;
  private DovutoElaboratoEntePrimarioTo entePrimarioElaboratoDetail;
  
  //dovuto multibeneficiario
  private DovutoMultibeneficiarioTo dovutoMultibeneficiario;
  private DovutoMultibeneficiarioElaboratoTo dovutoMultibeneficiarioElaborato;
  
}
