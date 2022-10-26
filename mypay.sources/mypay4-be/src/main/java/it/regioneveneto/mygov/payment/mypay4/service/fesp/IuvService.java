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
package it.regioneveneto.mygov.payment.mypay4.service.fesp;

import it.regioneveneto.mygov.payment.mypay4.exception.IuvGenerationException;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.Ente;
import it.regioneveneto.mygov.payment.mypay4.model.fesp.TipiVersamento;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
@Slf4j
public class IuvService {

  public static final String IUV_SISTEMA_INFORMATIVO_ID = "00";
  public static final String PAY_PRESSO_PSP = "PO";
  public static final String CREDITOR_REFERENCE_PREFIX = "RF";

  public static final String GENERATOR_FULL = "IUV25";
  public static final String GENERATOR_SMALL = "IUV17";

  private static final String PROGRESSIVO_PADDING_ZEROES_SMALL = "%011d";
  private static final String PROGRESSIVO_PADDING_ZEROES_FULL = "%013d";


  @Autowired
  private ProgressiviVersamentoService progressiviVersamentoService;

  @Autowired
  private TipiVersamentoService tipiVersamentoService;

  @Autowired
  private EnteService enteService;

  @Transactional(transactionManager = "tmFesp", propagation = Propagation.REQUIRED)
  public String generateIuv(final String identificativoDominio, final String tipoVersamento,
                            final String tipoGeneratore, final String importo, final String auxDigit) {

    if (tipoGeneratore.equals(GENERATOR_SMALL) && StringUtils.isBlank(auxDigit)) {
      log.error("Errore, il campo auxDigit è nullo per il generatore di IUV a 17");
      throw new IuvGenerationException("Errore, il campo auxDigit è nullo per il generatore di IUV a 17");
    }

    Ente ente = enteService.getEnteByCodFiscale(identificativoDominio);

    if(ente==null){
      log.error("Nessun ente trovato con identificativoDominio = [" + identificativoDominio + "]");
      throw new IuvGenerationException("Nessun ente trovato con identificativoDominio = ["
          + identificativoDominio + "]");
    }

    String iuv;
    if(tipoGeneratore.equals(GENERATOR_SMALL)){
      iuv = generateIUVSmall(ente.getCodIpaEnte(), tipoVersamento, importo, ente.getCodCodiceSegregazione(), auxDigit);
    } else if(tipoGeneratore.equals(GENERATOR_FULL)) {
      iuv = generateIUVFull(ente.getCodIpaEnte(), tipoVersamento, importo, ente.getCodCodiceSegregazione(), auxDigit);
    } else {
      log.error("Nessuna istanza trovata per il generatore di iuv per il tipo Generatore [" + tipoGeneratore + "]");
      throw new IuvGenerationException("Nessuna istanza trovata per il generatore di iuv per il tipo Generatore [" + tipoGeneratore + "]");
    }

    //just to check if transaction handling works!
//    if(importo.startsWith("10"))
//      throw new MyPayException("importo errato");

    return iuv;
  }




  private String generateIUVSmall(final String codiceIpaEnte, final String tipoVersamento,
                                  final String importo, final String codiceSegregazione, final String auxDigit) {
    //Composizione Creditor Reference
    StringBuilder creditorReference = new StringBuilder();

    /*
     * #######################
     * SPAZIO AD USO FUTURO
     * #######################
     */
    //		creditorReference.append("000");

    /*
     * ##########################
     * CODICE SEGREGAZIONE
     * ##########################
     */
    creditorReference.append(codiceSegregazione);

    /*
     * ##########################
     * SISTEMA INFORMATIVO LOCALE
     * ##########################
     */
    //Il sistema informativo id viene preso dal file di properties, ovvero dipende dall'installazione
    creditorReference.append(IUV_SISTEMA_INFORMATIVO_ID);

    /*
     * ##########################
     * PROGRESSIVO VERSAMENTO METTO SEMPRE FISSO TIPO A 'PO' PER EVITARE DUPLICATI VISTO CHE NON HO iuv_codice_tipo_versamento_id SU GEN_IUV_BREVE
     * ##########################
     */
    //Il progressivo viene generato appoggiandosi ad una tabella dei versamenti in base a Ente e TipoVersamento e sistema informativo
    long iuvCodiceProgressivoVersamento = progressiviVersamentoService.getNextProgressivoVersamento(codiceIpaEnte, GENERATOR_SMALL, PAY_PRESSO_PSP);
    if (iuvCodiceProgressivoVersamento == 0) {
      log.error("Errore nella generazione del nuovo progressivo");
      throw new IuvGenerationException("Errore nella generazione del nuovo progressivo per codiceIpaEnte [" + codiceIpaEnte + "], tipo versamento ["
          + tipoVersamento + "]");
    }

    creditorReference.append(String.format(PROGRESSIVO_PADDING_ZEROES_SMALL, iuvCodiceProgressivoVersamento));

 		/*
		 * Summary	0001380: Aggiornamento generatore IUV secondo specifiche Poste Italiane
		   Description	Aggiornare il generatore degli IUV in formato 15 cifre
		   ("http://casanova.ve.eng.it/dokuwiki/doku.php?id=mygov:documentazione_tecnica:formati_iuv") [^]
		   da

		   "<00><Progressivo Versamento><00>"

		   a

		   "<00><Progressivo Versamento><CC>"

		   sostituendo le ultime due cifre fisse "00" con il resto "CC" della divisione intera per "93" del numero di 16 cifre
		   composto dalla concatenazione di "001" con "<00><Progressivo Versamento>".
		 *
		 */

    String digitString = auxDigit + creditorReference.toString();
    long digit = Long.parseLong(digitString);
    long resto = digit % 93;
    String CC = StringUtils.leftPad(String.valueOf(resto), 2, '0');
    creditorReference.append(CC);

    /*
     * ##########################
     * SPAZIO AD USO FUTURO
     * ##########################
     */
    //creditorReference.append("00");

    return creditorReference.toString();
  }


  public String generateIUVFull(final String codiceIpaEnte, final String tipoVersamento, final String importo, final String codiceSegregazione, final String auxDigit)
      throws IuvGenerationException {

    //Composizione Creditor Reference
    StringBuffer creditorReference = new StringBuffer();

    /*
     * ##########################
     * CODICE SEGREGAZIONE
     * ##########################
     */
    creditorReference.append(codiceSegregazione);

    /*
     * ##########################
     * TIPO VERSAMENTO
     * ##########################
     */
    //A partire dal tipo di versamento, viene recuperato il codice del tipo versamento censito per lo iuv

    TipiVersamento tipoVersamentoObj = this.tipiVersamentoService.getByTipoVersamento(tipoVersamento);
    if (tipoVersamentoObj==null || StringUtils.isBlank(tipoVersamentoObj.getIuvCodiceTipoVersamentoId())) {
      log.error("Nessun tipo versamento trovato per il tipo versamento [" + tipoVersamento + "]");
      throw new IuvGenerationException("Nessun tipo versamento trovato per il tipo versamento [" + tipoVersamento
          + "]");
    }

    creditorReference.append(tipoVersamentoObj.getIuvCodiceTipoVersamentoId());

    /*
     * ##########################
     * PROGRESSIVO VERSAMENTO
     * ##########################
     */
    //Il progressivo viene generato appoggiandosi ad una tabella dei versamenti in base a Ente e TipoVersamento e sistema informativo
    long iuvCodiceProgressivoVersamento = this.progressiviVersamentoService.getNextProgressivoVersamento(
        codiceIpaEnte, GENERATOR_FULL, tipoVersamento);
    if (iuvCodiceProgressivoVersamento == 0) {
      log.error("Errore nella generazione del nuovo progressivo");
      throw new IuvGenerationException("Errore nella generazione del nuovo progressivo per codiceIpaEnte ["
          + codiceIpaEnte + "], tipo versamento [" + tipoVersamento + "]");
    }

    creditorReference.append(String.format(PROGRESSIVO_PADDING_ZEROES_FULL, iuvCodiceProgressivoVersamento));

    /*
     * ##########################
     * SISTEMA INFORMATIVO LOCALE
     * ##########################
     */
    //Il sistema informativo id viene preso dal file di properties, ovvero dipende dall'installazione
    creditorReference.append(IUV_SISTEMA_INFORMATIVO_ID);

    /*
     * ##########################
     * CIFRE FISSE
     * ##########################
     */
    creditorReference.append("00");

    /*
     * ##########################
     * Calcolo CheckDigits
     * ##########################
     */
    StringBuffer checkDigits = calculateCheckDigits(creditorReference);
    creditorReference.insert(0, checkDigits);
    creditorReference.insert(0, CREDITOR_REFERENCE_PREFIX);

    return creditorReference.toString();
  }

  private static StringBuffer calculateCheckDigits(StringBuffer creditorReference) {

    StringBuffer checkDigits;
    BigInteger creditorReferenceNum = new BigInteger(creditorReference + "271500");

    BigInteger modulo = creditorReferenceNum.mod(new BigInteger("97"));
    BigInteger checkDigitsNum = new BigInteger("98").subtract(modulo);

    if (checkDigitsNum.intValue() < 10) {
      checkDigits = new StringBuffer("0");
      checkDigits.append(checkDigitsNum.toString());
    } else {
      checkDigits = new StringBuffer(checkDigitsNum.toString());
    }

    return checkDigits;
  }
}