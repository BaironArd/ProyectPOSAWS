# 📋 LISTA DE ENTREGABLES FALTANTES - POS AWS SAM

## ✅ YA COMPLETADO

### Backend (pos-sam)
- ✅ Arquitectura serverless con 2 Lambda functions (Java 21)
- ✅ 14 endpoints REST (GET, POST, OPTIONS)
- ✅ Tablas DynamoDB (ProductosTable, VentasTable) con índices
- ✅ CORS preflight support (OPTIONS handlers)
- ✅ API Gateway desplegado en AWS (us-east-1)
- ✅ Specs SDD: requirements.md, design.md, tasks.md en `.kiro/specs/pos-backend/`
- ✅ SAM template.yaml con IAM roles

### Frontend (React)
- ✅ Aplicación React 18 + TypeScript con Vite
- ✅ Zustand para state management
- ✅ Arquitectura hexagonal (domain, infrastructure, application, ui)
- ✅ Consumo de API Lambda via fetch
- ✅ Componentes: ProductList, Cart, PaymentPanel, etc.
- ✅ Specs SDD: requirements.md, design.md, tasks.md en `.kiro/specs/pos-frontend/`
- ✅ UUID handling correcto (sin hashCode collisions)
- ✅ Vitest + fast-check property-based testing

### Limpieza
- ✅ Eliminada carpeta obsoleta: `PROYECTPOS/backend/pos-backend/`
- ✅ Reorganizada estructura specs: `.kiro/specs/pos-backend/` + `.kiro/specs/pos-frontend/`

---

## ❌ FALTA HACER - PRIORIDAD 🔴

### 1. **PRUEBAS UNITARIAS BACKEND** (2 horas) — CRÍTICO PARA EVALUACIÓN
**Archivo:** `pos-sam/get-products/src/test/java/...`

#### A. GetProductsHandlerTest
```java
public class GetProductsHandlerTest {
    @Test void testHandle_getAllProducts_success() { }
    @Test void testHandle_searchByName_partial() { }
    @Test void testHandle_searchById_notFound() { }
    @Test void testHandle_invalidUuid_returns400() { }
    @Test void testHandle_databaseError_returns500() { }
}
```

#### B. ProductServiceTest  
```java
public class ProductServiceTest {
    @Test void testGetAll_success() { /* mock repo */ }
    @Test void testSearchById_found() { /* mock repo */ }
    @Test void testSearchByName_caseInsensitive() { /* mock repo */ }
    @Test void testGetAll_databaseError() { /* mock repo throws */ }
}
```

#### C. SaveSaleHandlerTest
```java
public class SaveSaleHandlerTest {
    @Test void testHandle_postSales_success_returns201() { }
    @Test void testHandle_postSales_emptyItems_returns400() { }
    @Test void testHandle_postSales_insufficientPayment_returns400() { }
    @Test void testHandle_optionsPreflight_returns200() { }
}
```

#### D. SaleServiceTest
```java
public class SaleServiceTest {
    @Test void testCreateSale_calculatesIva() { /* 19% */ }
    @Test void testCreateSale_generatesUuid() { }
    @Test void testCreateSale_databaseError() { /* mock throws */ }
    @Test void testReportDaily_filtersDate() { /* 2024-12-15 */ }
}
```

**Requisitos:**
- [ ] Usar Mockito para mockar DynamoDbClient
- [ ] Coverage ≥70% (verificar con `mvn jacoco:report`)
- [ ] Archivos en: `src/test/java/com/pos/sam/...`
- [ ] Tests ejecutables con: `mvn test`

**Por qué es crítico:**
- Evaluación PDF 1 pide: "Pruebas unitarias con mocks" (25% de calificación)
- Rubric: "niveles de prueba, shift-left testing, aislamiento de dependencias"

---

### 2. **CAPTURAS POSTMAN** (45 min) — CRÍTICO PARA EVALUACIÓN
**Archivo:** Guardar capturas en `docs/postman-screenshots/`

Necesita **8+ screenshots** de Postman mostrando:

1. **GET /api/v1/products** (éxito 200)
   - Request: `GET https://.../api/v1/products`
   - Response: 50 productos en JSON
   - Caption: "Todos los productos"

2. **GET /api/v1/products?type=name&q=mouse** (búsqueda exitosa)
   - Response: productos con "mouse" en nombre
   - Caption: "Búsqueda por nombre (case-insensitive)"

3. **GET /api/v1/products?type=code&q=PERI-001** (búsqueda exacta)
   - Response: 1 producto con código exacto
   - Caption: "Búsqueda por código"

4. **GET /api/v1/products?type=id&q=not-uuid** (error 400)
   - Response: `{"error": "Invalid UUID format", "code": "INVALID_INPUT"}`
   - Caption: "Validación de UUID fallida"

5. **POST /api/v1/sales** (éxito 201)
   - Request body: items, paymentMethod, amountPaid
   - Response: id, total (con IVA 19%), createdAt
   - Caption: "Crear venta - éxito"

6. **POST /api/v1/sales (vacío)** (error 400)
   - Request: `items: []`
   - Response: `{"error": "Items cannot be empty"}`
   - Caption: "Validación fallida - items vacío"

7. **POST /api/v1/sales (pago insuficiente)** (error 400)
   - Request: amountPaid < subtotal
   - Response: `{"error": "Payment amount is less than total"}`
   - Caption: "Validación fallida - pago insuficiente"

8. **OPTIONS /api/v1/products** (preflight 200)
   - Response headers: `Access-Control-Allow-Origin: *`
   - Response headers: `Access-Control-Allow-Methods: GET, POST, OPTIONS`
   - Caption: "CORS preflight OK"

9. **GET /api/v1/reports/daily?date=2024-12-15** (reporte)
   - Response: resumen del día con totalSales, revenue, tax
   - Caption: "Reporte diario"

10. **Bonus:** GET /api/v1/sales (lista de ventas)
    - Caption: "Listar todas las ventas"

**Cómo tomar las capturas:**
1. Importar colección Postman: `postman-collection.json`
2. Configurar variable: `{{api_base}}` = URL actual del API Gateway
3. Ejecutar cada request
4. Captura pantalla (Postman + respuesta visible)
5. Guardar en `docs/postman-screenshots/`
6. Agregar caption en README

**Por qué es crítico:**
- PDF 1 rubric: "Documentación de endpoints funcionando"
- PDF 2 rubric: "Comunicación cliente-servidor"

---

### 3. **ACTUALIZAR README pos-sam** (45 min) — IMPORTANTE
**Archivo:** `pos-sam/README.md`

Agregar estas secciones:

#### A. Sección "SDD Process"
```markdown
## 📐 Proceso Spec-Driven Development

Este backend fue diseñado siguiendo **SDD**: Specs → Design → Implementation → Tests

**Artifacts:**
- `.kiro/specs/pos-backend/requirements.md` → Define QUÉ hace cada endpoint
- `.kiro/specs/pos-backend/design.md` → Define CÓMO se implementa
- `.kiro/specs/pos-backend/tasks.md` → Lista tareas de implementación

**Traceabilidad:** Cada test en GetProductsHandlerTest.java tiene una tarea asociada en tasks.md
```

#### B. Sección "Deployment"
```markdown
## 🚀 Despliegue en AWS

### Paso 1: Compilar
\`\`\`bash
cd pos-sam
sam build
\`\`\`

### Paso 2: Desplegar
\`\`\`bash
sam deploy --guided
# Responder: stack name=pos-sam, region=us-east-1, confirm changeset=Y
\`\`\`

### Paso 3: Probar
- API Base: https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod
- Test en Postman: ver capturas en docs/postman-screenshots/
```

#### C. Agregar imágenes de Postman
```markdown
## 📸 Capturas de Endpoints

### 1. GET /api/v1/products
![](docs/postman-screenshots/1-get-products-all.png)

### 2. GET /api/v1/products?type=name&q=mouse
![](docs/postman-screenshots/2-get-products-search.png)

[... más capturas ...]
```

#### D. Agregar tabla de testeo
```markdown
## ✅ Matriz de Casos Probados

| Endpoint | Caso | HTTP | Status |
|----------|------|------|--------|
| GET /products | Todos | 200 | ✅ |
| GET /products?q=mouse | Búsqueda | 200 | ✅ |
| POST /sales | Éxito | 201 | ✅ |
| POST /sales | Items vacío | 400 | ✅ |
| OPTIONS /* | CORS | 200 | ✅ |
```

---

### 4. **ACTUALIZAR README RAÍZ** (30 min) — IMPORTANTE
**Archivo:** `README.md` (raíz del repo)

Cambios necesarios:

#### A. Reemplazar Spring Boot → SAM
```markdown
# ProyectPOS — Sistema Serverless en AWS

## Stack Tecnológico

### Backend (Antes → Ahora)
- ❌ Spring Boot 3 + Tomcat (monolito)
- ✅ AWS SAM + Lambda Java 21 (serverless)

### Infraestructura
- API Gateway: https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod
- DynamoDB: ProductosTable, VentasTable
- Lambda: 2 funciones (GetProducts, SaveSale)
```

#### B. Agregar instrucciones SDD
```markdown
## 📐 Methodology: Spec-Driven Development

Este proyecto fue desarrollado con **SDD**: especificación → diseño → implementación → pruebas

Documentos:
- [Backend Specs](.kiro/specs/pos-backend/)
- [Frontend Specs](.kiro/specs/pos-frontend/)
```

#### C. Actualizar Quick Start
```markdown
## 🚀 Quick Start

### Backend (AWS SAM)
\`\`\`bash
cd pos-sam
sam build && sam deploy --guided
\`\`\`
API: https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod

### Frontend (React)
\`\`\`bash
cd PROYECTPOS/frontend/pos-frontend
npm install && npm run dev
\`\`\`
Web: http://localhost:5173

### Testing
\`\`\`bash
cd pos-sam
mvn test  # Unit tests con mocks
\`\`\`
```

#### D. Agregar diagrama arqui actualizado
```markdown
## 🏗️ Arquitectura Actual

\`\`\`
Browser (React)              AWS Cloud
    │                            │
    ├─ POST /sales           ┌───▼────┐
    └─ GET /products    ───► │ API    │
                             │Gateway │
                             └───┬────┘
                                 │
                    ┌────────────┼────────────┐
                    │            │            │
                ┌───▼───┐   ┌───▼────┐  ┌───▼───┐
                │GetProd│   │SaveSale│  │ OPTIONS
                │Lambda │   │ Lambda │  │Preflight
                └───┬───┘   └───┬────┘  └───────┘
                    │           │
                ┌───▼───────────▼───┐
                │   DynamoDB        │
                │ ProductosTable    │
                │ VentasTable       │
                └───────────────────┘
\`\`\`
```

---

### 5. **CAPTURAR TESTS EJECUTÁNDOSE** (20 min) — BONUS
**Archivo:** `docs/test-screenshots/`

Capturar:
1. Terminal: `cd pos-sam && mvn test` → consola mostrando tests corriendo
2. Terminal: `mvn jacoco:report` → resumen de coverage
3. Archivo: `pos-sam/target/site/jacoco/index.html` → reporte HTML
4. Captura de línea de cobertura (≥70%)

**Agregar en README:**
```markdown
## 🧪 Unit Tests

Todos los tests pasan con coverage ≥70%:

![Test Results](docs/test-screenshots/test-results.png)
![Coverage Report](docs/test-screenshots/coverage-report.png)
```

---

## 📊 PLAN DE EJECUCIÓN (Orden Recomendado)

| # | Tarea | Tiempo | Prioridad | Bloquea |
|---|-------|--------|-----------|---------|
| 1 | Escribir tests Java (mocks) | 2h | 🔴 CRÍTICA | Postman |
| 2 | Ejecutar tests + coverage | 15min | 🔴 CRÍTICA | - |
| 3 | Capturar 8+ Postman | 45min | 🔴 CRÍTICA | README |
| 4 | Capturar tests en terminal | 20min | 🟡 IMPORTANTE | README |
| 5 | Actualizar README pos-sam | 45min | 🟡 IMPORTANTE | - |
| 6 | Actualizar README raíz | 30min | 🟡 IMPORTANTE | - |
| 7 | Verificar frontend está funcional | 15min | 🟡 IMPORTANTE | - |
| 8 | Commit + push a GitHub | 10min | 🟡 IMPORTANTE | - |

**Tiempo total: ~5 horas**

---

## 🎯 CHECKLIST FINAL ANTES DE ENTREGAR

- [ ] ✅ .kiro/specs/pos-backend/ con 3 archivos completos
- [ ] ✅ .kiro/specs/pos-frontend/ con 3 archivos completos
- [ ] ✅ pos-sam eliminada referencia a PROYECTPOS (es independiente)
- [ ] ✅ GetProductsHandlerTest.java ejecuta OK (mvn test)
- [ ] ✅ SaveSaleHandlerTest.java ejecuta OK (mvn test)
- [ ] ✅ Coverage ≥70% (mvn jacoco:report)
- [ ] ✅ Postman collection con 8+ requests
- [ ] ✅ 8+ screenshots de Postman en docs/
- [ ] ✅ README pos-sam actualizado con SDD + deployment
- [ ] ✅ README raíz actualizado (SAM, no Spring Boot)
- [ ] ✅ Frontend npm start funciona sin errores
- [ ] ✅ API Gateway URL válida y responde
- [ ] ✅ Postman importable y funciona
- [ ] ✅ GitHub repo es público
- [ ] ✅ Git history limpio (no secretos AWS)
- [ ] ✅ Main branch está actualizado

---

## 🚀 SIGUIENTES PASOS INMEDIATOS

**Usuario debe hacer:**
1. ⬜ Escribir tests Java (GetProductsHandlerTest, SaveSaleHandlerTest)
2. ⬜ Ejecutar `mvn test` y confirmar pasan
3. ⬜ Capturar screenshots Postman (8+)
4. ⬜ Actualizar README pos-sam
5. ⬜ Actualizar README raíz
6. ⬜ Verificar frontend funciona: `npm run dev`
7. ⬜ Commit + push final

**Yo puedo ayudarte con:**
- [ ] Escribir templates de tests (si los necesitas)
- [ ] Revisar estructura de tests
- [ ] Ayudarte con mock de DynamoDB
- [ ] Revisar PRs antes de merge

---

## 📞 PREGUNTAS FRECUENTES

**P: ¿Realmente necesito tests para pasar?**
R: SÍ. Rubric PDF 1: "Pruebas unitarias" = 25% de la nota. Sin tests = máximo 75%.

**P: ¿Postman es obligatorio?**
R: SÍ. Rubric requiere "documentación de endpoints funcionando" con evidencia visual.

**P: ¿Spring Boot pos-backend ya está eliminado?**
R: ✅ SÍ. Eliminado en esta sesión.

**P: ¿Frontend funciona con el backend AWS?**
R: ✅ SÍ. UUID strings están configurados. Solo falta verificar en navegador.

**P: ¿Cuánto tiempo tarda el deployment?**
R: `sam deploy` demora ~3-5 min la primera vez, <1 min las actualizaciones.
