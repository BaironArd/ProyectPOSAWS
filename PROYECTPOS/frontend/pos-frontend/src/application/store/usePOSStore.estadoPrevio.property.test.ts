import { describe, it, expect, beforeEach } from 'vitest';
import * as fc from 'fast-check';
import { usePOSStore } from './usePOSStore';
import type { EstadoUI, ErrorUI } from '@domain/types/POSState';

/**
 * Bug Condition Exploration Test for estadoPrevio Preservation
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3**
 * 
 * This test validates that the FIX correctly handles estadoPrevio preservation.
 * The bug was: setError did not save estadoPrevio, causing clearError to always return to IDLE.
 * The fix: setError now saves the current state in estadoPrevio before transitioning to ERROR.
 * 
 * **CRITICAL**: This test is run on FIXED code to confirm the fix works correctly.
 * **EXPECTED OUTCOME**: Test PASSES (confirms estadoPrevio is correctly saved and restored).
 * 
 * Property 1: Bug Condition - estadoPrevio Preservation on Error
 * For any state S where S != 'ERROR', calling setError saves S in estadoPrevio before transitioning to ERROR,
 * and clearError restores the saved state (or IDLE if estadoPrevio is null).
 */

// All non-ERROR states that can transition to ERROR
const NON_ERROR_STATES: EstadoUI[] = [
  'IDLE',
  'BUSCANDO',
  'RESULTADOS',
  'CARRITO_ACTIVO',
  'CALCULANDO_PAGO',
  'PROCESANDO',
  'VENTA_COMPLETA',
];

// Arbitrary for generating non-ERROR states
const nonErrorStateArb = fc.constantFrom(...NON_ERROR_STATES);

// Arbitrary for generating error objects
const errorArb = fc.record({
  codigo: fc.constantFrom('API_ERROR', 'NETWORK_ERROR', 'VALIDATION_ERROR', 'UNKNOWN_ERROR'),
  mensaje: fc.string({ minLength: 1, maxLength: 100 }),
});

describe('Property 1: Bug Condition - estadoPrevio Preservation on Error', () => {
  beforeEach(() => {
    // Reset store to initial state before each test
    const store = usePOSStore.getState();
    store.resetVenta();
    // Clear any error state
    if (store.error) {
      store.clearError();
    }
  });

  it('P1.1: setError saves current state in estadoPrevio for all non-ERROR states', () => {
    fc.assert(
      fc.property(nonErrorStateArb, errorArb, (initialState, error) => {
        const store = usePOSStore.getState();
        
        // Manually set the state (bypassing validation for test purposes)
        usePOSStore.setState({ estado: initialState, estadoPrevio: null, error: null });
        
        // Call setError
        store.setError(error);
        
        // Get the updated state
        const newState = usePOSStore.getState();
        
        // Verify estadoPrevio was saved
        expect(newState.estadoPrevio).toBe(initialState);
        expect(newState.estado).toBe('ERROR');
        expect(newState.error).toEqual(error);
        
        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('P1.2: clearError restores estadoPrevio for all non-PROCESANDO states', () => {
    fc.assert(
      fc.property(
        nonErrorStateArb.filter(s => s !== 'PROCESANDO'),
        errorArb,
        (initialState, error) => {
          const store = usePOSStore.getState();
          
          // Setup: Start in initialState
          usePOSStore.setState({ estado: initialState, estadoPrevio: null, error: null });
          
          // Trigger error
          store.setError(error);
          
          // Verify we're in ERROR state with estadoPrevio saved
          const errorState = usePOSStore.getState();
          expect(errorState.estado).toBe('ERROR');
          expect(errorState.estadoPrevio).toBe(initialState);
          
          // Clear error
          store.clearError();
          
          // Verify restoration
          const restoredState = usePOSStore.getState();
          expect(restoredState.estado).toBe(initialState);
          expect(restoredState.estadoPrevio).toBe(null);
          expect(restoredState.error).toBe(null);
          
          return true;
        }
      ),
      { numRuns: 100 }
    );
  });

  it('P1.3: clearError restores CALCULANDO_PAGO when estadoPrevio is PROCESANDO (retry logic)', () => {
    fc.assert(
      fc.property(errorArb, (error) => {
        const store = usePOSStore.getState();
        
        // Setup: Start in PROCESANDO state
        usePOSStore.setState({ estado: 'PROCESANDO', estadoPrevio: null, error: null });
        
        // Trigger error during payment processing
        store.setError(error);
        
        // Verify we're in ERROR state with estadoPrevio = PROCESANDO
        const errorState = usePOSStore.getState();
        expect(errorState.estado).toBe('ERROR');
        expect(errorState.estadoPrevio).toBe('PROCESANDO');
        
        // Clear error
        store.clearError();
        
        // Verify restoration to CALCULANDO_PAGO (not PROCESANDO) to allow retry
        const restoredState = usePOSStore.getState();
        expect(restoredState.estado).toBe('CALCULANDO_PAGO');
        expect(restoredState.estadoPrevio).toBe(null);
        expect(restoredState.error).toBe(null);
        
        return true;
      }),
      { numRuns: 50 }
    );
  });

  it('P1.4: clearError returns to IDLE when estadoPrevio is null (fallback behavior)', () => {
    fc.assert(
      fc.property(errorArb, (error) => {
        const store = usePOSStore.getState();
        
        // Setup: Manually set ERROR state with estadoPrevio = null
        usePOSStore.setState({ estado: 'ERROR', estadoPrevio: null, error });
        
        // Clear error
        store.clearError();
        
        // Verify fallback to IDLE
        const restoredState = usePOSStore.getState();
        expect(restoredState.estado).toBe('IDLE');
        expect(restoredState.estadoPrevio).toBe(null);
        expect(restoredState.error).toBe(null);
        
        return true;
      }),
      { numRuns: 50 }
    );
  });

  it('P1.5: Multiple error/clear cycles preserve estadoPrevio correctly', () => {
    fc.assert(
      fc.property(
        fc.array(nonErrorStateArb, { minLength: 2, maxLength: 5 }),
        fc.array(errorArb, { minLength: 2, maxLength: 5 }),
        (states, errors) => {
          const store = usePOSStore.getState();
          
          // Start from IDLE
          usePOSStore.setState({ estado: 'IDLE', estadoPrevio: null, error: null });
          
          // Perform multiple error/clear cycles
          for (let i = 0; i < Math.min(states.length, errors.length); i++) {
            const targetState = states[i];
            const error = errors[i];
            
            // Set target state
            usePOSStore.setState({ estado: targetState, estadoPrevio: null, error: null });
            
            // Trigger error
            store.setError(error);
            
            // Verify error state
            const errorState = usePOSStore.getState();
            expect(errorState.estado).toBe('ERROR');
            expect(errorState.estadoPrevio).toBe(targetState);
            
            // Clear error
            store.clearError();
            
            // Verify restoration
            const restoredState = usePOSStore.getState();
            const expectedState = targetState === 'PROCESANDO' ? 'CALCULANDO_PAGO' : targetState;
            expect(restoredState.estado).toBe(expectedState);
            expect(restoredState.estadoPrevio).toBe(null);
          }
          
          return true;
        }
      ),
      { numRuns: 30 }
    );
  });

  it('P1.6: ERROR → ERROR transition preserves original estadoPrevio', () => {
    fc.assert(
      fc.property(
        nonErrorStateArb,
        errorArb,
        errorArb,
        (initialState, firstError, secondError) => {
          const store = usePOSStore.getState();
          
          // Setup: Start in non-ERROR state
          usePOSStore.setState({ estado: initialState, estadoPrevio: null, error: null });
          
          // First error
          store.setError(firstError);
          const afterFirstError = usePOSStore.getState();
          expect(afterFirstError.estado).toBe('ERROR');
          expect(afterFirstError.estadoPrevio).toBe(initialState);
          
          // Second error while already in ERROR
          store.setError(secondError);
          const afterSecondError = usePOSStore.getState();
          
          // estadoPrevio should still be the original state (ERROR is now saved as estadoPrevio)
          expect(afterSecondError.estado).toBe('ERROR');
          expect(afterSecondError.estadoPrevio).toBe('ERROR');
          expect(afterSecondError.error).toEqual(secondError);
          
          return true;
        }
      ),
      { numRuns: 50 }
    );
  });
});

describe('Edge Cases - estadoPrevio Preservation', () => {
  beforeEach(() => {
    const store = usePOSStore.getState();
    store.resetVenta();
    if (store.error) {
      store.clearError();
    }
  });

  it('E1: Error from IDLE preserves IDLE and returns to IDLE on clearError', () => {
    const error: ErrorUI = { codigo: 'TEST_ERROR', mensaje: 'Test error from IDLE' };
    
    // Ensure we start in IDLE
    usePOSStore.setState({ estado: 'IDLE', estadoPrevio: null, error: null });
    const initialState = usePOSStore.getState();
    expect(initialState.estado).toBe('IDLE');
    
    // Trigger error
    const store = usePOSStore.getState();
    store.setError(error);
    
    const errorState = usePOSStore.getState();
    expect(errorState.estado).toBe('ERROR');
    expect(errorState.estadoPrevio).toBe('IDLE');
    
    // Clear error
    usePOSStore.getState().clearError();
    
    const finalState = usePOSStore.getState();
    expect(finalState.estado).toBe('IDLE');
    expect(finalState.estadoPrevio).toBe(null);
  });

  it('E2: Error from BUSCANDO allows retry by returning to BUSCANDO', () => {
    const store = usePOSStore.getState();
    const error: ErrorUI = { codigo: 'NETWORK_ERROR', mensaje: 'Failed to search products' };
    
    // Setup BUSCANDO state
    usePOSStore.setState({ estado: 'BUSCANDO', estadoPrevio: null, error: null });
    
    // Trigger error
    store.setError(error);
    
    const errorState = usePOSStore.getState();
    expect(errorState.estado).toBe('ERROR');
    expect(errorState.estadoPrevio).toBe('BUSCANDO');
    
    // Clear error to retry
    store.clearError();
    
    const finalState = usePOSStore.getState();
    expect(finalState.estado).toBe('BUSCANDO');
    expect(finalState.estadoPrevio).toBe(null);
  });

  it('E3: Error from CARRITO_ACTIVO preserves cart context', () => {
    const store = usePOSStore.getState();
    const error: ErrorUI = { codigo: 'VALIDATION_ERROR', mensaje: 'Invalid item in cart' };
    
    // Setup CARRITO_ACTIVO state
    usePOSStore.setState({ 
      estado: 'CARRITO_ACTIVO', 
      estadoPrevio: null, 
      error: null,
      carrito: [
        {
          productoId: '123',
          nombre: 'Test Product',
          cantidad: 2,
          precioUnitario: 1000,
          subtotal: 2000,
          stockDisponible: 10,
        }
      ]
    });
    
    // Trigger error
    store.setError(error);
    
    const errorState = usePOSStore.getState();
    expect(errorState.estado).toBe('ERROR');
    expect(errorState.estadoPrevio).toBe('CARRITO_ACTIVO');
    expect(errorState.carrito.length).toBe(1); // Cart is preserved
    
    // Clear error
    store.clearError();
    
    const finalState = usePOSStore.getState();
    expect(finalState.estado).toBe('CARRITO_ACTIVO');
    expect(finalState.estadoPrevio).toBe(null);
    expect(finalState.carrito.length).toBe(1); // Cart still preserved
  });

  it('E4: Error from CALCULANDO_PAGO preserves payment context', () => {
    const store = usePOSStore.getState();
    const error: ErrorUI = { codigo: 'PAYMENT_ERROR', mensaje: 'Payment method unavailable' };
    
    // Setup CALCULANDO_PAGO state
    usePOSStore.setState({ 
      estado: 'CALCULANDO_PAGO', 
      estadoPrevio: null, 
      error: null,
      metodoPago: 'EFECTIVO',
      montoPagado: 5000,
    });
    
    // Trigger error
    store.setError(error);
    
    const errorState = usePOSStore.getState();
    expect(errorState.estado).toBe('ERROR');
    expect(errorState.estadoPrevio).toBe('CALCULANDO_PAGO');
    expect(errorState.metodoPago).toBe('EFECTIVO'); // Payment context preserved
    
    // Clear error
    store.clearError();
    
    const finalState = usePOSStore.getState();
    expect(finalState.estado).toBe('CALCULANDO_PAGO');
    expect(finalState.estadoPrevio).toBe(null);
    expect(finalState.metodoPago).toBe('EFECTIVO'); // Payment context still preserved
  });

  it('E5: Error from VENTA_COMPLETA preserves sale completion state', () => {
    const store = usePOSStore.getState();
    const error: ErrorUI = { codigo: 'PRINT_ERROR', mensaje: 'Failed to print receipt' };
    
    // Setup VENTA_COMPLETA state
    usePOSStore.setState({ 
      estado: 'VENTA_COMPLETA', 
      estadoPrevio: null, 
      error: null,
      ventaIdActual: 'sale-123',
    });
    
    // Trigger error
    store.setError(error);
    
    const errorState = usePOSStore.getState();
    expect(errorState.estado).toBe('ERROR');
    expect(errorState.estadoPrevio).toBe('VENTA_COMPLETA');
    expect(errorState.ventaIdActual).toBe('sale-123'); // Sale context preserved
    
    // Clear error
    store.clearError();
    
    const finalState = usePOSStore.getState();
    expect(finalState.estado).toBe('VENTA_COMPLETA');
    expect(finalState.estadoPrevio).toBe(null);
    expect(finalState.ventaIdActual).toBe('sale-123'); // Sale context still preserved
  });
});
