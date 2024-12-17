


# MyPay


## INTRODUZIONE


Il decreto di semplificazione “Switch off PagoPA e strumenti di pagamento elettronico – Artt. 24 e 24-bis, D.L. 76/2020 (art. 65, D.Lgs. 217/2017)”  cita l’obbligo per i prestatori di servizi di pagamento abilitati all’utilizzo della piattaforma PagoPA per i pagamenti verso le PA di dotarsi di una piattaforma evoluta in grado di soddisfare i requisiti d'integrazione con Nodo Centrale Nazionale dei Pagamenti  e dettagliati nelle SANP  periodicamente aggiornate e pubblicate da PagoPA.

Regione del Veneto ha aderito al Sistema dei pagamenti elettronici pagoPA, in qualità di intermediario tecnologico per tutti gli enti veneti. Per lo svolgimento di questa funzione di Intermediario Tecnologico da parte di un soggetto Ente Pubblico come potrebbe essere una Regione, è stato realizzato un progetto per implementare un’ apposita piattaforma territoriale dei pagamenti elettronici, MyPay, che garantisce omogeneità di utilizzo del sistema da parte dei cittadini e delle imprese sul territorio e consente a tutti gli enti che hanno aderito di operare con un unico sistema di intermediazione con il nodo nazionale “PagoPa”.
Tale piattaforma consente la gestione dei pagamenti e regolarizzazione e riconciliazione delle posizioni debitorie tale da soddisfare i requisiti imposti dalle linee guida indicate da PagoPa, e rendere disponibili funzionalità dedicate ai cittadini e agli operatori degli enti per la verifica e gestione delle informazioni relative alle posizioni debitorie pagate e non pagate da parte di Enti (anche se non riscossori) 

_Obiettivi del progetto_

Obiettivo del progetto MyPay4 è rendere disponibile una piattaforma dedicata al mondo dei pagamenti in grado di soddisfare le esigenze di gestione dei pagamenti degli enti che necessitano di

- esporre ai cittadini le eventuali posizioni debitorie a loro carico per poterle consultare ed eventualmente pagare tramite l’integrazione con col Nodo Nazionale dei Pagamenti PagoPA

- gestire le posizioni debitorie pagate tramite il Nodo Nazionale dei Pagamenti PagoPA e conseguentemente da regolarizzare e riconciliare con i sistemi interno dell’ente stesso.

Col progetto si vogliono offrire servizi compatibili con le linee guida Agid in termini di adempienza agli SLA, all’accessibilità (UNI-en 301549-2018), all’enhancement della piattaforma in termini di usabilità e user experience per i cittadini, gli operatori e gli Enti.

Il progetto si basa sulle Specifiche Attuative del Nodo dei Pagamenti – SPC Versione 3.6 

Il sistema è fortemente orientata al mondo cloud e incentrata su concetti di scalabilità orizzontale e verticale al fine di potersi adattare in modo dinamico ed automatico ai crescenti carichi previsti vista la forte spinta all’utilizzo di strumenti di pagamenti telematici integrati con PagoPa.

Quindi se opportunamente deployato in ambiente clusterizzato e dockerizzato ha la possibilità di scalare in funzione del carico previsto e fortemente variabile in concomitanza di periodi temporali particolari per scadenze di pagamento previste dalla normativa nazionale.

La soluzione Suite dei pagamenti MyPay4/MyPivot4 è composta dalle seguenti componenti:


 - L'**applicazione MyPayCittadino App** orientata agli utenti cittadini che a seconda che si siano autenticati o meno potranno  avere a disposizione funzionalità dedicate. L'applicazione prevede sia un'area pubblica con alcune funzionalità che non necessitano di autenticazione per poter essere erogate e un'area privata nella quale previa autenticazione i cittadini avranno a disposizione funzionalità dedicate e incentrate sui propri dati personali. 
 
 - L'**applicazione MyPayOperatore App** orientata agli operatori sia degli Enti Locali EELL intermediati sia dell'Ente Intermediario. L'applicazione prevede che in funzione del proprio profilo applicativo ogni operatore possa usufruire di  diverse funzionalità per la gestione delle posizioni debitorie da pagare e pagate nell'ambito circoscritto alle fasi propedeutiche al pagamento e alla sua finalizzazione.


**Funzionalità Applicative**

Le macrocategorie di funzionalità applicative implementate nel progetto e incentrate sui pagamenti di posizioni debitorie varie e loro gestione sia in termini di pagamento effettuato tramite interazione PagoPA sia in termini di regolarizzazione e riconciliazione delle posizioni debitorie pagate e rendicontate da PAgoPA sono di seguito elencate e successivamente sinteticamente descritte:

 - **Funzionalità del cittadino autenticato.**
 - **Funzionalità del cittadino non autenticato.**
 - **Funzionalità di backoffice per l'operatore dell' ente intermediato in ambito pagamenti**
 - **Funzionalità di backoffice per l'operatore amministratore dei pagamenti dell' ente intermediato**
 - **Funzionalità di backoffice per l'amministratore dell' ente intermediario**

 **Funzionalità del cittadino autenticato**

Le funzionalità del cittadino autenticato sono funzioni che consentono al cittadino di avere una visione complessiva di tutte le posizioni debitorie aperte (in attesa di essere pagate), tramite avvisi di pagamento o dovuti spontanei che afferiscono al cittadino autenticato anche se di Enti diversi con indicazione per ogni posizione debitoria dell’Ente creditore.

Le funzioni introdotte facilitano l’attività del cittadino che può procedere attraverso opportuni filtri di ricerca all’individuazione dei dovuti in attesa di essere pagati o di avvisi di pagamento di interesse e al rispettivo pagamento di più dovuti in un’unica soluzione.

 - E’ possibile scaricare l’avviso di pagamento per procedere con il pagamento e le ricevute telematiche per conservarle su archivio locale.

 - La funzione di visualizzazione dello storico dei pagamenti consente di individuare le posizioni pagate per cui si vuole procedere con una verifica e visualizzare le informazioni di dettaglio del pagamento.

 - E’ disponibile una lista degli Enti per i quali è possibile effettuare pagamenti spontanei o altri pagamenti

 - E’ disponibile la funzione di gestione del carrello multi-Ente per inserire/rimuovere i dovuti spontanei e procedere al loro pagamento in un’unica transazione.

 - Nel wizard di compilazione dei dati necessari per procedere con il pagamento vi è la possibilità di differenziare l’intestatario dal versante se rappresentati da due soggetti diversi ed inviare mail di riepilogo del pagamento ad entrambi.

 - MyPay4 consente di procedere con il modello 3 del pagamento e verificare in tempo reale, prima di procedere con il pagamento, l’esistenza dell’importo del dovuto e la sua pagabilità presso gli archivi dell’Ente che ha generato la posizione debitoria e che utilizza MyPay.

**Funzionalità del cittadino non autenticato**

 - Il cittadino autenticato può visualizzare le tipologie di pagamenti (spontaneo o tramite avviso) per effettuare un pagamento.

 - Il cittadino può scaricare l'avviso di pagamento in formato PDF con tutte le informazioni corrette per stamparlo e pagarlo presso un PSP fisico.

 - Nel wizard di compilazione dei dati di pagamento per un avviso di pagamento sarà possibile inserire l’indirizzo e-mail dell’intestatario e del versante, se diversi, a cui inviare la ricevuta del pagamento effettuato.

 - Nel caso siano presenti su enti diversi, pagamenti con stesso IUV e stesso codice fiscale, sarà  possibile selezionare l'Ente Creditore per cui si intende effettuare il pagamento.

 - Al termine della compilazione dei dati di pagamento sarà sempre proposta un riepilogo dei dati inseriti e reindirizzati sui sistemi di PagoPA per finalizzare il pagamento.

**Funzionalità di backoffice per l’operatore dell’Ente Intermediato in ambito pagamenti**

 - L’accesso al sistema dei pagamenti da parte dell’operatore rendirizza l’operatore nella propria area personale, dove, se non gestito tramite un backoffice centralizzato l’accesso e la selezione dell’Ente per cui operare, sarà disponibile la lista degli Enti Amministrati con possibilità di selezionare l’Ente Amministrato per cui si intende operare.

 - Effettuato l’accesso, l’operatore avrà a disposizione le funzioni per cui è abilitato con cui può procedere a gestire la :

	 - gestione flussi
	 - gestione dovuti
	 - la gestione della regolarizzazione e riconciliazione dei pagamenti tramite funzionalità messe a disposizione dalla componente applicativa a MyPivot parte integrante della suite per la gestione dei pagamenti telematici

 - La funzione di “gestione flussi” mette a disposizione quattro funzionalità: import flussi, export flussi, flussi di rendicontazione.

 - “Import Flussi” – consente di eseguire l'upload dei flussi di import per caricare le posizioni debitorie sull'applicativo

 - “Export Flussi” – consente di visualizzare ed effettuare il download di flussi generati da MyPay per visualizzare i dovuti pagati dagli utenti.

 - “Flussi di Rendicontazione” – consente di visualizzare ed effettuare il download dei flussi di rendicontazione generati dai PSP

 - La funzione “Gestione dei Dovuti” permette di visualizzare e modificare i singoli dovuti caricati tramite flussi di importazione o manualmente dall’utente (dovuti spontanei) che hanno l’Ente corrente come beneficiario.

 - L’operatore potrà effettuare il download per i dovuti pagati e non pagati. Per questi ultimi la ricevuta telematica presenterà l’importo uguale a zero.

 - L’operatore può modificare le informazioni dei dovuti o annullare le posizioni debitorie ed inserire singoli dovuti per permettere al cittadino di pagarli.

Tramite l’accesso a MyPivot e relative funzionalità, l’operatore può gestire la rendicontazione dei flussi di pagamento.


**Funzionalità di backoffice per l'amministratore dell'ente intermediario**

Le funzionalità di backoffice per l’amministratore dell’ente intermediario consentono di:

 - accedere alla lista degli utenti registrati, allo scopo di verificare se un utente è già presente o va censito

 - visualizzare il dettaglio dell'utente ovvero i dati anagrafici dell'utente e la lista delle associazioni dell'utente agli Enti, allo scopo di verificare la configurazione

 - leggere tutti i dati anagrafici del singolo utente, eventualmente acquisiti da un sistema di anagrafe utenti centralizzata, scopo consultazione

 - modificare o rettificare i dati inseriti per l’Utente

 - leggere tutti gli Enti a cui l'utente è associato allo scopo di verificare la configurazione

 - associare e rimuovere l’utente ad un ente perché sia possibile successivamente associargli e abilitargli i dovuti

 - inserire un nuovo Utente nella lista degli Utenti registrati in MyPay, con

	  - acquisizione dei dati anagrafici se c'è integrazione MyPay vs sistema di anagrafe utenti centralizzato; lo scopo dell'inserimento è poter associare il nuovo utente all'Ente e quindi associare e abilitare allo stesso Utente i dovuti

 - leggere la lista degli Enti configurati per verificare se un Ente è già presente o va censito

 - visualizzare il dettaglio dell'Ente allo scopo di avere il quadro completo della configurazione

 - visualizzazione e modifica del logo e dei dati anagrafici dell’Ente

leggere le funzionalità disponibili ed il corrispondente stato attivo/spento per avere il quadro dell'erogazione

 - all'interno del dettaglio Ente, poter svolgere le funzioni di attivazione/disattivazione delle funzionalità, nel caso MyPay non sia integrato con base dati di configurazione Ente centralizzata, alternativamente trovare inibite le azioni di attiva/spegni funzionalità nel caso MyPay sia integrato con base dati di configurazione Ente centralizzata allo scopo di avere il controllo su MyPay dell'erogazione delle funzionalità solamente nel caso MyPay non sia integrato a base dati di configurazione Ente centralizzata

 - all'interno del dettaglio dell’Ente poter leggere la lista dei tipi dovuto che sono stati associati all'Ente allo scopo di verificare quanto configurato

 - all'interno del dettaglio dell’Ente poter abilitare e disabilitare il singolo tipo dovuto configurato per l'Ente per attivare o inibire i relativi pagamenti

 - all'interno del dettaglio dell’Ente poter rimuovere il singolo tipo dovuto presente nella lista tipi dovuto e su cui non ci sono stati ancora pagamenti

 - all'interno del dettaglio dell’Ente poter inserire nella lista dei tipi dovuto configurati sull’Ente un nuovo tipo dovuto

 - all'interno del dettaglio del tipo dovuto configurato sull’Ente, avere disponibili la gestione dei campi per la parametrizzazione del dovuto e la gestione della lista degli operatori associati all'Ente che sono abilitati o meno a trattare il tipo dovuto, il tutto allo scopo di gestire la configurazione del dovuto

 - Lettura dati di configurazione del tipo dovuto configurato sull'ente

 - Modifica dei dati di configurazione del tipo dovuto configurato sull'Ente

 - leggere la lista operatori dell'Ente abilitati ai tipi dovuto configurati sull'Ente

 - aggiungere/rimuovere l'associazione operatore dell'Ente ad un tipo dovuto configurato sull' Ente

 - Abilitazione/disabilitazione operatore associato ad un tipo dovuto configurato sull' Ente

 - Aggiunta operatore alla lista operatori associati all'Ente

 - aggiungere un operatore alla lista operatori associati all'Ente e contestualmente associarlo a tutti gli N dovuti configurati in quel momento sull'ente senza impostare alcuna abilitazione, allo scopo di eseguire le N associazioni in un solo passaggio

 - rimuovere un operatore dalla lista operatori associati all'Ente perché non più necessaria l’associazione

 - associare e rimuovere il ruolo di Amministratore ad un utente presente nella lista operatori associati ad un specifico Ente, allo scopo di abilitargli le relative funzionalità di amministratore

 - definire i permessi per il ruolo di Amministratore dell'Ente da assegnare ad uno o più utenti presenti nella lista degli operatori associati ad un specifico Ente

 - Associa/rimuovi tutti i tipi dovuto ad un operatore associato all'Ente

registrare una mail per ciascuno degli Enti dove l’operatore lavora allo scopo di ricevere separatamente le notifiche del sistema che riguardano un dato Ente

 - inserire un nuovo Ente nella lista degli Enti gestiti, con acquisizione dei dati anagrafici se è presente integrazione di MyPay vs un sistema di anagrafe Enti centralizzato; scopo dell’inserimento Ente è poter procedere con la configurazione tipi dovuti e operatori.


## STRUTTURA DEL REPOSITORY
Il repository git di MyPay ha le seguente struttura:

**/myPay.sources**: E’ la cartella che contiene i sorgenti e gli script gradle per la compilazione la creazione dell’immagine e la pubblicazione sul Repository Nexus.

**/myPay.deploy:** E’ la cartella che contiene i descrittori di base per il dispiegamento su kubernets e gli overlay specifici per ogni ambiente target di deploy.


## I SORGENTI
La cartella **`mypay.sources`** contiene i sorgenti dell'applicazione ed è così strutturata:

`gradle`: contiene file utilizzati per la build tramite Gradle
`mypay-batch`: contiene i sorgenti del processi batch Talend
`mypay-db`: contiene i sorgenti degli script database
`mypay4-be`: contiene i sorgenti del back-end Java Spring Boot myPay Cittadino e myPay Operatore
`mypay4-fe`: contiene i sorgenti del front-end Angular myPay Cittadino e myPay Operatore

## Esecuzione in modalità standalone
Per l'esecuzione in modalità standalone si rimanda al manuale di istallazione presente sotto documentazione e al file INSTALL.md nella cartella `mypay.sources`.

### Prerequisiti Infrastrutturali
La soluzione MyPay necessità di alcuni requisiti Infrastrutturali:

- **Redis** : Per la gestione della cache applicativa

In caso di installazione su K8s ( si veda il documento MI ) sono necessari anche:

- **Cluster K8s** :  Kubernetes (1.7.2 +) con support di container Docker (1.12.x +)
- **Repository Nexus** : Per la pubblicazione di artefatti e immagini docker una volta compilati i sorgenti


### Prerequisiti servizi MyPlace e verticali MyP3
La soluzione MyPay ha dipendenza verso alcuni servizi della piattaforma MyPlace:

- **MyProfile**: Per la gestione delle autorizzazioni e dei profili
- **MyDictionary**: Per la definizione degli xsd per le pagine dei dovuti spontanei

MyPay espone servizi e funzionalità ai SIL degli Enti intermediati attraverso l'esposizione di WS

### Configurazione MyPay
Per poter eseguire lo start dell’applicazione, è importante inserire i puntamenti dei propri ambienti per i componenti da cui dipende MyPay.

### Creazione del file jar

Per la build si rimanda al manuale di istallazione presente sotto documentazione e al file INSTALL.md nella cartella `mypay.sources`.

### Esecuzione dell'applicazione

Per l'esecuzione dell'applicazione in modalità standalone si rimanda al manuale di installazione presente sotto documentazione e al file INSTALL.md nella cartella `mypay.sources`.
 
## Esecuzione su cluster kubernetes

Per l'esecuzione su cluster kubernetes si rimanda al manuale di istallazione presente sotto documentazione.