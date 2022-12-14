package it.regioneveneto.mygov.payment.nodoregionalefesp.domain;
// Generated Jan 22, 2019 5:27:50 PM by Hibernate Tools 3.2.4.GA


import java.math.BigDecimal;
import java.util.Date;

/**
 * MygovAttivaRptE generated by hbm2java
 */
public class MygovAttivaRptE  implements java.io.Serializable {


     private Long mygovAttivaRptEId;
     private int version;
     private String codAttivarptIdPsp;
     private String codAttivarptIdentificativoIntermediarioPa;
     private String codAttivarptIdentificativoStazioneIntermediarioPa;
     private String codAttivarptIdentificativoDominio;
     private String codAttivarptIdentificativoUnivocoVersamento;
     private String codAttivarptCodiceContestoPagamento;
     private Date dtAttivarpt;
     private BigDecimal numAttivarptImportoSingoloVersamento;
     private String deAttivarptIbanAppoggio;
     private String deAttivarptBicAppoggio;
     private String codAttivarptSoggVersIdUnivVersTipoIdUnivoco;
     private String codAttivarptSoggVersIdUnivVersCodiceIdUnivoco;
     private String deAttivarptSoggVersAnagraficaVersante;
     private String deAttivarptSoggVersIndirizzoVersante;
     private String deAttivarptSoggVersCivicoVersante;
     private String codAttivarptSoggVersCapVersante;
     private String deAttivarptSoggVersLocalitaVersante;
     private String deAttivarptSoggVersProvinciaVersante;
     private String codAttivarptSoggVersNazioneVersante;
     private String deAttivarptSoggVersEmailVersante;
     private String deAttivarptIbanAddebito;
     private String deAttivarptBicAddebito;
     private String codAttivarptSoggPagIdUnivPagTipoIdUnivoco;
     private String codAttivarptSoggPagIdUnivPagCodiceIdUnivoco;
     private String deAttivarptSoggPagAnagraficaPagatore;
     private String deAttivarptSoggPagIndirizzoPagatore;
     private String deAttivarptSoggPagCivicoPagatore;
     private String codAttivarptSoggPagCapPagatore;
     private String deAttivarptSoggPagLocalitaPagatore;
     private String deAttivarptSoggPagProvinciaPagatore;
     private String codAttivarptSoggPagNazionePagatore;
     private String deAttivarptSoggPagEmailPagatore;
     private Date dtEAttivarpt;
     private BigDecimal numEAttivarptImportoSingoloVersamento;
     private String deEAttivarptIbanAccredito;
     private String deEAttivarptBicAccredito;
     private String codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco;
     private String codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco;
     private String deEAttivarptEnteBenefDenominazioneBeneficiario;
     private String codEAttivarptEnteBenefCodiceUnitOperBeneficiario;
     private String deEAttivarptEnteBenefDenomUnitOperBeneficiario;
     private String deEAttivarptEnteBenefIndirizzoBeneficiario;
     private String deEAttivarptEnteBenefCivicoBeneficiario;
     private String codEAttivarptEnteBenefCapBeneficiario;
     private String deEAttivarptEnteBenefLocalitaBeneficiario;
     private String deEAttivarptEnteBenefProvinciaBeneficiario;
     private String codEAttivarptEnteBenefNazioneBeneficiario;
     private String deEAttivarptCredenzialiPagatore;
     private String deEAttivarptCausaleVersamento;
     private String deAttivarptEsito;
     private String codAttivarptFaultCode;
     private String deAttivarptFaultString;
     private String codAttivarptId;
     private String deAttivarptDescription;
     private Integer codAttivarptSerial;
     private String codAttivarptIdIntermediarioPsp;
     private String codAttivarptIdCanalePsp;
     private String codAttivarptOriginalFaultCode;
     private String deAttivarptOriginalFaultString;
     private String deAttivarptOriginalFaultDescription;

    public MygovAttivaRptE() {
    }

	
    public MygovAttivaRptE(String codAttivarptIdPsp, String codAttivarptIdentificativoIntermediarioPa, String codAttivarptIdentificativoStazioneIntermediarioPa, String codAttivarptIdentificativoDominio, String codAttivarptIdentificativoUnivocoVersamento, String codAttivarptCodiceContestoPagamento, Date dtAttivarpt, BigDecimal numAttivarptImportoSingoloVersamento) {
        this.codAttivarptIdPsp = codAttivarptIdPsp;
        this.codAttivarptIdentificativoIntermediarioPa = codAttivarptIdentificativoIntermediarioPa;
        this.codAttivarptIdentificativoStazioneIntermediarioPa = codAttivarptIdentificativoStazioneIntermediarioPa;
        this.codAttivarptIdentificativoDominio = codAttivarptIdentificativoDominio;
        this.codAttivarptIdentificativoUnivocoVersamento = codAttivarptIdentificativoUnivocoVersamento;
        this.codAttivarptCodiceContestoPagamento = codAttivarptCodiceContestoPagamento;
        this.dtAttivarpt = dtAttivarpt;
        this.numAttivarptImportoSingoloVersamento = numAttivarptImportoSingoloVersamento;
    }
    public MygovAttivaRptE(String codAttivarptIdPsp, String codAttivarptIdentificativoIntermediarioPa, String codAttivarptIdentificativoStazioneIntermediarioPa, String codAttivarptIdentificativoDominio, String codAttivarptIdentificativoUnivocoVersamento, String codAttivarptCodiceContestoPagamento, Date dtAttivarpt, BigDecimal numAttivarptImportoSingoloVersamento, String deAttivarptIbanAppoggio, String deAttivarptBicAppoggio, String codAttivarptSoggVersIdUnivVersTipoIdUnivoco, String codAttivarptSoggVersIdUnivVersCodiceIdUnivoco, String deAttivarptSoggVersAnagraficaVersante, String deAttivarptSoggVersIndirizzoVersante, String deAttivarptSoggVersCivicoVersante, String codAttivarptSoggVersCapVersante, String deAttivarptSoggVersLocalitaVersante, String deAttivarptSoggVersProvinciaVersante, String codAttivarptSoggVersNazioneVersante, String deAttivarptSoggVersEmailVersante, String deAttivarptIbanAddebito, String deAttivarptBicAddebito, String codAttivarptSoggPagIdUnivPagTipoIdUnivoco, String codAttivarptSoggPagIdUnivPagCodiceIdUnivoco, String deAttivarptSoggPagAnagraficaPagatore, String deAttivarptSoggPagIndirizzoPagatore, String deAttivarptSoggPagCivicoPagatore, String codAttivarptSoggPagCapPagatore, String deAttivarptSoggPagLocalitaPagatore, String deAttivarptSoggPagProvinciaPagatore, String codAttivarptSoggPagNazionePagatore, String deAttivarptSoggPagEmailPagatore, Date dtEAttivarpt, BigDecimal numEAttivarptImportoSingoloVersamento, String deEAttivarptIbanAccredito, String deEAttivarptBicAccredito, String codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco, String codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco, String deEAttivarptEnteBenefDenominazioneBeneficiario, String codEAttivarptEnteBenefCodiceUnitOperBeneficiario, String deEAttivarptEnteBenefDenomUnitOperBeneficiario, String deEAttivarptEnteBenefIndirizzoBeneficiario, String deEAttivarptEnteBenefCivicoBeneficiario, String codEAttivarptEnteBenefCapBeneficiario, String deEAttivarptEnteBenefLocalitaBeneficiario, String deEAttivarptEnteBenefProvinciaBeneficiario, String codEAttivarptEnteBenefNazioneBeneficiario, String deEAttivarptCredenzialiPagatore, String deEAttivarptCausaleVersamento, String deAttivarptEsito, String codAttivarptFaultCode, String deAttivarptFaultString, String codAttivarptId, String deAttivarptDescription, Integer codAttivarptSerial, String codAttivarptIdIntermediarioPsp, String codAttivarptIdCanalePsp, String codAttivarptOriginalFaultCode, String deAttivarptOriginalFaultString, String deAttivarptOriginalFaultDescription) {
       this.codAttivarptIdPsp = codAttivarptIdPsp;
       this.codAttivarptIdentificativoIntermediarioPa = codAttivarptIdentificativoIntermediarioPa;
       this.codAttivarptIdentificativoStazioneIntermediarioPa = codAttivarptIdentificativoStazioneIntermediarioPa;
       this.codAttivarptIdentificativoDominio = codAttivarptIdentificativoDominio;
       this.codAttivarptIdentificativoUnivocoVersamento = codAttivarptIdentificativoUnivocoVersamento;
       this.codAttivarptCodiceContestoPagamento = codAttivarptCodiceContestoPagamento;
       this.dtAttivarpt = dtAttivarpt;
       this.numAttivarptImportoSingoloVersamento = numAttivarptImportoSingoloVersamento;
       this.deAttivarptIbanAppoggio = deAttivarptIbanAppoggio;
       this.deAttivarptBicAppoggio = deAttivarptBicAppoggio;
       this.codAttivarptSoggVersIdUnivVersTipoIdUnivoco = codAttivarptSoggVersIdUnivVersTipoIdUnivoco;
       this.codAttivarptSoggVersIdUnivVersCodiceIdUnivoco = codAttivarptSoggVersIdUnivVersCodiceIdUnivoco;
       this.deAttivarptSoggVersAnagraficaVersante = deAttivarptSoggVersAnagraficaVersante;
       this.deAttivarptSoggVersIndirizzoVersante = deAttivarptSoggVersIndirizzoVersante;
       this.deAttivarptSoggVersCivicoVersante = deAttivarptSoggVersCivicoVersante;
       this.codAttivarptSoggVersCapVersante = codAttivarptSoggVersCapVersante;
       this.deAttivarptSoggVersLocalitaVersante = deAttivarptSoggVersLocalitaVersante;
       this.deAttivarptSoggVersProvinciaVersante = deAttivarptSoggVersProvinciaVersante;
       this.codAttivarptSoggVersNazioneVersante = codAttivarptSoggVersNazioneVersante;
       this.deAttivarptSoggVersEmailVersante = deAttivarptSoggVersEmailVersante;
       this.deAttivarptIbanAddebito = deAttivarptIbanAddebito;
       this.deAttivarptBicAddebito = deAttivarptBicAddebito;
       this.codAttivarptSoggPagIdUnivPagTipoIdUnivoco = codAttivarptSoggPagIdUnivPagTipoIdUnivoco;
       this.codAttivarptSoggPagIdUnivPagCodiceIdUnivoco = codAttivarptSoggPagIdUnivPagCodiceIdUnivoco;
       this.deAttivarptSoggPagAnagraficaPagatore = deAttivarptSoggPagAnagraficaPagatore;
       this.deAttivarptSoggPagIndirizzoPagatore = deAttivarptSoggPagIndirizzoPagatore;
       this.deAttivarptSoggPagCivicoPagatore = deAttivarptSoggPagCivicoPagatore;
       this.codAttivarptSoggPagCapPagatore = codAttivarptSoggPagCapPagatore;
       this.deAttivarptSoggPagLocalitaPagatore = deAttivarptSoggPagLocalitaPagatore;
       this.deAttivarptSoggPagProvinciaPagatore = deAttivarptSoggPagProvinciaPagatore;
       this.codAttivarptSoggPagNazionePagatore = codAttivarptSoggPagNazionePagatore;
       this.deAttivarptSoggPagEmailPagatore = deAttivarptSoggPagEmailPagatore;
       this.dtEAttivarpt = dtEAttivarpt;
       this.numEAttivarptImportoSingoloVersamento = numEAttivarptImportoSingoloVersamento;
       this.deEAttivarptIbanAccredito = deEAttivarptIbanAccredito;
       this.deEAttivarptBicAccredito = deEAttivarptBicAccredito;
       this.codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco = codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco;
       this.codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco = codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco;
       this.deEAttivarptEnteBenefDenominazioneBeneficiario = deEAttivarptEnteBenefDenominazioneBeneficiario;
       this.codEAttivarptEnteBenefCodiceUnitOperBeneficiario = codEAttivarptEnteBenefCodiceUnitOperBeneficiario;
       this.deEAttivarptEnteBenefDenomUnitOperBeneficiario = deEAttivarptEnteBenefDenomUnitOperBeneficiario;
       this.deEAttivarptEnteBenefIndirizzoBeneficiario = deEAttivarptEnteBenefIndirizzoBeneficiario;
       this.deEAttivarptEnteBenefCivicoBeneficiario = deEAttivarptEnteBenefCivicoBeneficiario;
       this.codEAttivarptEnteBenefCapBeneficiario = codEAttivarptEnteBenefCapBeneficiario;
       this.deEAttivarptEnteBenefLocalitaBeneficiario = deEAttivarptEnteBenefLocalitaBeneficiario;
       this.deEAttivarptEnteBenefProvinciaBeneficiario = deEAttivarptEnteBenefProvinciaBeneficiario;
       this.codEAttivarptEnteBenefNazioneBeneficiario = codEAttivarptEnteBenefNazioneBeneficiario;
       this.deEAttivarptCredenzialiPagatore = deEAttivarptCredenzialiPagatore;
       this.deEAttivarptCausaleVersamento = deEAttivarptCausaleVersamento;
       this.deAttivarptEsito = deAttivarptEsito;
       this.codAttivarptFaultCode = codAttivarptFaultCode;
       this.deAttivarptFaultString = deAttivarptFaultString;
       this.codAttivarptId = codAttivarptId;
       this.deAttivarptDescription = deAttivarptDescription;
       this.codAttivarptSerial = codAttivarptSerial;
       this.codAttivarptIdIntermediarioPsp = codAttivarptIdIntermediarioPsp;
       this.codAttivarptIdCanalePsp = codAttivarptIdCanalePsp;
       this.codAttivarptOriginalFaultCode = codAttivarptOriginalFaultCode;
       this.deAttivarptOriginalFaultString = deAttivarptOriginalFaultString; 
       this.deAttivarptOriginalFaultDescription = deAttivarptOriginalFaultDescription;
    }
   
    public Long getMygovAttivaRptEId() {
        return this.mygovAttivaRptEId;
    }
    
    public void setMygovAttivaRptEId(Long mygovAttivaRptEId) {
        this.mygovAttivaRptEId = mygovAttivaRptEId;
    }
    public int getVersion() {
        return this.version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    public String getCodAttivarptIdPsp() {
        return this.codAttivarptIdPsp;
    }
    
    public void setCodAttivarptIdPsp(String codAttivarptIdPsp) {
        this.codAttivarptIdPsp = codAttivarptIdPsp;
    }
    public String getCodAttivarptIdentificativoIntermediarioPa() {
        return this.codAttivarptIdentificativoIntermediarioPa;
    }
    
    public void setCodAttivarptIdentificativoIntermediarioPa(String codAttivarptIdentificativoIntermediarioPa) {
        this.codAttivarptIdentificativoIntermediarioPa = codAttivarptIdentificativoIntermediarioPa;
    }
    public String getCodAttivarptIdentificativoStazioneIntermediarioPa() {
        return this.codAttivarptIdentificativoStazioneIntermediarioPa;
    }
    
    public void setCodAttivarptIdentificativoStazioneIntermediarioPa(String codAttivarptIdentificativoStazioneIntermediarioPa) {
        this.codAttivarptIdentificativoStazioneIntermediarioPa = codAttivarptIdentificativoStazioneIntermediarioPa;
    }
    public String getCodAttivarptIdentificativoDominio() {
        return this.codAttivarptIdentificativoDominio;
    }
    
    public void setCodAttivarptIdentificativoDominio(String codAttivarptIdentificativoDominio) {
        this.codAttivarptIdentificativoDominio = codAttivarptIdentificativoDominio;
    }
    public String getCodAttivarptIdentificativoUnivocoVersamento() {
        return this.codAttivarptIdentificativoUnivocoVersamento;
    }
    
    public void setCodAttivarptIdentificativoUnivocoVersamento(String codAttivarptIdentificativoUnivocoVersamento) {
        this.codAttivarptIdentificativoUnivocoVersamento = codAttivarptIdentificativoUnivocoVersamento;
    }
    public String getCodAttivarptCodiceContestoPagamento() {
        return this.codAttivarptCodiceContestoPagamento;
    }
    
    public void setCodAttivarptCodiceContestoPagamento(String codAttivarptCodiceContestoPagamento) {
        this.codAttivarptCodiceContestoPagamento = codAttivarptCodiceContestoPagamento;
    }
    public Date getDtAttivarpt() {
        return this.dtAttivarpt;
    }
    
    public void setDtAttivarpt(Date dtAttivarpt) {
        this.dtAttivarpt = dtAttivarpt;
    }
    public BigDecimal getNumAttivarptImportoSingoloVersamento() {
        return this.numAttivarptImportoSingoloVersamento;
    }
    
    public void setNumAttivarptImportoSingoloVersamento(BigDecimal numAttivarptImportoSingoloVersamento) {
        this.numAttivarptImportoSingoloVersamento = numAttivarptImportoSingoloVersamento;
    }
    public String getDeAttivarptIbanAppoggio() {
        return this.deAttivarptIbanAppoggio;
    }
    
    public void setDeAttivarptIbanAppoggio(String deAttivarptIbanAppoggio) {
        this.deAttivarptIbanAppoggio = deAttivarptIbanAppoggio;
    }
    public String getDeAttivarptBicAppoggio() {
        return this.deAttivarptBicAppoggio;
    }
    
    public void setDeAttivarptBicAppoggio(String deAttivarptBicAppoggio) {
        this.deAttivarptBicAppoggio = deAttivarptBicAppoggio;
    }
    public String getCodAttivarptSoggVersIdUnivVersTipoIdUnivoco() {
        return this.codAttivarptSoggVersIdUnivVersTipoIdUnivoco;
    }
    
    public void setCodAttivarptSoggVersIdUnivVersTipoIdUnivoco(String codAttivarptSoggVersIdUnivVersTipoIdUnivoco) {
        this.codAttivarptSoggVersIdUnivVersTipoIdUnivoco = codAttivarptSoggVersIdUnivVersTipoIdUnivoco;
    }
    public String getCodAttivarptSoggVersIdUnivVersCodiceIdUnivoco() {
        return this.codAttivarptSoggVersIdUnivVersCodiceIdUnivoco;
    }
    
    public void setCodAttivarptSoggVersIdUnivVersCodiceIdUnivoco(String codAttivarptSoggVersIdUnivVersCodiceIdUnivoco) {
        this.codAttivarptSoggVersIdUnivVersCodiceIdUnivoco = codAttivarptSoggVersIdUnivVersCodiceIdUnivoco;
    }
    public String getDeAttivarptSoggVersAnagraficaVersante() {
        return this.deAttivarptSoggVersAnagraficaVersante;
    }
    
    public void setDeAttivarptSoggVersAnagraficaVersante(String deAttivarptSoggVersAnagraficaVersante) {
        this.deAttivarptSoggVersAnagraficaVersante = deAttivarptSoggVersAnagraficaVersante;
    }
    public String getDeAttivarptSoggVersIndirizzoVersante() {
        return this.deAttivarptSoggVersIndirizzoVersante;
    }
    
    public void setDeAttivarptSoggVersIndirizzoVersante(String deAttivarptSoggVersIndirizzoVersante) {
        this.deAttivarptSoggVersIndirizzoVersante = deAttivarptSoggVersIndirizzoVersante;
    }
    public String getDeAttivarptSoggVersCivicoVersante() {
        return this.deAttivarptSoggVersCivicoVersante;
    }
    
    public void setDeAttivarptSoggVersCivicoVersante(String deAttivarptSoggVersCivicoVersante) {
        this.deAttivarptSoggVersCivicoVersante = deAttivarptSoggVersCivicoVersante;
    }
    public String getCodAttivarptSoggVersCapVersante() {
        return this.codAttivarptSoggVersCapVersante;
    }
    
    public void setCodAttivarptSoggVersCapVersante(String codAttivarptSoggVersCapVersante) {
        this.codAttivarptSoggVersCapVersante = codAttivarptSoggVersCapVersante;
    }
    public String getDeAttivarptSoggVersLocalitaVersante() {
        return this.deAttivarptSoggVersLocalitaVersante;
    }
    
    public void setDeAttivarptSoggVersLocalitaVersante(String deAttivarptSoggVersLocalitaVersante) {
        this.deAttivarptSoggVersLocalitaVersante = deAttivarptSoggVersLocalitaVersante;
    }
    public String getDeAttivarptSoggVersProvinciaVersante() {
        return this.deAttivarptSoggVersProvinciaVersante;
    }
    
    public void setDeAttivarptSoggVersProvinciaVersante(String deAttivarptSoggVersProvinciaVersante) {
        this.deAttivarptSoggVersProvinciaVersante = deAttivarptSoggVersProvinciaVersante;
    }
    public String getCodAttivarptSoggVersNazioneVersante() {
        return this.codAttivarptSoggVersNazioneVersante;
    }
    
    public void setCodAttivarptSoggVersNazioneVersante(String codAttivarptSoggVersNazioneVersante) {
        this.codAttivarptSoggVersNazioneVersante = codAttivarptSoggVersNazioneVersante;
    }
    public String getDeAttivarptSoggVersEmailVersante() {
        return this.deAttivarptSoggVersEmailVersante;
    }
    
    public void setDeAttivarptSoggVersEmailVersante(String deAttivarptSoggVersEmailVersante) {
        this.deAttivarptSoggVersEmailVersante = deAttivarptSoggVersEmailVersante;
    }
    public String getDeAttivarptIbanAddebito() {
        return this.deAttivarptIbanAddebito;
    }
    
    public void setDeAttivarptIbanAddebito(String deAttivarptIbanAddebito) {
        this.deAttivarptIbanAddebito = deAttivarptIbanAddebito;
    }
    public String getDeAttivarptBicAddebito() {
        return this.deAttivarptBicAddebito;
    }
    
    public void setDeAttivarptBicAddebito(String deAttivarptBicAddebito) {
        this.deAttivarptBicAddebito = deAttivarptBicAddebito;
    }
    public String getCodAttivarptSoggPagIdUnivPagTipoIdUnivoco() {
        return this.codAttivarptSoggPagIdUnivPagTipoIdUnivoco;
    }
    
    public void setCodAttivarptSoggPagIdUnivPagTipoIdUnivoco(String codAttivarptSoggPagIdUnivPagTipoIdUnivoco) {
        this.codAttivarptSoggPagIdUnivPagTipoIdUnivoco = codAttivarptSoggPagIdUnivPagTipoIdUnivoco;
    }
    public String getCodAttivarptSoggPagIdUnivPagCodiceIdUnivoco() {
        return this.codAttivarptSoggPagIdUnivPagCodiceIdUnivoco;
    }
    
    public void setCodAttivarptSoggPagIdUnivPagCodiceIdUnivoco(String codAttivarptSoggPagIdUnivPagCodiceIdUnivoco) {
        this.codAttivarptSoggPagIdUnivPagCodiceIdUnivoco = codAttivarptSoggPagIdUnivPagCodiceIdUnivoco;
    }
    public String getDeAttivarptSoggPagAnagraficaPagatore() {
        return this.deAttivarptSoggPagAnagraficaPagatore;
    }
    
    public void setDeAttivarptSoggPagAnagraficaPagatore(String deAttivarptSoggPagAnagraficaPagatore) {
        this.deAttivarptSoggPagAnagraficaPagatore = deAttivarptSoggPagAnagraficaPagatore;
    }
    public String getDeAttivarptSoggPagIndirizzoPagatore() {
        return this.deAttivarptSoggPagIndirizzoPagatore;
    }
    
    public void setDeAttivarptSoggPagIndirizzoPagatore(String deAttivarptSoggPagIndirizzoPagatore) {
        this.deAttivarptSoggPagIndirizzoPagatore = deAttivarptSoggPagIndirizzoPagatore;
    }
    public String getDeAttivarptSoggPagCivicoPagatore() {
        return this.deAttivarptSoggPagCivicoPagatore;
    }
    
    public void setDeAttivarptSoggPagCivicoPagatore(String deAttivarptSoggPagCivicoPagatore) {
        this.deAttivarptSoggPagCivicoPagatore = deAttivarptSoggPagCivicoPagatore;
    }
    public String getCodAttivarptSoggPagCapPagatore() {
        return this.codAttivarptSoggPagCapPagatore;
    }
    
    public void setCodAttivarptSoggPagCapPagatore(String codAttivarptSoggPagCapPagatore) {
        this.codAttivarptSoggPagCapPagatore = codAttivarptSoggPagCapPagatore;
    }
    public String getDeAttivarptSoggPagLocalitaPagatore() {
        return this.deAttivarptSoggPagLocalitaPagatore;
    }
    
    public void setDeAttivarptSoggPagLocalitaPagatore(String deAttivarptSoggPagLocalitaPagatore) {
        this.deAttivarptSoggPagLocalitaPagatore = deAttivarptSoggPagLocalitaPagatore;
    }
    public String getDeAttivarptSoggPagProvinciaPagatore() {
        return this.deAttivarptSoggPagProvinciaPagatore;
    }
    
    public void setDeAttivarptSoggPagProvinciaPagatore(String deAttivarptSoggPagProvinciaPagatore) {
        this.deAttivarptSoggPagProvinciaPagatore = deAttivarptSoggPagProvinciaPagatore;
    }
    public String getCodAttivarptSoggPagNazionePagatore() {
        return this.codAttivarptSoggPagNazionePagatore;
    }
    
    public void setCodAttivarptSoggPagNazionePagatore(String codAttivarptSoggPagNazionePagatore) {
        this.codAttivarptSoggPagNazionePagatore = codAttivarptSoggPagNazionePagatore;
    }
    public String getDeAttivarptSoggPagEmailPagatore() {
        return this.deAttivarptSoggPagEmailPagatore;
    }
    
    public void setDeAttivarptSoggPagEmailPagatore(String deAttivarptSoggPagEmailPagatore) {
        this.deAttivarptSoggPagEmailPagatore = deAttivarptSoggPagEmailPagatore;
    }
    public Date getDtEAttivarpt() {
        return this.dtEAttivarpt;
    }
    
    public void setDtEAttivarpt(Date dtEAttivarpt) {
        this.dtEAttivarpt = dtEAttivarpt;
    }
    public BigDecimal getNumEAttivarptImportoSingoloVersamento() {
        return this.numEAttivarptImportoSingoloVersamento;
    }
    
    public void setNumEAttivarptImportoSingoloVersamento(BigDecimal numEAttivarptImportoSingoloVersamento) {
        this.numEAttivarptImportoSingoloVersamento = numEAttivarptImportoSingoloVersamento;
    }
    public String getDeEAttivarptIbanAccredito() {
        return this.deEAttivarptIbanAccredito;
    }
    
    public void setDeEAttivarptIbanAccredito(String deEAttivarptIbanAccredito) {
        this.deEAttivarptIbanAccredito = deEAttivarptIbanAccredito;
    }
    public String getDeEAttivarptBicAccredito() {
        return this.deEAttivarptBicAccredito;
    }
    
    public void setDeEAttivarptBicAccredito(String deEAttivarptBicAccredito) {
        this.deEAttivarptBicAccredito = deEAttivarptBicAccredito;
    }
    public String getCodEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco() {
        return this.codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco;
    }
    
    public void setCodEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco(String codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco) {
        this.codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco = codEAttivarptEnteBenefIdUnivBenefTipoIdUnivoco;
    }
    public String getCodEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco() {
        return this.codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco;
    }
    
    public void setCodEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco(String codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco) {
        this.codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco = codEAttivarptEnteBenefIdUnivBenefCodiceIdUnivoco;
    }
    public String getDeEAttivarptEnteBenefDenominazioneBeneficiario() {
        return this.deEAttivarptEnteBenefDenominazioneBeneficiario;
    }
    
    public void setDeEAttivarptEnteBenefDenominazioneBeneficiario(String deEAttivarptEnteBenefDenominazioneBeneficiario) {
        this.deEAttivarptEnteBenefDenominazioneBeneficiario = deEAttivarptEnteBenefDenominazioneBeneficiario;
    }
    public String getCodEAttivarptEnteBenefCodiceUnitOperBeneficiario() {
        return this.codEAttivarptEnteBenefCodiceUnitOperBeneficiario;
    }
    
    public void setCodEAttivarptEnteBenefCodiceUnitOperBeneficiario(String codEAttivarptEnteBenefCodiceUnitOperBeneficiario) {
        this.codEAttivarptEnteBenefCodiceUnitOperBeneficiario = codEAttivarptEnteBenefCodiceUnitOperBeneficiario;
    }
    public String getDeEAttivarptEnteBenefDenomUnitOperBeneficiario() {
        return this.deEAttivarptEnteBenefDenomUnitOperBeneficiario;
    }
    
    public void setDeEAttivarptEnteBenefDenomUnitOperBeneficiario(String deEAttivarptEnteBenefDenomUnitOperBeneficiario) {
        this.deEAttivarptEnteBenefDenomUnitOperBeneficiario = deEAttivarptEnteBenefDenomUnitOperBeneficiario;
    }
    public String getDeEAttivarptEnteBenefIndirizzoBeneficiario() {
        return this.deEAttivarptEnteBenefIndirizzoBeneficiario;
    }
    
    public void setDeEAttivarptEnteBenefIndirizzoBeneficiario(String deEAttivarptEnteBenefIndirizzoBeneficiario) {
        this.deEAttivarptEnteBenefIndirizzoBeneficiario = deEAttivarptEnteBenefIndirizzoBeneficiario;
    }
    public String getDeEAttivarptEnteBenefCivicoBeneficiario() {
        return this.deEAttivarptEnteBenefCivicoBeneficiario;
    }
    
    public void setDeEAttivarptEnteBenefCivicoBeneficiario(String deEAttivarptEnteBenefCivicoBeneficiario) {
        this.deEAttivarptEnteBenefCivicoBeneficiario = deEAttivarptEnteBenefCivicoBeneficiario;
    }
    public String getCodEAttivarptEnteBenefCapBeneficiario() {
        return this.codEAttivarptEnteBenefCapBeneficiario;
    }
    
    public void setCodEAttivarptEnteBenefCapBeneficiario(String codEAttivarptEnteBenefCapBeneficiario) {
        this.codEAttivarptEnteBenefCapBeneficiario = codEAttivarptEnteBenefCapBeneficiario;
    }
    public String getDeEAttivarptEnteBenefLocalitaBeneficiario() {
        return this.deEAttivarptEnteBenefLocalitaBeneficiario;
    }
    
    public void setDeEAttivarptEnteBenefLocalitaBeneficiario(String deEAttivarptEnteBenefLocalitaBeneficiario) {
        this.deEAttivarptEnteBenefLocalitaBeneficiario = deEAttivarptEnteBenefLocalitaBeneficiario;
    }
    public String getDeEAttivarptEnteBenefProvinciaBeneficiario() {
        return this.deEAttivarptEnteBenefProvinciaBeneficiario;
    }
    
    public void setDeEAttivarptEnteBenefProvinciaBeneficiario(String deEAttivarptEnteBenefProvinciaBeneficiario) {
        this.deEAttivarptEnteBenefProvinciaBeneficiario = deEAttivarptEnteBenefProvinciaBeneficiario;
    }
    public String getCodEAttivarptEnteBenefNazioneBeneficiario() {
        return this.codEAttivarptEnteBenefNazioneBeneficiario;
    }
    
    public void setCodEAttivarptEnteBenefNazioneBeneficiario(String codEAttivarptEnteBenefNazioneBeneficiario) {
        this.codEAttivarptEnteBenefNazioneBeneficiario = codEAttivarptEnteBenefNazioneBeneficiario;
    }
    public String getDeEAttivarptCredenzialiPagatore() {
        return this.deEAttivarptCredenzialiPagatore;
    }
    
    public void setDeEAttivarptCredenzialiPagatore(String deEAttivarptCredenzialiPagatore) {
        this.deEAttivarptCredenzialiPagatore = deEAttivarptCredenzialiPagatore;
    }
    public String getDeEAttivarptCausaleVersamento() {
        return this.deEAttivarptCausaleVersamento;
    }
    
    public void setDeEAttivarptCausaleVersamento(String deEAttivarptCausaleVersamento) {
        this.deEAttivarptCausaleVersamento = deEAttivarptCausaleVersamento;
    }
    public String getDeAttivarptEsito() {
        return this.deAttivarptEsito;
    }
    
    public void setDeAttivarptEsito(String deAttivarptEsito) {
        this.deAttivarptEsito = deAttivarptEsito;
    }
    public String getCodAttivarptFaultCode() {
        return this.codAttivarptFaultCode;
    }
    
    public void setCodAttivarptFaultCode(String codAttivarptFaultCode) {
        this.codAttivarptFaultCode = codAttivarptFaultCode;
    }
    public String getDeAttivarptFaultString() {
        return this.deAttivarptFaultString;
    }
    
    public void setDeAttivarptFaultString(String deAttivarptFaultString) {
        this.deAttivarptFaultString = deAttivarptFaultString;
    }
    public String getCodAttivarptId() {
        return this.codAttivarptId;
    }
    
    public void setCodAttivarptId(String codAttivarptId) {
        this.codAttivarptId = codAttivarptId;
    }
    public String getDeAttivarptDescription() {
        return this.deAttivarptDescription;
    }
    
    public void setDeAttivarptDescription(String deAttivarptDescription) {
        this.deAttivarptDescription = deAttivarptDescription;
    }
    public Integer getCodAttivarptSerial() {
        return this.codAttivarptSerial;
    }
    
    public void setCodAttivarptSerial(Integer codAttivarptSerial) {
        this.codAttivarptSerial = codAttivarptSerial;
    }
    public String getCodAttivarptIdIntermediarioPsp() {
        return this.codAttivarptIdIntermediarioPsp;
    }
    
    public void setCodAttivarptIdIntermediarioPsp(String codAttivarptIdIntermediarioPsp) {
        this.codAttivarptIdIntermediarioPsp = codAttivarptIdIntermediarioPsp;
    }
    public String getCodAttivarptIdCanalePsp() {
        return this.codAttivarptIdCanalePsp;
    }
    
    public void setCodAttivarptIdCanalePsp(String codAttivarptIdCanalePsp) {
        this.codAttivarptIdCanalePsp = codAttivarptIdCanalePsp;
    }


	public String getCodAttivarptOriginalFaultCode() {
		return codAttivarptOriginalFaultCode;
	}


	public void setCodAttivarptOriginalFaultCode(String codAttivarptOriginalFaultCode) {
		this.codAttivarptOriginalFaultCode = codAttivarptOriginalFaultCode;
	}


	public String getDeAttivarptOriginalFaultString() {
		return deAttivarptOriginalFaultString;
	}


	public void setDeAttivarptOriginalFaultString(String deAttivarptOriginalFaultString) {
		this.deAttivarptOriginalFaultString = deAttivarptOriginalFaultString;
	}


	public String getDeAttivarptOriginalFaultDescription() {
		return deAttivarptOriginalFaultDescription;
	}


	public void setDeAttivarptOriginalFaultDescription(String deAttivarptOriginalFaultDescription) {
		this.deAttivarptOriginalFaultDescription = deAttivarptOriginalFaultDescription;
	}




}


