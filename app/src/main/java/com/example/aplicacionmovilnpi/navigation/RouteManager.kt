package com.example.aplicacionmovilnpi.navigation

import com.example.aplicacionmovilnpi.model.Node
import com.example.aplicacionmovilnpi.model.PointOfInterest
import java.util.PriorityQueue

class RouteManager {

    /**
     * Calcula la ruta óptima entre dos POIs usando el algoritmo de Dijkstra.
     *
     * @param startPoiId ID del punto de inicio
     * @param destPoiId ID del punto de destino
     * @param onlyAccessible Si es true, ignora escaleras y prioriza ascensores (PMR)
     * @return Lista ordenada de [Node] que componen el camino, o lista vacía si no hay ruta.
     */
    fun calculateRoute(
        startPoiId: String,
        destPoiId: String,
        onlyAccessible: Boolean = false
    ): List<Node> {
        // 1. Obtener los IDs de los nodos asociados a cada POI
        val startNodeId = EtsiitData.pois.find { it.id == startPoiId }?.nodeId ?: return emptyList()
        val destNodeId = EtsiitData.pois.find { it.id == destPoiId }?.nodeId ?: return emptyList()

        if (startNodeId == destNodeId) {
            return listOfNotNull(EtsiitData.nodes[startNodeId])
        }

        // 2. Filtrar conexiones según accesibilidad (si se requiere PMR, se excluyen escaleras)
        val validEdges = if (onlyAccessible) {
            EtsiitData.edges.filter { it.isAccessible }
        } else {
            EtsiitData.edges
        }

        // 3. Crear lista de adyacencia (grafo no dirigido: pasillos transitables en ambos sentidos)
        val adj = mutableMapOf<String, MutableList<Pair<String, Float>>>()
        for (edge in validEdges) {
            adj.getOrPut(edge.from) { mutableListOf() }.add(edge.to to edge.weight)
            adj.getOrPut(edge.to) { mutableListOf() }.add(edge.from to edge.weight)
        }

        // 4. Estructuras auxiliares para Dijkstra
        val distances = mutableMapOf<String, Float>().withDefault { Float.MAX_VALUE }
        val previous = mutableMapOf<String, String?>()
        val priorityQueue = PriorityQueue<Pair<String, Float>>(compareBy { it.second })

        distances[startNodeId] = 0f
        priorityQueue.add(startNodeId to 0f)

        // 5. Exploración de caminos
        while (priorityQueue.isNotEmpty()) {
            val (current, currentDist) = priorityQueue.poll()!!

            if (current == destNodeId) break
            if (currentDist > distances.getValue(current)) continue

            adj[current]?.forEach { (neighbor, weight) ->
                val newDist = currentDist + weight
                if (newDist < distances.getValue(neighbor)) {
                    distances[neighbor] = newDist
                    previous[neighbor] = current
                    priorityQueue.add(neighbor to newDist)
                }
            }
        }

        // 6. Reconstruir el camino desde el destino hacia atrás
        val path = mutableListOf<Node>()
        var step: String? = destNodeId

        while (step != null) {
            EtsiitData.nodes[step]?.let { path.add(0, it) }
            step = previous[step]
        }

        // Si el primer nodo no es el de inicio, significa que no hubo camino posible
        return if (path.firstOrNull()?.id == startNodeId) path else emptyList()
    }

    /**
     * Devuelve la lista completa de POIs disponibles para mostrarlos en la UI.
     */
    fun getAllPois(): List<PointOfInterest> = EtsiitData.pois
}