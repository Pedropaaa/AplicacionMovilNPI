package com.example.aplicacionmovilnpi.qr

import com.example.aplicacionmovilnpi.model.Node
import com.example.aplicacionmovilnpi.model.PointOfInterest
import com.example.aplicacionmovilnpi.navigation.EtsiitData

/**
 * Resultado de resolver un código QR correspondiente a una ubicación individual.
 *
 * @property node Nodo físico del mapa al que apunta la posición escaneada.
 * @property poi Punto de interés (POI) asociado a dicho nodo, si existe.
 */
data class QrMatchResult(
    val node: Node,
    val poi: PointOfInterest?
)

/**
 * Resultado tipado y sellado retornado tras el procesamiento de un código QR escaneado.
 * Permite a la interfaz diferenciar si el QR corresponde a una posición individual o a una ruta completa.
 */
sealed class QrScanResult {
    /**
     * Representa la lectura de una ubicación geográfica puntual dentro del edificio.
     *
     * @property node Nodo del grafo asociado al punto escaneado.
     * @property poi Punto de interés asociado a la ubicación (si está registrado).
     */
    data class Location(
        val node: Node,
        val poi: PointOfInterest?
    ) : QrScanResult()

    /**
     * Representa la lectura de un código QR de ruta compartida entre dos puntos de interés.
     *
     * @property startPoi POI de origen desde donde comenzará la navegación.
     * @property destPoi POI de destino al que se desea llegar.
     * @property isAccessible Indica si la ruta debe calcularse excluyendo barreras arquitectónicas (escaleras).
     */
    data class Route(
        val startPoi: PointOfInterest,
        val destPoi: PointOfInterest,
        val isAccessible: Boolean
    ) : QrScanResult()
}

/**
 * Intérprete y decodificador del contenido de códigos QR dentro del sistema de navegación.
 *
 * Analiza el texto escaneado para identificar si corresponde a:
 * - Una ruta formateada (`ROUTE:<origen>:<destino>:<accesible>`).
 * - Una lista de nodos antiguos ordenados por comas (`n_entrada,n_pasillo,n_aula`).
 * - Un identificador directo de nodo (`n_aula12`), identificador de POI (`poi_aula12`) o nombre visible.
 */
object QrLocationResolver {

    /**
     * Lista de IDs de nodos escaneados provenientes de un formato de ruta heredado (por comas).
     * Se conserva para garantizar compatibilidad con consumidores antiguos.
     */
    var scannedRouteNodeIds: List<String> = emptyList()

    /**
     * Analiza el texto escaneado de un código QR y devuelve el resultado tipado [QrScanResult].
     *
     * Soporta los formatos actuales de rutas (`ROUTE:...`), rutas heredadas por comas y ubicaciones individuales.
     *
     * @param qrContent Cadena de texto bruta obtenida por la cámara / escáner.
     * @return Instancia de [QrScanResult.Route] o [QrScanResult.Location] según el contenido, o `null` si no se reconoce.
     */
    fun resolveQrCodeResult(qrContent: String): QrScanResult? {
        // 1. Limpiar espacios en blanco del contenido capturado
        val cleanContent = qrContent.trim()
        scannedRouteNodeIds = emptyList()

        // 2. Comprobar si corresponde al formato de ruta compartida ("ROUTE:origen:destino:accesible")
        if (cleanContent.startsWith("ROUTE:", ignoreCase = true) ||
            cleanContent.startsWith("RUTAS:", ignoreCase = true)
        ) {
            val parts = cleanContent.split(':')
            if (parts.size < 3) return null

            val startId = parts[1].trim()
            val destId = parts[2].trim()
            if (startId.isEmpty() || destId.isEmpty()) return null

            val isAccessible = parts.getOrNull(3)
                ?.trim()
                ?.lowercase()
                ?.toBooleanStrictOrNull()
                ?: false

            val startPoi = findPoi(startId) ?: return null
            val destPoi = findPoi(destId) ?: return null

            return QrScanResult.Route(
                startPoi = startPoi,
                destPoi = destPoi,
                isAccessible = isAccessible
            )
        }

        // 3. Comprobar si es una ruta en formato heredado (lista de IDs de nodo separados por comas)
        if (cleanContent.contains(',')) {
            val match = resolveQrCode(cleanContent) ?: return null
            val nodeIds = scannedRouteNodeIds
            val firstNode = nodeIds.firstOrNull()?.let { nodeId ->
                EtsiitData.nodes[nodeId]
            }
            val firstPoi = firstNode?.let { node ->
                EtsiitData.pois.find { it.nodeId == node.id }
            }
            val lastPoi = match.poi

            // Si se identifican POIs de inicio y fin, se retorna como ruta
            if (firstPoi != null && lastPoi != null) {
                return QrScanResult.Route(
                    startPoi = firstPoi,
                    destPoi = lastPoi,
                    isAccessible = false
                )
            }

            // Si faltan POIs asociados, se retorna como la ubicación del nodo final reconocido
            return QrScanResult.Location(match.node, match.poi)
        }

        // 4. Intentar resolver como una ubicación puntual individual (Nodo o POI)
        val match = resolveQrCode(cleanContent) ?: return null
        return QrScanResult.Location(match.node, match.poi)
    }

    /**
     * Resuelve un código QR de ubicación individual buscando coincidencias directas en la base de datos de la ETSIIT.
     *
     * @param qrContent Identificador o nombre a resolver.
     * @return Objeto [QrMatchResult] con el nodo y POI asociados, o `null` si no hay coincidencias.
     */
    fun resolveQrCode(qrContent: String): QrMatchResult? {
        val cleanContent = qrContent.trim()
        scannedRouteNodeIds = emptyList()

        // 1. Manejo del formato antiguo de lista de nodos separados por comas
        if (cleanContent.contains(',')) {
            val nodeIds = cleanContent
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            scannedRouteNodeIds = nodeIds

            val lastNodeId = nodeIds.lastOrNull() ?: return null
            val lastNode = EtsiitData.nodes[lastNodeId] ?: return null
            val associatedPoi = EtsiitData.pois.find { it.nodeId == lastNode.id }
            return QrMatchResult(lastNode, associatedPoi)
        }

        // 2. Coincidencia directa por ID de nodo registrado (ej: "n_entrada", "n_aula12")
        val directNode = EtsiitData.nodes[cleanContent]
        if (directNode != null) {
            val associatedPoi = EtsiitData.pois.find { it.nodeId == directNode.id }
            return QrMatchResult(directNode, associatedPoi)
        }

        // 3. Coincidencia por ID de POI (ej: "poi_aula12") o por nombre
        val directPoi = findPoi(cleanContent)
        if (directPoi != null) {
            val associatedNode = EtsiitData.nodes[directPoi.nodeId]
            if (associatedNode != null) {
                return QrMatchResult(associatedNode, directPoi)
            }
        }

        return null
    }

    /**
     * Busca un punto de interés (POI) en los datos del edificio por ID o nombre (español/inglés).
     *
     * @param value Cadena a buscar.
     * @return El [PointOfInterest] coincidente o `null` si no existe.
     */
    private fun findPoi(value: String): PointOfInterest? {
        return EtsiitData.pois.find { poi ->
            poi.id.equals(value, ignoreCase = true) ||
                    poi.nameEs.equals(value, ignoreCase = true) ||
                    poi.nameEn.equals(value, ignoreCase = true)
        }
    }
}
