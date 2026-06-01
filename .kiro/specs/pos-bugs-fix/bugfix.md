# Bugfix Requirements Document

## Introduction

Se han identificado 4 bugs en el sistema POS AWS que impiden el correcto funcionamiento del flujo de caja. Los bugs afectan: la recuperación de estado tras un error (frontend), los re-renders infinitos en la búsqueda de productos (frontend), la búsqueda de productos por código (frontend) y el rechazo incorrecto de ventas válidas por diferencias de redondeo en el cálculo del IVA (backend Lambda).

---

## Bug Analysis

---

### Bug 1 — estadoPrevio no se guarda antes de ir a ERROR

#### Current Behavior (Defect)

1.1 WHEN ocurre un error en cualquier estado del flujo POS (BUSCANDO, RESULTADOS, CARRITO_ACTIVO, etc.) THEN el sistema llama a `setError` y transiciona a `ERROR` sin guardar el estado anterior en `estadoPrevio`

1.2 WHEN el usuario llama a `clearError` después de un error THEN el sistema intenta leer `estadoPrevio` que siempre es `null` y retorna al estado `IDLE` en lugar del estado previo al error

1.3 WHEN ocurre un error durante la carga inicial (estado `BUSCANDO`) THEN el sistema no puede recuperarse al estado correcto y el usuario queda en `IDLE` sin posibilidad de reintentar la búsqueda

#### Expected Behavior (Correct)

2.1 WHEN ocurre un error en cualquier estado del flujo POS THEN el sistema SHALL guardar el estado actual en `estadoPrevio` antes de transicionar a `ERROR`

2.2 WHEN el usuario llama a `clearError` después de un error THEN el sistema SHALL restaurar el estado al valor guardado en `estadoPrevio` (no a `IDLE` por defecto)

2.3 WHEN ocurre un error durante la carga inicial (estado `BUSCANDO`) THEN el sistema SHALL restaurar el estado a `BUSCANDO` al limpiar el error, permitiendo al usuario reintentar

#### Unchanged Behavior (Regression Prevention)

3.1 WHEN no hay error activo y el usuario navega entre estados válidos THEN el sistema SHALL CONTINUE TO respetar las transiciones definidas en `TRANSICIONES_VALIDAS`

3.2 WHEN se llama a `clearError` y `estadoPrevio` es `null` (caso de error en estado inicial `IDLE`) THEN el sistema SHALL CONTINUE TO retornar a `IDLE` como estado de fallback

3.3 WHEN se produce una venta exitosa y se llama a `resetVenta` THEN el sistema SHALL CONTINUE TO retornar al estado `IDLE` sin afectar la lógica de `estadoPrevio`

---

### Bug 2 — productoPort inestable en useSearch causa re-renders infinitos

#### Current Behavior (Defect)

1.4 WHEN el componente padre que usa `useSearch` recrea la instancia de `productoPort` en cada render THEN el `useEffect` de `useSearch` se dispara en bucle infinito porque `productoPort` es una dependencia del efecto y cambia en cada render

1.5 WHEN el bucle de re-renders se activa THEN el sistema realiza llamadas HTTP repetidas e incontroladas al API de productos, degradando el rendimiento y potencialmente saturando el backend

#### Expected Behavior (Correct)

2.4 WHEN `useSearch` recibe `productoPort` como parámetro THEN el sistema SHALL garantizar que la referencia a `productoPort` sea estable entre renders (singleton o referencia memoizada), evitando que el `useEffect` se dispare innecesariamente

2.5 WHEN el componente que usa `useSearch` se re-renderiza por causas no relacionadas con la búsqueda THEN el sistema SHALL ejecutar el efecto de búsqueda únicamente cuando cambie `query`, no cuando cambie `productoPort`

#### Unchanged Behavior (Regression Prevention)

3.4 WHEN el usuario escribe una nueva query THEN el sistema SHALL CONTINUE TO ejecutar la búsqueda con debounce de 300 ms

3.5 WHEN `productoPort` es el singleton `productoAdapter` exportado desde `main.tsx` THEN el sistema SHALL CONTINUE TO funcionar correctamente sin re-renders adicionales

3.6 WHEN la query tiene menos de 2 caracteres THEN el sistema SHALL CONTINUE TO limpiar los resultados y retornar a `IDLE`

---

### Bug 3 — Búsqueda por código de producto no implementada

#### Current Behavior (Defect)

1.6 WHEN el usuario escribe una query que contiene el carácter `-` (patrón de código de producto, ej. `LAP-001`) THEN el sistema construye la URL con `type=name` en lugar de `type=code`

1.7 WHEN se realiza una búsqueda con `type=name` usando un código de producto THEN el sistema no retorna resultados porque el backend busca por nombre y los códigos no coinciden con nombres

#### Expected Behavior (Correct)

2.6 WHEN el usuario escribe una query que contiene el carácter `-` THEN el sistema SHALL construir la URL con `type=code` para buscar por código de producto

2.7 WHEN el usuario escribe una query que no contiene `-` THEN el sistema SHALL construir la URL con `type=name` para buscar por nombre de producto

2.8 WHEN se realiza una búsqueda con `type=code` usando un código válido (ej. `LAP-001`) THEN el sistema SHALL retornar los productos cuyo código coincida con la query

#### Unchanged Behavior (Regression Prevention)

3.7 WHEN la query está vacía THEN el sistema SHALL CONTINUE TO usar `type=all` para traer todos los productos

3.8 WHEN el usuario busca por nombre (sin `-`) THEN el sistema SHALL CONTINUE TO usar `type=name` y retornar resultados por nombre

3.9 WHEN la búsqueda retorna resultados THEN el sistema SHALL CONTINUE TO mapear la respuesta de la Lambda al formato `Producto` esperado por el store

---

### Bug 4 — Diferencia de redondeo en IVA rechaza ventas válidas

#### Current Behavior (Defect)

1.8 WHEN el frontend calcula el total con IVA y lo muestra al usuario, y el usuario ingresa exactamente ese monto como `amountPaid` THEN puede existir una diferencia de 1 centavo entre el cálculo del frontend y el de la Lambda por errores de punto flotante

1.9 WHEN la diferencia de redondeo produce que `change` sea un valor negativo muy pequeño (ej. `-0.001`) THEN el sistema lanza `IllegalArgumentException("Monto insuficiente")` y rechaza una venta que en realidad está correctamente pagada

#### Expected Behavior (Correct)

2.9 WHEN se compara `amountPaid` con `total` para determinar si el pago es suficiente THEN el sistema SHALL convertir ambos valores a enteros en centavos usando `Math.round(value * 100)` antes de comparar, eliminando errores de punto flotante

2.10 WHEN `Math.round(amountPaid * 100) >= Math.round(total * 100)` THEN el sistema SHALL aceptar la venta como correctamente pagada sin lanzar excepción

2.11 WHEN `Math.round(amountPaid * 100) < Math.round(total * 100)` THEN el sistema SHALL rechazar la venta con el mensaje "Monto insuficiente"

#### Unchanged Behavior (Regression Prevention)

3.10 WHEN el monto pagado es genuinamente menor al total (diferencia mayor a 1 centavo) THEN el sistema SHALL CONTINUE TO rechazar la venta con `IllegalArgumentException`

3.11 WHEN la venta es aceptada THEN el sistema SHALL CONTINUE TO calcular el vuelto (`change`) y persistir la venta con el total correcto

3.12 WHEN se procesan ventas con subtotales y IVA del 19% THEN el sistema SHALL CONTINUE TO calcular `iva = Math.round(subtotal * IVA * 100.0) / 100.0` y `total = Math.round((subtotal + iva) * 100.0) / 100.0`

---

## Bug Condition Summary

### Bug 1 — estadoPrevio

```pascal
FUNCTION isBugCondition_B1(accion)
  INPUT: accion de tipo string
  OUTPUT: boolean
  RETURN accion = 'setError' AND estadoPrevio = null
END FUNCTION

// Fix Checking
FOR ALL estado WHERE isBugCondition_B1(estado) DO
  result ← setError'(error)
  ASSERT result.estadoPrevio = estadoAnterior AND result.estado = 'ERROR'
END FOR

// Preservation Checking
FOR ALL accion WHERE NOT isBugCondition_B1(accion) DO
  ASSERT F(accion) = F'(accion)
END FOR
```

### Bug 2 — productoPort inestable

```pascal
FUNCTION isBugCondition_B2(productoPort)
  INPUT: productoPort de tipo IProductoPort
  OUTPUT: boolean
  RETURN productoPort es una nueva instancia en cada render
END FUNCTION

// Fix Checking
FOR ALL render WHERE isBugCondition_B2(productoPort) DO
  ASSERT useEffect NO se dispara por cambio de productoPort
END FOR

// Preservation Checking
FOR ALL render WHERE NOT isBugCondition_B2(productoPort) DO
  ASSERT comportamiento de búsqueda es idéntico al original
END FOR
```

### Bug 3 — Búsqueda por código

```pascal
FUNCTION isBugCondition_B3(query)
  INPUT: query de tipo string
  OUTPUT: boolean
  RETURN query.contains('-')
END FUNCTION

// Fix Checking
FOR ALL query WHERE isBugCondition_B3(query) DO
  url ← buildUrl'(query)
  ASSERT url.contains('type=code')
END FOR

// Preservation Checking
FOR ALL query WHERE NOT isBugCondition_B3(query) AND query != '' DO
  url ← buildUrl'(query)
  ASSERT url.contains('type=name')
END FOR
```

### Bug 4 — Redondeo IVA

```pascal
FUNCTION isBugCondition_B4(amountPaid, total)
  INPUT: amountPaid y total de tipo double
  OUTPUT: boolean
  RETURN Math.round(amountPaid * 100) >= Math.round(total * 100)
         AND (amountPaid - total) < 0
END FUNCTION

// Fix Checking
FOR ALL pago WHERE isBugCondition_B4(pago.amountPaid, pago.total) DO
  result ← createSale'(pago)
  ASSERT result NO lanza IllegalArgumentException
END FOR

// Preservation Checking
FOR ALL pago WHERE amountPaid < total - 0.005 DO
  ASSERT createSale'(pago) lanza IllegalArgumentException
END FOR
```
