package it.regioneveneto.mygov.payment.nodoregionalefesp.domain;
// Generated Jun 28, 2017 11:14:13 AM by Hibernate Tools 3.2.4.GA


import java.util.Date;

/**
 * MygovEnte generated by hbm2java
 */
public class MygovEnte  implements java.io.Serializable {


     private Long mygovEnteId;
     private String codIpaEnte;
     private String codiceFiscaleEnte;
     private String deNomeEnte;
     private String emailAmministratore;
     private Date dtCreazione;
     private Date dtUltimaModifica;
     private String deRpInviarpTipoFirma;
     private String codRpEnteBenefIdUnivBenefTipoIdUnivoco;
     private String codRpEnteBenefIdUnivBenefCodiceIdUnivoco;
     private String deRpEnteBenefDenominazioneBeneficiario;
     private String codRpEnteBenefCodiceUnitOperBeneficiario;
     private String deRpEnteBenefDenomUnitOperBeneficiario;
     private String deRpEnteBenefIndirizzoBeneficiario;
     private String deRpEnteBenefCivicoBeneficiario;
     private String codRpEnteBenefCapBeneficiario;
     private String deRpEnteBenefLocalitaBeneficiario;
     private String deRpEnteBenefProvinciaBeneficiario;
     private String codRpEnteBenefNazioneBeneficiario;
     private String codRpDatiVersFirmaRicevuta;
     private String paaSilInviaRispostaPagamentoUrl;
     private String deWispUrlBack;
     private String deWispUrlReturn;
     private String codCodiceSegregazione;
	 private String deUrlEsterniAttiva;

    public MygovEnte() {
    }

	
    public MygovEnte(String codIpaEnte, String codiceFiscaleEnte, String deNomeEnte, Date dtCreazione, Date dtUltimaModifica, String codRpEnteBenefIdUnivBenefTipoIdUnivoco, String codRpEnteBenefIdUnivBenefCodiceIdUnivoco, String deRpEnteBenefDenominazioneBeneficiario, String codRpDatiVersFirmaRicevuta, String deWispUrlBack, String deWispUrlReturn, String codCodiceSegregazione) {
        this.codIpaEnte = codIpaEnte;
        this.codiceFiscaleEnte = codiceFiscaleEnte;
        this.deNomeEnte = deNomeEnte;
        this.dtCreazione = dtCreazione;
        this.dtUltimaModifica = dtUltimaModifica;
        this.codRpEnteBenefIdUnivBenefTipoIdUnivoco = codRpEnteBenefIdUnivBenefTipoIdUnivoco;
        this.codRpEnteBenefIdUnivBenefCodiceIdUnivoco = codRpEnteBenefIdUnivBenefCodiceIdUnivoco;
        this.deRpEnteBenefDenominazioneBeneficiario = deRpEnteBenefDenominazioneBeneficiario;
        this.codRpDatiVersFirmaRicevuta = codRpDatiVersFirmaRicevuta;
        this.deWispUrlBack = deWispUrlBack;
        this.deWispUrlReturn = deWispUrlReturn;
        this.codCodiceSegregazione = codCodiceSegregazione;
    }
    public MygovEnte(String codIpaEnte, String codiceFiscaleEnte, String deNomeEnte, String emailAmministratore, Date dtCreazione, Date dtUltimaModifica, String deRpInviarpTipoFirma, String codRpEnteBenefIdUnivBenefTipoIdUnivoco, String codRpEnteBenefIdUnivBenefCodiceIdUnivoco, String deRpEnteBenefDenominazioneBeneficiario, String codRpEnteBenefCodiceUnitOperBeneficiario, String deRpEnteBenefDenomUnitOperBeneficiario, String deRpEnteBenefIndirizzoBeneficiario, String deRpEnteBenefCivicoBeneficiario, String codRpEnteBenefCapBeneficiario, String deRpEnteBenefLocalitaBeneficiario, String deRpEnteBenefProvinciaBeneficiario, String codRpEnteBenefNazioneBeneficiario, String codRpDatiVersFirmaRicevuta, String paaSilInviaRispostaPagamentoUrl, String deWispUrlBack, String deWispUrlReturn, String codCodiceSegregazione, String deUrlEsterniAttiva) {
       this.codIpaEnte = codIpaEnte;
       this.codiceFiscaleEnte = codiceFiscaleEnte;
       this.deNomeEnte = deNomeEnte;
       this.emailAmministratore = emailAmministratore;
       this.dtCreazione = dtCreazione;
       this.dtUltimaModifica = dtUltimaModifica;
       this.deRpInviarpTipoFirma = deRpInviarpTipoFirma;
       this.codRpEnteBenefIdUnivBenefTipoIdUnivoco = codRpEnteBenefIdUnivBenefTipoIdUnivoco;
       this.codRpEnteBenefIdUnivBenefCodiceIdUnivoco = codRpEnteBenefIdUnivBenefCodiceIdUnivoco;
       this.deRpEnteBenefDenominazioneBeneficiario = deRpEnteBenefDenominazioneBeneficiario;
       this.codRpEnteBenefCodiceUnitOperBeneficiario = codRpEnteBenefCodiceUnitOperBeneficiario;
       this.deRpEnteBenefDenomUnitOperBeneficiario = deRpEnteBenefDenomUnitOperBeneficiario;
       this.deRpEnteBenefIndirizzoBeneficiario = deRpEnteBenefIndirizzoBeneficiario;
       this.deRpEnteBenefCivicoBeneficiario = deRpEnteBenefCivicoBeneficiario;
       this.codRpEnteBenefCapBeneficiario = codRpEnteBenefCapBeneficiario;
       this.deRpEnteBenefLocalitaBeneficiario = deRpEnteBenefLocalitaBeneficiario;
       this.deRpEnteBenefProvinciaBeneficiario = deRpEnteBenefProvinciaBeneficiario;
       this.codRpEnteBenefNazioneBeneficiario = codRpEnteBenefNazioneBeneficiario;
       this.codRpDatiVersFirmaRicevuta = codRpDatiVersFirmaRicevuta;
       this.paaSilInviaRispostaPagamentoUrl = paaSilInviaRispostaPagamentoUrl;
       this.deWispUrlBack = deWispUrlBack;
       this.deWispUrlReturn = deWispUrlReturn;
       this.codCodiceSegregazione = codCodiceSegregazione;
       this.deUrlEsterniAttiva = deUrlEsterniAttiva;
    }
   
    public Long getMygovEnteId() {
        return this.mygovEnteId;
    }
    
    public void setMygovEnteId(Long mygovEnteId) {
        this.mygovEnteId = mygovEnteId;
    }
    public String getCodIpaEnte() {
        return this.codIpaEnte;
    }
    
    public void setCodIpaEnte(String codIpaEnte) {
        this.codIpaEnte = codIpaEnte;
    }
    public String getCodiceFiscaleEnte() {
        return this.codiceFiscaleEnte;
    }
    
    public void setCodiceFiscaleEnte(String codiceFiscaleEnte) {
        this.codiceFiscaleEnte = codiceFiscaleEnte;
    }
    public String getDeNomeEnte() {
        return this.deNomeEnte;
    }
    
    public void setDeNomeEnte(String deNomeEnte) {
        this.deNomeEnte = deNomeEnte;
    }
    public String getEmailAmministratore() {
        return this.emailAmministratore;
    }
    
    public void setEmailAmministratore(String emailAmministratore) {
        this.emailAmministratore = emailAmministratore;
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
    public String getDeRpInviarpTipoFirma() {
        return this.deRpInviarpTipoFirma;
    }
    
    public void setDeRpInviarpTipoFirma(String deRpInviarpTipoFirma) {
        this.deRpInviarpTipoFirma = deRpInviarpTipoFirma;
    }
    public String getCodRpEnteBenefIdUnivBenefTipoIdUnivoco() {
        return this.codRpEnteBenefIdUnivBenefTipoIdUnivoco;
    }
    
    public void setCodRpEnteBenefIdUnivBenefTipoIdUnivoco(String codRpEnteBenefIdUnivBenefTipoIdUnivoco) {
        this.codRpEnteBenefIdUnivBenefTipoIdUnivoco = codRpEnteBenefIdUnivBenefTipoIdUnivoco;
    }
    public String getCodRpEnteBenefIdUnivBenefCodiceIdUnivoco() {
        return this.codRpEnteBenefIdUnivBenefCodiceIdUnivoco;
    }
    
    public void setCodRpEnteBenefIdUnivBenefCodiceIdUnivoco(String codRpEnteBenefIdUnivBenefCodiceIdUnivoco) {
        this.codRpEnteBenefIdUnivBenefCodiceIdUnivoco = codRpEnteBenefIdUnivBenefCodiceIdUnivoco;
    }
    public String getDeRpEnteBenefDenominazioneBeneficiario() {
        return this.deRpEnteBenefDenominazioneBeneficiario;
    }
    
    public void setDeRpEnteBenefDenominazioneBeneficiario(String deRpEnteBenefDenominazioneBeneficiario) {
        this.deRpEnteBenefDenominazioneBeneficiario = deRpEnteBenefDenominazioneBeneficiario;
    }
    public String getCodRpEnteBenefCodiceUnitOperBeneficiario() {
        return this.codRpEnteBenefCodiceUnitOperBeneficiario;
    }
    
    public void setCodRpEnteBenefCodiceUnitOperBeneficiario(String codRpEnteBenefCodiceUnitOperBeneficiario) {
        this.codRpEnteBenefCodiceUnitOperBeneficiario = codRpEnteBenefCodiceUnitOperBeneficiario;
    }
    public String getDeRpEnteBenefDenomUnitOperBeneficiario() {
        return this.deRpEnteBenefDenomUnitOperBeneficiario;
    }
    
    public void setDeRpEnteBenefDenomUnitOperBeneficiario(String deRpEnteBenefDenomUnitOperBeneficiario) {
        this.deRpEnteBenefDenomUnitOperBeneficiario = deRpEnteBenefDenomUnitOperBeneficiario;
    }
    public String getDeRpEnteBenefIndirizzoBeneficiario() {
        return this.deRpEnteBenefIndirizzoBeneficiario;
    }
    
    public void setDeRpEnteBenefIndirizzoBeneficiario(String deRpEnteBenefIndirizzoBeneficiario) {
        this.deRpEnteBenefIndirizzoBeneficiario = deRpEnteBenefIndirizzoBeneficiario;
    }
    public String getDeRpEnteBenefCivicoBeneficiario() {
        return this.deRpEnteBenefCivicoBeneficiario;
    }
    
    public void setDeRpEnteBenefCivicoBeneficiario(String deRpEnteBenefCivicoBeneficiario) {
        this.deRpEnteBenefCivicoBeneficiario = deRpEnteBenefCivicoBeneficiario;
    }
    public String getCodRpEnteBenefCapBeneficiario() {
        return this.codRpEnteBenefCapBeneficiario;
    }
    
    public void setCodRpEnteBenefCapBeneficiario(String codRpEnteBenefCapBeneficiario) {
        this.codRpEnteBenefCapBeneficiario = codRpEnteBenefCapBeneficiario;
    }
    public String getDeRpEnteBenefLocalitaBeneficiario() {
        return this.deRpEnteBenefLocalitaBeneficiario;
    }
    
    public void setDeRpEnteBenefLocalitaBeneficiario(String deRpEnteBenefLocalitaBeneficiario) {
        this.deRpEnteBenefLocalitaBeneficiario = deRpEnteBenefLocalitaBeneficiario;
    }
    public String getDeRpEnteBenefProvinciaBeneficiario() {
        return this.deRpEnteBenefProvinciaBeneficiario;
    }
    
    public void setDeRpEnteBenefProvinciaBeneficiario(String deRpEnteBenefProvinciaBeneficiario) {
        this.deRpEnteBenefProvinciaBeneficiario = deRpEnteBenefProvinciaBeneficiario;
    }
    public String getCodRpEnteBenefNazioneBeneficiario() {
        return this.codRpEnteBenefNazioneBeneficiario;
    }
    
    public void setCodRpEnteBenefNazioneBeneficiario(String codRpEnteBenefNazioneBeneficiario) {
        this.codRpEnteBenefNazioneBeneficiario = codRpEnteBenefNazioneBeneficiario;
    }
    public String getCodRpDatiVersFirmaRicevuta() {
        return this.codRpDatiVersFirmaRicevuta;
    }
    
    public void setCodRpDatiVersFirmaRicevuta(String codRpDatiVersFirmaRicevuta) {
        this.codRpDatiVersFirmaRicevuta = codRpDatiVersFirmaRicevuta;
    }
    public String getPaaSilInviaRispostaPagamentoUrl() {
        return this.paaSilInviaRispostaPagamentoUrl;
    }
    
    public void setPaaSilInviaRispostaPagamentoUrl(String paaSilInviaRispostaPagamentoUrl) {
        this.paaSilInviaRispostaPagamentoUrl = paaSilInviaRispostaPagamentoUrl;
    }
    public String getDeWispUrlBack() {
        return this.deWispUrlBack;
    }
    
    public void setDeWispUrlBack(String deWispUrlBack) {
        this.deWispUrlBack = deWispUrlBack;
    }
    public String getDeWispUrlReturn() {
        return this.deWispUrlReturn;
    }
    
    public void setDeWispUrlReturn(String deWispUrlReturn) {
        this.deWispUrlReturn = deWispUrlReturn;
    }
    public String getCodCodiceSegregazione() {
        return this.codCodiceSegregazione;
    }
    
    public void setCodCodiceSegregazione(String codCodiceSegregazione) {
        this.codCodiceSegregazione = codCodiceSegregazione;
    }

	public String getDeUrlEsterniAttiva() {
		return deUrlEsterniAttiva;
	}

	public void setDeUrlEsterniAttiva(String deUrlEsterniAttiva) {
		this.deUrlEsterniAttiva = deUrlEsterniAttiva;
	}




}


