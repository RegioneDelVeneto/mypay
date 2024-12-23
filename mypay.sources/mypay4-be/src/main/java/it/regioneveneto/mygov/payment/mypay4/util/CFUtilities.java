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
package it.regioneveneto.mygov.payment.mypay4.util;

import java.util.Hashtable;
import java.util.Vector;

public class CFUtilities {
  /**
   * Classe che implementa e svolge l'algoritmo di controllo di validità della
   * partita iva e del codice fiscale delle persone non fisiche confrontando
   * anche il valore del check-digit la classe restituisce un codice di
   * ritorno che può assumere dei valori predeterminati.
   * Questo codice è preso da:
   * http://www.agenziaentrate.gov.it/wps/content/Nsilib/Nsi/Home/CosaDeviFare/Richiedere/Codice+fiscale+e+tessera+sanitaria/Richiesta+TS_CF/SchedaI/Programma+correttezza+formale+CF/clone+Caratteristiche+del+pacchetto+per+il+controllo+formale+del+codice+fiscale
   * Versione 1.2.2
   */
  public static class UCheckNum {
    private String strCfNum;
    private int pintCodUff;

    /**
     * Costruttore parametrico che inizializza l'oggetto con la stringa che
     * gli viene passata come parametro
     */
    public UCheckNum(String s) {
      strCfNum = s;
    }

    /**
     * Metodo che gestisce il caso di una partita iva o di un codice fiscale
     * di una persona non fisica
     *
     * @return char
     * @param p
     *            java.lang.String
     */
    public String cfNum(String p) {
      String strRetcode = null;
      if (Integer.parseInt(p.substring(0, 7)) < 8000000) {
        strRetcode = "2";
      } // partita iva o codice fiscale delle p.n.f.
      else if (pintCodUff > 95) {
        strRetcode = "N";
      } // codice ufficio errato per c.f. delle p.n.f.
      else {
        strRetcode = "5";
      } // c.f. p.n.f. non contribuenti iva

      if ((controlCfNum(p.substring(0, 11)) == false) && (strRetcode.equals("2"))) {
        strRetcode = "Q";
      } // check-digit errato
      else if (controlCfNum(p.substring(0, 11)) == false) {
        strRetcode = "T";
      } // check-digit errato

      return strRetcode;
    }

    /**
     * Metodo pubblico di codifica del return code che acquisisce una
     * stringa contenente un solo carattere al quale corrisponde una stringa
     * di dettaglio sul codice di ritorno
     *
     */
    public String codificaRetCodeCfNum(char chrValore) {
      String strRetcode = null; // codice di ritorno

      switch (chrValore) // viene attribuito ad ogni valore ricevuto un
      // significato
      {
        case '0':
          strRetcode = "NUMERO DI CODICE FISCALE DEFINITIVO";
          break;

        case '1':
          strRetcode = "NUMERO DI CODICE FISCALE PROVVISORIO ASSEGNATO D'UFFICIO (DU.74 DU.75 101.75)";
          break;

        case '2':
          strRetcode = "NUMERO DI CODICE FISCALE DELLE P.N.F. OPPURE NUMERO DI PARTITA I.V.A.";
          break;

        case '3':
          strRetcode = "NUMERO DI CODICE FISCALE PROVVISORIO ASSEGNATO DALLE INTENDENZE DI FINANZE";
          break;

        case '4':
          strRetcode = "NUMERO DI CODICE FISCALE PROVVISORIO ASSEGNATO DALLE IMPOSTE DIRETTE";
          break;

        case '5':
          strRetcode = "NUMERO DI CODICE FISCALE DELLE P.N.F. NON CONTRIBUENTI I.V.A.";
          break;

        case '6':
          strRetcode = "NUMERO DI CODICE FISCALE DEFINITIVO P.F. SOPRAELEVATA DI OMOCODICE";
          break;

        case 'A':
          strRetcode = "NUMERO DI CODICE FISCALE DEFINITIVO CON CHECK DIGIT ERRATO";
          break;

        case 'B':
          strRetcode = "NUMERO DI CODICE FISCALE DEFINITIVO FORMALMENTE ERRATO";
          break;

        case 'I':
          strRetcode = "NUMERO DI CODICE FISCALE A 11 CIFRE NON ALLINEATO";
          break;

        case 'L':
          strRetcode = "NUMERO DI CODICE FISCALE A 11 CIFRE NON NUMERICO";
          break;

        case 'M':
          strRetcode = "NUMERO DI CODICE FISCALE A 11 CIFRE AVENTE LE PRIME 7 CIFRE A ZERO";
          break;

        case 'N':
          strRetcode = "NUMERO DI CODICE FISCALE A 11 CIFRE CON NUMERO DI CODICE UFFICIO ERRATO";
          break;

        case 'O':

          strRetcode = "NUMERO DI CODICE FISCALE PROVVISORIO ASSEGNATO D'UFFICIO AVENTE PROGRESSIVO NUMERICO NON AMMISSIBILE (FUORI RANGE)";
          break;

        case 'P':
          strRetcode = "NUMERO DI CODICE FISCALE PROVVISORIO ASSEGNATO D'UFFICIO (DU.75 101.75) CON CHECK DIGIT ERRATO";
          break;

        case 'Q':
          strRetcode = "NUMERO DI CODICE FISCALE DELLE P.N.F. OPPURE NUMERO DI PARTITA I.V.A. CON CHECK DIGIT ERRATO";
          break;

        case 'R':
          strRetcode = "NUMERO DI CODICE FISCALE PROVVISORIO ASSEGNATO DALLE II.FF. CON CHECK DIGIT ERRATO";
          break;

        case 'S':
          strRetcode = "NUMERO DI CODICE FISCALE PROVVISORIO ASSEGNATO DALLE II.DD. CON CHECK DIGIT ERRATO";
          break;

        case 'T':
          strRetcode = "NUMERO DI CODICE FISCALE DELLE P.N.F. NON CONTRIBUENTI I.V.A. CON CHECK DIGIT ERRATO";
          break;

        default:
          break;
      }
      return strRetcode;
    }

    /**
     * Metodo che esegue sulla stringa partita iva il controllo sul
     * check-digit calcolando il complemento a dieci dell'ultimo carattere a
     * destra
     */
    public boolean controlCfNum(String s) {

      Integer intAppoggio;
      int pintAppo;
      int pintUltimoCarattere;
      int pintTotale = 0;
      // somma dei valori attribuiti alle cifre pari
      for (int i = 1; i < 11; i += 2) {
        String strElem = getCfNum().substring(i, i + 1);
        intAppoggio = Integer.valueOf(strElem); // da una singola cifra
        // della stringa passo
        // ad un integer
        pintAppo = (intAppoggio.intValue()) * 2; // passaggio ad un
        // intero per
        // moltiplicarlo
        String strS2 = String.valueOf(pintAppo); // passo di nuovo a
        // stringa per
        // dividerla in due
        // parti senza
        // utilizzare un
        // array
        for (int j = 0; j < strS2.length(); j++) {
          String strElem1 = strS2.substring(j, j + 1);
          pintTotale += Integer.parseInt(strElem1); // sommo il valore
          // delle due
          // cifre della
          // stringa della
          // somma
        }
      }
      // somma dei valori attribuiti alle cifre dispari
      for (int k = 0; k < 9; k += 2) {
        String strElem2 = getCfNum().substring(k, k + 1);
        pintTotale += Integer.parseInt(strElem2);
      }
      // estraggo l'ultima cifra della partita iva
      String strElem = getCfNum().substring(10, 11);
      intAppoggio = Integer.valueOf(strElem);
      pintUltimoCarattere = (intAppoggio.intValue());
      // controllo del complemento a dieci della cifra a destra del totale
      if (pintTotale % 10 == 0) {
        return (pintTotale % 10) == pintUltimoCarattere;
      } else {
        return (10 - (pintTotale % 10)) == pintUltimoCarattere;
      }
    }

    /**
     * @return boolean
     */
    public boolean controllaCfNum() {

      char c = trattCfNum().charAt(0);

      switch (c) {
        case '0':
          return true;
        case '1':
          return true;
        case '2':
          return true;
        case '3':
          return true;
        case '4':
          return true;
        case '5':
          return true;
        case 'A':
          return false;
        case 'B':
          return false;
        case 'I':
          return false;
        case 'L':
          return false;
        case 'M':
          return false;
        case 'N':
          return false;
        case 'O':
          return false;
        case 'P':
          return false;
        case 'Q':
          return false;
        case 'R':
          return false;
        case 'S':
          return false;
        case 'T':
          return false;

      }
      return false;
    }

    /**
     * Metodo che ritorna una stringa come partita iva.
     *
     * @return java.lang.String
     */
    public String getCfNum() {
      String s = this.strCfNum;
      return s;
    }

    /**
     * Metodo che inizializza una partita iva prendendo come parametro di
     * passaggio una stringa.
     */
    public void setCfNum(String s1) {
      strCfNum = s1;
      return;
    }

    /**
     * Metodo pubblico di controlli formali sulle parti che compongono la
     * partita iva in maniera specifica sul codice ufficio e sul carattere
     * di controllo;per quanto riguarda i numeri di codice fiscale a 11
     * cifre oltre al check digit viene controllato anche il codice
     * dell'ufficio; se trattasi di numeri di codice fiscale provvisori
     * assegnati dalle fonti DU.74 DU.75 e 101.75, vengono controllati anche
     * i range dei progressivi assegnati; quindi accede alla routine di
     * attribuzione dei return code da restituire
     */
    public String trattCfNum() {

      for (int i = 0; i < 11; i++) // ciclo per vedere se è stata passata
      // una
      // partita iva con un carattere non numerico
      {
        if (!(Character.isDigit(strCfNum.charAt(i)))) {
          return "L";
        }
      }
      try // controllo ulteriore sulla numericità del codice ufficio
      {
        pintCodUff = Integer.parseInt(strCfNum.substring(7, 10));
      } catch (java.lang.RuntimeException exc) {
        return "L";
      }
      if (strCfNum.substring(0, 7).equals("0000000")) // errore
      {
        return "M";
      }

      if (pintCodUff == 0) // controlli sul codice ufficio
      {
        if ((Integer.parseInt(strCfNum.substring(0, 7)) > 0)
            && (Integer.parseInt(strCfNum.substring(0, 7)) < 273961)) {
          return "1";
        }
        if ((Integer.parseInt(strCfNum.substring(0, 7)) > 0400000)
            && (Integer.parseInt(strCfNum.substring(0, 7)) < 1072480)
            || (Integer.parseInt(strCfNum.substring(0, 7)) > 1500000)
            && (Integer.parseInt(strCfNum.substring(0, 7)) < 1828637)
            || (Integer.parseInt(strCfNum.substring(0, 7)) > 2000000)
            && (Integer.parseInt(strCfNum.substring(0, 7)) < 2054096)) {
          if (controlCfNum(strCfNum)) {
            return "1";
          } else {
            return "P";
          }
        } else {
          return "O";
        }

      }

      else if (pintCodUff == 999) {
        return cfNum(strCfNum);
      }

      else if (((pintCodUff > 0) && (pintCodUff < 101)) || ((pintCodUff > 119) && (pintCodUff < 122))
          || (pintCodUff == 888)) {
        return cfNum(strCfNum);
      } else if ((pintCodUff > 150) && (pintCodUff < 246)) {
        if (controlCfNum(strCfNum)) // controllo sul check digit
        {
          return "3";
        } else {
          return "R";
        }
      }

      else if ((pintCodUff > 300) && (pintCodUff < 767))

      {
        if (controlCfNum(strCfNum.substring(0, 11))) {
          return "4";
        } else {
          return "S";
        }
      } else if ((pintCodUff > 899) && (pintCodUff < 951))

      {
        if (controlCfNum(strCfNum.substring(0, 11))) {
          return "4";
        } else {
          return "S";
        }
      } else {
        return "N";
      }

    }
  }

  public static class UCheckDigit {
    private String strCodFisc;

    private static Vector<String> vctCd;
    private static Vector<String> vctCp;
    private static Vector<String> vctNd;
    private static Vector<String> vctNp;
    private static Vector<String> vctVm; // vector dei mesi
    private static Hashtable<String, Integer> ctr_giorno;

    static {
      creaCarattereDispari();
      creaCaratterePari();
      creaNumeroDispari();
      creaNumeroPari();
      creaMese();
      crea_ctr_giorno();
    }

    /**
     * Costruttore della classe.
     */
    public UCheckDigit(String g) {

      strCodFisc = g;
    }

    /**
     * @return boolean
     */
    public boolean controllaCheckDigit() {
      int intAppoggio = 0;
      char chrCarattereEsaminato;

      // Ciclo di conteggio dei valori sui primi 15 caratteri del codice
      // fiscale
      for (int i = 0; i < 15; i++) {
        chrCarattereEsaminato = getCodFisc().charAt(i);
        String strElem = getCodFisc().substring(i, i + 1);
        int intResto = (i % 2);
        switch (intResto) {
          case 0:
            if (Character.isDigit(chrCarattereEsaminato) == false) {
              intAppoggio += getVectCarDisp(strElem);
            } else {
              intAppoggio += getVectNumDisp(strElem);
            }
            break;
          case 1:
            if (Character.isDigit(chrCarattereEsaminato) == false) {
              intAppoggio += getVectCarPari(strElem);
            } else {
              intAppoggio += getVectNumPari(strElem);
            }
            break;
          default:
            break;

        }
      }

      // Estraggo il carattere di controllo
      String ceckdigit = getCodFisc().substring(15, 16);
      return (intAppoggio % 26) == getVectCarPari(ceckdigit);
    }

    /**
     * @return boolean
     */
    public boolean controllaCorrettezza() {
      return controllaCorrettezzaChar() == '0';
    }

    /**
     * @return char
     */
    public char controllaCorrettezzaChar() {
      boolean bolLettera = false;

      for (int i = 0; i < 6; i++) {
        if (!(strCodFisc.charAt(i) == 'A' || strCodFisc.charAt(i) == 'B' || strCodFisc.charAt(i) == 'C' // controllo
            // dei
            // primi
            // 6
            // caratteri
            // alfabetici
            || strCodFisc.charAt(i) == 'D' || strCodFisc.charAt(i) == 'E' || strCodFisc.charAt(i) == 'F'
            || strCodFisc.charAt(i) == 'G' || strCodFisc.charAt(i) == 'H' || strCodFisc.charAt(i) == 'I'
            || strCodFisc.charAt(i) == 'J' || strCodFisc.charAt(i) == 'K' || strCodFisc.charAt(i) == 'L'
            || strCodFisc.charAt(i) == 'M' || strCodFisc.charAt(i) == 'N' || strCodFisc.charAt(i) == 'O'
            || strCodFisc.charAt(i) == 'P' || strCodFisc.charAt(i) == 'Q' || strCodFisc.charAt(i) == 'R'
            || strCodFisc.charAt(i) == 'S' || strCodFisc.charAt(i) == 'T' || strCodFisc.charAt(i) == 'U'
            || strCodFisc.charAt(i) == 'V' || strCodFisc.charAt(i) == 'W' || strCodFisc.charAt(i) == 'X'
            || strCodFisc.charAt(i) == 'Y' || strCodFisc.charAt(i) == 'Z'))

          return '2';
      }

      for (int i = 6; i < 8; i++) {

        if (!(Character.isDigit(strCodFisc.charAt(i)))
            && ((strCodFisc.charAt(i) != 'L') && (strCodFisc.charAt(i) != 'M')
            && (strCodFisc.charAt(i) != 'N') && (strCodFisc.charAt(i) != 'P')
            && (strCodFisc.charAt(i) != 'Q') && (strCodFisc.charAt(i) != 'R')
            && (strCodFisc.charAt(i) != 'S') && (strCodFisc.charAt(i) != 'T')
            && (strCodFisc.charAt(i) != 'U') && (strCodFisc.charAt(i) != 'V')))
          return '2';
      }

      if (!((strCodFisc.charAt(8) == 'A') || (strCodFisc.charAt(8) == 'B') || // controllo
          // del
          // mese
          (strCodFisc.charAt(8) == 'C') || (strCodFisc.charAt(8) == 'D') || (strCodFisc.charAt(8) == 'E')
          || (strCodFisc.charAt(8) == 'H') || (strCodFisc.charAt(8) == 'L') || (strCodFisc.charAt(8) == 'M')
          || (strCodFisc.charAt(8) == 'P') || (strCodFisc.charAt(8) == 'R') || (strCodFisc.charAt(8) == 'S')
          || (strCodFisc.charAt(8) == 'T')))
        return '2';

      for (int i = 9; i < 11; i++) {

        if (!(Character.isDigit(strCodFisc.charAt(i)))
            && ((strCodFisc.charAt(i) != 'L') && (strCodFisc.charAt(i) != 'M')
            && (strCodFisc.charAt(i) != 'N') && (strCodFisc.charAt(i) != 'P')
            && (strCodFisc.charAt(i) != 'Q') && (strCodFisc.charAt(i) != 'R')
            && (strCodFisc.charAt(i) != 'S') && (strCodFisc.charAt(i) != 'T')
            && (strCodFisc.charAt(i) != 'U') && (strCodFisc.charAt(i) != 'V')))
          return '2';
      }

      int intGiorno = trasforma_giorno(9); // controllo formale del giorno

      if (intGiorno > 31)
        intGiorno -= 40;
      if (intGiorno < 1 || intGiorno > 31)
        return '2';

      String strElem = strCodFisc.substring(8, 9); // lettera del mese
      String strMese = String.valueOf(getVectMese(strElem)); // valore
      // della
      // lettera
      // del mese
      if (strMese.length() == 1) // se mese ha una sola cifra
        strMese = "0" + strMese; // viene aggiunto uno zero

      String strAnno = String.valueOf(trasforma_giorno(6));
      if (strAnno.length() == 1) // viene aggiunto uno zero
        strAnno = "0" + strAnno;
      String strGiorno = String.valueOf(intGiorno); // se giorno ha una
      // sola cifra
      if (strGiorno.length() == 1) // viene aggiunto uno zero
        strGiorno = "0" + strGiorno;

      String data = strGiorno + strMese + strAnno; // controllo
      // dell'intera data

      if (!(controllaData(data)))
        return '2';
      if (((strCodFisc.charAt(11) != 'A') && (strCodFisc.charAt(11) != 'B') &&
          // controllo del 1° carattere
          (strCodFisc.charAt(11) != 'C') && (strCodFisc.charAt(11) != 'D') &&
          // del codice catastale
          (strCodFisc.charAt(11) != 'E') && (strCodFisc.charAt(11) != 'F') &&

          (strCodFisc.charAt(11) != 'G') && (strCodFisc.charAt(11) != 'H') &&

          (strCodFisc.charAt(11) != 'I') && (strCodFisc.charAt(11) != 'L') &&

          (strCodFisc.charAt(11) != 'M') && (strCodFisc.charAt(11) != 'Z')))
        return '2';

      for (int i = 12; i < 15; i++) {
        if (!(Character.isDigit(strCodFisc.charAt(i)))) {
          bolLettera = true;
          if ((strCodFisc.charAt(i) != 'L') && (strCodFisc.charAt(i) != 'M') && // controllo
              (strCodFisc.charAt(i) != 'N') && (strCodFisc.charAt(i) != 'P') && // del
              // codice
              // catastale
              (strCodFisc.charAt(i) != 'Q') && (strCodFisc.charAt(i) != 'R')
              && (strCodFisc.charAt(i) != 'S') && (strCodFisc.charAt(i) != 'T')
              && (strCodFisc.charAt(i) != 'U') && (strCodFisc.charAt(i) != 'V'))
            return '3';
        }
      }

      if (bolLettera == false) {
        int intNumeroCodCat = Integer.parseInt(strCodFisc.substring(12, 15));

        if (intNumeroCodCat == 000)
          return '2';

      }

      if (controllaCheckDigit())
        return '0';
      return '1';

    }

    /**
     * @return int
     */
    public int controllaCorrettezzaInt() {
      return (controllaCorrettezzaChar() - 48);
    }

    /**
     * @return boolean
     */
    public boolean controllaData(String s) {

      // controllo l'anno dopo averlo estrapolato dalla stringa
      try {
        String strAnno = s.substring(4);
        if ((s.length() == 8) || (s.length() == 6)) {
          if (s.length() == 6) {
            strAnno = "19" + s;
          }
          if (Integer.parseInt(strAnno) < 1870) {
            return false;
          }
        } else {
          return false;

        }

        // Estrapolazione mese e giorno
        String strMese = s.substring(2, 4);
        String strGiorno = s.substring(0, 2);

        // Trasformazione delle stringhe in interi
        int intMese = Integer.parseInt(strMese);
        int intGiorno = Integer.parseInt(strGiorno);
        int intAnno = Integer.parseInt(strAnno);

        // controlli di ammissibilità sul giorno e sul mese
        if ((intMese > 12) || (intGiorno > 31) || (intMese < 1) || (intGiorno < 1)) {
          return false;
        }

        // controllo mese
        switch (intMese) {
          // febbraio
          case 2:
            boolean bisestile = false;
            if (intAnno % 4 == 0) {
              if (intAnno % 400 == 0) {
                if (intAnno % 1000 == 0) {
                  bisestile = true;
                }
              } else {
                bisestile = true;
              }
            }
            if ((bisestile && (intGiorno > 29)) || (!bisestile && (intGiorno > 28))) {
              return false;
            }
            break;
          // aprile
          case 4:
            if (intGiorno > 30) {
              return false;
            }
            break;
          // giugno
          case 6:
            if (intGiorno > 30) {
              return false;
            }
            break;
          // settembre
          case 9:
            if (intGiorno > 30) {
              return false;
            }
            break;
          // novembre
          case 11:
            if (intGiorno > 30) {
              return false;
            }
            break;

          default:
            break;
        }
        // se arrivo a questo punto vuol dire che la data è corretta
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    /**
     * Tabella (di tipo Vector) dei valori dei caratteri con posizione di
     * ordine dispari.
     */
    public static Vector creaCarattereDispari() {

      vctCd = new Vector<String>();

      vctCd.addElement("B"); // valore dei caratteri dispari
      vctCd.addElement("A");
      vctCd.addElement("K");
      vctCd.addElement("P");
      vctCd.addElement("L");
      vctCd.addElement("C");
      vctCd.addElement("Q");
      vctCd.addElement("D");
      vctCd.addElement("R");
      vctCd.addElement("E");
      vctCd.addElement("V");
      vctCd.addElement("O");
      vctCd.addElement("S");
      vctCd.addElement("F");
      vctCd.addElement("T");
      vctCd.addElement("G");
      vctCd.addElement("U");
      vctCd.addElement("H");
      vctCd.addElement("M");
      vctCd.addElement("I");
      vctCd.addElement("N");
      vctCd.addElement("J");
      vctCd.addElement("W");
      vctCd.addElement("Z");
      vctCd.addElement("Y");
      vctCd.addElement("X");

      return vctCd;
    }

    /**
     * Tabella (di tipo Vector) dei valori dei caratteri con posizione di
     * ordine pari.
     */
    public static Vector creaCaratterePari() {
      vctCp = new Vector<String>();

      vctCp.addElement("A"); // valore dei caratteri pari
      vctCp.addElement("B");
      vctCp.addElement("C");
      vctCp.addElement("D");
      vctCp.addElement("E");
      vctCp.addElement("F");
      vctCp.addElement("G");
      vctCp.addElement("H");
      vctCp.addElement("I");
      vctCp.addElement("J");
      vctCp.addElement("K");
      vctCp.addElement("L");
      vctCp.addElement("M");
      vctCp.addElement("N");
      vctCp.addElement("O");
      vctCp.addElement("P");
      vctCp.addElement("Q");
      vctCp.addElement("R");
      vctCp.addElement("S");
      vctCp.addElement("T");
      vctCp.addElement("U");
      vctCp.addElement("V");
      vctCp.addElement("W");
      vctCp.addElement("X");
      vctCp.addElement("Y");
      vctCp.addElement("Z");

      return vctCp;
    }

    /**
     * @return vector
     */
    public static Vector creaMese() {
      vctVm = new Vector<String>();

      vctVm.addElement(" ");
      vctVm.addElement("A"); // gennaio
      vctVm.addElement("B"); // febbraio
      vctVm.addElement("C"); // marzo
      vctVm.addElement("D"); // aprile
      vctVm.addElement("E"); // maggio
      vctVm.addElement("H"); // giugno
      vctVm.addElement("L"); // luglio
      vctVm.addElement("M"); // agosto
      vctVm.addElement("P"); // settembre
      vctVm.addElement("R"); // ottobre
      vctVm.addElement("S"); // novembre
      vctVm.addElement("T"); // dicembre

      return vctVm;
    }

    /**
     * Tabella (di tipo Vector) dei valori dei numeri con posizione di
     * ordine dispari.
     */
    public static Vector creaNumeroDispari() {
      vctNd = new Vector<String>();

      vctNd.addElement("1"); // valore dei numeri dispari
      vctNd.addElement("0");
      vctNd.addElement(" ");
      vctNd.addElement(" ");
      vctNd.addElement(" ");
      vctNd.addElement("2");
      vctNd.addElement(" ");
      vctNd.addElement("3");
      vctNd.addElement(" ");
      vctNd.addElement("4");
      vctNd.addElement(" ");
      vctNd.addElement(" ");
      vctNd.addElement(" ");
      vctNd.addElement("5");
      vctNd.addElement(" ");
      vctNd.addElement("6");
      vctNd.addElement(" ");
      vctNd.addElement("7");
      vctNd.addElement(" ");
      vctNd.addElement("8");
      vctNd.addElement(" ");
      vctNd.addElement("9");

      return vctNd;
    }

    public static Hashtable<String, Integer> crea_ctr_giorno() {
      ctr_giorno = new Hashtable<String, Integer>();
      ctr_giorno.put("L", 0);
      ctr_giorno.put("M", 1);
      ctr_giorno.put("N", 2);
      ctr_giorno.put("P", 3);
      ctr_giorno.put("Q", 4);
      ctr_giorno.put("R", 5);
      ctr_giorno.put("S", 6);
      ctr_giorno.put("T", 7);
      ctr_giorno.put("U", 8);
      ctr_giorno.put("V", 9);

      return ctr_giorno;
    }

    /**
     * Tabella (di tipo Vector) dei valori dei numeri con posizione di
     * ordine pari.
     */
    public static Vector creaNumeroPari() {
      vctNp = new Vector<String>();

      vctNp.addElement("0"); // valore dei numeri pari
      vctNp.addElement("1");
      vctNp.addElement("2");
      vctNp.addElement("3");
      vctNp.addElement("4");
      vctNp.addElement("5");
      vctNp.addElement("6");
      vctNp.addElement("7");
      vctNp.addElement("8");
      vctNp.addElement("9");

      return vctNp;
    }

    /**
     * Questo metodo prende il codice fiscale come parametro e torna una
     * stringa.
     *
     * @return java.lang.String
     */
    public String getCodFisc() {
      String s = this.strCodFisc;
      return s;
    }

    /**
     * Calcola valore del carattere dispari.
     */
    public int getVectCarDisp(String elem) {

      return vctCd.indexOf(elem);

    }

    /**
     * Calcola valore del carattere pari.
     *
     * @return int
     */
    public int getVectCarPari(String elem) {
      return vctCp.indexOf(elem);

    }

    /**
     * @return int
     * @param elem
     *            java.lang.String
     */
    public int getVectMese(String stringa) {

      return vctVm.indexOf(stringa);
    }

    /**
     * Calcola valore del numero dispari.
     *
     * @return int
     */
    public int getVectNumDisp(String elem) {
      return vctNd.indexOf(elem);
    }

    /**
     * Calcola valore del numero pari.
     *
     * @return int
     */
    public int getVectNumPari(String elem) {
      return vctNp.indexOf(elem);
    }

    /**
     * Inizializzazione di un codice fiscale come stringa.
     *
     * @param s
     *            java.lang.String
     */
    public void setCodFisc(String s) {
      this.strCodFisc = s;
      return;
    }

    public Hashtable<String, Integer> getCtr_giorno() {
      return ctr_giorno;
    }

    public void setCtr_giorno(Hashtable<String, Integer> ctr_giorno) {
      UCheckDigit.ctr_giorno = ctr_giorno;
    }

    /*
     * c=byte campo substring 9 giorno 6 anno
     */
    public int trasforma_giorno(int c) {

      String appo = "";

      for (int i = c; i < c + 2; i++) {
        if (Character.isDigit(strCodFisc.charAt(i)))
          appo += strCodFisc.charAt(i);
        else
          appo += getCtr_giorno().get(String.valueOf(strCodFisc.charAt(i)));

      }

      return Integer.parseInt(appo);
    }
  }

}
