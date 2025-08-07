package velocidadgestores;

import java.sql.*;

public class ConexionMySqlserver implements ConexionBaseDeDatos {
    private Connection conexionBD;

    @Override
    public Connection getConexion() {
        return conexionBD;
    }

    @Override
    public void conectar() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String baseDeDatos = "jdbc:mysql://localhost:3306/BDPRODUCTO";
            String user = "root";
            String password = "mysql123";

            conexionBD = DriverManager.getConnection(baseDeDatos, user, password);

            if (conexionBD != null) {
                // --- CORRECCIÓN AQUÍ: SE COMENTA LA LÍNEA PARA NO DESACTIVAR EL AUTOCOMMIT ---
                // Para pruebas de velocidad con hilos, es mejor dejar el autocommit activado.
                // conexionBD.setAutoCommit(false);
                System.out.println("✅ Conectado a MySQL correctamente.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Error: No se encontró el driver de MySQL.\n" + e.getMessage(), e);
        }
    }

    @Override
    public boolean ejecutar(String sql) throws SQLException {
        Statement sentencia = null;
        boolean resultado = false;

        try {
            if (getConexion() == null || getConexion().isClosed()) {
                throw new SQLException("Error: La conexión a la base de datos no está establecida o está cerrada.");
            }

            sentencia = getConexion().createStatement();
            
            // Si el procedimiento en MySQL devuelve un resultado, el `executeUpdate`
            // podría fallar. El `execute` es más seguro. Sin embargo, para `CALL`
            // en MySQL, a menudo `executeUpdate` funciona si no se esperan resultados.
            // Si el problema persiste, considera cambiarlo a `sentencia.execute(sql);`
            sentencia.executeUpdate(sql);
            
            // --- CORRECCIÓN AQUÍ: SE COMENTA LA LÍNEA DE COMMIT ---
            // Con autocommit activado, no se necesita commit manual.
            // getConexion().commit();
            
            resultado = true;
            System.out.println("✅ SQL ejecutado en MySQL: " + sql);

        } catch (SQLException e) {
            // --- CORRECCIÓN AQUÍ: SE COMENTA LA LÍNEA DE ROLLBACK ---
            // Con autocommit, no se necesita rollback manual.
            // if (getConexion() != null) {
            //     try {
            //         getConexion().rollback();
            //     } catch (SQLException rollbackEx) {
            //         System.err.println("Error al realizar el rollback: " + rollbackEx.getMessage());
            //     }
            // }
            System.err.println("❌ Error al ejecutar SQL en MySQL: " + e.getMessage());
            throw e; // re-lanzamos para que el hilo pueda capturarlo
        } finally {
            if (sentencia != null) {
                try {
                    sentencia.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar el Statement: " + e.getMessage());
                }
            }
        }
        return resultado;
    }

    @Override
    public void cerrarConexion() {
        if (conexionBD != null) {
            try {
                conexionBD.close();
                System.out.println("🔒 Conexión MySQL cerrada.");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}