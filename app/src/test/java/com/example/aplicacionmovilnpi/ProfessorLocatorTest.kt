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

class ProfessorLocatorTest {

    private val cabrera = FacultyData.professors.first { it.email == "mcabrera@ugr.es" }
    private val navarro = FacultyData.professors.first { it.email == "gnavarro@ugr.es" }

    private fun at(day: Int, hour: Int, minute: Int = 0, semester: Int = 1) =
        Moment(semester, day, hour * 60 + minute)

    @Test
    fun cabrera_jueves_9_esta_en_teoria_de_npi_en_aula_05() {
        val result = ProfessorLocator.locate(cabrera, at(day = 4, hour = 9))
        val slot = (result as Whereabouts.Located).slot
        assertEquals(ActivityKind.CLASE, slot.kind)
        assertEquals("Aula 0.5", slot.place)
        assertEquals("poi_aula05", slot.poiId)
    }

    @Test
    fun cabrera_lunes_12_esta_en_tutoria_en_su_despacho() {
        val slot = (ProfessorLocator.locate(cabrera, at(day = 1, hour = 12)) as Whereabouts.Located).slot
        assertEquals(ActivityKind.TUTORIA, slot.kind)
        assertEquals("poi_despachos_p3", slot.poiId)
    }

    @Test
    fun la_hora_de_fin_ya_no_cuenta_como_ocupado() {
        // NPI teoría acaba a las 10:30 y justo empiezan las prácticas A2
        val slot = (ProfessorLocator.locate(cabrera, at(day = 4, hour = 10, minute = 30)) as Whereabouts.Located).slot
        assertEquals("NPI · Prácticas A2", slot.title)
    }

    @Test
    fun sin_clase_ni_tutoria_devuelve_desconocido_con_la_siguiente_franja() {
        // Martes 1er semestre: Cabrera no tiene nada; lo siguiente es el jueves a las 8:30
        val result = ProfessorLocator.locate(cabrera, at(day = 2, hour = 10)) as Whereabouts.Unknown
        assertNotNull(result.next)
        assertEquals(4, result.next!!.day)
        assertEquals("8:30", result.next!!.start)
    }

    @Test
    fun la_siguiente_franja_da_la_vuelta_a_la_semana() {
        // Viernes por la tarde -> lunes siguiente (tutoría de las 11:30)
        val next = (ProfessorLocator.locate(cabrera, at(day = 5, hour = 18)) as Whereabouts.Unknown).next!!
        assertEquals(1, next.day)
        assertEquals("11:30", next.start)
    }

    @Test
    fun profesor_sin_nada_en_el_semestre_no_tiene_siguiente_franja() {
        // Navarro solo da clase en el 2º semestre y no tiene tutorías
        val result = ProfessorLocator.locate(navarro, at(day = 3, hour = 10)) as Whereabouts.Unknown
        assertNull(result.next)
        assertNotNull(navarro.note)
    }

    @Test
    fun aulas_que_no_estan_en_el_plano_no_tienen_poi() {
        assertEquals("poi_aula210", FacultyData.roomPoiId("2.10"))
        assertEquals("poi_aula311", FacultyData.roomPoiId("3.11"))
        assertNull(FacultyData.roomPoiId("1.8")) // aún no digitalizada
    }

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

    @Test
    fun todas_las_clases_de_la_alumna_se_pueden_guiar() {
        assertTrue(FacultyData.studentSchedule.all { it.poiId != null })
    }

    @Test
    fun moment_now_convierte_dia_y_semestre() {
        val miercoles = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 21, 11, 45) }
        assertEquals(Moment(semester = 1, day = 3, minutes = 11 * 60 + 45), Moment.now(miercoles))
        val domingoMarzo = Calendar.getInstance().apply { set(2027, Calendar.MARCH, 7, 9, 0) }
        assertEquals(Moment(semester = 2, day = 7, minutes = 9 * 60), Moment.now(domingoMarzo))
    }

    @Test
    fun horario_semanal_de_la_alumna_tiene_fechas_de_la_semana_actual() {
        val miercoles = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 21, 11, 45) }
        val week = SampleData.weekSchedule(miercoles)
        assertEquals(listOf("19", "20", "21", "22", "23"), week.map { it.date })
        assertEquals("octubre", week[0].month)
        assertEquals(3, week[3].classes.size) // jueves: NPI, VC prácticas, VC teoría
        assertEquals("Nuevos Paradigmas de Interacción · Teoría", week[3].classes[0].name)
    }

    @Test
    fun proxima_clase_de_la_alumna() {
        // Miércoles 11:45: TIC ya acabó -> jueves 8:30 NPI
        val next = SampleData.nextLesson(at(day = 3, hour = 11, minute = 45))!!
        assertEquals(4, next.day)
        assertEquals("NPI · Teoría", next.title)
        // Martes 10:00: TIC prácticas en curso
        assertEquals("TIC · Prácticas", SampleData.nextLesson(at(day = 2, hour = 10))!!.title)
    }
}
