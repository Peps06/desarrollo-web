package com.bitacora.viajes.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bitacora.viajes.model.Gasto;
import com.bitacora.viajes.model.RolParticipante;

/**
 * Acceso a la tabla de gastos.
 *
 * @author Citlaly Morales Viveros
 */
public interface GastoRepository extends JpaRepository<Gasto, Integer> {

    // Suma todos los gastos de un viaje; regresa 0 si no hay gastos
    @Query("select coalesce(sum(g.monto), 0) from Gasto g where g.viaje.id = :viajeId")
    BigDecimal sumarMontoPorViaje(@Param("viajeId") Integer viajeId);

    // HU-21: cuánto pagó cada usuario en el viaje. La suma la hace MySQL (GROUP BY),
    // así no se cargan todos los gastos. Cada fila es [usuarioId (Integer), total (BigDecimal)].
    // Los usuarios que no han pagado nada no aparecen.
    @Query("select g.usuario.id, sum(g.monto) from Gasto g "
            + "where g.viaje.id = :viajeId group by g.usuario.id")
    List<Object[]> sumarMontoPorUsuario(@Param("viajeId") Integer viajeId);

    // Revisa si el usuario tiene cierto rol dentro del viaje
    @Query("select count(p) > 0 from ParticipanteViaje p "
            + "where p.viaje.id = :viajeId and p.usuario.id = :usuarioId and p.rol = :rol")
    boolean esRolEnViaje(@Param("viajeId") Integer viajeId,
            @Param("usuarioId") Integer usuarioId,
            @Param("rol") RolParticipante rol);

    // Revisa si el usuario participa en el viaje, sin importar el rol
    @Query("select count(p) > 0 from ParticipanteViaje p "
            + "where p.viaje.id = :viajeId and p.usuario.id = :usuarioId")
    boolean existeParticipacion(@Param("viajeId") Integer viajeId,
            @Param("usuarioId") Integer usuarioId);
}
