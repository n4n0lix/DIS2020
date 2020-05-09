package de.dis.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Einfaches Singleton zur Verwaltung von Datenbank-Verbindungen.
 *
 * @author Michael von Riegen
 * @version April 2009
 */
public class DBConnectionManager {

    // instance of Driver Manager
    private static DBConnectionManager _instance = null;

    // DB connection
    private Connection _con;

    /**
     * Erzeugt eine Datenbank-Verbindung
     */
    private DBConnectionManager() {
        try {
            // Holen der Einstellungen aus der db.properties Datei
            Properties properties = new Properties();
            URL url = ClassLoader.getSystemResource("db.properties");
            FileInputStream stream = new FileInputStream(new File(url.toURI()));
            properties.load(stream);
            stream.close();

            String jdbcUser = properties.getProperty("jdbc_user");
            String jdbcPass = properties.getProperty("jdbc_pass");
            String jdbcUrl = properties.getProperty("jdbc_url");

            // Verbindung zur DB herstellen
            Class.forName("com.ibm.db.jcc.DBDriver");
            _con = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);


        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    /**
     * Liefert Instanz des Managers
     *
     * @return DBConnectionManager
     */
    public static DBConnectionManager getInstance() {
        if (_instance == null) {
            _instance = new DBConnectionManager();
        }
        return _instance;
    }

    /**
     * Liefert eine Verbindung zur DB zurC<ck
     *
     * @return Connection
     */
    public Connection getConnection() {
        return _con;
    }

}