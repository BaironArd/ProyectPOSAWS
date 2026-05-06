import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { POSApp } from './ui/POSApp';

// Mocks (cambiar por adapters reales cuando el backend esté listo)
import { authMock } from './infrastructure/mocks/auth.mock';
import { productoMock } from './infrastructure/mocks/productos.mock';
import { ventaMock } from './infrastructure/mocks/venta.mock';
import { historialMock } from './infrastructure/mocks/historial.mock';
import { devolucionMock } from './infrastructure/mocks/devolucion.mock';
import { inventarioMock } from './infrastructure/mocks/inventario.mock';
import { reporteMock } from './infrastructure/mocks/reporte.mock';
import { impresionAdapter } from './infrastructure/adapters/ImpresionAdapter';

import './index.css';

const root = document.getElementById('root');
if (!root) throw new Error('No se encontró el elemento #root');

createRoot(root).render(
  <StrictMode>
    <POSApp
      authPort={authMock}
      productoPort={productoMock}
      ventaPort={ventaMock}
      historialPort={historialMock}
      devolucionPort={devolucionMock}
      inventarioPort={inventarioMock}
      reportePort={reporteMock}
      impresionPort={impresionAdapter}
    />
  </StrictMode>
);
