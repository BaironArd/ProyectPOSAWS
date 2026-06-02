# Script para probar el API y generar capturas automaticas
# Ejecutar: .\test-api.ps1

$API_BASE = "https://vgs199hevi.execute-api.us-east-1.amazonaws.com/dev"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PRUEBAS DEL API - POS Backend" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: GET /productos
Write-Host "TEST 1: GET /productos" -ForegroundColor Yellow
Write-Host "URL: $API_BASE/productos" -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "$API_BASE/productos" -Method Get -ContentType "application/json"
    Write-Host "SUCCESS - Codigo: 200 OK" -ForegroundColor Green
    Write-Host "Productos encontrados:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 10 | Write-Host
} catch {
    Write-Host "ERROR:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 2: POST /ventas (exitoso)
Write-Host "TEST 2: POST /ventas (Exitoso)" -ForegroundColor Yellow
Write-Host "URL: $API_BASE/ventas" -ForegroundColor Gray
Write-Host ""

$ventaExitosa = @{
    productos = @(
        @{
            productoId = "PROD001"
            nombre = "Coca Cola 500ml"
            cantidad = 2
            precioUnitario = 1.50
        },
        @{
            productoId = "PROD002"
            nombre = "Papas Lays"
            cantidad = 1
            precioUnitario = 2.00
        }
    )
    metodoPago = "EFECTIVO"
    montoRecibido = 10.00
} | ConvertTo-Json -Depth 10

Write-Host "Body enviado:" -ForegroundColor Gray
Write-Host $ventaExitosa -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "$API_BASE/ventas" -Method Post -Body $ventaExitosa -ContentType "application/json"
    Write-Host "SUCCESS - Codigo: 201 Created" -ForegroundColor Green
    Write-Host "Respuesta:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 10 | Write-Host
} catch {
    Write-Host "ERROR:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Detalles: $responseBody" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 3: POST /ventas (error - sin productos)
Write-Host "TEST 3: POST /ventas (Error - datos invalidos)" -ForegroundColor Yellow
Write-Host "URL: $API_BASE/ventas" -ForegroundColor Gray
Write-Host ""

$ventaInvalida = @{
    productos = @()
    metodoPago = "EFECTIVO"
    montoRecibido = 0
} | ConvertTo-Json -Depth 10

Write-Host "Body enviado (invalido):" -ForegroundColor Gray
Write-Host $ventaInvalida -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "$API_BASE/ventas" -Method Post -Body $ventaInvalida -ContentType "application/json"
    Write-Host "Respuesta inesperada (deberia dar error):" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 10 | Write-Host
} catch {
    Write-Host "ERROR ESPERADO - Codigo: 400/500" -ForegroundColor Green
    Write-Host "Mensaje de error:" -ForegroundColor Green
    Write-Host $_.Exception.Message -ForegroundColor Yellow
    if ($_.Exception.Response) {
        $statusCode = [int]$_.Exception.Response.StatusCode
        Write-Host "Codigo HTTP: $statusCode" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TODAS LAS PRUEBAS COMPLETADAS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "INSTRUCCIONES PARA LAS CAPTURAS:" -ForegroundColor Cyan
Write-Host ""
Write-Host "Para las capturas del backend:" -ForegroundColor White
Write-Host "1. Toma captura de TODO este output (Windows + Shift + S)" -ForegroundColor White
Write-Host "2. Esto demuestra que probaste GET /productos, POST /ventas y manejo de errores" -ForegroundColor White
Write-Host ""
Write-Host "Para las capturas de las pruebas unitarias:" -ForegroundColor White
Write-Host "3. Ejecuta: cd aws-microservices\productos-service" -ForegroundColor Yellow
Write-Host "            mvn test" -ForegroundColor Yellow
Write-Host "4. Toma captura del resultado" -ForegroundColor White
Write-Host "5. Ejecuta: cd ..\ventas-service" -ForegroundColor Yellow
Write-Host "            mvn test" -ForegroundColor Yellow
Write-Host "6. Toma captura del resultado" -ForegroundColor White
Write-Host ""
