package it.corsojava.cashreg;

import it.corsojava.cashreg.core.RegistratoreScontrini;
import it.corsojava.cashreg.core.exceptions.RegistratoreLoadException;
import it.corsojava.cashreg.core.implementation.IdGenerator;
import it.corsojava.cashreg.core.implementation.RegistratoreScontriniImpl;
import it.corsojava.cashreg.core.implementation.StoreEngine;
import it.corsojava.cashreg.core.implementation.engine.JdbcService;
import it.corsojava.cashreg.core.implementation.engine.JdbcStoreEngine;
import it.corsojava.cashreg.core.implementation.exceptions.StoreEngineException;
import it.corsojava.cashreg.core.implementation.exceptions.StoreEngineInitException;
import it.corsojava.cashreg.ui.*;

public class Main {
    public static void main(String[] args) {

        TerminalUi ui = new TerminalUi(new TerminalStdFormats(),System.in, System.out);
        ScontrinoPrinter printer=new ScontrinoBasicPrinter();

        // TODO implementare concretamente StoreEngine nel package it.corsojava.cashreg.implementation.engine
        // l'implementazione dovà salvare i dati all'interno di un RDBMS a piacimento tra MySql o PostgreSql

        JdbcService.setUrl("jdbc:mysql://localhost/cashreg");
        JdbcService.setUserName("corsosql");
        JdbcService.setUserPass("corsosql");

        StoreEngine engine = new JdbcStoreEngine();

        RegistratoreScontrini registratore = null;
        try {
            registratore = new RegistratoreScontriniImpl(engine);
        } catch (StoreEngineException e) {
            ui.writeln("Si e' verificato un problema durante l'avvio dei servizi del programma. Impossibile proseguire");
            e.printStackTrace();
            return;
        } catch (RegistratoreLoadException e) {
            ui.writeln("Si e' verificato un problema durante l'avvio dei servizi del programma. Impossibile proseguire");
            e.printStackTrace();
            return;
        }

        TerminalUiAgent agent=new TerminalUiAgent(registratore);
        agent.setTerminalUi(ui);
        agent.setPrinter(printer);

        try {
            agent.run();
        } catch (UiAgentStartupException e) {
            ui.writeln("Si e' verificato un problema durante l'avvio dei servizi del programma. Impossibile proseguire");
            e.printStackTrace();
            return;
        }

    }
}
