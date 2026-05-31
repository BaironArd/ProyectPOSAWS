# 📸 INSTRUCCIONES PARA TOMAR CAPTURAS DE PANTALLA

## ✅ ESTADO ACTUAL

- ✅ API Gateway desplegado: `https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod`
- ✅ 50 productos cargados en DynamoDB
- ✅ Frontend corriendo en: **http://localhost:5173/**
- ✅ Archivo .env configurado correctamente

---

## 📸 CAPTURAS REQUERIDAS (3 capturas)

### 1. productos-listado.png
**Qué capturar**: Vista principal con productos cargados desde el API

**Pasos**:
1. Abrir navegador en: http://localhost:5173/
2. Esperar a que carguen los productos (deberían aparecer automáticamente)
3. Capturar pantalla completa mostrando:
   - ✅ Barra de búsqueda en la parte superior
   - ✅ Lista de productos con nombre, precio y stock
   - ✅ Botones "Agregar" en cada producto
   - ✅ Carrito vacío en el lado derecho

**Herramienta**: Windows + Shift + S (Recorte de pantalla)

**Guardar como**: `C:\Users\Usuario\Downloads\proyectPOSAWS\PROYECTPOS\frontend\pos-frontend\docs\screenshots\productos-listado.png`

---

### 2. venta-exitosa.png
**Qué capturar**: Confirmación de venta registrada correctamente

**Pasos**:
1. En http://localhost:5173/, buscar un producto (ej: "Red Bull")
2. Hacer clic en "Agregar" para agregarlo al carrito
3. Hacer clic en el botón de pago (o presionar F2)
4. Ingresar monto pagado (mayor o igual al total)
5. Hacer clic en "Confirmar venta" (o presionar Enter)
6. Esperar a que aparezca la pantalla de confirmación
7. Capturar pantalla mostrando:
   - ✅ Mensaje "¡Venta completada!"
   - ✅ ID de venta (ej: VNT-20260530-001)
   - ✅ Cálculo de cambio
   - ✅ Botones "Imprimir recibo" y "Nueva venta"

**Guardar como**: `C:\Users\Usuario\Downloads\proyectPOSAWS\PROYECTPOS\frontend\pos-frontend\docs\screenshots\venta-exitosa.png`

---

### 3. error-api-caido.png
**Qué capturar**: Manejo de error cuando el API no está disponible

**Pasos**:
1. **Detener el servidor de desarrollo** (Ctrl+C en la terminal)
2. **Modificar temporalmente el .env** para simular API caído:
   ```
   VITE_API_BASE_URL=https://api-inexistente.execute-api.us-east-1.amazonaws.com/Prod
   ```
3. **Reiniciar el servidor**: `npm run dev`
4. Abrir http://localhost:5173/
5. Intentar buscar un producto
6. Esperar a que aparezca el banner de error
7. Capturar pantalla mostrando:
   - ✅ Banner de error visible (rojo)
   - ✅ Mensaje descriptivo del error
   - ✅ Botón de reintentar (si aplica)
   - ✅ La aplicación no se rompe

**Guardar como**: `C:\Users\Usuario\Downloads\proyectPOSAWS\PROYECTPOS\frontend\pos-frontend\docs\screenshots\error-api-caido.png`

**IMPORTANTE**: Después de tomar esta captura, **restaurar el .env original**:
```
VITE_API_BASE_URL=https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod
```

---

## 🔧 ALTERNATIVA PARA CAPTURA 3 (Más fácil)

Si no quieres modificar el .env, puedes:

1. Abrir DevTools (F12)
2. Ir a la pestaña "Network"
3. Activar "Offline" mode
4. Intentar buscar un producto
5. Capturar el error

---

## ✅ DESPUÉS DE TOMAR LAS CAPTURAS

### Verificar que las 3 imágenes estén guardadas:
```
docs/screenshots/
├── productos-listado.png  ✅
├── venta-exitosa.png      ✅
└── error-api-caido.png    ✅
```

### Hacer commit y push:
```bash
cd C:\Users\Usuario\Downloads\proyectPOSAWS
git add .
git commit -m "docs: agregar capturas de pantalla del sistema funcionando"
git push
```

---

## 🎯 CHECKLIST FINAL

Después de subir las capturas:

- [x] ✅ Errores TypeScript eliminados
- [x] ✅ Atajos de teclado implementados
- [x] ✅ Configuración del API Gateway
- [x] ✅ README completo
- [x] ✅ Deploy de Lambda
- [x] ✅ .env configurado
- [x] ✅ Frontend corriendo
- [ ] ⚠️ Capturas de pantalla (PENDIENTE - HACER AHORA)
- [ ] ⚠️ Commit final con capturas

---

## 📝 NOTAS

- El frontend está corriendo en: **http://localhost:5173/**
- La API está en: **https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod**
- Hay **50 productos** cargados en DynamoDB
- Los atajos de teclado están activos:
  - **F1** o **/** → Enfocar buscador
  - **F2** → Ir al panel de pago
  - **Enter** → Confirmar venta
  - **Escape** → Cancelar
  - **F12** → Nueva venta

---

## 🚀 DESPUÉS DE COMPLETAR

El proyecto estará **100% COMPLETO** y listo para entregar al profesor.

**URL del repositorio**: https://github.com/BaironArd/ProyectPOSAWS
