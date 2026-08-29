package BD;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DataSourceProvider {

    private static volatile DataSource ds;

    private DataSourceProvider() {}

    public static DataSource getDataSource() {
        if (ds == null) {
            synchronized (DataSourceProvider.class) {
                if (ds == null) {
                    ds = createDataSource();
                }
            }
        }
        return ds;
    }

    private static DataSource createDataSource() {
        String url      = lireVariable("JDBC_URL");
        if (url == null) url = "jdbc:postgresql://localhost:5432/gestion_etudiants";

        String user = lireVariable("JDBC_USER");
        if (user == null) user = "admin";

        String password = lireVariable("JDBC_PASSWORD");
        if (password == null) password = "admin123";

        // Charger le driver PostgreSQL
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL non trouvé sur le classpath.", e);
        }

        final String finalUrl = url;
        final String finalUser = user;
        final String finalPass = password;

        // DataSource simple basé sur DriverManager
        return new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                Properties props = new Properties();
                props.setProperty("user", finalUser);
                props.setProperty("password", finalPass);
                return DriverManager.getConnection(finalUrl, props);
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return DriverManager.getConnection(finalUrl, username, password);
            }

            @Override
            public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }

            @Override
            public boolean isWrapperFor(Class<?> iface) { return false; }

            @Override
            public java.io.PrintWriter getLogWriter() { throw new UnsupportedOperationException(); }

            @Override
            public void setLogWriter(java.io.PrintWriter out) { throw new UnsupportedOperationException(); }

            @Override
            public void setLoginTimeout(int seconds) { throw new UnsupportedOperationException(); }

            @Override
            public int getLoginTimeout() { return 0; }

            @Override
            public java.util.logging.Logger getParentLogger() { throw new UnsupportedOperationException(); }
        };
    }

    private static String lireVariable(String nom) {
        String valeur = System.getenv(nom);
        return (valeur == null || valeur.isBlank()) ? null : valeur;
    }

    public static void close() {
        ds = null;
    }
}
