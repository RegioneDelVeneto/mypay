package it.regioneveneto.mygov.payment.nodoregionalefesp.domain;
// Generated Nov 23, 2018 12:29:26 PM by Hibernate Tools 3.2.4.GA


import java.math.BigDecimal;
import java.util.Date;

/**
 * MygovRichiestaAvvisi generated by hbm2java
 */
public class MygovRichiestaAvvisi  implements java.io.Serializable {


     private Long mygovRichiestaAvvisiId;
     private Date dtCreazione;
     private Date dtUltimaModifica;
     private String codIdentificativoIntermediarioPa;
     private String codIdentificativoStazioneIntermediarioPa;
     private String codIdentificativoDominio;
     private String codIdentificativoPsp;
     private String codIdServizio;
     private String deDatiSpecificiServizio;
     private String codEsito;
     private String codNumeroAvvisoAuxDigit;
     private String codNumeroAvvisoApplicationCode;
     private String codNumeroAvvisoCodIuv;
     private BigDecimal numDatiPagamPaImportoSingoloVersamento;
     private String codDatiPagamPaIbanAccredito;
     private String codDatiPagamPaBicAccredito;
     private String codDatiPagamPaEnteBenefIdUnivBenefTipoIdUnivoco;
     private String codDatiPagamPaEnteBenefIdUnivBenefCodiceIdUnivoco;
     private String deDatiPagamPaEnteBenefDenominazioneBeneficiario;
     private String codDatiPagamPaEnteBenefCodiceUnitOperBeneficiario;
     private String deDatiPagamPaEnteBenefDenomUnitOperBeneficiario;
     private String deDatiPagamPaEnteBenefIndirizzoBeneficiario;
     private String deDatiPagamPaEnteBenefCivicoBeneficiario;
     private String codDatiPagamPaEnteBenefCapBeneficiario;
     private String deDatiPagamPaEnteBenefLocalitaBeneficiario;
     private String deDatiPagamPaEnteBenefProvinciaBeneficiario;
     private String codDatiPagamPaEnteBenefNazioneBeneficiario;
     private String deDatiPagamentoPaCredenzialiPagatore;
     private String deDatiPagamentoPaCausaleVersamento;
     private String codRichiediAvvisoFaultId;
     private String codRichiediAvvisoFaultCode;
     private String codRichiediAvvisoFaultString;
     private String codRichiediAvvisoFaultDescription;
     private Integer codRichiediAvvisoFaultSerial;
     private String codRichiediAvvisoOriginalFaultCode;
     private String deRichiediAvvisoOriginalFaultString;
     private String deRichiediAvvisoOriginalFaultDescription;

    public MygovRichiestaAvvisi() {
    }

	
    public MygovRichiestaAvvisi(Date dtCreazione, Date dtUltimaModifica, String codIdentificativoIntermediarioPa, String codIdentificativoStazioneIntermediarioPa, String codIdentificativoDominio, String codIdentificativoPsp, String deDatiSpecificiServizio) {
        this.dtCreazione = dtCreazione;
        this.dtUltimaModifica = dtUltimaModifica;
        this.codIdentificativoIntermediarioPa = codIdentificativoIntermediarioPa;
        this.codIdentificativoStazioneIntermediarioPa = codIdentificativoStazioneIntermediarioPa;
        this.codIdentificativoDominio = codIdentificativoDominio;
        this.codIdentificativoPsp = codIdentificativoPsp;
        this.deDatiSpecificiServizio = deDatiSpecificiServizio;
    }
    public MygovRichiestaAvvisi(Date dtCreazione, Date dtUltimaModifica, String codIdentificativoIntermediarioPa, String codIdentificativoStazioneIntermediarioPa, String codIdentificativoDominio, String codIdentificativoPsp, String codIdServizio, String deDatiSpecificiServizio, String codEsito, String codNumeroAvvisoAuxDigit, String codNumeroAvvisoApplicationCode, String codNumeroAvvisoCodIuv, BigDecimal numDatiPagamPaImportoSingoloVersamento, String codDatiPagamPaIbanAccredito, String codDatiPagamPaBicAccredito, String codDatiPagamPaEnteBenefIdUnivBenefTipoIdUnivoco, String codDatiPagamPaEnteBenefIdUnivBenefCodiceIdUnivoco, String deDatiPagamPaEnteBenefDenominazioneBeneficiario, String codDatiPagamPaEnteBenefCodiceUnitOperBeneficiario, String deDatiPagamPaEnteBenefDenomUnitOperBeneficiario, String deDatiPagamPaEnteBenefIndirizzoBeneficiario, String deDatiPagamPaEnteBenefCivicoBeneficiario, String codDatiPagamPaEnteBenefCapBeneficiario, String deDatiPagamPaEnteBenefLocalitaBeneficiario, String deDatiPagamPaEnteBenefProvinciaBeneficiario, String codDatiPagamPaEnteBenefNazioneBeneficiario, String deDatiPagamentoPaCredenzialiPagatore, String deDatiPagamentoPaCausaleVersamento, String codRichiediAvvisoFaultId, String codRichiediAvvisoFaultCode, String codRichiediAvvisoFaultString, String codRichiediAvvisoFaultDescription, Integer codRichiediAvvisoFaultSerial, String codRichiediAvvisoOriginalFaultCode, String deRichiediAvvisoOriginalFaultString, String deRichiediAvvisoOriginalFaultDescription) {
       this.dtCreazione = dtCreazione;
       this.dtUltimaModifica = dtUltimaModifica;
       this.codIdentificativoIntermediarioPa = codIdentificativoIntermediarioPa;
       this.codIdentificativoStazioneIntermediarioPa = codIdentificativoStazioneIntermediarioPa;
       this.codIdentificativoDominio = codIdentificativoDominio;
       this.codIdentificativoPsp = codIdentificativoPsp;
       this.codIdServizio = codIdServizio;
       this.deDatiSpecificiServizio = deDatiSpecificiServizio;
       this.codEsito = codEsito;
       this.codNumeroAvvisoAuxDigit = codNumeroAvvisoAuxDigit;
       this.codNumeroAvvisoApplicationCode = codNumeroAvvisoApplicationCode;
       this.codNumeroAvvisoCodIuv = codNumeroAvvisoCodIuv;
       this.numDatiPagamPaImportoSingoloVersamento = numDatiPagamPaImportoSingoloVersamento;
       this.codDatiPagamPaIbanAccredito = codDatiPagamPaIbanAccredito;
       this.codDatiPagamPaBicAccredito = codDatiPagamPaBicAccredito;
       this.codDatiPagamPaEnteBenefIdUnivBenefTipoIdUnivoco = codDatiPagamPaEnteBenefIdUnivBenefTipoIdUnivoco;
       this.codDatiPagamPaEnteBenefIdUnivBenefCodiceIdUnivoco = codDatiPagamPaEnteBenefIdUnivBenefCodiceIdUnivoco;
       this.deDatiPagamPaEnteBenefDenominazioneBeneficiario = deDatiPagamPaEnteBenefDenominazioneBeneficiario;
       this.codDatiPagamPaEnteBenefCodiceUnitOperBeneficiario = codDatiPagamPaEnteBenefCodiceUnitOperBeneficiario;
       this.deDatiPagamPaEnteBenefDenomUnitOperBeneficiario = deDatiPagamPaEnteBenefDenomUnitOperBeneficiario;
       this.deDatiPagamPaEnteBenefIndirizzoBeneficiario = deDatiPagamPaEnteBenefIndirizzoBeneficiario;
       this.deDatiPagamPaEnteBenefCivicoBeneficiario = deDatiPagamPaEnteBenefCivicoBeneficiario;
       this.codDatiPagamPaEnteBenefCapBeneficiario = codDatiPagamPaEnteBenefCapBeneficiario;
       this.deDatiPagamPaEnteBenefLocalitaBeneficiario = deDatiPagamPaEnteBenefLocalitaBeneficiario;
       this.deDatiPagamPaEnteBenefProvinciaBeneficiario = deDatiPagamPaEnteBenefProvinciaBeneficiario;
       this.codDatiPagamPaEnteBenefNazioneBeneficiario = codDatiPagamPaEnteBenefNazioneBeneficiario;
       this.deDatiPagamentoPaCredenzialiPagatore = deDatiPagamentoPaCredenzialiPagatore;
       this.deDatiPagamentoPaCausaleVersamento = deDatiPagamentoPaCausaleVersamento;
       this.codRichiediAvvisoFaultId = codRichiediAvvisoFaultId;
       this.codRichiediAvvisoFaultCode = codRichiediAvvisoFaultCode;
       this.codRichiediAvvisoFaultString = codRichiediAvvisoFaultString;
       this.codRichiediAvvisoFaultDescription = codRichiediAvvisoFaultDescription;
       this.codRichiediAvvisoFaultSerial = codRichiediAvvisoFaultSerial;
       this.codRichiediAvvisoOriginalFaultCode = codRichiediAvvisoOriginalFaultCode;
       this.deRichiediAvvisoOriginalFaultString = deRichiediAvvisoOriginalFaultString;
       this.deRichiediAvvisoOriginalFaultDescription = deRichiediAvvisoOriginalFaultDescription;
    }
   
    public Long getMygovRichiestaAvvisiId() {
        return this.mygovRichiestaAvvisiId;
    }
    
    public void setMygovRichiestaAvvisiId(Long mygovRichiestaAvvisiId) {
        this.mygovRichiestaAvvisiId = mygovRichiestaAvvisiId;
    }
    public Date getDtCreazione() {
        return this.dtCreazione;
    }
    
    public void setDtCreazione(Date dtCreazione) {
        this.dtCreazione = dtCreazione;
    }
    public Date getDtUltimaModifica() {
        return this.dtUltimaModifica;
    }
    
    public void setDtUltimaModifica(Date dtUltimaModifica) {
        this.dtUltimaModifica = dtUltimaModifica;
    }
    public String getCodIdentificativoIntermediarioPa() {
        return this.codIdentificativoIntermediarioPa;
    }
    
    public void setCodIdentificativoIntermediarioPa(String codIdentificativoIntermediarioPa) {
        this.codIdentificativoIntermediarioPa = codIdentificativoIntermediarioPa;
    }
    public String getCodIdentificativoStazioneIntermediarioPa() {
        return this.codIdentificativoStazioneIntermediarioPa;
    }
    
    public void setCodIdentificativoStazioneIntermediarioPa(String codIdentificativoStazioneIntermediarioPa) {
        this.codIdentificativoStazioneIntermediarioPa = codIdentificativoStazioneIntermediarioPa;
    }
    public String getCodIdentificativoDominio() {
        return this.codIdentificativoDominio;
    }
    
    public void setCodIdentificativoDominio(String codIdentificativoDominio) {
        this.codIdentificativoDominio = codIdentificativoDominio;
    }
    public String getCodIdentificativoPsp() {
        return this.codIdentificativoPsp;
    }
    
    public void setCodIdentificativoPsp(String codIdentificativoPsp) {
        this.codIdentificativoPsp = codIdentificativoPsp;
    }
    public String getCodIdServizio() {
        return this.codIdServizio;
    }
    
    public void setCodIdServizio(String codIdServizio) {
        this.codIdServizio = codIdServizio;
    }
    public String getDeDatiSpecificiServizio() {
        return this.deDatiSpecificiServizio;
    }
    
    public void setDeDatiSpecificiServizio(String deDatiSpecificiServizio) {
        this.deDatiSpecificiServizio = deDatiSpecificiServizio;
    }
    public String getCodEsito() {
        return this.codEsito;
    }
    
    public void setCodEsito(String codEsito) {
        this.codEsito = codEsito;
    }
    public String getCodNumeroAvvisoAuxDigit() {
        return this.codNumeroAvvisoAuxDigit;
    }
    
    public void setCodNumeroAvvisoAuxDigit(String codNumeroAvvisoAuxDigit) {
        this.codNumeroAvvisoAuxDigit = codNumeroAvvisoAuxDigit;
    }
    public String getCodNumeroAvvisoApplicationCode() {
        return this.codNumeroAvvisoApplicationCode;
    }
    
    public void setCodNumeroAvvisoApplicationCode(String codNumeroAvvisoApplicationCode) {
        this.codNumeroAvvisoApplicationCode = codNumeroAvvisoApplicationCode;
    }
    public String getCodNumeroAvvisoCodIuv() {
        return this.codNumeroAvvisoCodIuv;
    }
    
    public void setCodNumeroAvvisoCodIuv(String codNumeroAvvisoCodIuv) {
        this.codNumeroAvvisoCodIuv = codNumeroAvvisoCodIuv;
    }
    public BigDecimal getNumDatiPagamPaImportoSingoloVersamento() {
        return this.numDatiPagamPaImportoSingoloVersamento;
    }
    
    public void setNumDatiPagamPaImportoSingoloVersamento(BigDecimal numDatiPagamPaImportoSingoloVersamento) {
        this.numDatiPagamPaImportoSingoloVersamento = numDatiPagamPaImportoSingoloVersamento;
    }
    public String getCodDatiPagamPaIbanAccredito() {
        return this.codDatiPagamPaIbanAccredito;
    }
    
    public void setCodDatiPagamPaIbanAccredito(String codDatiPagamPaIbanAccredito) {
        this.codDatiPagamPaIbanAccredito = codDatiPagamPaIbanAccredito;
    }
    public String getCodDatiPagamPaBicAccredito() {
        return this.codDatiPagamPaBicAccredito;
    }
    
    public void setCodDatiPagamPaBicAccredito(String codDatiPagamPaBicAccredito) {
        this.codDatiPagamPaBicAccredito = codDatiPagamPaBicAccredito;
    }
    public String getCodDatiPagamPaEnteBenefIdUnivBenefTipoIdUnivoco() {
        return this.codDatiPagamPaEnteBenefIdUnivBenefTipoIdUnivoco;
    }
    
    public void setCodDatiPagamPaEnteBenefIdUnivBenefTipoIdUnivoco(String codDatiPagamPaEnteBenefIdUnivBenefTipoIdUnivoco) {
        this.codDatiPagamPaEnteBenefIdUnivBenefTipoIdUnivoco = codDatiPagamPaEnteBenefIdUnivBenefTipoIdUnivoco;
    }
    public String getCodDatiPagamPaEnteBenefIdUnivBenefCodiceIdUnivoco() {
        return this.codDatiPagamPaEnteBenefIdUnivBenefCodiceIdUnivoco;
    }
    
    public void setCodDatiPagamPaEnteBenefIdUnivBenefCodiceIdUnivoco(String codDatiPagamPaEnteBenefIdUnivBenefCodiceIdUnivoco) {
        this.codDatiPagamPaEnteBenefIdUnivBenefCodiceIdUnivoco = codDatiPagamPaEnteBenefIdUnivBenefCodiceIdUnivoco;
    }
    public String getDeDatiPagamPaEnteBenefDenominazioneBeneficiario() {
        return this.deDatiPagamPaEnteBenefDenominazioneBeneficiario;
    }
    
    public void setDeDatiPagamPaEnteBenefDenominazioneBeneficiario(String deDatiPagamPaEnteBenefDenominazioneBeneficiario) {
        this.deDatiPagamPaEnteBenefDenominazioneBeneficiario = deDatiPagamPaEnteBenefDenominazioneBeneficiario;
    }
    public String getCodDatiPagamPaEnteBenefCodiceUnitOperBeneficiario() {
        return this.codDatiPagamPaEnteBenefCodiceUnitOperBeneficiario;
    }
    
    public void setCodDatiPagamPaEnteBenefCodiceUnitOperBeneficiario(String codDatiPagamPaEnteBenefCodiceUnitOperBeneficiario) {
        this.codDatiPagamPaEnteBenefCodiceUnitOperBeneficiario = codDatiPagamPaEnteBenefCodiceUnitOperBeneficiario;
    }
    public String getDeDatiPagamPaEnteBenefDenomUnitOperBeneficiario() {
        return this.deDatiPagamPaEnteBenefDenomUnitOperBeneficiario;
    }
    
    public void setDeDatiPagamPaEnteBenefDenomUnitOperBeneficiario(String deDatiPagamPaEnteBenefDenomUnitOperBeneficiario) {
        this.deDatiPagamPaEnteBenefDenomUnitOperBeneficiario = deDatiPagamPaEnteBenefDenomUnitOperBeneficiario;
    }
    public String getDeDatiPagamPaEnteBenefIndirizzoBeneficiario() {
        return this.deDatiPagamPaEnteBenefIndirizzoBeneficiario;
    }
    
    public void setDeDatiPagamPaEnteBenefIndirizzoBeneficiario(String deDatiPagamPaEnteBenefIndirizzoBeneficiario) {
        this.deDatiPagamPaEnteBenefIndirizzoBeneficiario = deDatiPagamPaEnteBenefIndirizzoBeneficiario;
    }
    public String getDeDatiPagamPaEnteBenefCivicoBeneficiario() {
        return this.deDatiPagamPaEnteBenefCivicoBeneficiario;
    }
    
    public void setDeDatiPagamPaEnteBenefCivicoBeneficiario(String deDatiPagamPaEnteBenefCivicoBeneficiario) {
        this.deDatiPagamPaEnteBenefCivicoBeneficiario = deDatiPagamPaEnteBenefCivicoBeneficiario;
    }
    public String getCodDatiPagamPaEnteBenefCapBeneficiario() {
        return this.codDatiPagamPaEnteBenefCapBeneficiario;
    }
    
    public void setCodDatiPagamPaEnteBenefCapBeneficiario(String codDatiPagamPaEnteBenefCapBeneficiario) {
        this.codDatiPagamPaEnteBenefCapBeneficiario = codDatiPagamPaEnteBenefCapBeneficiario;
    }
    public String getDeDatiPagamPaEnteBenefLocalitaBeneficiario() {
        return this.deDatiPagamPaEnteBenefLocalitaBeneficiario;
    }
    
    public void setDeDatiPagamPaEnteBenefLocalitaBeneficiario(String deDatiPagamPaEnteBenefLocalitaBeneficiario) {
        this.deDatiPagamPaEnteBenefLocalitaBeneficiario = deDatiPagamPaEnteBenefLocalitaBeneficiario;
    }
    public String getDeDatiPagamPaEnteBenefProvinciaBeneficiario() {
        return this.deDatiPagamPaEnteBenefProvinciaBeneficiario;
    }
    
    public void setDeDatiPagamPaEnteBenefProvinciaBeneficiario(String deDatiPagamPaEnteBenefProvinciaBeneficiario) {
        this.deDatiPagamPaEnteBenefProvinciaBeneficiario = deDatiPagamPaEnteBenefProvinciaBeneficiario;
    }
    public String getCodDatiPagamPaEnteBenefNazioneBeneficiario() {
        return this.codDatiPagamPaEnteBenefNazioneBeneficiario;
    }
    
    public void setCodDatiPagamPaEnteBenefNazioneBeneficiario(String codDatiPagamPaEnteBenefNazioneBeneficiario) {
        this.codDatiPagamPaEnteBenefNazioneBeneficiario = codDatiPagamPaEnteBenefNazioneBeneficiario;
    }
    public String getDeDatiPagamentoPaCredenzialiPagatore() {
        return this.deDatiPagamentoPaCredenzialiPagatore;
    }
    
    public void setDeDatiPagamentoPaCredenzialiPagatore(String deDatiPagamentoPaCredenzialiPagatore) {
        this.deDatiPagamentoPaCredenzialiPagatore = deDatiPagamentoPaCredenzialiPagatore;
    }
    public String getDeDatiPagamentoPaCausaleVersamento() {
        return this.deDatiPagamentoPaCausaleVersamento;
    }
    
    public void setDeDatiPagamentoPaCausaleVersamento(String deDatiPagamentoPaCausaleVersamento) {
        this.deDatiPagamentoPaCausaleVersamento = deDatiPagamentoPaCausaleVersamento;
    }
    public String getCodRichiediAvvisoFaultId() {
        return this.codRichiediAvvisoFaultId;
    }
    
    public void setCodRichiediAvvisoFaultId(String codRichiediAvvisoFaultId) {
        this.codRichiediAvvisoFaultId = codRichiediAvvisoFaultId;
    }
    public String getCodRichiediAvvisoFaultCode() {
        return this.codRichiediAvvisoFaultCode;
    }
    
    public void setCodRichiediAvvisoFaultCode(String codRichiediAvvisoFaultCode) {
        this.codRichiediAvvisoFaultCode = codRichiediAvvisoFaultCode;
    }
    public String getCodRichiediAvvisoFaultString() {
        return this.codRichiediAvvisoFaultString;
    }
    
    public void setCodRichiediAvvisoFaultString(String codRichiediAvvisoFaultString) {
        this.codRichiediAvvisoFaultString = codRichiediAvvisoFaultString;
    }
    public String getCodRichiediAvvisoFaultDescription() {
        return this.codRichiediAvvisoFaultDescription;
    }
    
    public void setCodRichiediAvvisoFaultDescription(String codRichiediAvvisoFaultDescription) {
        this.codRichiediAvvisoFaultDescription = codRichiediAvvisoFaultDescription;
    }
    public Integer getCodRichiediAvvisoFaultSerial() {
        return this.codRichiediAvvisoFaultSerial;
    }
    
    public void setCodRichiediAvvisoFaultSerial(Integer codRichiediAvvisoFaultSerial) {
        this.codRichiediAvvisoFaultSerial = codRichiediAvvisoFaultSerial;
    }


	public String getCodRichiediAvvisoOriginalFaultCode() {
		return codRichiediAvvisoOriginalFaultCode;
	}


	public void setCodRichiediAvvisoOriginalFaultCode(String codRichiediAvvisoOriginalFaultCode) {
		this.codRichiediAvvisoOriginalFaultCode = codRichiediAvvisoOriginalFaultCode;
	}


	public String getDeRichiediAvvisoOriginalFaultString() {
		return deRichiediAvvisoOriginalFaultString;
	}


	public void setDeRichiediAvvisoOriginalFaultString(String deRichiediAvvisoOriginalFaultString) {
		this.deRichiediAvvisoOriginalFaultString = deRichiediAvvisoOriginalFaultString;
	}


	public String getDeRichiediAvvisoOriginalFaultDescription() {
		return deRichiediAvvisoOriginalFaultDescription;
	}


	public void setDeRichiediAvvisoOriginalFaultDescription(String deRichiediAvvisoOriginalFaultDescription) {
		this.deRichiediAvvisoOriginalFaultDescription = deRichiediAvvisoOriginalFaultDescription;
	}




}


