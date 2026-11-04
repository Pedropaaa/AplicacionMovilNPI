package com.example.aplicacionmovilnpi.qr

import com.example.aplicacionmovilnpi.model.Node
import com.example.aplicacionmovilnpi.model.PointOfInterest
import com.example.aplicacionmovilnpi.navigation.EtsiitData

data class QrMatchResult(
    val node: Node,
    val poi: PointOfInterest?
)

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
     * Resuelve un código QR que puede ser de ubicación individual o de una ruta completa.
     */
    fun resolveQrCodeResult(qrContent: String): QrScanResult? {
        val cleanContent = qrContent.trim()

        // 0. Coincidencia por formato de Ruta (ej: "ROUTE:poi_entrada:poi_aula12:false" o "ROUTE:poi_entrada:poi_aula12")
        if (cleanContent.startsWith("ROUTE:", ignoreCase = true) || cleanContent.startsWith("RUTAS:", ignoreCase = true)) {
            val parts = cleanContent.split(":")
            if (parts.size >= 3) {
                val startId = parts[1].trim()
                val destId = parts[2].trim()
                val isAccessible = parts.getOrNull(3)?.trim()?.toBooleanStrictOrNull() ?: false

                val startPoi = EtsiitData.pois.find { it.id.equals(startId, ignoreCase = true) || it.nameEs.equals(startId, ignoreCase = true) }
                val destPoi = EtsiitData.pois.find { it.id.equals(destId, ignoreCase = true) || it.nameEs.equals(destId, ignoreCase = true) }

                if (startPoi != null && destPoi != null) {
                    return QrScanResult.Route(startPoi, destPoi, isAccessible)
                }
            }
        }

        // 1. Coincidencia de Ubicación individual
        val match = resolveQrCode(cleanContent)
        if (match != null) {
            return QrScanResult.Location(match.node, match.poi)
        }

        return null
    }

    /**
     * Coincidencia de ubicación individual por ID de nodo, ID de POI o nombre.
     */
    fun resolveQrCode(qrContent: String): QrMatchResult? {
        val cleanContent = qrContent.trim()

        // 1. Coincidencia directa por ID de Nodo (ej: "n_aula12")
        val directNode = EtsiitData.nodes[cleanContent]
        if (directNode != null) {
            val associatedPoi = EtsiitData.pois.find { it.nodeId == directNode.id }
            return QrMatchResult(directNode, associatedPoi)
        }

        // 2. Coincidencia por ID de POI (ej: "poi_aula12")
        val directPoi = EtsiitData.pois.find { it.id.equals(cleanContent, ignoreCase = true) }
        if (directPoi != null) {
            val associatedNode = EtsiitData.nodes[directPoi.nodeId]
            if (associatedNode != null) {
                return QrMatchResult(associatedNode, directPoi)
            }
        }

        // 3. Búsqueda por nombre legible (ej: "Aula 1.2")
        val poiByName = EtsiitData.pois.find {
            it.nameEs.equals(cleanContent, ignoreCase = true) ||
                    it.nameEn.equals(cleanContent, ignoreCase = true)
        }
        if (poiByName != null) {
            val associatedNode = EtsiitData.nodes[poiByName.nodeId]
            if (associatedNode != null) {
                return QrMatchResult(associatedNode, poiByName)
            }
        }

        return null
    }
}
