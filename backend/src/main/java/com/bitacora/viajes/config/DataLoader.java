package com.bitacora.viajes.config;

import com.bitacora.viajes.model.Actividad;
import com.bitacora.viajes.model.CategoriaGasto;
import com.bitacora.viajes.model.EstadoViaje;
import com.bitacora.viajes.model.Gasto;
import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.model.RolParticipante;
import com.bitacora.viajes.model.Tarea;
import com.bitacora.viajes.model.Usuario;
import com.bitacora.viajes.model.Viaje;
import com.bitacora.viajes.model.Voto;
import com.bitacora.viajes.repository.ActividadRepository;
import com.bitacora.viajes.repository.GastoRepository;
import com.bitacora.viajes.repository.ParticipanteViajeRepository;
import com.bitacora.viajes.repository.TareaRepository;
import com.bitacora.viajes.repository.UsuarioRepository;
import com.bitacora.viajes.repository.VotoRepository;
import com.bitacora.viajes.repository.ViajeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    /** Contraseña en texto plano de los usuarios de prueba. Solo para desarrollo. */
    private static final String PASSWORD_PRUEBA = "Password123";

    private final UsuarioRepository usuarioRepository;
    private final ViajeRepository viajeRepository;
    private final ParticipanteViajeRepository participanteViajeRepository;
    private final ActividadRepository actividadRepository;
    private final VotoRepository votoRepository;
    private final GastoRepository gastoRepository;
    private final TareaRepository tareaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsuarioRepository usuarioRepository,
                      ViajeRepository viajeRepository,
                      ParticipanteViajeRepository participanteViajeRepository,
                      ActividadRepository actividadRepository,
                      VotoRepository votoRepository,
                      GastoRepository gastoRepository,
                      TareaRepository tareaRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.viajeRepository = viajeRepository;
        this.participanteViajeRepository = participanteViajeRepository;
        this.actividadRepository = actividadRepository;
        this.votoRepository = votoRepository;
        this.gastoRepository = gastoRepository;
        this.tareaRepository = tareaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Si ya hay usuarios, no se duplican los datos al reiniciar.
        if (usuarioRepository.count() > 0) {
            return;
        }

        // 2 usuarios
        Usuario ana = crearUsuario("Ana López", "ana@example.com");
        Usuario luis = crearUsuario("Luis Pérez", "luis@example.com");

        // 1 viaje con ambos como participantes
        Viaje viaje = new Viaje();
        viaje.setDestino("Oaxaca, Oaxaca");
        viaje.setFechaInicio(LocalDate.of(2026, 12, 20));
        viaje.setFechaFin(LocalDate.of(2026, 12, 27));
        viaje.setPresupuestoTotal(new BigDecimal("15000.00"));
        viaje.setMonedaBase("MXN");
        viaje.setEstado(EstadoViaje.ACTIVO);
        viaje = viajeRepository.save(viaje);

        participanteViajeRepository.save(new ParticipanteViaje(viaje, ana, RolParticipante.ORGANIZADOR));
        participanteViajeRepository.save(new ParticipanteViaje(viaje, luis, RolParticipante.PARTICIPANTE));

        // 2 actividades
        Actividad hierve = new Actividad();
        hierve.setViaje(viaje);
        hierve.setAutor(ana);
        hierve.setTitulo("Visita a Hierve el Agua");
        hierve.setDescripcion("Cascadas petrificadas y albercas naturales.");
        hierve.setDia(LocalDate.of(2026, 12, 21));
        hierve.setHora(LocalTime.of(9, 0));
        hierve.setCostoEstimado(new BigDecimal("350.00"));
        hierve = actividadRepository.save(hierve);

        Actividad cena = new Actividad();
        cena.setViaje(viaje);
        cena.setAutor(luis);
        cena.setTitulo("Cena en el Mercado 20 de Noviembre");
        cena.setDescripcion("Probar tlayudas y mole.");
        cena.setDia(LocalDate.of(2026, 12, 22));
        cena.setHora(LocalTime.of(20, 0));
        cena.setCostoEstimado(new BigDecimal("400.00"));
        cena = actividadRepository.save(cena);

        // 3 votos: 2 para Hierve y 1 para la cena
        votoRepository.save(new Voto(hierve, ana));
        votoRepository.save(new Voto(hierve, luis));
        votoRepository.save(new Voto(cena, ana));

        // 2 gastos. El segundo está en USD para probar monto_original y tipo_cambio.
        Gasto hospedaje = new Gasto();
        hospedaje.setViaje(viaje);
        hospedaje.setUsuario(ana);
        hospedaje.setConcepto("Hospedaje (anticipo)");
        hospedaje.setCategoria(CategoriaGasto.HOSPEDAJE);
        hospedaje.setMonedaOriginal("MXN");
        hospedaje.setMontoOriginal(new BigDecimal("4500.00"));
        hospedaje.setTipoCambio(BigDecimal.ONE);
        hospedaje.setMonto(new BigDecimal("4500.00"));
        gastoRepository.save(hospedaje);

        Gasto taller = new Gasto();
        taller.setViaje(viaje);
        taller.setUsuario(luis);
        taller.setConcepto("Taller de cerámica");
        taller.setCategoria(CategoriaGasto.ACTIVIDADES);
        taller.setMonedaOriginal("USD");
        taller.setMontoOriginal(new BigDecimal("30.00"));
        taller.setTipoCambio(new BigDecimal("17.500000"));
        taller.setMonto(new BigDecimal("525.00"));
        gastoRepository.save(taller);

        // 2 tareas: una completada y una pendiente
        Tarea reservar = new Tarea();
        reservar.setViaje(viaje);
        reservar.setCreador(ana);
        reservar.setDescripcion("Reservar hospedaje");
        reservar.setCompletada(true);
        tareaRepository.save(reservar);

        Tarea boletos = new Tarea();
        boletos.setViaje(viaje);
        boletos.setCreador(luis);
        boletos.setDescripcion("Comprar boletos de autobús");
        boletos.setCompletada(false);
        tareaRepository.save(boletos);
    }

    private Usuario crearUsuario(String nombre, String correo) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setContrasenaHash(passwordEncoder.encode(PASSWORD_PRUEBA));
        return usuarioRepository.save(u);
    }
}
