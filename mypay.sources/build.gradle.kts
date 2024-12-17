plugins {
  java
  //license header
  id("com.github.hierynomus.license") version "0.16.1"
}

sourceSets {
  main {
    resources {
      srcDir(".")
      include(
        "mypay4-be/*",
        "mypay4-be/src/**",
        "mypay4-fe/*",
        "mypay4-fe/projects/**",
        //"db/**",
        //"mypay4-batch/**",
      )
      exclude(
        "**/.*/**",
        "mypay4-be/src/main/resources/wsdl/fesp/envelope.xsd",
        "mypay4-be/src/main/resources/wsdl/fesp/nodo-regionale-per-nodo-spc.wsdl",
        "mypay4-be/src/main/resources/wsdl/fesp/nodo-sac-common-types-1.0.xsd",
        "mypay4-be/src/main/resources/wsdl/fesp/nodo-spc-per-nodo-regionale.wsdl",
        "mypay4-be/src/main/resources/wsdl/fesp/NodoPerPaAvvisiDigitali.wsdl",
        "mypay4-be/src/main/resources/wsdl/fesp/paForNode.wsdl",
        "mypay4-be/src/main/resources/wsdl/fesp/PagInf_RPT_RT_6_2_0.xsd",
        "mypay4-be/src/main/resources/wsdl/fesp/RR_ER_1_0_0.xsd",
        "mypay4-be/src/main/resources/wsdl/fesp/sac-common-types-1.0.xsd",
        "mypay4-be/src/main/resources/wsdl/fesp/soap-envelope.xsd",
        // fe
        "mypay4-fe/node_modules/**",
        "mypay4-fe/dist/**",
        "mypay4-fe/.idea/**",
        "mypay4-fe/version.ts",
        "mypay4-fe/projects/mypay4-fe-common/assets/cookiebar/**",
        // batch
        "mypay4-batch/pa-batch/AVVISATURA_BATCH_PA/**",
        "mypay4-batch/pa-batch/LOAD_PA/**",
        "mypay4-batch/pa-batch/PA_TALEND/**",
        "mypay4-batch/pa-batch/TEMPLATE_COMUNE/**",
        "mypay4-batch/pa-batch/UTILITY/**",
        "mypay4-batch/pa-batch/RELEASE/batch/jobs/**",
        "mypay4-batch/pa-batch/RELEASE/batch/xsd/avvisi-digitali-1.0.xsd",
        "mypay4-batch/pa-batch/RELEASE/batch/xsd/FlussoRiversamento.xsd",
        "mypay4-batch/pa-batch/RELEASE/batch/xsd/presa-in-carico-1.0.xsd",
        "mypay4-batch/pa-batch/RELEASE/batch/xsd/sac-common-types-1.0.xsd",
        "mypay4-batch/fesp-batch/BATCH_INVIO_SFTP/**",
        "mypay4-batch/fesp-batch/NODO_REGIONALE_FESP_TALEND/**",
        "mypay4-batch/fesp-batch/RELEASE/batch/jobs/**",
        "mypay4-batch/fesp-batch/RELEASE/batch/xsd/FlussoRiversamento.xsd",
      )
    }
  }
}

license {
  header = rootProject.file("LICENSE_HEADER")
  strictCheck = true
  mapping(mapOf(
    Pair("wsdl", "XML_STYLE"),
    Pair("xjb", "XML_STYLE"),
    Pair("jrxml", "XML_STYLE"),
    Pair("kts", "SLASHSTAR_STYLE"),
    Pair("ts", "SLASHSTAR_STYLE"),
    Pair("css", "JAVADOC_STYLE"),
    Pair("scss", "JAVADOC_STYLE"),
  ))
  includes(listOf(
    "**/*.kts",
    "**/*.java",
    "**/*.sql",
    "**/*.wsdl",
    "**/*.xsd",
    "**/*.xjb",
    "**/*.properties",
    "**/*.jrxml",
    "**/*.ts",
    "**/*.html",
    "**/*.ts",
    "**/*.scss",
  ))
  ext.set("year", "2022") //Calendar.getInstance().get(Calendar.YEAR))
  ext.set("name", "Regione Veneto")
  ext.set("desc", "MyPay - Payment portal of Regione Veneto.")
}