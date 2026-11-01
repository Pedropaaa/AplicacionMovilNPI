package com.example.aplicacionmovilnpi.qr

import com.example.aplicacionmovilnpi.model.Node
import com.example.aplicacionmovilnpi.model.PointOfInterest
import com.example.aplicacionmovilnpi.navigation.EtsiitData

data class QrMatchResult(
    val node: Node,
    val poi: PointOfInterest?
)

object QrLocationResolver {

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