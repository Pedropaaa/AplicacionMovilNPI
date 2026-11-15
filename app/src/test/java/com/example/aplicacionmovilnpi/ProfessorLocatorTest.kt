package com.example.aplicacionmovilnpi

import com.example.aplicacionmovilnpi.data.ActivityKind
import com.example.aplicacionmovilnpi.data.FacultyData
import com.example.aplicacionmovilnpi.data.Moment
import com.example.aplicacionmovilnpi.data.ProfessorLocator
import com.example.aplicacionmovilnpi.data.SampleData
import com.example.aplicacionmovilnpi.data.Whereabouts
import com.example.aplicacionmovilnpi.navigation.EtsiitData
import com.example.aplicacionmovilnpi.navigation.RouteManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Suite de pruebas unitarias para el algoritmo de localización de profesorado ([ProfessorLocator]),
 * resolución de aulas a POIs ([FacultyData.roomPoiId]), ordenación de horarios y comprobación de rutas.
 */
class ProfessorLocatorTest {

    private val cabrera = FacultyData.professors.first { it.email == "mcabrera@ugr.es" }
    private val navarro = FacultyData.professors.first { it.email == "gnavarro@ugr.es" }

    private fun at(day: Int, hour: Int, minute: Int = 0, semester: Int = 1) =
        Moment(semester, day, hour * 60 + minute)

    // ======================================================
    // PRUEBAS DE LOCALIZACIÓN DE PROFESORADO Y DISPONIBILIDAD
    // ======================================================

    /**
     * Valida que el jueves a las 9:00 (1er semestre), el profesor Marcelino Cabrera esté identificado impartiendo teoría de NPI en el Aula 0.5.
     */
    @Test
    fun cabrera_jueves_9_esta_en_teoria_de_npi_en_aula_05() {
        // 1. Consultar la disponibilidad del profesor en el instante fijado
        val result = ProfessorLocator.locate(cabrera, at(day = 4, hour = 9))
        
        // 2. Verificar que el estado devuelto sea Located con los datos correctos del aula y POI
        val slot = (result as Whereabouts.Located).slot
        assertEquals(ActivityKind.CLASE, slot.kind)
        assertEquals("Aula 0.5", slot.place)
        assertEquals("poi_aula05", slot.poiId)
    }

    /**
     * Valida que el lunes a las 12:00 (1er semestre), el profesor Marcelino Cabrera esté localizado en tutorías en su despacho.
     */
    @Test
    fun cabrera_lunes_12_esta_en_tutoria_en_su_despacho() {
        val slot = (ProfessorLocator.locate(cabrera, at(day = 1, hour = 12)) as Whereabouts.Located).slot
        assertEquals(ActivityKind.TUTORIA, slot.kind)
        assertEquals("poi_despachos_p3", slot.poiId)
    }

    /**
     * Comprueba que al llegar a la hora exacta de fin de una clase (10:30), el buscador active la clase siguiente que comienza a esa hora.
     */
    @Test
    fun la_hora_de_fin_ya_no_cuenta_como_ocupado() {
        val slot = (ProfessorLocator.locate(cabrera, at(day = 4, hour = 10, minute = 30)) as Whereabouts.Located).slot
        assertEquals("NPI · Prácticas A2", slot.title)
    }

    /**
     * Valida que si el profesor no tiene actividades activas a la hora consultada, devuelva [Whereabouts.Unknown] indicando la siguiente franja.
     */
    @Test
    fun sin_clase_ni_tutoria_devuelve_desconocido_con_la_siguiente_franja() {
        val result = ProfessorLocator.locate(cabrera, at(day = 2, hour = 10)) as Whereabouts.Unknown
        assertNotNull(result.next)
        assertEquals(4, result.next!!.day)
        assertEquals("8:30", result.next!!.start)
    }

    /**
     * Comprueba que la búsqueda de la siguiente franja dé la vuelta al fin de semana para encontrar la primera clase del lunes.
     */
    @Test
    fun la_siguiente_franja_da_la_vuelta_a_la_semana() {
        val next = (ProfessorLocator.locate(cabrera, at(day = 5, hour = 18)) as Whereabouts.Unknown).next!!
        assertEquals(1, next.day)
        assertEquals("11:30", next.start)
    }

    /**
     * Valida que un profesor sin franjas lectivas registradas en el semestre devuelva un resultado sin siguiente franja (`next == null`).
     */
    @Test
    fun profesor_sin_nada_en_el_semestre_no_tiene_siguiente_franja() {
        val result = ProfessorLocator.locate(navarro, at(day = 3, hour = 10)) as Whereabouts.Unknown
        assertNull(result.next)
        assertNotNull(navarro.note)
    }

    // ======================================================
    // PRUEBAS DE VINCULACIÓN CON EL GRAFO Y POIS DEL MAPA
    // ======================================================

    /**
     * Comprueba la resolución de códigos de aula a IDs de POI registrados en el mapa y el manejo de aulas no digitalizadas.
     */
    @Test
    fun aulas_que_no_estan_en_el_plano_no_tienen_poi() {
        assertEquals("poi_aula210", FacultyData.roomPoiId("2.10"))
        assertEquals("poi_aula311", FacultyData.roomPoiId("3.11"))
        assertNull(FacultyData.roomPoiId("1.9")) // Aula no digitalizada en el plano
    }

    /**
     * Valida que todos los POIs asignados en los horarios de profesores y alumnas existan en el plano y sean alcanzables mediante Dijkstra.
     */
    @Test
    fun todos_los_poi_de_profesores_y_alumna_existen_y_tienen_ruta() {
        val routeManager = RouteManager()
        val poiIds = (FacultyData.professors.flatMap { p -> p.schedule.map { it.poiId } + p.officePoiId } +
                FacultyData.studentSchedule.map { it.poiId }).filterNotNull().toSet()
        poiIds.forEach { id ->
            assertTrue("POI inexistente: $id", EtsiitData.pois.any { it.id == id })
            assertTrue("Sin ruta hasta $id", routeManager.calculateRoute("poi_entrada", id).isNotEmpty())
        }
    }

    /**
     * Comprueba que todas las clases del horario de la estudiante tengan asignado un POI navegable en el mapa.
     */
    @Test
    fun todas_las_clases_de_la_alumna_se_pueden_guiar() {
        assertTrue(FacultyData.studentSchedule.all { it.poiId != null })
    }

    /**
     * Valida la conversión correcta de objetos [Calendar] al modelo de instante [Moment] según fecha y semestre.
     */
    @Test
    fun moment_now_convierte_dia_y_semestre() {
        val miercoles = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 21, 11, 45) }
        assertEquals(Moment(semester = 1, day = 3, minutes = 11 * 60 + 45), Moment.now(miercoles))
        val domingoMarzo = Calendar.getInstance().apply { set(2027, Calendar.MARCH, 7, 9, 0) }
        assertEquals(Moment(semester = 2, day = 7, minutes = 9 * 60), Moment.now(domingoMarzo))
    }

    /**
     * Valida que el generador del horario semanal de la estudiante calcule correctamente las fechas del mes para la semana en curso.
     */
    @Test
    fun horario_semanal_de_la_alumna_tiene_fechas_de_la_semana_actual() {
        val miercoles = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 21, 11, 45) }
        val week = SampleData.weekSchedule(miercoles)
        assertEquals(listOf("19", "20", "21", "22", "23"), week.map { it.date })
        assertEquals("octubre", week[0].month)
        assertEquals(3, week[3].classes.size)
        assertEquals("Nuevos Paradigmas de Interacción · Teoría", week[3].classes[0].name)
    }

    /**
     * Valida la búsqueda de la próxima clase matriculada a partir de un instante dado.
     */
    @Test
    fun proxima_clase_de_la_alumna() {
        val next = SampleData.nextLesson(at(day = 3, hour = 11, minute = 45))!!
        assertEquals(4, next.day)
        assertEquals("NPI · Teoría", next.title)
        assertEquals("TIC · Prácticas", SampleData.nextLesson(at(day = 2, hour = 10))!!.title)
    }
}
