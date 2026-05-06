import { useState } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import type { IReportePort } from '@domain/ports/IReportePort';
import type { ReporteCierre } from '@domain/types/POSState';

export function useReports(reportePort: IReportePort) {
  const [reporte, setReporte] = useState<ReporteCierre | null>(null);
  const [cargando, setCargando] = useState(false);
  const setError = usePOSStore((s) => s.setError);

  async function generar(fechaDesde: string, fechaHasta: string) {
    setCargando(true);
    try {
      const r = await reportePort.generarCierre(fechaDesde, fechaHasta);
      setReporte(r);
    } catch {
      setError({ codigo: 'REPORTE_ERROR', mensaje: 'No se pudo generar el reporte.' });
    } finally {
      setCargando(false);
    }
  }

  async function exportarCSV() {
    if (!reporte) return;
    const blob = await reportePort.exportarCSV(reporte);
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `reporte-${reporte.fechaDesde}-${reporte.fechaHasta}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  return { reporte, cargando, generar, exportarCSV };
}
