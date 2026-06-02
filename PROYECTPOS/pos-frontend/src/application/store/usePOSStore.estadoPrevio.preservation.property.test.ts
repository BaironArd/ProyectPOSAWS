import { describe, it, expect, beforeEach } from 'vitest';
import * as fc from 'fast-check';
import { usePOSStore } from './usePOSStore';
import type { EstadoUI } from '@domain/types/POSState';

/**
 * Preservation Property Tests for estadoPrevio
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3**
 * 
 * This test ensures that non-error state transitions work exactly as before the fix.
 * The fix should ONLY affect error handling (setError/clearError), not normal transitions.
 * 
 * **IMPORTANT**: Follow observation-first methodology
 * - Observe behavior on FIXED code for non-error state transitions
 * - Test that valid transitions in TRANSICIONES_VALIDAS work correctly
 * - Test that resetVenta returns to IDLE without affecting estadoPrevio
 * - Test that invalid transitions are still blocked
 * 
 * **EXPECTED OUTCOME**: Tests PASS (confirms baseline behavior is preserved)
 * 
 * Property 2: Preservation - Non-Error State Transitions
 * For any state transition that does NOT involve setError or clearError, 
 * the fixed code produces exactly the same state transitions as defined in TRANSICIONES_VALIDAS.
 */

// Valid state transitions from TRANSICIONES_VALIDAS
const TRANSICIONES_VALIDAS: Partial<Record<EstadoUI, EstadoUI[]>> = {
  IDLE:             ['BUSCANDO', 'ERROR'],
  BUSCANDO:         ['RESULTADOS', 'IDLE', 'ERROR'],
  RESULTADOS:       ['CARRITO_ACTIVO', 'BUSCANDO', 'IDLE', 'ERROR'],
  CARRITO_ACTIVO:   ['CALCULANDO_PAGO', 'RESULTADOS', 'ERROR'],
  CALCULANDO_PAGO:  ['PROCESANDO', 'CARRITO_ACTIVO', 'VENTA_COMPLETA', 'ERROR'],
  PROCESANDO:       ['VENTA_COMPLETA', 'ERROR'],
  VENTA_COMPLETA:   ['IDLE', 'ERROR'],
  ERROR:            ['IDLE', 'BUSCANDO', 'RESULTADOS', 'CARRITO_ACTIVO', 'CALCULANDO_PAGO'],
};

// All possible states
const ALL_STATES: EstadoUI[] = [
  'IDLE',
  'BUSCANDO',
  'RESULTADOS',
  'CARRITO_ACTIVO',
  'CALCULANDO_PAGO',
  'PROCESANDO',
  'VENTA_COMPLETA',
  'ERROR',
];

// Arbitrary for generating any state
const stateArb = fc.constantFrom(...ALL_STATES);

// Arbitrary for generating valid transitions
const validTransitionArb = fc
  .constantFrom(...(Object.entries(TRANSICIONES_VALIDAS) as [EstadoUI, EstadoUI[]][]))
  .chain(([from, toList]) => 
    fc.constantFrom(...toList).map(to => ({ from, to }))
  );

// Arbitrary for generating invalid transitions (from states that have valid transitions defined)
const invalidTransitionArb = fc
  .constantFrom(...(Object.entries(TRANSICIONES_VALIDAS) as [EstadoUI, EstadoUI[]][]))
  .chain(([from, validToList]) => {
    const invalidToList = ALL_STATES.filter(to => !validToList.includes(to));
    if (invalidToList.length === 0) {
      // If no invalid transitions exist, return a dummy that will be filtered
      return fc.constant({ from, to: from, isValid: true });
    }
    return fc.constantFrom(...invalidToList).map(to => ({ from, to, isValid: false }));
  })
  .filter(t => !t.isValid);

describe('Property 2: Preservation - Non-Error State Transitions', () => {
  beforeEach(() => {
    // Reset store to initial state before each test
    const store = usePOSStore.getState();
    store.resetVenta();
    // Clear any error state
    if (store.error) {
      store.clearError();
    }
  });

  it('P2.1: Valid transitions in TRANSICIONES_VALIDAS work correctly', () => {
    fc.assert(
      fc.property(validTransitionArb, ({ from, to }) => {
        // Skip ERROR transitions as they are tested separately
        if (from === 'ERROR' || to === 'ERROR') {
          return true;
        }

        // Setup: Start in 'from' state
        usePOSStore.setState({ 
          estado: from, 
          estadoPrevio: null, 
          error: null 
        });

        const initialState = usePOSStore.getState();
        expect(initialState.estado).toBe(from);
        expect(initialState.estadoPrevio).toBe(null);

        // Attempt transition using setEstado
        const store = usePOSStore.getState();
        store.setEstado(to);

        // Verify transition succeeded
        const newState = usePOSStore.getState();
        expect(newState.estado).toBe(to);
        
        // Verify estadoPrevio is NOT affected by normal transitions
        expect(newState.estadoPrevio).toBe(null);

        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('P2.2: Invalid transitions are blocked by TRANSICIONES_VALIDAS', () => {
    fc.assert(
      fc.property(invalidTransitionArb, ({ from, to }) => {
        // Skip if from === to (same state is not a transition)
        if (from === to) {
          return true;
        }

        // Setup: Start in 'from' state
        usePOSStore.setState({ 
          estado: from, 
          estadoPrevio: null, 
          error: null 
        });

        const initialState = usePOSStore.getState();
        expect(initialState.estado).toBe(from);

        // Attempt invalid transition using setEstado
        const store = usePOSStore.getState();
        store.setEstado(to);

        // Verify transition was blocked - estado should remain unchanged
        const newState = usePOSStore.getState();
        expect(newState.estado).toBe(from);
        
        // Verify estadoPrevio is still null (not affected)
        expect(newState.estadoPrevio).toBe(null);

        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('P2.3: resetVenta returns to IDLE without affecting estadoPrevio logic', () => {
    fc.assert(
      fc.property(stateArb, (initialState) => {
        // Skip ERROR state as it has special handling
        if (initialState === 'ERROR') {
          return true;
        }

        // Setup: Start in any state with some data
        usePOSStore.setState({ 
          estado: initialState, 
          estadoPrevio: null, 
          error: null,
          query: 'test query',
          carrito: [
            {
              productoId: '123',
              nombre: 'Test Product',
              cantidad: 2,
              precioUnitario: 1000,
              subtotal: 2000,
              stockDisponible: 10,
            }
          ],
          metodoPago: 'EFECTIVO',
          montoPagado: 5000,
        });

        const beforeReset = usePOSStore.getState();
        expect(beforeReset.estado).toBe(initialState);
        expect(beforeReset.carrito.length).toBeGreaterThan(0);

        // Call resetVenta
        const store = usePOSStore.getState();
        store.resetVenta();

        // Verify reset behavior
        const afterReset = usePOSStore.getState();
        
        // Should return to IDLE
        expect(afterReset.estado).toBe('IDLE');
        
        // estadoPrevio should remain null (not affected by resetVenta)
        expect(afterReset.estadoPrevio).toBe(null);
        
        // Verify data is cleared
        expect(afterReset.carrito).toEqual([]);
        expect(afterReset.query).toBe('');
        expect(afterReset.productos).toEqual([]);
        expect(afterReset.metodoPago).toBe(null);
        expect(afterReset.montoPagado).toBe(0);

        return true;
      }),
      { numRuns: 50 }
    );
  });

  it('P2.4: Normal state flow sequence works without estadoPrevio interference', () => {
    // Test a typical happy path: IDLE → BUSCANDO → RESULTADOS → CARRITO_ACTIVO → CALCULANDO_PAGO
    const happyPathSequence: EstadoUI[] = [
      'IDLE',
      'BUSCANDO',
      'RESULTADOS',
      'CARRITO_ACTIVO',
      'CALCULANDO_PAGO',
    ];

    // Start at IDLE
    usePOSStore.setState({ estado: 'IDLE', estadoPrevio: null, error: null });
    const store = usePOSStore.getState();

    // Walk through the sequence
    for (let i = 1; i < happyPathSequence.length; i++) {
      const currentState = happyPathSequence[i - 1];
      const nextState = happyPathSequence[i];

      // Verify current state
      const before = usePOSStore.getState();
      expect(before.estado).toBe(currentState);
      expect(before.estadoPrevio).toBe(null);

      // Transition to next state
      store.setEstado(nextState);

      // Verify transition
      const after = usePOSStore.getState();
      expect(after.estado).toBe(nextState);
      expect(after.estadoPrevio).toBe(null); // Should never be set during normal flow
    }
  });

  it('P2.5: Multiple sequential valid transitions preserve null estadoPrevio', () => {
    fc.assert(
      fc.property(
        fc.array(validTransitionArb, { minLength: 3, maxLength: 10 }),
        (transitions) => {
          // Filter out ERROR transitions for this test
          const nonErrorTransitions = transitions.filter(
            t => t.from !== 'ERROR' && t.to !== 'ERROR'
          );

          if (nonErrorTransitions.length < 2) {
            return true; // Skip if not enough valid transitions
          }

          // Start from the first 'from' state
          const startState = nonErrorTransitions[0].from;
          usePOSStore.setState({ estado: startState, estadoPrevio: null, error: null });

          const store = usePOSStore.getState();

          // Execute each transition
          for (const { from, to } of nonErrorTransitions) {
            // Set the 'from' state if needed
            const current = usePOSStore.getState();
            if (current.estado !== from) {
              usePOSStore.setState({ estado: from, estadoPrevio: null, error: null });
            }

            // Perform transition
            store.setEstado(to);

            // Verify estadoPrevio remains null
            const after = usePOSStore.getState();
            expect(after.estadoPrevio).toBe(null);
          }

          return true;
        }
      ),
      { numRuns: 30 }
    );
  });

  it('P2.6: irAIdle action returns to IDLE without affecting estadoPrevio', () => {
    fc.assert(
      fc.property(stateArb, (initialState) => {
        // Skip ERROR state
        if (initialState === 'ERROR') {
          return true;
        }

        // Setup: Start in any state
        usePOSStore.setState({ 
          estado: initialState, 
          estadoPrevio: null, 
          error: null,
          query: 'test',
          carrito: [],
        });

        const store = usePOSStore.getState();
        
        // Call irAIdle
        store.irAIdle();

        // Verify behavior
        const after = usePOSStore.getState();
        expect(after.estado).toBe('IDLE');
        expect(after.estadoPrevio).toBe(null); // Should not be affected
        expect(after.query).toBe('');

        return true;
      }),
      { numRuns: 50 }
    );
  });
});

describe('Preservation - State Machine Integrity', () => {
  beforeEach(() => {
    const store = usePOSStore.getState();
    store.resetVenta();
    if (store.error) {
      store.clearError();
    }
  });

  it('I1: IDLE → BUSCANDO → RESULTADOS flow preserves estadoPrevio as null', () => {
    const store = usePOSStore.getState();

    // Start IDLE
    usePOSStore.setState({ estado: 'IDLE', estadoPrevio: null, error: null });
    expect(usePOSStore.getState().estadoPrevio).toBe(null);

    // Transition to BUSCANDO
    store.setEstado('BUSCANDO');
    expect(usePOSStore.getState().estado).toBe('BUSCANDO');
    expect(usePOSStore.getState().estadoPrevio).toBe(null);

    // Transition to RESULTADOS
    store.setEstado('RESULTADOS');
    expect(usePOSStore.getState().estado).toBe('RESULTADOS');
    expect(usePOSStore.getState().estadoPrevio).toBe(null);
  });

  it('I2: RESULTADOS → CARRITO_ACTIVO → CALCULANDO_PAGO flow preserves estadoPrevio as null', () => {
    const store = usePOSStore.getState();

    // Start RESULTADOS
    usePOSStore.setState({ estado: 'RESULTADOS', estadoPrevio: null, error: null });
    expect(usePOSStore.getState().estadoPrevio).toBe(null);

    // Transition to CARRITO_ACTIVO
    store.setEstado('CARRITO_ACTIVO');
    expect(usePOSStore.getState().estado).toBe('CARRITO_ACTIVO');
    expect(usePOSStore.getState().estadoPrevio).toBe(null);

    // Transition to CALCULANDO_PAGO
    store.setEstado('CALCULANDO_PAGO');
    expect(usePOSStore.getState().estado).toBe('CALCULANDO_PAGO');
    expect(usePOSStore.getState().estadoPrevio).toBe(null);
  });

  it('I3: CALCULANDO_PAGO → PROCESANDO → VENTA_COMPLETA flow preserves estadoPrevio as null', () => {
    const store = usePOSStore.getState();

    // Start CALCULANDO_PAGO
    usePOSStore.setState({ estado: 'CALCULANDO_PAGO', estadoPrevio: null, error: null });
    expect(usePOSStore.getState().estadoPrevio).toBe(null);

    // Transition to PROCESANDO
    store.setEstado('PROCESANDO');
    expect(usePOSStore.getState().estado).toBe('PROCESANDO');
    expect(usePOSStore.getState().estadoPrevio).toBe(null);

    // Transition to VENTA_COMPLETA
    store.setEstado('VENTA_COMPLETA');
    expect(usePOSStore.getState().estado).toBe('VENTA_COMPLETA');
    expect(usePOSStore.getState().estadoPrevio).toBe(null);
  });

  it('I4: VENTA_COMPLETA → IDLE completes the cycle without estadoPrevio changes', () => {
    const store = usePOSStore.getState();

    // Start VENTA_COMPLETA
    usePOSStore.setState({ estado: 'VENTA_COMPLETA', estadoPrevio: null, error: null });
    expect(usePOSStore.getState().estadoPrevio).toBe(null);

    // Transition back to IDLE
    store.setEstado('IDLE');
    expect(usePOSStore.getState().estado).toBe('IDLE');
    expect(usePOSStore.getState().estadoPrevio).toBe(null);
  });

  it('I5: Invalid transition IDLE → CARRITO_ACTIVO is blocked', () => {
    const store = usePOSStore.getState();

    // Start IDLE
    usePOSStore.setState({ estado: 'IDLE', estadoPrevio: null, error: null });

    // Attempt invalid transition
    store.setEstado('CARRITO_ACTIVO');

    // Should remain in IDLE
    const after = usePOSStore.getState();
    expect(after.estado).toBe('IDLE');
    expect(after.estadoPrevio).toBe(null);
  });

  it('I6: Invalid transition BUSCANDO → CALCULANDO_PAGO is blocked', () => {
    const store = usePOSStore.getState();

    // Start BUSCANDO
    usePOSStore.setState({ estado: 'BUSCANDO', estadoPrevio: null, error: null });

    // Attempt invalid transition
    store.setEstado('CALCULANDO_PAGO');

    // Should remain in BUSCANDO
    const after = usePOSStore.getState();
    expect(after.estado).toBe('BUSCANDO');
    expect(after.estadoPrevio).toBe(null);
  });

  it('I7: Invalid transition PROCESANDO → IDLE is blocked', () => {
    const store = usePOSStore.getState();

    // Start PROCESANDO
    usePOSStore.setState({ estado: 'PROCESANDO', estadoPrevio: null, error: null });

    // Attempt invalid transition
    store.setEstado('IDLE');

    // Should remain in PROCESANDO
    const after = usePOSStore.getState();
    expect(after.estado).toBe('PROCESANDO');
    expect(after.estadoPrevio).toBe(null);
  });
});

describe('Preservation - State Actions Integrity', () => {
  beforeEach(() => {
    const store = usePOSStore.getState();
    store.resetVenta();
    if (store.error) {
      store.clearError();
    }
  });

  it('A1: resetVenta from VENTA_COMPLETA clears all data and returns to IDLE', () => {
    const store = usePOSStore.getState();

    // Setup completed sale state
    usePOSStore.setState({
      estado: 'VENTA_COMPLETA',
      estadoPrevio: null,
      error: null,
      ventaIdActual: 'sale-123',
      carrito: [
        {
          productoId: '456',
          nombre: 'Product A',
          cantidad: 3,
          precioUnitario: 2000,
          subtotal: 6000,
          stockDisponible: 20,
        }
      ],
      resumen: { subtotal: 6000, iva: 1140, total: 7140 },
      metodoPago: 'TARJETA',
      montoPagado: 7140,
      cambio: 0,
    });

    // Call resetVenta
    store.resetVenta();

    // Verify complete reset
    const after = usePOSStore.getState();
    expect(after.estado).toBe('IDLE');
    expect(after.estadoPrevio).toBe(null);
    expect(after.carrito).toEqual([]);
    expect(after.resumen).toEqual({ subtotal: 0, iva: 0, total: 0 });
    expect(after.metodoPago).toBe(null);
    expect(after.montoPagado).toBe(0);
    expect(after.cambio).toBe(0);
    expect(after.ventaIdActual).toBe(null);
  });

  it('A2: resetVenta from CARRITO_ACTIVO clears cart and returns to IDLE', () => {
    const store = usePOSStore.getState();

    // Setup cart active state
    usePOSStore.setState({
      estado: 'CARRITO_ACTIVO',
      estadoPrevio: null,
      error: null,
      carrito: [
        {
          productoId: '789',
          nombre: 'Product B',
          cantidad: 1,
          precioUnitario: 5000,
          subtotal: 5000,
          stockDisponible: 5,
        }
      ],
      resumen: { subtotal: 5000, iva: 950, total: 5950 },
    });

    // Call resetVenta
    store.resetVenta();

    // Verify reset
    const after = usePOSStore.getState();
    expect(after.estado).toBe('IDLE');
    expect(after.estadoPrevio).toBe(null);
    expect(after.carrito).toEqual([]);
  });

  it('A3: irAIdle from any state preserves datosRecibo but clears other data', () => {
    const store = usePOSStore.getState();

    const testRecibo = {
      ventaId: 'sale-999',
      fecha: '2024-01-15',
      items: [],
      subtotal: 10000,
      iva: 1900,
      total: 11900,
      metodoPago: 'EFECTIVO' as const,
      montoPagado: 12000,
      cambio: 100,
    };

    // Setup with receipt data
    usePOSStore.setState({
      estado: 'VENTA_COMPLETA',
      estadoPrevio: null,
      error: null,
      datosRecibo: testRecibo,
      carrito: [
        {
          productoId: '111',
          nombre: 'Product C',
          cantidad: 2,
          precioUnitario: 3000,
          subtotal: 6000,
          stockDisponible: 10,
        }
      ],
      query: 'test query',
    });

    // Call irAIdle
    store.irAIdle();

    // Verify behavior
    const after = usePOSStore.getState();
    expect(after.estado).toBe('IDLE');
    expect(after.estadoPrevio).toBe(null);
    expect(after.carrito).toEqual([]);
    expect(after.query).toBe('');
    expect(after.datosRecibo).toEqual(testRecibo); // Preserved for display
  });
});
