/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocidadgestores;

import java.sql.*;

/**
 *
 * @author MCROBERTW
 */
public class ConexionSqlserver implements ConexionBaseDeDatos { // <-- AQUI SE IMPLEMENTA LA INTERFAZ
    private Connection conexionBD;

    @Override // Anotación opcional pero recomendada
    public Connection getConexion() {
        return conexionBD;
    }

    @Override // Anotación opcional pero recomendada
    public void conectar() throws SQLException {
        try {
            // Carga el driver JDBC para SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            // Configuración de la conexión a la base de datos SQL Server
            String baseDeDatos = "jdbc:sqlserver://localhost:1433;databaseName=BDPRODUCTO;integratedSecurity=true";
            
            // Establece la conexión
            conexionBD = DriverManager.getConnection(baseDeDatos);
            
            // --- CORRECCIÓN AQUÍ: SE COMENTA LA LÍNEA PARA NO DESACTIVAR EL AUTOCOMMIT ---
            // Dejar el autocommit en true (por defecto) es crucial para evitar
            // bloqueos entre hilos en pruebas de velocidad.
            // conexionBD.setAutoCommit(false);
            
        } catch (ClassNotFoundException e) {
            // Se encapsula la excepción en una SQLException para que el hilo la capture
            throw new SQLException("Error: No se encontró el driver de SQL Server.\n" + e.getMessage(), e);
        }
    }
    
    @Override // Anotación opcional pero recomendada
    public boolean ejecutar(String sql) throws SQLException {
        Statement sentencia = null; // Inicializamos la sentencia como null
        boolean resultado = false;

        try {
            // Verificamos si la conexión es nula o está cerrada antes de usarla
            if (getConexion() == null || getConexion().isClosed()) {
                throw new SQLException("Error: La conexión a la base de datos no está establecida o está cerrada.");
            }
            
            // Crear el Statement
            sentencia = getConexion().createStatement();
            
            // Ejecuta la sentencia SQL (INSERT, UPDATE, DELETE)
            sentencia.executeUpdate(sql);
            
            // --- CORRECCIÓN AQUÍ: SE COMENTA LA LÍNEA DE COMMIT ---
            // Con autocommit activado, no se necesita commit manual.
            // getConexion().commit();
            
            resultado = true;
            
        } catch (SQLException e) {
            // --- CORRECCIÓN AQUÍ: SE COMENTA LA LÍNEA DE ROLLBACK ---
            // Con autocommit, no es necesario un rollback manual.
            // if (getConexion() != null) {
            //     try {
            //         getConexion().rollback();
            //     } catch (SQLException rollbackEx) {
            //         System.err.println("Error al realizar el rollback: " + rollbackEx.getMessage());
            //     }
            // }
            throw e; // Re-lanzar la excepción para que el hilo la capture
        } finally {
            // Aseguramos que el Statement se cierre siempre para liberar recursos
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

    @Override // Anotación opcional pero recomendada
    public void cerrarConexion() {
        if (conexionBD != null) {
            try {
                conexionBD.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}