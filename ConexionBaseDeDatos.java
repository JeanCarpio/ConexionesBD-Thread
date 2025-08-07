package velocidadgestores;

import java.sql.Connection;
import java.sql.SQLException; // Importa la clase SQLException

public interface ConexionBaseDeDatos {
    Connection getConexion();
    void conectar() throws SQLException; // <-- ¡Añade esta parte!
    boolean ejecutar(String sql) throws SQLException; // <-- ¡Añade esta parte!
    void cerrarConexion(); // Aunque este método puede lanzar SQLException, no es un error si no se declara en la interfaz. Sin embargo, por consistencia, es una buena práctica.
}