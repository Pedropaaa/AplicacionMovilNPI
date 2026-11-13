package com.example.aplicacionmovilnpi.qr

import com.example.aplicacionmovilnpi.model.Node
import com.example.aplicacionmovilnpi.model.PointOfInterest
import com.example.aplicacionmovilnpi.navigation.EtsiitData

/** Resultado de resolver un QR de ubicación individual. */
data class QrMatchResult(
    val node: Node,
    val poi: PointOfInterest?
)

/**
 * Resultado tipado que consume el escáner moderno.
 *
 * Location: QR de una ubicación individual.
 * Route: QR de una ruta entre dos POI.
 */
sealed class QrScanResult {
    data class Location(
        val node: Node,
        val poi: PointOfInterest?
    ) : QrScanResult()

    data class Route(
        val startPoi: PointOfInterest,
        val destPoi: PointOfInterest,
        val isAccessible: Boolean
    ) : QrScanResult()
}

object QrLocationResolver {

    /**
     * Compatibilidad con los QR antiguos que contienen la ruta como una lista
     * de IDs de nodo separados por comas, por ejemplo "n_entrada,n_pasillo,n_aula".
     * Se conserva para los consumidores antiguos de resolveQrCode().
     */
    var scannedRouteNodeIds: List<String> = emptyList()

    /**
     * Resuelve los formatos actuales y antiguos:
     * - "ROUTE:poi_origen:poi_destino:false" (o "RUTAS:...") para rutas.
     * - "n_entrada,n_pasillo,n_aula" para rutas antiguas por nodos.
     * - ID de nodo, ID de POI o nombre visible para ubicaciones individuales.
     */
    fun resolveQrCodeResult(qrContent: String): QrScanResult? {
        val cleanContent = qrContent.trim()
        scannedRouteNodeIds = emptyList()

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

        // Compatibilidad con rutas serializadas como IDs de nodo separados por comas.
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

            // El formato antiguo no guardaba el modo accesible: se conserva como false.
            if (firstPoi != null && lastPoi != null) {
                return QrScanResult.Route(
                    startPoi = firstPoi,
                    destPoi = lastPoi,
                    isAccessible = false
                )
            }

            // Si faltan POI asociados, devolvemos al menos la última ubicación reconocida.
            return QrScanResult.Location(match.node, match.poi)
        }

        val match = resolveQrCode(cleanContent) ?: return null
        return QrScanResult.Location(match.node, match.poi)
    }

    /**
     * Resuelve un QR individual por ID de nodo, ID de POI o nombre (español/inglés).
     * También mantiene el comportamiento antiguo de los QR de ruta con comas:
     * guarda todos los IDs en [scannedRouteNodeIds] y devuelve el último nodo.
     */
    fun resolveQrCode(qrContent: String): QrMatchResult? {
        val cleanContent = qrContent.trim()
        scannedRouteNodeIds = emptyList()

        // Ruta antigua: lista ordenada de IDs de nodo.
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

        // 1. Coincidencia directa por ID de nodo (ej. "n_aula12").
        val directNode = EtsiitData.nodes[cleanContent]
        if (directNode != null) {
            val associatedPoi = EtsiitData.pois.find { it.nodeId == directNode.id }
            return QrMatchResult(directNode, associatedPoi)
        }

        // 2. Coincidencia por ID de POI (ej. "poi_aula12").
        val directPoi = findPoi(cleanContent)
        if (directPoi != null) {
            val associatedNode = EtsiitData.nodes[directPoi.nodeId]
            if (associatedNode != null) {
                return QrMatchResult(associatedNode, directPoi)
            }
        }

        return null
    }

    private fun findPoi(value: String): PointOfInterest? {
        return EtsiitData.pois.find { poi ->
            poi.id.equals(value, ignoreCase = true) ||
                    poi.nameEs.equals(value, ignoreCase = true) ||
                    poi.nameEn.equals(value, ignoreCase = true)
        }
    }
}