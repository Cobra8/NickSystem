package me.Cobra_8.objects.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.Cobra_8.NickSystem;

/**
 *
 * @author Cobra_8
 */
public class MySQL {

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String database;
    private Connection conn;

    public MySQL(String host, int port, String user, String password, String database) throws SQLException {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;

        this.conn = openConnection();
    }

    public MySQL() throws SQLException {
        String pf = "MySQL.";
        this.host = NickSystem.getInstance().getConfiguration().getString(pf + "host");
        this.port = NickSystem.getInstance().getConfiguration().getInt(pf + "port");
        this.user = NickSystem.getInstance().getConfiguration().getString(pf + "user");
        this.password = NickSystem.getInstance().getConfiguration().getString(pf + "password");
        this.database = NickSystem.getInstance().getConfiguration().getString(pf + "database");

        this.conn = openConnection();
    }

    public void execute(String query) {
        if (!(checkConnection())) {
            return;
        }
        Statement st = null;
        try {
            st = getConnection().createStatement();
            st.execute(query);
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeRessources(null, st);
        }
    }

    public void executeUpdate(String query) {
        if (!(checkConnection())) {
            return;
        }
        Statement st = null;
        try {
            st = getConnection().createStatement();
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeRessources(null, st);
        }
    }

    public ResultSet executeQuery(String query) {
        if (!(checkConnection())) {
            return null;
        }
        Statement st;
        ResultSet rs = null;
        try {
            st = getConnection().createStatement();
            rs = st.executeQuery(query);
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    private Connection openConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            System.err.println("Treiber nicht gefunden !");
        }
        String url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true";
        return DriverManager.getConnection(url, this.user, this.password);
    }

    public Connection getConnection() {
        return this.conn;
    }

    public void setConncetion(Connection conn) {
        this.conn = conn;
    }

    public boolean checkConnection() {
        if (!hasConnection()) {
            try {
                this.conn = openConnection();
                return true;
            } catch (SQLException ex) {
                return false;
            }
        }
        return true;
    }

    public boolean hasConnection() {
        try {
            return this.conn.isValid(2) && !this.conn.isClosed();
        } catch (SQLException ex) {
            return false;
        }
    }

    public void closeRessources(ResultSet rs, Statement st) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeConnection() {
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.conn = null;
        }
    }
}
