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
public class ConexionPostgresql implements ConexionBaseDeDatos { // <-- AQUI SE IMPLEMENTA LA INTERFAZ
    private Connection conexionBD;

    @Override // Anotación opcional pero recomendada
    public Connection getConexion() {
        return conexionBD;
    }
    
    @Override // Anotación opcional pero recomendada
    public void conectar() throws SQLException {
        try {
            // Carga el driver JDBC para PostgreSQL
            Class.forName("org.postgresql.Driver");

            // Configuración de la conexión a la base de datos PostgreSQL
            String baseDeDatos = "jdbc:postgresql://localhost:5432/BDPRODUCTO";
            String user = "postgres";
            String password = "123456";
            
            // Establece la conexión
            // --- CORRECCIÓN AQUÍ: Pasa el nombre de usuario (user) en lugar de la URL ---
            conexionBD = DriverManager.getConnection(baseDeDatos, user, password);

            // --- CORRECCIÓN ANTERIOR: SE COMENTA LA LÍNEA PARA NO DESACTIVAR EL AUTOCOMMIT ---
            // Dejar el autocommit en true (por defecto) es lo que permite que las inserciones
            // de hilos separados no se bloqueen mutuamente.
            // conexionBD.setAutoCommit(false);
            
        } catch (ClassNotFoundException e) {
            // Se encapsula la excepción en una SQLException para que el hilo la capture
            throw new SQLException("Error: No se encontró el driver de PostgreSQL.\n" + e.getMessage(), e);
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
            
            // Usamos execute() que es el método genérico que maneja todo tipo de sentencias.
            sentencia.execute(sql);
            
            // --- CORRECCIÓN ANTERIOR: SE COMENTA LA LÍNEA DE COMMIT ---
            // getConexion().commit();
            
            resultado = true;
            
        } catch (SQLException e) {
            // --- CORRECCIÓN ANTERIOR: SE COMENTA LA LÍNEA DE ROLLBACK ---
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
