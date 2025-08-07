package velocidadgestores;

public class ContadorTiempo {
    private long tiempoInicio;
    private long tiempoFin;

    public void iniciar() {
        this.tiempoInicio = System.currentTimeMillis();
        this.tiempoFin = 0; // Reiniciar el tiempo de finalización
    }

    public void detener() {
        this.tiempoFin = System.currentTimeMillis();
    }

    public long getTiempo() {
        if (tiempoFin == 0) {
            // Si el contador no ha sido detenido, se calcula el tiempo actual
            return System.currentTimeMillis() - tiempoInicio;
        }
        return tiempoFin - tiempoInicio;
    }
}