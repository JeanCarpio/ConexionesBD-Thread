package velocidadgestores;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class VentanaPrincipal2 extends JFrame {
    private final JTextArea areaResultados;
    private final JButton btnIniciar;
    private final int numeroInserciones = 15500;
    private final int totalHilos = 1000;
    private final AtomicInteger hilosFinalizados = new AtomicInteger(0);

    public VentanaPrincipal2() {
        setTitle("Prueba de Inserciones con Thread");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        areaResultados = new JTextArea();
        areaResultados.setEditable(false);

        btnIniciar = new JButton("Iniciar Pruebas");
        btnIniciar.addActionListener(e -> iniciarPruebas());

        setLayout(new BorderLayout());
        add(new JScrollPane(areaResultados), BorderLayout.CENTER);
        add(btnIniciar, BorderLayout.SOUTH);
    }

    private void iniciarPruebas() {
        areaResultados.setText("");
        areaResultados.append("🚀 Iniciando prueba de inserciones con Thread...\n");
        btnIniciar.setEnabled(false);
        hilosFinalizados.set(0);

        try {
            // MySQL
            Thread hiloMySQL = new Thread(new InsercionThread("MySQL", this, numeroInserciones));
            hiloMySQL.start();
            hiloMySQL.join();

            // Oracle
            Thread hiloOracle = new Thread(new InsercionThread("Oracle", this, numeroInserciones));
            hiloOracle.start();
            hiloOracle.join();

            // PostgreSQL
            Thread hiloPostgres = new Thread(new InsercionThread("PostgreSQL", this, numeroInserciones));
            hiloPostgres.start();
            hiloPostgres.join();

            // SQL Server
            Thread hiloSQLServer = new Thread(new InsercionThread("SQL Server", this, numeroInserciones));
            hiloSQLServer.start();
            hiloSQLServer.join();

        } catch (InterruptedException e) {
            mostrarResultado("⚠ Error: ejecución interrumpida - " + e.getMessage());
        }

        mostrarResultado("🎉 Todas las pruebas han finalizado.");
        btnIniciar.setEnabled(true);
    }

    public void mostrarResultado(String mensaje) {
        SwingUtilities.invokeLater(() -> areaResultados.append(mensaje + "\n"));
    }
    
    public void hiloFinalizado(String mensaje) {
        mostrarResultado(mensaje);
        hilosFinalizados.incrementAndGet();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal2().setVisible(true));
    }

    private static class InsercionThread implements Runnable {
        private final String gestor;
        private final VentanaPrincipal2 ventana;
        private final int numeroInserciones;

        public InsercionThread(String gestor, VentanaPrincipal2 ventana, int numeroInserciones) {
            this.gestor = gestor;
            this.ventana = ventana;
            this.numeroInserciones = numeroInserciones;
        }

        @Override
        public void run() {
            ventana.mostrarResultado("✔ Hilo para " + gestor + " iniciado.");
            ContadorTiempo contador = new ContadorTiempo();
            ConexionBaseDeDatos conexion = null;
            String sql = "";

            // SQL para cada gestor
            switch (gestor) {
                case "MySQL":
                    conexion = new ConexionMySqlserver();
                    sql = "CALL PA_INSERTARPRODUCTO('1','ProductoMySQL',10,20)";
                    break;
                case "Oracle":
                    conexion = (ConexionBaseDeDatos) new ConexionOracle();
                    sql = "CALL PA_INSERTARPRODUCTO('2','ProductoOracle',15,25)";
                    break;
                case "PostgreSQL":
                    conexion = (ConexionBaseDeDatos) new ConexionPostgresql();
                    sql = "SELECT PA_INSERTARPRODUCTO('3','ProductoPostgres',30,40)"; // o CALL si ya es PROCEDURE
                    break;
                case "SQL Server":
                    conexion = new ConexionSqlserver();
                    sql = "EXECUTE PA_INSERTARPRODUCTO '4','ProductoSQLServer',50,60";
                    break;
            }

            String mensajeResultado;
            try {
                if (conexion != null) {
                    conexion.conectar();
                    contador.iniciar();

                    for (int i = 0; i < numeroInserciones; i++) {
                        try {
                            conexion.ejecutar(sql);
                        } catch (SQLException ex) {
                            String errorMsg = "❌ Error en " + gestor + ": " + ex.getMessage();
                            ventana.mostrarResultado(errorMsg);
                            System.out.println(errorMsg);
                        }
                    }

                    contador.detener();
                    
                    // --- INICIO DE LA MODIFICACIÓN ---
                    long tiemposegundos = contador.getTiempo();
                    double tiempoMinutos = (double) tiemposegundos / 1000.0; 
                    mensajeResultado = String.format("✅ Finalizado en %s. Insertados %d registros. (Tiempo: %.2f segundos)",
                            gestor, numeroInserciones, tiempoMinutos);
                    // --- FIN DE LA MODIFICACIÓN ---
                } else {
                    mensajeResultado = "❌ Error: Gestor no reconocido.";
                }
            } catch (SQLException ex) {
                mensajeResultado = "❌ Error al conectar con " + gestor + ": " + ex.getMessage();
            } finally {
                if (conexion != null) {
                    conexion.cerrarConexion();
                }
            }

            ventana.mostrarResultado(mensajeResultado);
            System.out.println(mensajeResultado);
            ventana.hiloFinalizado("🏁 Hilo de " + gestor + " terminado.");
        }
    }
}
