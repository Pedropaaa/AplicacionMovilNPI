package com.example.aplicacionmovilnpi.navigation

import com.example.aplicacionmovilnpi.model.Node
import java.util.PriorityQueue

class RouteManager {

    /**
     * Busca una ruta de coste mínimo entre dos puntos de interés del edificio.
     *
     * Los POI se convierten en sus nodos asociados y se recorren las conexiones del grafo como
     * tramos transitables en ambos sentidos. La búsqueda usa Dijkstra, teniendo en cuenta el
     * peso de cada conexión. Se añade una penalización al atravesar un nodo de escaleras o
     * ascensor y volver a la misma planta, para evitar usar esos elementos como atajos
     * horizontales.
     *
     * Si [onlyAccessible] es `true`, solo se consideran conexiones marcadas como accesibles.
     * Esto excluye, por ejemplo, los tramos de escaleras no accesibles.
     *
     * @param startPoiId Identificador del POI de origen.
     * @param destPoiId Identificador del POI de destino.
     * @param onlyAccessible Si es `true`, limita la búsqueda a conexiones accesibles.
     * @return Los nodos de la ruta, en orden desde el origen hasta el destino, ambos incluidos.
     *         Devuelve una lista vacía si algún POI no existe o no hay una ruta válida.
     *         Si origen y destino corresponden al mismo nodo, devuelve ese nodo.
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

        // 4. Dijkstra sobre estados (nodo, ¿se llegó a él desde su misma planta?).
        //    Así se puede penalizar usar unas escaleras o un ascensor como atajo dentro de una
        //    planta (entrar y salir por la misma planta) sin encarecer cambiar de planta con ellos.
        data class State(val nodeId: String, val enteredFromSameFloor: Boolean)

        val start = State(startNodeId, false)
        val distances = mutableMapOf(start to 0f)
        val previous = mutableMapOf<State, State>()
        val priorityQueue = PriorityQueue<Pair<State, Float>>(compareBy { it.second })
        priorityQueue.add(start to 0f)
        var reached: State? = null

        // 5. Exploración de caminos
        while (priorityQueue.isNotEmpty()) {
            val (current, currentDist) = priorityQueue.poll()!!

            if (current.nodeId == destNodeId) {
                reached = current
                break
            }
            if (currentDist > (distances[current] ?: Float.MAX_VALUE)) continue

            val currentFloor = EtsiitData.nodes[current.nodeId]?.floor
            adj[current.nodeId]?.forEach { (neighbor, weight) ->
                val sameFloor = EtsiitData.nodes[neighbor]?.floor == currentFloor
                val transitPenalty =
                    if (current.enteredFromSameFloor && sameFloor && isFloorChangeNode(current.nodeId)) {
                        FLOOR_CHANGE_TRANSIT_PENALTY
                    } else {
                        0f
                    }
                val next = State(neighbor, sameFloor)
                val newDist = currentDist + weight + transitPenalty
                if (newDist < (distances[next] ?: Float.MAX_VALUE)) {
                    distances[next] = newDist
                    previous[next] = current
                    priorityQueue.add(next to newDist)
                }
            }
        }

        // 6. Reconstruir el camino desde el destino hacia atrás
        if (reached == null) return emptyList()
        val path = mutableListOf<Node>()
        var step: State? = reached

        while (step != null) {
            EtsiitData.nodes[step.nodeId]?.let { path.add(0, it) }
            step = previous[step]
        }

        // Si el primer nodo no es el de inicio, significa que no hubo camino posible
        return if (path.firstOrNull()?.id == startNodeId) path else emptyList()
    }

    /** Nodos de escaleras o ascensor: solo se deben cruzar para cambiar de planta. */
    private fun isFloorChangeNode(nodeId: String): Boolean =
        nodeId.contains("escaleras") || nodeId.contains("ascensor")

    companion object {
        /**
         * Coste extra por cruzar unas escaleras o un ascensor sin cambiar de planta (entrar y
         * salir por la misma). Es alto para que solo se haga si no hay otro camino razonable.
         */
        const val FLOOR_CHANGE_TRANSIT_PENALTY = 40f
    }
}
