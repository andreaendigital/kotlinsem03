# Sistema de gestión de pizzería - Semana 03

Aplicación de consola desarrollada en Kotlin para la tarea sumativa de la semana 03. El sistema permite gestionar clientes, productos (pizzas e ingredientes), pedidos e inventario, integrando estructuras fundamentales de Kotlin, manejo de errores y principios de programación orientada a objetos.

## Características generales

- Registro de clientes, productos y pedidos.
- Catálogo de pizzas con precios e ingredientes necesarios.
- Control y reabastecimiento básico del inventario.
- Validación de stock antes de registrar un pedido.
- Cálculo de subtotales, descuentos, IVA y total final.
- Actualización y filtrado de pedidos por estado.
- Generación de reportes diarios.
- Manejo de errores, valores nulos y entradas inválidas.

## Requerimientos implementados

### Paso 1: Regex y ranges

- Validación de correos electrónicos con expresiones regulares.
- Solicitud de reingreso cuando el correo no cumple el formato esperado.
- Formateo de teléfonos al estilo `+XX (XXX) XXX-XXXX`.
- Validación de cantidades de productos dentro del rango de `1..100`.
- Verificación de fechas dentro del periodo promocional del `01-11-2026` al `30-11-2026`.
- Aplicación de un descuento promocional del 15% cuando el pedido está dentro del periodo.

### Paso 2: Anotaciones y reflection

- Anotación personalizada `@Promocionable`.
- Identificación dinámica de productos que pueden acceder a promociones.
- Listado de propiedades y métodos de las clases `Cliente` y `Producto` mediante reflection.

### Paso 3: Sobrecarga de operadores

- Sobrecarga del operador `+` en `Pedido` para combinar pedidos del mismo cliente.
- Recalculo del total del pedido combinado, incluyendo descuentos e impuestos.
- Sobrecarga de `==` mediante `equals()` en `Producto`.
- Comparación de productos utilizando su nombre y categoría.

### Paso 4: Declaraciones de desestructuración

- Desestructuración de `Cliente` para obtener nombre, correo y teléfono.
- Desestructuración de `Pedido` para obtener cliente, productos y total.

### Paso 5: Evaluación de igualdad de objetos

- Implementación de `equals()` y `hashCode()` en `Cliente` y `Producto`.
- Clientes considerados iguales por nombre y correo.
- Productos considerados iguales por nombre y categoría.
- Prevención de duplicados al registrar clientes y productos.

### Paso 6: Resumen y ejecución

La opción de resumen de la aplicación muestra en la consola:

- Pedidos combinados mediante el operador `+`.
- Información de un cliente desestructurado.
- Productos identificados como promocionables.
- Evaluación de igualdad entre clientes y productos.

## Menú del sistema

1. Ingresar un nuevo pedido.
2. Revisar y gestionar el inventario.
3. Filtrar y actualizar pedidos.
4. Generar un reporte por día.
5. Acceder a clientes y reflection.
6. Acceder a pedidos y generar el resumen de funcionalidades.
7. Salir del sistema.

## Conceptos de Kotlin utilizados

- Variables, strings, condicionales y ciclos.
- Funciones y validación de datos.
- Arreglos y colecciones mutables e inmutables.
- Clases, constructores, propiedades y herencia.
- Sobrecarga de operadores.
- Desestructuración de objetos.
- Anotaciones personalizadas.
- Reflection.
- Manejo de errores con `try/catch`.
- Manejo de valores nulos y operador Elvis.
- Expresiones regulares y rangos.
- Manejo de fechas con `LocalDate`.

## Requisitos y ejecución

- IntelliJ IDEA.
- Kotlin/JVM.
- JDK compatible con la versión de Kotlin configurada.

Para ejecutar el proyecto:

1. Abrir el proyecto en IntelliJ IDEA.
2. Abrir `src/Main.kt`.
3. Ejecutar la función `main`.
4. Utilizar las opciones disponibles en la consola.

## Estructura principal

```text
Sem03/
├── src/
│   └── Main.kt
├── .gitignore
├── Sem03.iml
└── README.md
```

## Objetivo académico

Aplicar los aprendizajes de Kotlin y POO en una solución funcional para la gestión de una pizzería, demostrando validación de datos, manejo de errores, control de inventario, cálculo de totales y uso de características avanzadas del lenguaje.
