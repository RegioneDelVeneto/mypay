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
package it.regioneveneto.mygov.payment.mypay4.config;

import it.regioneveneto.mygov.payment.mypay4.dao.EnteDao;
import it.regioneveneto.mygov.payment.mypay4.dao.GiornaleDao;
import it.regioneveneto.mygov.payment.mypay4.dao.*;
import it.regioneveneto.mygov.payment.mypay4.dao.catalog.CatalogDao;
import it.regioneveneto.mygov.payment.mypay4.dao.common.DbToolsDao;
import it.regioneveneto.mygov.payment.mypay4.dao.fesp.*;
import it.regioneveneto.mygov.payment.mypay4.logging.JdbiSqlLogger;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class JdbiConfiguration {

  @Value("${sql-logging.enabled:false}")
  private String sqlLogginEnabled;

  @Value("${sql-logging.slow.milliseconds:0}")
  private int sqlLogginSlowQueryTresholdMs;


  @Autowired
  JdbiSqlLogger jdbiSqlLogger;

  @Primary
  @Bean("jdbiPa")
  public Jdbi paJdbi(@Qualifier("dsPa") DataSource ds, List<JdbiPlugin> jdbiPlugins, List<RowMapper<?>> rowMappers) {
    return _createJdbi(ds, jdbiPlugins, rowMappers);
  }

  @Bean("jdbiFesp")
  public Jdbi fespJdbi(@Qualifier("dsFesp") DataSource ds, List<JdbiPlugin> jdbiPlugins, List<RowMapper<?>> rowMappers) {
    return _createJdbi(ds, jdbiPlugins, rowMappers);
  }

  private Jdbi _createJdbi (DataSource ds, List<JdbiPlugin> jdbiPlugins, List<RowMapper<?>> rowMappers) {
    TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(ds);
    Jdbi jdbi = Jdbi.create(proxy);
    if(!"false".equalsIgnoreCase(sqlLogginEnabled)) {
      jdbiSqlLogger.setBehaviour(sqlLogginEnabled);
      jdbiSqlLogger.setSlowQueryTresholdMs(sqlLogginSlowQueryTresholdMs);
      jdbi.setSqlLogger(jdbiSqlLogger);
    }
    // Register all available plugins
    String dsString;
    try{
      dsString = ds.getConnection().getMetaData().getURL();
    } catch (Exception e){
      dsString = ds.toString();
    }
    log.debug("Datasource {} - Installing jdbi plugins... ({} found): {}"
        , dsString, jdbiPlugins.size()
        , jdbiPlugins.stream().map(x -> x.getClass().getName()).collect(Collectors.joining(", ")) );
    jdbiPlugins.forEach(jdbi::installPlugin);
    // Register all available rowMappers
    log.debug("Datasource {} - Installing jdbi rowMappers... ({} found): {}"
        , dsString, rowMappers.size()
        , rowMappers.stream().map(x -> x.getClass().getName()).collect(Collectors.joining(", ")) );
    rowMappers.forEach(jdbi::registerRowMapper);
    return jdbi;
  }

  @Bean
  public JdbiPlugin sqlObjectPlugin() {
    return new SqlObjectPlugin();
  }

  @Bean
  public ResourceBundleMessageSource messageSource() {
    var source = new ResourceBundleMessageSource();
    source.setBasenames("messages/messages");
    source.setUseCodeAsDefaultMessage(true);
    return source;
  }


  @Bean
  public EnteDao enteDao(Jdbi jdbi) {
    return jdbi.onDemand(EnteDao.class);
  }

  @Bean
  public AnagraficaStatoDao anagraficaStatoDao(Jdbi jdbi) {
    return jdbi.onDemand(AnagraficaStatoDao.class);
  }

  @Bean
  public DovutoElaboratoDao dovutoElaboratoDao(Jdbi jdbi) { return jdbi.onDemand(DovutoElaboratoDao.class); }

  @Bean
  public DovutoDao dovutoDao(Jdbi jdbi) {
    return jdbi.onDemand(DovutoDao.class);
  }

  @Bean
  public EnteTipoDovutoDao enteTipoDovutoDao(Jdbi jdbi) {
    return jdbi.onDemand(EnteTipoDovutoDao.class);
  }

  @Bean
  public AvvisoDao avvisoDao(Jdbi jdbi) {
    return jdbi.onDemand(AvvisoDao.class);
  }

  @Bean
  public FlussoDao flussiDao(Jdbi jdbi) {
    return jdbi.onDemand(FlussoDao.class);
  }

  @Bean
  public LocationDao locationDao(Jdbi jdbi) {
    return jdbi.onDemand(LocationDao.class);
  }

  @Bean
  public ImportDovutiDao importDovutiDao(Jdbi jdbi) {
    return jdbi.onDemand(ImportDovutiDao.class);
  }

  @Bean
  public ExportDovutiDao exportDovutiDao(Jdbi jdbi) {
    return jdbi.onDemand(ExportDovutiDao.class);
  }

  @Bean
  public UtenteDao utenteDao(Jdbi jdbi) {
    return jdbi.onDemand(UtenteDao.class);
  }

  @Bean
  public ValidazioneEmailDao validazioneEmailDao(Jdbi jdbi) {
    return jdbi.onDemand(ValidazioneEmailDao.class);
  }

  @Bean
  public AvvisoDigitaleDao avvisoDigitaleDao(Jdbi jdbi) {
    return jdbi.onDemand(AvvisoDigitaleDao.class);
  }

  @Bean
  public EnteFunzionalitaDao enteFunzionalitaDao(Jdbi jdbi) {
    return jdbi.onDemand(EnteFunzionalitaDao.class);
  }

  @Bean
  public CarrelloDao carrelloDao(Jdbi jdbi) {
    return jdbi.onDemand(CarrelloDao.class);
  }

  @Bean
  public CarrelloMultiBeneficiarioDao carrelloMultiBeneficiarioDao(Jdbi jdbi) { return jdbi.onDemand(CarrelloMultiBeneficiarioDao.class); }

  @Bean
  public GiornaleDao giornaleDao(Jdbi jdbi) { return jdbi.onDemand(GiornaleDao.class); }

  @Bean
  public FlussoAvvisoDigitaleDao flussoAvvisoDigitaleDao(Jdbi jdbi) {
    return jdbi.onDemand(FlussoAvvisoDigitaleDao.class);
  }

  @Bean
  public EsitoAvvisoDigitaleDao esitoAvvisoDigitaleDao(Jdbi jdbi) {
    return jdbi.onDemand(EsitoAvvisoDigitaleDao.class);
  }

  @Bean
  public OperatoreDao operatoreDao(Jdbi jdbi) {
    return jdbi.onDemand(OperatoreDao.class);
  }

  @Bean
  public OperatoreEnteTipoDovutoDao operatoreEnteTipoDovutoDao(Jdbi jdbi) {
    return jdbi.onDemand(OperatoreEnteTipoDovutoDao.class);
  }

  @Bean
  public DovutoCarrelloDao dovutoCarrelloDao(Jdbi jdbi) {
    return jdbi.onDemand(DovutoCarrelloDao.class);
  }

  @Bean
  public IdentificativoUnivocoDao identificativoUnivocoDao(Jdbi jdbi) {
    return jdbi.onDemand(IdentificativoUnivocoDao.class);
  }

  @Bean
  public DatiMarcaBolloDigitaleDao datiMarcaBolloDigitaleDao(Jdbi jdbi) {
    return jdbi.onDemand(DatiMarcaBolloDigitaleDao.class);
  }

  @Bean
  public TassonomiaDao tassonomiaDao(Jdbi jdbi) {
    return jdbi.onDemand(TassonomiaDao.class);
  }

  @Bean
  public PushEsitoSilDao pushEsitoSilDao(Jdbi jdbi) {
    return jdbi.onDemand(PushEsitoSilDao.class);
  }

  @Bean
  public EnteSilDao enteSilDao(Jdbi jdbi) {
    return jdbi.onDemand(EnteSilDao.class);
  }

  @Bean
  public FlussoTassonomiaDao flussoTassonomiaDao(Jdbi jdbi) {
    return jdbi.onDemand(FlussoTassonomiaDao.class);
  }

  @Bean
  public RegistroOperazioneDao registroOperazioneDao(Jdbi jdbi) {
    return jdbi.onDemand(RegistroOperazioneDao.class);
  }

  @Bean
  public ProgressiviVersamentoDao fespProgressiviVersamentoDao(@Qualifier("jdbiFesp")Jdbi jdbi) {
    return jdbi.onDemand(ProgressiviVersamentoDao.class);
  }

  @Bean
  public TipiVersamentoDao fespTipiVersamentoDao(@Qualifier("jdbiFesp")Jdbi jdbi) {
    return jdbi.onDemand(TipiVersamentoDao.class);
  }

  @Bean
  public it.regioneveneto.mygov.payment.mypay4.dao.fesp.EnteDao fespEnteDao(@Qualifier("jdbiFesp")Jdbi jdbi) {
    return jdbi.onDemand(it.regioneveneto.mygov.payment.mypay4.dao.fesp.EnteDao.class);
  }

  @Bean
  public FlussoQuadSpcDao fespFlussoQuadSpcDao(@Qualifier("jdbiFesp")Jdbi jdbi) {
    return jdbi.onDemand(FlussoQuadSpcDao.class);
  }

  @Bean
  public FlussoRendSpcDao fespFlussoRendSpcDao(@Qualifier("jdbiFesp")Jdbi jdbi) {
    return jdbi.onDemand(FlussoRendSpcDao.class);
  }

  @Bean
  public it.regioneveneto.mygov.payment.mypay4.dao.fesp.GiornaleDao fespGiornaleDao(@Qualifier("jdbiFesp")Jdbi jdbi) {
    return jdbi.onDemand(it.regioneveneto.mygov.payment.mypay4.dao.fesp.GiornaleDao.class);
  }

  @Bean
  public AttivaRptDao attivaRptDao(@Qualifier("jdbiFesp")Jdbi jdbi) {
    return jdbi.onDemand(AttivaRptDao.class);
  }

  @Bean
  public RptRtDao rptRtDao(@Qualifier("jdbiFesp")Jdbi jdbi) { return jdbi.onDemand(RptRtDao.class); }

  @Bean
  public CarrelloRptDao carrelloRptDao(@Qualifier("jdbiFesp")Jdbi jdbi) { return jdbi.onDemand(CarrelloRptDao.class); }

  @Bean
  public CarrelloRpDao carrelloRpDao(@Qualifier("jdbiFesp")Jdbi jdbi) { return jdbi.onDemand(CarrelloRpDao.class); }

  @Bean
  public RpEDao rpEDao(@Qualifier("jdbiFesp")Jdbi jdbi) { return jdbi.onDemand(RpEDao.class); }

  @Bean
  public RptRtDettaglioDao rptRtDettaglioDao(@Qualifier("jdbiFesp")Jdbi jdbi) { return jdbi.onDemand(RptRtDettaglioDao.class); }

  @Bean
  public RpEDettaglioDao rpEDettaglioDao(@Qualifier("jdbiFesp")Jdbi jdbi) { return jdbi.onDemand(RpEDettaglioDao.class); }


  @Bean("paCatalogDao")
  public CatalogDao paCatalogDao(@Qualifier("jdbiPa")Jdbi jdbi) { return jdbi.onDemand(CatalogDao.class); }

  @Bean("fespCatalogDao")
  public CatalogDao fespCatalogDao(@Qualifier("jdbiFesp")Jdbi jdbi) { return jdbi.onDemand(CatalogDao.class); }

  @Bean
  public it.regioneveneto.mygov.payment.mypay4.dao.fesp.LockDao fespLockDao(@Qualifier("jdbiFesp")Jdbi jdbi) {
    return jdbi.onDemand(it.regioneveneto.mygov.payment.mypay4.dao.fesp.LockDao.class);
  }

  @Bean
  public StandardTipoDovutoDao standardEnteTipoDovutoDao(Jdbi jdbi) {
    return jdbi.onDemand(StandardTipoDovutoDao.class);
  }

  @Bean
  public GiornaleElapsedDao giornaleElapsedDao(@Qualifier("jdbiFesp")Jdbi jdbi) { return jdbi.onDemand(GiornaleElapsedDao.class); }

  @Bean("paDbToolsDao")
  public DbToolsDao paDbToolsDao(Jdbi jdbi) {
    return jdbi.onDemand(DbToolsDao.class);
  }

  @Bean("fespDbToolsDao")
  public DbToolsDao fespDbToolsDao(@Qualifier("jdbiFesp")Jdbi jdbi) {
    return jdbi.onDemand(DbToolsDao.class);
  }

}