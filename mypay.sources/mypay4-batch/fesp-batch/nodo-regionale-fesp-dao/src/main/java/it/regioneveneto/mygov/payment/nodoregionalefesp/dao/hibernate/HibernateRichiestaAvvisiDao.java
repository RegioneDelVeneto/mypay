package it.regioneveneto.mygov.payment.nodoregionalefesp.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.regioneveneto.mygov.payment.nodoregionalefesp.dao.RichiestaAvvisiDao;
import it.regioneveneto.mygov.payment.nodoregionalefesp.domain.MygovRichiestaAvvisi;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtDatiPagamentoPA;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtFaultBean;
import it.veneto.regione.pagamenti.pa.papernodoregionale.richiestaavvisi.CtNumeroAvviso;
import it.veneto.regione.schemas._2012.pagamenti.CtEnteBeneficiario;

public class HibernateRichiestaAvvisiDao extends HibernateDaoSupport implements RichiestaAvvisiDao {

	@Override
	public MygovRichiestaAvvisi insertWithRefresh(String identificativoIntermediarioPa, String identificativoStazioneIntermediarioPA, String identificativoDominio, String identificativoPSP, String idServizio, String datiSpecificiServizio) {
		MygovRichiestaAvvisi richiestaAvvisi = new MygovRichiestaAvvisi();
		
		richiestaAvvisi.setDtCreazione(new Date());
		richiestaAvvisi.setDtUltimaModifica(new Date());
		richiestaAvvisi.setCodIdentificativoIntermediarioPa(identificativoIntermediarioPa);
		richiestaAvvisi.setCodIdentificativoStazioneIntermediarioPa(identificativoStazioneIntermediarioPA);
		richiestaAvvisi.setCodIdentificativoDominio(identificativoDominio);
		richiestaAvvisi.setCodIdentificativoPsp(identificativoPSP);
		if (StringUtils.isNotBlank(idServizio))
			richiestaAvvisi.setCodIdServizio(idServizio);
		richiestaAvvisi.setDeDatiSpecificiServizio(datiSpecificiServizio);
		
		getHibernateTemplate().save(richiestaAvvisi);
		getHibernateTemplate().flush();
		getHibernateTemplate().refresh(richiestaAvvisi);
		
		return richiestaAvvisi;
	}

	@Override
	public void updateDatiPagamentoPa(Long richiestaAvvisiId, CtDatiPagamentoPA datiPagamentoPa) {
		MygovRichiestaAvvisi richiestaAvvisi = getById(richiestaAvvisiId);
		
		richiestaAvvisi.setNumDatiPagamPaImportoSingoloVersamento(datiPagamentoPa.getImportoSingoloVersamento());
		richiestaAvvisi.setCodDatiPagamPaIbanAccredito(datiPagamentoPa.getIbanAccredito());
		richiestaAvvisi.setCodDatiPagamPaBicAccredito(datiPagamentoPa.getBicAccredito());
		richiestaAvvisi.setDeDatiPagamentoPaCredenzialiPagatore(datiPagamentoPa.getCredenzialiPagatore());
		richiestaAvvisi.setDeDatiPagamentoPaCausaleVersamento(datiPagamentoPa.getCausaleVersamento());
		
		richiestaAvvisi.setDtUltimaModifica(new Date());
		getHibernateTemplate().update(richiestaAvvisi);
	}

	@Override
	public void updateFaultBean(Long richiestaAvvisiId, CtFaultBean faultBean) {
		MygovRichiestaAvvisi richiestaAvvisi = getById(richiestaAvvisiId);
		richiestaAvvisi.setCodRichiediAvvisoFaultCode(faultBean.getFaultCode());
		
		richiestaAvvisi.setCodRichiediAvvisoFaultString(faultBean.getFaultString());
		String description = faultBean.getDescription();
		if (description != null && description.length() > 1024)	
			richiestaAvvisi.setCodRichiediAvvisoFaultDescription(description.substring(0, 1024));
		else
			richiestaAvvisi.setCodRichiediAvvisoFaultDescription(description);
		richiestaAvvisi.setCodRichiediAvvisoFaultSerial(faultBean.getSerial());
		richiestaAvvisi.setCodRichiediAvvisoFaultId(faultBean.getId());
		
		richiestaAvvisi.setCodRichiediAvvisoOriginalFaultCode(faultBean.getOriginalFaultCode());
		richiestaAvvisi.setDeRichiediAvvisoOriginalFaultString(faultBean.getOriginalFaultString());
		String originalDescription = faultBean.getOriginalDescription();
		if (originalDescription != null && description.length() > 1024)	
			richiestaAvvisi.setDeRichiediAvvisoOriginalFaultDescription(originalDescription.substring(0, 1024));
		else
			richiestaAvvisi.setDeRichiediAvvisoOriginalFaultDescription(originalDescription);
		
		richiestaAvvisi.setDtUltimaModifica(new Date());
		getHibernateTemplate().update(richiestaAvvisi);
	}

	@Override
	public void updateNumeroAvviso(Long richiestaAvvisiId, CtNumeroAvviso numeroAvviso) {
		MygovRichiestaAvvisi richiestaAvvisi = getById(richiestaAvvisiId);
		
		richiestaAvvisi.setCodNumeroAvvisoAuxDigit(numeroAvviso.getAuxDigit());
		richiestaAvvisi.setCodNumeroAvvisoApplicationCode(numeroAvviso.getApplicationCode());
		richiestaAvvisi.setCodNumeroAvvisoCodIuv(numeroAvviso.getIUV());
		
		richiestaAvvisi.setDtUltimaModifica(new Date());
		getHibernateTemplate().update(richiestaAvvisi);
	}

	@Override
	public void updateEnteBeneficiario(Long richiestaAvvisiId, CtEnteBeneficiario enteBeneficiario) {
		MygovRichiestaAvvisi richiestaAvvisi = getById(richiestaAvvisiId);
				
		richiestaAvvisi.setCodDatiPagamPaEnteBenefIdUnivBenefTipoIdUnivoco(enteBeneficiario.getIdentificativoUnivocoBeneficiario().getTipoIdentificativoUnivoco().value());
		richiestaAvvisi.setCodDatiPagamPaEnteBenefIdUnivBenefCodiceIdUnivoco(enteBeneficiario.getIdentificativoUnivocoBeneficiario().getCodiceIdentificativoUnivoco());
		richiestaAvvisi.setDeDatiPagamPaEnteBenefDenominazioneBeneficiario(enteBeneficiario.getDenominazioneBeneficiario());
		richiestaAvvisi.setCodDatiPagamPaEnteBenefCodiceUnitOperBeneficiario(enteBeneficiario.getCodiceUnitOperBeneficiario());
		richiestaAvvisi.setDeDatiPagamPaEnteBenefDenomUnitOperBeneficiario(enteBeneficiario.getDenomUnitOperBeneficiario());
		richiestaAvvisi.setDeDatiPagamPaEnteBenefIndirizzoBeneficiario(enteBeneficiario.getIndirizzoBeneficiario());
		richiestaAvvisi.setDeDatiPagamPaEnteBenefCivicoBeneficiario(enteBeneficiario.getCivicoBeneficiario());
		richiestaAvvisi.setCodDatiPagamPaEnteBenefCapBeneficiario(enteBeneficiario.getCapBeneficiario());
		richiestaAvvisi.setDeDatiPagamPaEnteBenefLocalitaBeneficiario(enteBeneficiario.getLocalitaBeneficiario());
		richiestaAvvisi.setDeDatiPagamPaEnteBenefProvinciaBeneficiario(enteBeneficiario.getProvinciaBeneficiario());
		richiestaAvvisi.setCodDatiPagamPaEnteBenefNazioneBeneficiario(enteBeneficiario.getNazioneBeneficiario());
		
		richiestaAvvisi.setDtUltimaModifica(new Date());
		getHibernateTemplate().update(richiestaAvvisi);
	}

	@Override
	public void updateEsito(Long richiestaAvvisiId, String codEsito) {
		MygovRichiestaAvvisi richiestaAvvisi = getById(richiestaAvvisiId);
		
		richiestaAvvisi.setCodEsito(codEsito);
		
		richiestaAvvisi.setDtUltimaModifica(new Date());
		getHibernateTemplate().update(richiestaAvvisi);
	}

	@Override
	@SuppressWarnings("unchecked")
	public MygovRichiestaAvvisi getById(Long richiestaAvvisiId) throws DataAccessException {
		DetachedCriteria criteria = DetachedCriteria.forClass(MygovRichiestaAvvisi.class);
		criteria.add(Restrictions.eq("mygovRichiestaAvvisiId", richiestaAvvisiId));
		List<MygovRichiestaAvvisi> richiestaAvvisi = getHibernateTemplate().findByCriteria(criteria);
		if (richiestaAvvisi.size() > 1) {
			throw new DataRetrievalFailureException("richiestaAvvisiId [" + richiestaAvvisiId + "] is not valid");
		}
		
		return richiestaAvvisi.get(0);
	}

	
	

}
