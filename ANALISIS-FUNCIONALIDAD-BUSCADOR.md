# Análisis de Funcionalidad del Buscador - Requisitos Pendientes

## Fecha: 1 de junio de 2026

---

## 1. ANÁLISIS DE DOCUMENTACIÓN ACTUAL

### 1.1 Lo que dice el Requirement 2 (SPEC-001)

**Criterios de Aceptación actuales:**

1. ✅ **Mínimo 2 caracteres**: "WHEN el Cajero escribe menos de 2 caracteres en el SearchBar, THE System SHALL mantener el UIState en `IDLE` sin invocar `IProductoPort.buscar`"

2. ✅ **Búsqueda después de 2 caracteres**: "WHEN el Cajero escribe 2 o más caracteres en el SearchBar, THE System SHALL transicionar el UIState a `BUSCANDO` e invocar `IProductoPort.buscar` con el texto ingresado"

3. ❌ **NO menciona mostrar todos los productos al inicio**

4. ❌ **NO menciona búsqueda por código de producto**

5. ❌ **NO menciona auto-agregar al carrito con código completo (lector de barras)**

6. ❌ **NO menciona navegación completa por teclado**

### 1.2 Lo que dice el Bug 3 (Bugfix)

**Bug identificado:**
- "WHEN el usuario escribe una query que contiene el carácter `-` (patrón de código de producto, ej. `LAP-001`) THEN el sistema construye la URL con `type=name` en lugar de `type=code`"

**Fix implementado:**
- ✅ Detecta queries con `-` y usa `type=code`
- ✅ Búsqueda por código funciona DESPUÉS de escribir 2 caracteres

**Limitaciones del fix actual:**
- ❌ NO implementa auto-agregar al carrito cuando se pega código completo
- ❌ NO diferencia entre escritura manual vs. paste (lector de barras)

---

## 2. REQUISITOS DEL EXAMEN (PDFs)

### 2.1 Examen Frontend (Desarrollo avanzado aplicaciones en red)

**Punto 4 - Desarrollar el frontend:**
> "Vista de productos: consumir GET /productos y mostrar listado con nombre, precio y opción de selección"

**Interpretación:**
- ✅ Debe mostrar productos
- ⚠️ NO especifica si debe mostrar TODOS al inicio o solo después de buscar

### 2.2 Requisito del Profesor (verbal)

**"El sistema debe ser manejable completamente desde teclado"**

Esto implica:
- ❌ Navegación entre productos con flechas arriba/abajo
- ❌ Agregar producto con Enter
- ❌ Enfocar búsqueda con atajo (ej. Ctrl+F o F3)
- ❌ Navegar entre secciones con Tab
- ❌ Confirmar venta con atajo (ej. F9)
- ❌ Cancelar/volver con Escape

---

## 3. ANÁLISIS DE GAPS (LO QUE FALTA)

### Gap 1: Mostrar todos los productos al inicio ⚠️

**Estado actual:**
- El buscador NO muestra productos hasta escribir 2 caracteres
- El estado inicial es `IDLE` sin productos visibles

**Requisito sugerido:**
- Al cargar la aplicación, debería mostrar TODOS los productos (GET /productos?type=all)
- Esto facilita la navegación sin necesidad de buscar

**Justificación:**
- En un POS real, el cajero necesita ver el catálogo completo
- Facilita la selección rápida de productos comunes
- Mejora la UX para usuarios que no saben el nombre exacto

**Prioridad:** 🟡 MEDIA (mejora UX pero no es crítico para el examen)

---

### Gap 2: Búsqueda por código con mínimo 2 caracteres ✅

**Estado actual:**
- ✅ Bug 3 ya implementa búsqueda por código detectando `-`
- ✅ Funciona después de 2 caracteres

**Requisito cumplido:** SÍ

**Ejemplo:**
- Usuario escribe "LA" → no busca (menos de 2 caracteres)
- Usuario escribe "LAP" → busca por nombre (no tiene `-`)
- Usuario escribe "LA-" → busca por código (tiene `-`, 3 caracteres)
- Usuario escribe "LAP-001" → busca por código (tiene `-`, 7 caracteres)

---

### Gap 3: Auto-agregar al carrito con código completo (lector de barras) 🔴

**Estado actual:**
- ❌ NO implementado
- El sistema solo MUESTRA el producto, el usuario debe hacer clic en "Agregar"

**Requisito del profesor:**
- Cuando se pega un código completo (ej. "LAP-001"), el producto debe agregarse AUTOMÁTICAMENTE al carrito
- Simula el comportamiento de un lector de código de barras

**Casos de uso:**

| Escenario | Comportamiento actual | Comportamiento esperado |
|-----------|----------------------|-------------------------|
| Usuario escribe "LAP-001" manualmente | Muestra producto, espera clic "Agregar" | Muestra producto, espera clic "Agregar" |
| Usuario pega "LAP-001" (Ctrl+V) | Muestra producto, espera clic "Agregar" | **Agrega automáticamente al carrito** |
| Lector de barras escanea "LAP-001" | Muestra producto, espera clic "Agregar" | **Agrega automáticamente al carrito** |

**Implementación sugerida:**

```typescript
// En useSearch hook
const handlePaste = (event: ClipboardEvent) => {
  const pastedText = event.clipboardData?.getData('text');
  
  // Detectar si es un código de producto completo (patrón: XXX-NNN)
  const isProductCode = /^[A-Z]{3}-\d{3}$/.test(pastedText || '');
  
  if (isProductCode) {
    // Buscar producto por código
    const producto = await productoPort.buscar(pastedText);
    
    if (producto.length === 1) {
      // Auto-agregar al carrito
      store.agregarAlCarrito(producto[0]);
      
      // Limpiar búsqueda
      setQuery('');
      
      // Mostrar notificación
      toast.success(`${producto[0].nombre} agregado al carrito`);
    }
  }
};
```

**Prioridad:** 🔴 ALTA (requisito explícito del profesor)

---

### Gap 4: Navegación completa por teclado 🔴

**Estado actual:**
- ❌ NO implementado
- El sistema requiere mouse para todas las operaciones

**Requisito del profesor:**
- "El sistema debe ser manejable completamente desde teclado"

**Atajos de teclado sugeridos:**

| Atajo | Acción | Contexto |
|-------|--------|----------|
| **F3** o **Ctrl+F** | Enfocar campo de búsqueda | Cualquier pantalla |
| **↑ / ↓** | Navegar entre productos | Lista de productos |
| **Enter** | Agregar producto seleccionado al carrito | Producto enfocado |
| **Tab** | Navegar entre secciones (búsqueda → lista → carrito → pago) | Flujo de venta |
| **Shift+Tab** | Navegar hacia atrás | Flujo de venta |
| **Escape** | Cancelar/volver | Cualquier modal o pantalla |
| **F9** | Confirmar venta | Pantalla de pago |
| **F10** | Abrir historial | Pantalla principal |
| **+** | Incrementar cantidad | Producto en carrito |
| **-** | Decrementar cantidad | Producto en carrito |
| **Delete** | Eliminar producto del carrito | Producto en carrito |

**Implementación sugerida:**

```typescript
// Hook global para atajos de teclado
const useKeyboardShortcuts = () => {
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      // F3 o Ctrl+F: Enfocar búsqueda
      if (event.key === 'F3' || (event.ctrlKey && event.key === 'f')) {
        event.preventDefault();
        document.getElementById('search-input')?.focus();
      }
      
      // F9: Confirmar venta
      if (event.key === 'F9' && store.estado === 'CALCULANDO_PAGO') {
        event.preventDefault();
        store.confirmarVenta();
      }
      
      // Escape: Cancelar/volver
      if (event.key === 'Escape') {
        event.preventDefault();
        store.cancelar();
      }
      
      // Flechas arriba/abajo: Navegar productos
      if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
        event.preventDefault();
        navegarProductos(event.key === 'ArrowDown' ? 1 : -1);
      }
      
      // Enter: Agregar producto seleccionado
      if (event.key === 'Enter' && productoSeleccionado) {
        event.preventDefault();
        store.agregarAlCarrito(productoSeleccionado);
      }
    };
    
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [store.estado, productoSeleccionado]);
};
```

**Prioridad:** 🔴 ALTA (requisito explícito del profesor)

---

## 4. RESUMEN DE IMPLEMENTACIÓN PENDIENTE

### 4.1 Prioridad ALTA (Crítico para aprobación)

#### A. Auto-agregar al carrito con código completo (lector de barras)

**Archivos a modificar:**
- `src/application/hooks/useSearch.ts`
- `src/ui/components/SearchBar.tsx`

**Tareas:**
1. Detectar evento `paste` en el campo de búsqueda
2. Validar si el texto pegado es un código de producto completo (patrón: `XXX-NNN`)
3. Si es código completo:
   - Buscar producto por código
   - Si se encuentra exactamente 1 producto, agregarlo automáticamente al carrito
   - Limpiar campo de búsqueda
   - Mostrar notificación de éxito
4. Si no es código completo, comportamiento normal (mostrar resultados)

**Tiempo estimado:** 2 horas

---

#### B. Navegación completa por teclado

**Archivos a modificar:**
- `src/application/hooks/useKeyboardShortcuts.ts` (nuevo)
- `src/ui/components/ProductList.tsx`
- `src/ui/components/Cart.tsx`
- `src/ui/components/PaymentPanel.tsx`
- `src/ui/POSApp.tsx`

**Tareas:**
1. Crear hook `useKeyboardShortcuts` con todos los atajos
2. Implementar navegación con flechas en ProductList (índice seleccionado)
3. Implementar Enter para agregar producto seleccionado
4. Implementar F3/Ctrl+F para enfocar búsqueda
5. Implementar F9 para confirmar venta
6. Implementar Escape para cancelar/volver
7. Implementar +/- para modificar cantidades en carrito
8. Implementar Delete para eliminar del carrito
9. Agregar indicadores visuales de producto seleccionado (highlight)
10. Agregar tooltip con atajos disponibles en cada pantalla

**Tiempo estimado:** 4-5 horas

---

### 4.2 Prioridad MEDIA (Mejora UX)

#### C. Mostrar todos los productos al inicio

**Archivos a modificar:**
- `src/application/hooks/useSearch.ts`
- `src/application/store/usePOSStore.ts`

**Tareas:**
1. Al cargar la aplicación (estado `IDLE`), invocar `IProductoPort.buscar('')` (query vacía = type=all)
2. Mostrar todos los productos en ProductList
3. Cuando el usuario empieza a escribir (≥2 caracteres), filtrar resultados

**Tiempo estimado:** 1 hora

---

## 5. ACTUALIZACIÓN DE SPECS REQUERIDA

### 5.1 Actualizar requirements.md

**Agregar nuevo requisito:**

```markdown
### Requirement 20: Barcode Scanner Support (SPEC-016)

**User Story:** As a Cashier, I want to scan product barcodes to add them automatically to the cart, to speed up the checkout process.

#### Acceptance Criteria

1. WHEN the Cashier pastes a complete product code (pattern: XXX-NNN) in the SearchBar, THE System SHALL detect it as a barcode scan.
2. WHEN a barcode is detected, THE System SHALL invoke `IProductoPort.buscar` with `type=code` and the scanned code.
3. WHEN the barcode search returns exactly 1 product, THE System SHALL add that product to the Cart automatically without requiring a click.
4. WHEN the product is auto-added, THE System SHALL clear the SearchBar and show a success notification.
5. WHEN the barcode search returns 0 or more than 1 product, THE System SHALL show the results in ProductList without auto-adding.
6. THE System SHALL differentiate between manual typing and paste/scan events.
```

**Agregar nuevo requisito:**

```markdown
### Requirement 21: Keyboard Navigation (SPEC-017)

**User Story:** As a Cashier, I want to operate the entire POS system using only the keyboard, to maximize checkout speed without using the mouse.

#### Acceptance Criteria

1. WHEN the Cashier presses F3 or Ctrl+F, THE System SHALL focus the SearchBar input field.
2. WHEN the Cashier presses ↑ or ↓ in ProductList, THE System SHALL navigate between products and highlight the selected one.
3. WHEN the Cashier presses Enter with a product selected, THE System SHALL add that product to the Cart.
4. WHEN the Cashier presses F9 in `CALCULANDO_PAGO` state, THE System SHALL confirm the sale.
5. WHEN the Cashier presses Escape, THE System SHALL cancel the current operation or return to the previous state.
6. WHEN the Cashier presses + or - on a CartItem, THE System SHALL increment or decrement the quantity.
7. WHEN the Cashier presses Delete on a CartItem, THE System SHALL remove that item from the Cart.
8. THE System SHALL show keyboard shortcut hints in tooltips or a help panel.
```

### 5.2 Actualizar design.md

**Agregar sección:**

```markdown
## 9. Keyboard Shortcuts

| Shortcut | Action | Context |
|----------|--------|---------|
| F3, Ctrl+F | Focus search | Any screen |
| ↑ / ↓ | Navigate products | Product list |
| Enter | Add selected product | Product focused |
| Tab / Shift+Tab | Navigate sections | Sale flow |
| Escape | Cancel/back | Any modal |
| F9 | Confirm sale | Payment screen |
| F10 | Open history | Main screen |
| +/- | Adjust quantity | Cart item |
| Delete | Remove from cart | Cart item |

## 10. Barcode Scanner Integration

The system detects barcode scans by:
1. Listening to `paste` events on SearchBar
2. Validating pattern: `/^[A-Z]{3}-\d{3}$/`
3. Auto-adding product if exactly 1 match found
4. Clearing search and showing notification
```

### 5.3 Actualizar tasks.md

**Agregar nuevas tareas:**

```markdown
### Phase 19: Barcode Scanner Support

- [ ] 19.1 Implement paste event detection in SearchBar
- [ ] 19.2 Add barcode pattern validation (XXX-NNN)
- [ ] 19.3 Implement auto-add to cart logic
- [ ] 19.4 Add success notification on auto-add
- [ ] 19.5 Test with manual paste and simulated scanner

### Phase 20: Keyboard Navigation

- [ ] 20.1 Create useKeyboardShortcuts hook
- [ ] 20.2 Implement F3/Ctrl+F for search focus
- [ ] 20.3 Implement ↑/↓ navigation in ProductList
- [ ] 20.4 Implement Enter to add selected product
- [ ] 20.5 Implement F9 for sale confirmation
- [ ] 20.6 Implement Escape for cancel/back
- [ ] 20.7 Implement +/- for quantity adjustment
- [ ] 20.8 Implement Delete for cart item removal
- [ ] 20.9 Add visual indicators for selected items
- [ ] 20.10 Add keyboard shortcut help panel
```

---

## 6. PLAN DE ACCIÓN RECOMENDADO

### Orden de implementación:

1. **Actualizar specs** (30 minutos)
   - Agregar Requirement 20 y 21 a requirements.md
   - Actualizar design.md con secciones 9 y 10
   - Agregar Phase 19 y 20 a tasks.md

2. **Implementar auto-agregar con código de barras** (2 horas)
   - Detectar paste event
   - Validar patrón de código
   - Auto-agregar al carrito
   - Notificación de éxito

3. **Implementar navegación por teclado** (4-5 horas)
   - Hook de atajos globales
   - Navegación con flechas
   - Atajos de función (F3, F9, etc.)
   - Indicadores visuales

4. **(Opcional) Mostrar todos los productos al inicio** (1 hora)
   - Cargar productos en IDLE
   - Filtrar al escribir

5. **Probar todo el flujo con teclado** (1 hora)
   - Verificar que TODAS las operaciones funcionen sin mouse
   - Documentar atajos en README

6. **Capturar pantallas** (2 horas)
   - Screenshots de Postman
   - Screenshots del frontend funcionando
   - Screenshot mostrando uso de teclado

**Tiempo total estimado:** 10-12 horas

---

## 7. JUSTIFICACIÓN PARA EL PROFESOR

### ¿Por qué estas funcionalidades son importantes?

1. **Auto-agregar con código de barras:**
   - Simula un POS real con lector de código de barras
   - Aumenta la velocidad de checkout
   - Reduce errores de selección manual

2. **Navegación por teclado:**
   - Requisito explícito del profesor
   - Mejora la ergonomía para cajeros que procesan muchas ventas
   - Demuestra dominio de eventos de teclado en JavaScript
   - Cumple con estándares de accesibilidad (WCAG)

3. **Mostrar productos al inicio:**
   - Mejora la UX para usuarios nuevos
   - Facilita la exploración del catálogo
   - Reduce la fricción en el flujo de venta

---

## 8. RIESGOS Y MITIGACIÓN

### Riesgo 1: Tiempo insuficiente antes de la entrega

**Mitigación:**
- Priorizar auto-agregar con código de barras (2 horas)
- Implementar solo atajos críticos de teclado (F3, Enter, F9, Escape) (2 horas)
- Dejar atajos avanzados (+/-, Delete) como opcional

### Riesgo 2: Conflictos con funcionalidad existente

**Mitigación:**
- Probar exhaustivamente después de cada cambio
- Mantener comportamiento actual para escritura manual
- Solo cambiar comportamiento para paste/scan

### Riesgo 3: Specs desactualizados

**Mitigación:**
- Actualizar specs ANTES de implementar
- Documentar decisiones en design.md
- Agregar tareas a tasks.md para trazabilidad

---

## 9. CONCLUSIÓN

**Estado actual del buscador:**
- ✅ Búsqueda por nombre funciona
- ✅ Búsqueda por código funciona (Bug 3 resuelto)
- ✅ Mínimo 2 caracteres implementado
- ❌ NO auto-agrega con código completo (lector de barras)
- ❌ NO tiene navegación por teclado
- ⚠️ NO muestra productos al inicio (opcional)

**Prioridad de implementación:**
1. 🔴 **ALTA**: Auto-agregar con código de barras (requisito del profesor)
2. 🔴 **ALTA**: Navegación por teclado (requisito del profesor)
3. 🟡 **MEDIA**: Mostrar productos al inicio (mejora UX)

**Recomendación:**
Implementar las funcionalidades de prioridad ALTA antes de las capturas de pantalla, ya que el profesor espera ver el sistema funcionando completamente desde teclado.

---

**Próxima acción:** ¿Quieres que implemente estas funcionalidades ahora o prefieres revisar el análisis primero?
