import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { POSApp } from './ui/POSApp';

import { productoAdapter } from './infrastructure/adapters/ProductoAdapter';
import { ventaAdapter }    from './infrastructure/adapters/VentaAdapter';
import { impresionAdapter } from './infrastructure/adapters/ImpresionAdapter';

import './index.css';

const root = document.getElementById('root');
if (!root) throw new Error('No se encontró el elemento #root');

createRoot(root).render(
  <StrictMode>
    <POSApp
      productoPort={productoAdapter}
      ventaPort={ventaAdapter}
      impresionPort={impresionAdapter}
    />
  </StrictMode>
);
