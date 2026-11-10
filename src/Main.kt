import java.time.LocalDate
import kotlin.math.round

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Promocionable(val descripcion: String = "Promoción especial")

open class Usuario(
    val nombre: String,
    val contacto: String,
    val correo: String
)

class Cliente(
    nombre: String,
    contacto: String,
    correo: String,
    val direccion: String,
    val notas: String?
) : Usuario(nombre, contacto, correo) {
    operator fun component1() = nombre
    operator fun component2() = correo
    operator fun component3() = contacto

    override fun equals(other: Any?): Boolean =
        other is Cliente && nombre.equals(other.nombre, true) && correo.equals(other.correo, true)

    override fun hashCode(): Int = 31 * nombre.lowercase().hashCode() + correo.lowercase().hashCode()
}

class Repartidor(
    nombre: String,
    contacto: String,
    correo: String,
    val zona: String
) : Usuario(nombre, contacto, correo) {
    fun entregarPedido(pedido: Pedido) {
        println("🚚 [Repartidor $nombre] Entregando el pedido a ${pedido.cliente.nombre} en ${pedido.cliente.direccion}...")
        pedido.estado = "Entregado"
    }
}

@Promocionable("Pizza o combo con promoción especial")
class Producto(
    val nombre: String,
    val precio: Double,
    val ingredientesNecesarios: Map<String, Int>,
    val categoria: String = "pizza"
) {
    fun detallarProducto(): String {
        val ingredientes = ingredientesNecesarios.entries.joinToString(", ") { "${it.key}: ${it.value} unid." }
        return "${categoria.replaceFirstChar { it.uppercase() }}: $nombre | Precio: $$precio | Ingredientes: [$ingredientes]"
    }

    override fun equals(other: Any?): Boolean =
        other is Producto && nombre.equals(other.nombre, true) && categoria.equals(other.categoria, true)

    override fun hashCode(): Int = 31 * nombre.lowercase().hashCode() + categoria.lowercase().hashCode()

    operator fun compareTo(other: Producto): Int = nombre.compareTo(other.nombre, ignoreCase = true)
}

class Pedido(
    val fecha: String,
    val cliente: Cliente,
    val productos: List<Pair<Producto, Int>>,
    var estado: String,
    val repartidor: Repartidor?
) {
    operator fun component1() = cliente
    operator fun component2() = productos
    operator fun component3() = calcularTotales()["totalFinal"] ?: 0.0

    operator fun plus(otro: Pedido): Pedido {
        require(cliente == otro.cliente) { "Solo se pueden combinar pedidos del mismo cliente." }
        return Pedido(
            fecha = if (fecha >= otro.fecha) fecha else otro.fecha,
            cliente = cliente,
            productos = productos + otro.productos,
            estado = "Combinado",
            repartidor = repartidor ?: otro.repartidor
        )
    }

    fun calcularTotales(): Map<String, Double> {
        val subtotal = productos.sumOf { (producto, cantidad) -> producto.precio * cantidad }
        val cantidadTotal = productos.sumOf { it.second }
        val descuentoCantidad = if (cantidadTotal > 3) subtotal * 0.10 else 0.0
        val descuentoPromocion = if (estaEnPromocion(fecha)) subtotal * 0.15 else 0.0
        val descuento = descuentoCantidad + descuentoPromocion
        val subtotalConDescuento = subtotal - descuento
        val impuesto = subtotalConDescuento * 0.19
        return mapOf(
            "subtotal" to subtotal,
            "descuento" to descuento,
            "descuentoPromocion" to descuentoPromocion,
            "impuesto" to impuesto,
            "totalFinal" to subtotalConDescuento + impuesto
        )
    }
}

private val correoRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val telefonoRegex = Regex("^\\d{12}$")
private val inicioPromocion = LocalDate.of(2026, 11, 1)
private val finPromocion = LocalDate.of(2026, 11, 30)

fun estaEnPromocion(fecha: String): Boolean = try {
    val fechaPedido = LocalDate.parse(fecha)
    !fechaPedido.isBefore(inicioPromocion) && !fechaPedido.isAfter(finPromocion)
} catch (_: Exception) {
    false
}

fun formatearTelefono(telefono: String): String {
    val digitos = telefono.filter(Char::isDigit)
    require(telefonoRegex.matches(digitos)) {
        "El teléfono debe contener 12 dígitos para el formato +XX (XXX) XXX-XXXX."
    }
    return "+${digitos.substring(0, 2)} (${digitos.substring(2, 5)}) " +
            "${digitos.substring(5, 8)}-${digitos.substring(8, 12)}"
}

fun leerCorreoValido(mensaje: String): String {
    while (true) {
        print(mensaje)
        val correo = readlnOrNull()?.trim().orEmpty()
        if (correoRegex.matches(correo)) return correo
        println("❌ Correo no válido. Use el formato nombre@dominio.com e inténtelo nuevamente.")
    }
}

fun leerTelefonoValido(mensaje: String): String {
    while (true) {
        print(mensaje)
        val telefono = readlnOrNull()?.trim().orEmpty()
        try {
            return formatearTelefono(telefono)
        } catch (e: IllegalArgumentException) {
            println("❌ ${e.message}")
        }
    }
}

fun leerEnteroSeguro(mensaje: String, minimo: Int = 0, maximo: Int? = null): Int {
    while (true) {
        print(mensaje)
        val valor = readlnOrNull()?.trim()?.toIntOrNull()
        if (valor != null && valor >= minimo && (maximo == null || valor <= maximo)) return valor
        val limite = maximo?.let { " y menor o igual a $it" }.orEmpty()
        println("❌ Ingrese un número entero mayor o igual a $minimo$limite.")
    }
}

fun listarReflection(tipo: Class<*>) {
    println("\n--- Reflection de ${tipo.simpleName} ---")
    println("Propiedades: ${tipo.declaredFields.joinToString { it.name }}")
    println("Métodos: ${tipo.declaredMethods.joinToString { it.name }.ifEmpty { "ninguno" }}")
}

fun registrarCliente(clientes: MutableList<Cliente>, nuevo: Cliente): Cliente {
    val existente = clientes.find { it == nuevo }
    if (existente != null) {
        println("ℹ️ Cliente duplicado detectado; se utilizará el registro existente.")
        return existente
    }
    clientes.add(nuevo)
    return nuevo
}

fun registrarProducto(productos: MutableList<Producto>, nuevo: Producto): Producto {
    val existente = productos.find { it == nuevo }
    if (existente != null) return existente
    productos.add(nuevo)
    return nuevo
}

fun imprimirResumen(historial: List<Pedido>, clientes: List<Cliente>, productos: List<Producto>) {
    println("\n================ RESUMEN DE FUNCIONALIDADES ================")
    if (historial.size >= 2 && historial[0].cliente == historial[1].cliente) {
        val combinado = historial[0] + historial[1]
        println("Pedidos combinados mediante +: ${combinado.productos.size} líneas, total $${
            round(combinado.component3())
        }")
    } else {
        println("Pedidos combinados mediante +: se requieren dos pedidos del mismo cliente.")
    }
    clientes.firstOrNull()?.let { (nombre, correo, telefono) ->
        println("Cliente desestructurado: nombre=$nombre | correo=$correo | teléfono=$telefono")
    }
    productos.filter { it::class.java.isAnnotationPresent(Promocionable::class.java) }
        .forEach { println("Producto promocionable: ${it.nombre}") }
    if (clientes.size >= 2) println("Igualdad de clientes: ${clientes[0] == clientes[1]}")
    if (productos.size >= 2) println("Igualdad de productos: ${productos[0] == productos[1]}")
    println("============================================================")
}

fun main() {
    val inventario = mutableMapOf(
        "carne" to 10, "salchicha" to 10, "salame" to 10, "queso" to 25,
        "piña" to 15, "jamon" to 15, "aceitunas" to 15
    )
    val repartidoresDisponibles = listOf(
        Repartidor("Carlos Pérez", "569123456789", "carlos@pizzeria.cl", "Zona Norte"),
        Repartidor("Ana Gómez", "569876543210", "ana@pizzeria.cl", "Zona Centro"),
        Repartidor("Mateo Soto", "569555544332", "mateo@pizzeria.cl", "Zona Sur")
    )
    val productosRegistrados = mutableListOf<Producto>()
    val catalogoPizzas = listOf(
        registrarProducto(productosRegistrados, Producto("Pizza de Carnes", 9990.0,
            mapOf("carne" to 1, "salchicha" to 1, "salame" to 1, "queso" to 1))),
        registrarProducto(productosRegistrados, Producto("Pizza Hawaiana", 8990.0,
            mapOf("queso" to 1, "piña" to 1, "jamon" to 1, "aceitunas" to 1)))
    )
    val historialPedidos = mutableListOf<Pedido>()
    val clientesRegistrados = mutableListOf<Cliente>()

    while (true) {
        println("\n==================================================")
        println("          SISTEMA DE GESTIÓN - PIZZERIA")
        println("==================================================")
        println("[1] Ingresar nuevo pedido")
        println("[2] Revisión y gestión de stock de inventario")
        println("[3] Filtrar pedidos por estado y actualizar")
        println("[4] Generar reporte por día")
        println("[5] Acceder a clientes y reflection")
        println("[6] Acceder a pedidos y generar resumen")
        println("[7] Salir del sistema")
        print("Seleccione una opción: ")

        when (readlnOrNull()?.trim()) {
            "1" -> {
                println("\n--- NUEVO PEDIDO ---")
                print("Ingrese la fecha (AAAA-MM-DD o Enter para hoy): ")
                val fechaInput = readlnOrNull()?.trim()
                val fechaPedido = if (fechaInput.isNullOrEmpty()) LocalDate.now().toString() else fechaInput
                print("Ingrese el nombre del cliente: ")
                val nombreCliente = readlnOrNull()?.trim()?.takeIf(String::isNotEmpty) ?: "Cliente General"
                val telefonoCliente = leerTelefonoValido("Ingrese teléfono (12 dígitos, ejemplo 569123456789): ")
                val correoCliente = leerCorreoValido("Ingrese correo electrónico: ")
                print("Ingrese la dirección de entrega: ")
                val direccionCliente = readlnOrNull()?.trim()?.takeIf(String::isNotEmpty) ?: "Dirección por definir"
                print("Ingrese notas adicionales (opcional): ")
                val notas = readlnOrNull()?.trim()?.takeIf(String::isNotEmpty) ?: "Sin notas"
                val cliente = registrarCliente(
                    clientesRegistrados,
                    Cliente(nombreCliente, telefonoCliente, correoCliente, direccionCliente, notas)
                )

                println("\n--- Menú de Pizzas Disponibles ---")
                catalogoPizzas.forEachIndexed { index, producto ->
                    println("[${index + 1}] ${producto.detallarProducto()}")
                }
                val opcionPizza = leerEnteroSeguro("Seleccione la pizza (1-2): ", 1, catalogoPizzas.size)
                val pizzaSeleccionada = catalogoPizzas[opcionPizza - 1]
                val cantidadPizzas = leerEnteroSeguro("Ingrese cantidad de productos (1-100): ", 1, 100)

                while (true) {
                    val faltantes = pizzaSeleccionada.ingredientesNecesarios.mapNotNull { (ingrediente, porUnidad) ->
                        val necesario = porUnidad * cantidadPizzas
                        val disponible = inventario[ingrediente] ?: 0
                        if (disponible < necesario) ingrediente to (necesario to disponible) else null
                    }.toMap()
                    if (faltantes.isEmpty()) break
                    println("⚠️ Stock insuficiente:")
                    faltantes.forEach { (ingrediente, cantidades) ->
                        println("• $ingrediente: se necesitan ${cantidades.first}, quedan ${cantidades.second}")
                        print("¿Desea reabastecerlo? (s/n): ")
                        if (readlnOrNull()?.trim()?.lowercase() == "s") {
                            val extra = leerEnteroSeguro("Unidades a comprar: ", 1, 100)
                            inventario[ingrediente] = (inventario[ingrediente] ?: 0) + extra
                        }
                    }
                }
                pizzaSeleccionada.ingredientesNecesarios.forEach { (ingrediente, porUnidad) ->
                    inventario[ingrediente] = inventario[ingrediente]!! - porUnidad * cantidadPizzas
                }
                val repartidor = repartidoresDisponibles.find { it.zona == "Zona Centro" }
                    ?: repartidoresDisponibles.first()
                val pedido = Pedido(fechaPedido, cliente, listOf(pizzaSeleccionada to cantidadPizzas), "En preparación", repartidor)
                historialPedidos.add(pedido)
                val totales = pedido.calcularTotales()
                println("\n✅ PEDIDO REGISTRADO")
                println("Cliente: ${cliente.nombre} | Tel: ${cliente.contacto} | Correo: ${cliente.correo}")
                println("Producto: ${pizzaSeleccionada.nombre} x $cantidadPizzas")
                println("Subtotal: $${round(totales["subtotal"]!!)}")
                println("Descuento total: $${round(totales["descuento"]!!)}")
                println("Descuento promocional por rango: $${round(totales["descuentoPromocion"]!!)}")
                println("IVA (19%): $${round(totales["impuesto"]!!)} | Total: $${round(totales["totalFinal"]!!)}")
                println("Estado: ${pedido.estado} | Repartidor: ${repartidor.nombre}")
            }
            "2" -> {
                println("\n--- INVENTARIO ---")
                inventario.forEach { (ingrediente, cantidad) -> println("• $ingrediente: $cantidad") }
                print("¿Desea reabastecer? (s/n): ")
                if (readlnOrNull()?.trim()?.lowercase() == "s") {
                    print("Ingrediente exacto: ")
                    val ingrediente = readlnOrNull()?.trim().orEmpty()
                    if (inventario.containsKey(ingrediente)) {
                        val extra = leerEnteroSeguro("Cantidad (1-100): ", 1, 100)
                        inventario[ingrediente] = inventario.getValue(ingrediente) + extra
                    } else println("❌ El ingrediente no existe.")
                }
            }
            "3" -> {
                if (historialPedidos.isEmpty()) {
                    println("ℹ️ No hay pedidos registrados.")
                } else {
                    print("Estado a filtrar (1 preparación, 2 listo, 3 camino, 4 entregado, 5 todos): ")
                    val estado = when (readlnOrNull()?.trim()) {
                        "1" -> "En preparación"; "2" -> "Listo"; "3" -> "En camino"; "4" -> "Entregado"; "5" -> "TODOS"; else -> null
                    }
                    if (estado == null) println("❌ Opción no válida.")
                    else {
                        val pedidosFiltrados = historialPedidos.filter { estado == "TODOS" || it.estado == estado }
                        pedidosFiltrados.forEachIndexed { index, pedido ->
                            println("[$index] ${pedido.fecha} | ${pedido.cliente.nombre} | ${pedido.estado} | $${
                                round(pedido.component3())
                            }")
                        }
                        if (pedidosFiltrados.isNotEmpty()) {
                            print("¿Desea actualizar un estado? (s/n): ")
                            if (readlnOrNull()?.trim()?.lowercase() == "s") {
                                val indice = leerEnteroSeguro("Índice del pedido: ", 0, pedidosFiltrados.lastIndex)
                                println("[1] En preparación [2] Listo [3] En camino [4] Entregado")
                                pedidosFiltrados[indice].estado = when (readlnOrNull()?.trim()) {
                                    "1" -> "En preparación"
                                    "2" -> "Listo"
                                    "3" -> "En camino"
                                    "4" -> "Entregado"
                                    else -> pedidosFiltrados[indice].estado
                                }
                                println("✅ Estado actualizado: ${pedidosFiltrados[indice].estado}")
                            }
                        }
                    }
                }
            }
            "4" -> {
                print("Fecha a consultar (AAAA-MM-DD o Enter para hoy): ")
                val fecha = readlnOrNull()?.trim().takeUnless { it.isNullOrEmpty() } ?: LocalDate.now().toString()
                val pedidos = historialPedidos.filter { it.fecha == fecha }
                println("\n--- REPORTE $fecha ---")
                pedidos.forEach { pedido ->
                    val (_, productos, total) = pedido
                    println("${pedido.cliente.nombre}: ${productos.joinToString { "${it.first.nombre} x${it.second}" }} | Total: $${round(total)}")
                }
                println("Pedidos: ${pedidos.size}")
            }
            "5" -> {
                println("\n--- CLIENTES REGISTRADOS ---")
                clientesRegistrados.forEachIndexed { index, cliente ->
                    val (nombre, correo, telefono) = cliente
                    println("[$index] $nombre | $correo | $telefono")
                }
                listarReflection(Cliente::class.java)
                listarReflection(Producto::class.java)
            }
            "6" -> {
                if (historialPedidos.size >= 2 && historialPedidos[0].cliente == historialPedidos[1].cliente) {
                    val combinado = historialPedidos[0] + historialPedidos[1]
                    val (cliente, productos, total) = combinado
                    println("Pedido combinado: cliente=${cliente.nombre}, productos=${productos.size}, total=$${round(total)}")
                }
                imprimirResumen(historialPedidos, clientesRegistrados, productosRegistrados)
            }
            "7" -> {
                println("👋 Saliendo del sistema de pizzería. ¡Hasta luego!")
                break
            }
            else -> println("❌ Opción no válida, intente de nuevo.")
        }
    }
}