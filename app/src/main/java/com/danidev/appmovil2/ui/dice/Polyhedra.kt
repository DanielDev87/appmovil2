package com.danidev.appmovil2.ui.dice

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * HU-07: dados de más caras dibujados como sólidos.
 *
 * Los dados D4, D8, D12 y D20 son poliedros regulares (tetraedro, octaedro, dodecaedro
 * e icosaedro). Este archivo calcula cómo se ven en pantalla: gira el sólido, proyecta
 * sus vértices en 2D, se queda con las caras que miran al frente y calcula cuánta luz
 * recibe cada una. Así el dado se dibuja con volumen, igual que el cubo de puntos del D6.
 *
 * Es Kotlin puro (sin Compose ni Android) para poder probarlo con pruebas unitarias.
 */

/** Matriz de giro 3x3. */
private typealias Mat = Array<DoubleArray>

/** Punto 2D dentro de una caja cuadrada de lado 1. */
data class Point2(val x: Float, val y: Float)

/**
 * Una cara visible del dado, ya proyectada.
 *
 * @property points Vértices de la cara en orden, con coordenadas entre 0 y 1.
 * @property brightness Luz que recibe la cara, de 0 (oscura) a 1 (muy iluminada).
 * @property area Superficie de la cara en la proyección (caja de lado 1).
 * @property center Centro de la cara, donde se dibuja el número.
 * @property inradius Radio aproximado del mayor círculo que cabe en la cara; sirve para
 * elegir el tamaño del número.
 * @property isMain `true` en la cara que muestra el resultado del tiro: la de arriba en el
 * cubo, y la que más de frente se ve en los demás dados.
 * @property id Posición de la cara en el sólido completo; sirve para que cada cara conserve su
 * número mientras el dado gira.
 */
data class ProjectedFace(
    val points: List<Point2>,
    val brightness: Float,
    val area: Float,
    val center: Point2,
    val inradius: Float,
    val isMain: Boolean = false,
    val id: Int = -1
)

/** Poliedros regulares de los dados y su proyección en pantalla. */
object Polyhedra {

    /** Números de caras que se dibujan como sólido. */
    val SUPPORTED_SIDES = listOf(4, 6, 8, 12, 20)

    private val PHI = (1.0 + sqrt(5.0)) / 2.0

    /** Dirección de la luz: desde arriba, a la izquierda y al frente. */
    private val LIGHT = Vec3(-0.45, 0.75, 0.55).normalized()

    /** Margen que se deja alrededor del sólido dentro de la caja. */
    private const val MARGIN = 0.05

    /**
     * Desde dónde se mira cada dado.
     *
     * @property vertex Si no es `null`, el vértice que se pone de frente; si lo es, se
     * pone de frente la cara 0.
     * @property zDeg Giro sobre el eje que sale de la pantalla.
     * @property yDeg Giro hacia los lados.
     * @property xDeg Inclinación hacia arriba o abajo (negativo = se ve desde arriba).
     */
    private data class View(val vertex: Int?, val zDeg: Double, val yDeg: Double, val xDeg: Double)

    private val VIEWS = mapOf(
        // Cubo mirado por una esquina: se ve la cara de arriba y dos laterales (vista isométrica).
        6 to View(vertex = 0, zDeg = 105.0, yDeg = 0.0, xDeg = 0.0),
        4 to View(vertex = 0, zDeg = 0.0, yDeg = -8.0, xDeg = -12.0),
        8 to View(vertex = 0, zDeg = 20.0, yDeg = -12.0, xDeg = -18.0),
        12 to View(vertex = null, zDeg = 0.0, yDeg = -20.0, xDeg = -20.0),
        20 to View(vertex = null, zDeg = 0.0, yDeg = -20.0, xDeg = -20.0)
    )

    private val cache = HashMap<Int, List<ProjectedFace>>()

    /**
     * Caras visibles del dado de [sides] caras, proyectadas en una caja de lado 1.
     *
     * Para un número de caras que no es D4, D8, D12 ni D20 devuelve las del D20.
     */
    fun project(sides: Int): List<ProjectedFace> {
        val key = if (sides in SUPPORTED_SIDES) sides else 20
        return synchronized(cache) { cache.getOrPut(key) { compute(key) } }
    }

    /** La cara que muestra el resultado del tiro. */
    fun numberedFace(faces: List<ProjectedFace>): ProjectedFace = faces.first { it.isMain }

    /**
     * Número que lleva cada cara visible de [faces] (en el mismo orden): la cara principal
     * muestra [value] y las demás, otros números del dado, distintos entre sí.
     *
     * Cada cara del sólido conserva siempre el mismo número, así que no cambia mientras el dado
     * gira. En el dado de 6 caras las caras opuestas suman 7, como en los dados reales.
     */
    fun faceValues(sides: Int, value: Int, faces: List<ProjectedFace>): List<Int> {
        val labels = faceLabels(sides, value)
        return faces.map { labels[it.id] }
    }

    /**
     * Número de cada cara del sólido completo (por [ProjectedFace.id]) cuando la cara que queda
     * de frente en reposo muestra [value].
     */
    fun faceLabels(sides: Int, value: Int): IntArray {
        val actual = if (sides in SUPPORTED_SIDES) sides else 20
        val (vertices, faces) = solid(actual)
        val mainId = project(actual).first { it.isMain }.id
        val labels = IntArray(faces.size)
        if (actual == 6) {
            // Caras opuestas suman 7: se reparten las dos parejas que quedan junto a la principal.
            val normals = faces.map { f -> centroid(f.map { vertices[it] }).normalized() }
            fun opposite(i: Int) = faces.indices.first { j -> j != i && (normals[i] dot normals[j]) < -0.99 }
            val mainOpposite = opposite(mainId)
            labels[mainId] = value
            labels[mainOpposite] = 7 - value
            val rest = (1..6).filter { it != value && it != 7 - value }
            val a = rest.first()
            val b = rest.first { it != a && it != 7 - a }
            val others = faces.indices.filter { it != mainId && it != mainOpposite }
            val first = others.first()
            val second = others.first { it != first && it != opposite(first) }
            labels[first] = a
            labels[opposite(first)] = 7 - a
            labels[second] = b
            labels[opposite(second)] = 7 - b
        } else {
            val n = faces.size
            for (id in 0 until n) labels[id] = ((id - mainId + value - 1) % n + n) % n + 1
        }
        return labels
    }

    /**
     * Caras visibles del dado de [sides] caras mientras gira: es la vista en reposo con un giro
     * adicional de [yawDeg] (hacia los lados), [pitchDeg] (adelante o atrás) y [rollDeg]
     * (sobre el eje que sale de la pantalla). Con los tres en 0 es igual a [project].
     *
     * Usa siempre el mismo tamaño y el mismo centro que la vista en reposo, para que el dado no
     * cambie de tamaño al girar ni al detenerse.
     */
    fun tumble(sides: Int, yawDeg: Double, pitchDeg: Double, rollDeg: Double): List<ProjectedFace> {
        val key = if (sides in SUPPORTED_SIDES) sides else 20
        val frame = frameFor(key)
        var extra = rotX(Math.toRadians(pitchDeg))
        extra = mul(rotY(Math.toRadians(yawDeg)), extra)
        extra = mul(rotZ(Math.toRadians(rollDeg)), extra)
        val projected = build(frame, extra)
        val main = if (key == 6) {
            projected.indices.minBy { projected[it].center.y }
        } else {
            projected.indices.maxBy { projected[it].area }
        }
        return projected.mapIndexed { i, face -> face.copy(isMain = i == main) }
    }

    /** Cantidad de caras del sólido completo (visibles y ocultas). */
    internal fun faceCount(sides: Int): Int = solid(if (sides in SUPPORTED_SIDES) sides else 20).second.size

    // region Sólidos

    private class Vec3(val x: Double, val y: Double, val z: Double) {
        operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
        operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
        operator fun times(k: Double) = Vec3(x * k, y * k, z * k)
        infix fun dot(o: Vec3) = x * o.x + y * o.y + z * o.z
        infix fun cross(o: Vec3) = Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)
        fun length() = sqrt(this dot this)
        fun normalized(): Vec3 = this * (1.0 / length())
    }

    private fun centroid(points: List<Vec3>): Vec3 =
        points.reduce { a, b -> a + b } * (1.0 / points.size)

    /** Los 12 vértices del icosaedro, con aristas de longitud 2. */
    private fun icosahedronVertices(): List<Vec3> {
        val v = ArrayList<Vec3>()
        for (s1 in listOf(1.0, -1.0)) {
            for (s2 in listOf(1.0, -1.0)) {
                v += Vec3(0.0, s1, s2 * PHI)
                v += Vec3(s1, s2 * PHI, 0.0)
                v += Vec3(s2 * PHI, 0.0, s1)
            }
        }
        return v
    }

    /** Ordena los vértices [idx] de una cara dando la vuelta alrededor de [normal]. */
    private fun orderAround(v: List<Vec3>, idx: List<Int>, normal: Vec3): List<Int> {
        val n = normal.normalized()
        val ref = if (abs(n.x) < 0.9) Vec3(1.0, 0.0, 0.0) else Vec3(0.0, 1.0, 0.0)
        val e1 = (n cross ref).normalized()
        val e2 = n cross e1
        val c = centroid(idx.map { v[it] })
        return idx.sortedBy { i -> atan2((v[i] - c) dot e2, (v[i] - c) dot e1) }
    }

    /** Caras del sólido formadas por los vértices que más se acercan a cada normal. */
    private fun facesFromNormals(v: List<Vec3>, normals: List<Vec3>): List<List<Int>> =
        normals.map { n ->
            val best = v.maxOf { it dot n }
            val idx = v.indices.filter { (v[it] dot n) > best - 1e-6 }
            orderAround(v, idx, n)
        }

    private fun icosahedronFaces(v: List<Vec3>): List<List<Int>> {
        val faces = ArrayList<List<Int>>()
        for (i in v.indices) for (j in i + 1 until v.size) for (k in j + 1 until v.size) {
            fun edge(a: Int, b: Int) = abs((v[a] - v[b]).length() - 2.0) < 1e-6
            if (edge(i, j) && edge(j, k) && edge(i, k)) faces += listOf(i, j, k)
        }
        return faces.map { f -> orderAround(v, f, centroid(f.map { v[it] })) }
    }

    private fun solid(sides: Int): Pair<List<Vec3>, List<List<Int>>> = when (sides) {
        6 -> {
            val v = ArrayList<Vec3>()
            for (sx in listOf(1.0, -1.0)) for (sy in listOf(1.0, -1.0)) for (sz in listOf(1.0, -1.0)) {
                v += Vec3(sx, sy, sz)
            }
            val normals = listOf(
                Vec3(1.0, 0.0, 0.0), Vec3(-1.0, 0.0, 0.0),
                Vec3(0.0, 1.0, 0.0), Vec3(0.0, -1.0, 0.0),
                Vec3(0.0, 0.0, 1.0), Vec3(0.0, 0.0, -1.0)
            )
            v to facesFromNormals(v, normals)
        }
        4 -> {
            val v = listOf(Vec3(1.0, 1.0, 1.0), Vec3(1.0, -1.0, -1.0), Vec3(-1.0, 1.0, -1.0), Vec3(-1.0, -1.0, 1.0))
            v to facesFromNormals(v, v.map { it * -1.0 })
        }
        8 -> {
            val v = listOf(
                Vec3(1.0, 0.0, 0.0), Vec3(-1.0, 0.0, 0.0),
                Vec3(0.0, 1.0, 0.0), Vec3(0.0, -1.0, 0.0),
                Vec3(0.0, 0.0, 1.0), Vec3(0.0, 0.0, -1.0)
            )
            val normals = ArrayList<Vec3>()
            for (sx in listOf(1.0, -1.0)) for (sy in listOf(1.0, -1.0)) for (sz in listOf(1.0, -1.0)) {
                normals += Vec3(sx, sy, sz)
            }
            v to facesFromNormals(v, normals)
        }
        12 -> {
            // El dodecaedro es el "dual" del icosaedro: sus vértices son los centros de las
            // caras del icosaedro y cada una de sus caras rodea un vértice del icosaedro.
            val iv = icosahedronVertices()
            val ifaces = icosahedronFaces(iv)
            val v = ifaces.map { f -> centroid(f.map { iv[it] }) }
            val faces = iv.indices.map { vi ->
                val around = ifaces.indices.filter { vi in ifaces[it] }
                orderAround(v, around, iv[vi])
            }
            v to faces
        }
        else -> {
            val v = icosahedronVertices()
            v to icosahedronFaces(v)
        }
    }

    // endregion

    // region Giros

    private fun rotX(a: Double): Mat {
        val c = cos(a); val s = sin(a)
        return arrayOf(doubleArrayOf(1.0, 0.0, 0.0), doubleArrayOf(0.0, c, -s), doubleArrayOf(0.0, s, c))
    }

    private fun rotY(a: Double): Mat {
        val c = cos(a); val s = sin(a)
        return arrayOf(doubleArrayOf(c, 0.0, s), doubleArrayOf(0.0, 1.0, 0.0), doubleArrayOf(-s, 0.0, c))
    }

    private fun rotZ(a: Double): Mat {
        val c = cos(a); val s = sin(a)
        return arrayOf(doubleArrayOf(c, -s, 0.0), doubleArrayOf(s, c, 0.0), doubleArrayOf(0.0, 0.0, 1.0))
    }

    private fun identity(): Mat =
        arrayOf(doubleArrayOf(1.0, 0.0, 0.0), doubleArrayOf(0.0, 1.0, 0.0), doubleArrayOf(0.0, 0.0, 1.0))

    private fun mul(a: Mat, b: Mat): Mat =
        Array(3) { i -> DoubleArray(3) { j -> (0 until 3).sumOf { k -> a[i][k] * b[k][j] } } }

    private fun apply(m: Mat, p: Vec3) = Vec3(
        m[0][0] * p.x + m[0][1] * p.y + m[0][2] * p.z,
        m[1][0] * p.x + m[1][1] * p.y + m[1][2] * p.z,
        m[2][0] * p.x + m[2][1] * p.y + m[2][2] * p.z
    )

    /** Giro (fórmula de Rodrigues) que lleva la dirección [direction] hacia el eje Z. */
    private fun alignToZ(direction: Vec3): Mat {
        val n = direction.normalized()
        val z = Vec3(0.0, 0.0, 1.0)
        val c = n dot z
        if (c > 1 - 1e-9) return identity()
        if (c < -1 + 1e-9) return rotX(PI)
        val k = (n cross z).normalized()
        val angle = acos(c)
        val kk = arrayOf(
            doubleArrayOf(0.0, -k.z, k.y),
            doubleArrayOf(k.z, 0.0, -k.x),
            doubleArrayOf(-k.y, k.x, 0.0)
        )
        val k2 = mul(kk, kk)
        val s = sin(angle)
        val c1 = 1 - cos(angle)
        return Array(3) { i -> DoubleArray(3) { j -> identity()[i][j] + s * kk[i][j] + c1 * k2[i][j] } }
    }

    // endregion

    /** Vista en reposo de un sólido: sus vértices ya girados y cómo se encaja en la caja. */
    private class Frame(
        val rotated: List<Vec3>,
        val faces: List<List<Int>>,
        val cx: Double,
        val cy: Double,
        val scale: Double
    )

    private val frames = HashMap<Int, Frame>()

    private fun frameFor(sides: Int): Frame = synchronized(frames) {
        frames.getOrPut(sides) {
            val view = VIEWS.getValue(sides)
            val (vertices, faces) = solid(sides)

            val front = if (view.vertex != null) vertices[view.vertex] else centroid(faces[0].map { vertices[it] })
            var rotation = alignToZ(front)
            rotation = mul(rotZ(Math.toRadians(view.zDeg)), rotation)
            rotation = mul(rotY(Math.toRadians(view.yDeg)), rotation)
            rotation = mul(rotX(Math.toRadians(view.xDeg)), rotation)
            val rotated = vertices.map { apply(rotation, it) }

            val visible = faces.filter { f -> centroid(f.map { rotated[it] }).normalized().z > 1e-6 }
            val xs = visible.flatMap { f -> f.map { rotated[it].x } }
            val ys = visible.flatMap { f -> f.map { rotated[it].y } }
            Frame(
                rotated = rotated,
                faces = faces,
                cx = (xs.min() + xs.max()) / 2,
                cy = (ys.min() + ys.max()) / 2,
                scale = (1 - 2 * MARGIN) / max(xs.max() - xs.min(), ys.max() - ys.min())
            )
        }
    }

    /** Proyecta las caras visibles del [frame], con un giro [extra] opcional sobre el centro del dado. */
    private fun build(frame: Frame, extra: Mat? = null): List<ProjectedFace> {
        val points = if (extra == null) frame.rotated else frame.rotated.map { apply(extra, it) }

        // Solo se ven las caras cuya normal apunta hacia la cámara.
        val visible = frame.faces.withIndex().filter { (_, f) ->
            centroid(f.map { points[it] }).normalized().z > 1e-6
        }

        return visible.map { (id, f) ->
            val normal = centroid(f.map { points[it] }).normalized()
            val pts = f.map { i ->
                Point2(
                    x = (0.5 + (points[i].x - frame.cx) * frame.scale).toFloat(),
                    y = (0.5 - (points[i].y - frame.cy) * frame.scale).toFloat()
                )
            }
            var twiceArea = 0f
            var perimeter = 0f
            for (i in pts.indices) {
                val a = pts[i]
                val b = pts[(i + 1) % pts.size]
                twiceArea += a.x * b.y - b.x * a.y
                perimeter += sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y))
            }
            val area = abs(twiceArea) / 2f
            ProjectedFace(
                points = pts,
                brightness = max(0.0, normal dot LIGHT).toFloat(),
                area = area,
                center = Point2(pts.map { it.x }.average().toFloat(), pts.map { it.y }.average().toFloat()),
                inradius = if (perimeter > 0f) 2f * area / perimeter else 0f,
                id = id
            )
        }
    }

    private fun compute(sides: Int): List<ProjectedFace> {
        val projected = build(frameFor(sides))

        // El cubo muestra el resultado en la cara de arriba; el resto, en la más grande.
        val mainIndex = if (sides == 6) {
            projected.indices.minBy { projected[it].center.y }
        } else {
            projected.indices.maxBy { projected[it].area }
        }
        return projected.mapIndexed { i, face -> face.copy(isMain = i == mainIndex) }
    }
}
