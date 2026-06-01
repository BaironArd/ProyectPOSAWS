# POS Bugs Fix - Bugfix Design

## Overview

This design document addresses four critical bugs in the POS AWS system that were preventing correct operation of the cashier flow. The bugs span both frontend (React/TypeScript) and backend (Java Lambda) components:

1. **Bug 1 - estadoPrevio not saved**: State machine fails to preserve previous state before transitioning to ERROR, preventing proper error recovery
2. **Bug 2 - productoPort instability**: Unstable reference to productoPort causes infinite re-render loops in product search
3. **Bug 3 - Product search by code**: Missing implementation for searching products by code (e.g., "LAP-001") instead of name
4. **Bug 4 - IVA rounding differences**: Floating-point rounding errors in tax calculation cause valid sales to be rejected

All fixes have been implemented and this document formalizes the technical approach, validation strategy, and preservation requirements.

## Glossary

- **Bug_Condition (C)**: The condition that triggers each bug - varies per bug (see Bug Details section)
- **Property (P)**: The desired behavior when the bug condition holds - the fix must satisfy this
- **Preservation**: Existing correct behavior that must remain unchanged by each fix
- **EstadoUI**: The UI state machine type in the POS store (IDLE, BUSCANDO, RESULTADOS, CARRITO_ACTIVO, CALCULANDO_PAGO, PROCESANDO, VENTA_COMPLETA, ERROR)
- **estadoPrevio**: State variable that stores the previous state before transitioning to ERROR
- **productoPort**: Port interface (IProductoPort) for product search operations
- **useSearch**: React hook that manages product search with debouncing
- **usePOSStore**: Zustand store managing the entire POS state machine
- **SaleService**: Java service class containing business logic for sales, including IVA calculation
- **ProductoAdapter**: Frontend adapter that constructs API URLs and maps Lambda responses to domain types

## Bug Details


### Bug 1 - estadoPrevio not saved before going to ERROR state

#### Bug Condition

The bug manifests when an error occurs in any state of the POS flow (BUSCANDO, RESULTADOS, CARRITO_ACTIVO, CALCULANDO_PAGO, etc.) and the system transitions to ERROR without preserving the previous state. The `setError` action was not saving `estadoPrevio` before changing `estado` to 'ERROR', causing `clearError` to always return to IDLE instead of the actual previous state.

**Formal Specification:**
```
FUNCTION isBugCondition_B1(action, currentState)
  INPUT: action of type string, currentState of type EstadoUI
  OUTPUT: boolean
  
  RETURN action == 'setError' 
         AND currentState != 'ERROR'
         AND estadoPrevio is not being set to currentState
         AND clearError will return to IDLE instead of currentState
END FUNCTION
```

#### Examples

- **Example 1**: User is in BUSCANDO state searching for products → API fails → setError transitions to ERROR without saving BUSCANDO → clearError returns to IDLE → user cannot retry search
- **Example 2**: User is in CARRITO_ACTIVO with items in cart → network error occurs → setError transitions to ERROR without saving CARRITO_ACTIVO → clearError returns to IDLE → cart is lost
- **Example 3**: User is in CALCULANDO_PAGO ready to confirm sale → payment API fails → setError transitions to ERROR without saving CALCULANDO_PAGO → clearError returns to IDLE → entire sale context is lost
- **Edge case**: Error occurs in IDLE state → clearError should return to IDLE (fallback behavior) → this should continue working


### Bug 2 - productoPort instability causing infinite re-renders

#### Bug Condition

The bug manifests when the parent component that uses `useSearch` recreates the `productoPort` instance on every render. Since `productoPort` was a dependency of the `useEffect` in `useSearch`, each new instance triggered the effect, causing infinite re-renders and repeated HTTP calls to the products API.

**Formal Specification:**
```
FUNCTION isBugCondition_B2(productoPort, previousProductoPort)
  INPUT: productoPort of type IProductoPort, previousProductoPort of type IProductoPort
  OUTPUT: boolean
  
  RETURN productoPort !== previousProductoPort
         AND productoPort is in useEffect dependency array
         AND query has not changed
         AND useEffect executes causing new render
END FUNCTION
```

#### Examples

- **Example 1**: Parent component renders → creates new productoPort instance → useSearch useEffect fires → state update triggers parent re-render → infinite loop
- **Example 2**: User types in search box → parent re-renders with new query → new productoPort instance created → useEffect fires twice (once for query, once for productoPort) → duplicate API calls
- **Example 3**: Unrelated state change in parent → parent re-renders → new productoPort instance → useEffect fires unnecessarily → wasted API call
- **Edge case**: productoPort is a stable singleton reference → useEffect only fires when query changes → correct behavior (should be preserved)


### Bug 3 - Product search by code not implemented

#### Bug Condition

The bug manifests when a user enters a query containing the `-` character (pattern for product codes like "LAP-001", "MON-042"). The `ProductoAdapter.buscar` method was always using `type=name` for non-empty queries, causing the backend to search by product name instead of product code, returning no results.

**Formal Specification:**
```
FUNCTION isBugCondition_B3(query)
  INPUT: query of type string
  OUTPUT: boolean
  
  RETURN query.trim().includes('-')
         AND url is constructed with type=name
         AND backend searches by name field
         AND no results are returned for valid product codes
END FUNCTION
```

#### Examples

- **Example 1**: User types "LAP-001" → adapter constructs URL with `type=name&q=LAP-001` → backend searches name field → no match found → empty results
- **Example 2**: User types "MON-042" → adapter constructs URL with `type=name&q=MON-042` → backend searches name field → no match found → empty results
- **Example 3**: User types "Laptop" (no dash) → adapter constructs URL with `type=name&q=Laptop` → backend searches name field → correct results returned (should be preserved)
- **Edge case**: User types empty string → adapter constructs URL with `type=all` → backend returns all products (should be preserved)


### Bug 4 - IVA rounding differences rejecting valid sales

#### Bug Condition

The bug manifests when the frontend calculates a total with IVA (19% tax) and displays it to the user, and the user pays exactly that amount. Due to floating-point arithmetic differences between frontend (JavaScript) and backend (Java), the backend may calculate a total that differs by 1 centavo, causing the validation `if (change < 0)` to reject the sale with "Monto insuficiente" even though the payment is correct.

**Formal Specification:**
```
FUNCTION isBugCondition_B4(amountPaid, total)
  INPUT: amountPaid of type double, total of type double
  OUTPUT: boolean
  
  RETURN (amountPaid - total) is in range [-0.01, 0)
         AND Math.round(amountPaid * 100) >= Math.round(total * 100)
         AND original code throws IllegalArgumentException("Monto insuficiente")
         AND sale is actually valid (difference is rounding error, not insufficient payment)
END FUNCTION
```

#### Examples

- **Example 1**: Frontend calculates total = 11.90 → user pays 11.90 → backend calculates total = 11.901 due to rounding → change = -0.001 → exception thrown → sale rejected incorrectly
- **Example 2**: Subtotal = 10.00 → IVA = 1.90 → total = 11.90 → user pays 11.90 → backend rounds differently → change = -0.005 → exception thrown → sale rejected incorrectly
- **Example 3**: User pays 11.89 when total is 11.90 → change = -0.01 → exception thrown → sale correctly rejected (genuine insufficient payment, should be preserved)
- **Edge case**: User pays 12.00 when total is 11.90 → change = 0.10 → sale accepted → correct behavior (should be preserved)


## Expected Behavior

### Preservation Requirements

**Bug 1 - Unchanged Behaviors:**
- Valid state transitions defined in `TRANSICIONES_VALIDAS` must continue to work exactly as before
- When `clearError` is called and `estadoPrevio` is null (error occurred in IDLE), system must return to IDLE as fallback
- Successful sale completion with `resetVenta` must continue to return to IDLE without affecting `estadoPrevio` logic
- All non-error state transitions must remain unaffected

**Bug 2 - Unchanged Behaviors:**
- Search with debounce of 300ms must continue to work when query changes
- Queries with less than 2 characters must continue to clear results and return to IDLE
- Empty query must continue to fetch all products (type=all)
- Search results mapping from Lambda response to Producto domain type must remain unchanged
- Error handling for API failures must continue to work correctly

**Bug 3 - Unchanged Behaviors:**
- Empty query must continue to use `type=all` to fetch all products
- Queries without `-` character must continue to use `type=name` for name-based search
- Response mapping from Lambda format (LambdaProducto) to domain format (Producto) must remain unchanged
- UUID to number conversion using hashCode must continue to work
- Stock level and price mapping must remain unchanged

**Bug 4 - Unchanged Behaviors:**
- Sales with genuinely insufficient payment (difference > 1 centavo) must continue to be rejected with IllegalArgumentException
- Accepted sales must continue to calculate change correctly and persist with correct total
- IVA calculation formula `iva = Math.round(subtotal * IVA * 100.0) / 100.0` must remain unchanged
- Total calculation formula `total = Math.round((subtotal + iva) * 100.0) / 100.0` must remain unchanged
- All other validation logic (null checks, quantity > 0, price > 0) must remain unchanged

**Scope:**
All inputs and operations that do NOT trigger the specific bug conditions should be completely unaffected by these fixes. The fixes are surgical and targeted to address only the identified defects.


## Hypothesized Root Cause

Based on the bug descriptions and code analysis, the root causes are:

### Bug 1 - estadoPrevio not saved

1. **Missing State Preservation**: The `setError` action in `usePOSStore` was implemented as:
   ```typescript
   setError: (error) => set({ error, estado: 'ERROR' })
   ```
   This transitions to ERROR but does not capture the current state in `estadoPrevio`.

2. **Incorrect Recovery Logic**: The `clearError` action always fell back to IDLE:
   ```typescript
   clearError: () => set({ error: null, estado: s.estadoPrevio ?? 'IDLE' })
   ```
   Since `estadoPrevio` was always null, recovery always went to IDLE.

3. **State Machine Design Gap**: The state machine had transitions defined but no mechanism to preserve context across error boundaries.

### Bug 2 - productoPort instability

1. **Unstable Dependency**: The `useSearch` hook had `productoPort` in the useEffect dependency array:
   ```typescript
   useEffect(() => { /* search logic */ }, [query, productoPort])
   ```
   If the parent component created a new instance on each render, this triggered the effect repeatedly.

2. **Missing Memoization**: The parent component was not memoizing or using a singleton for `productoPort`, causing new instances on every render.

3. **React Reconciliation**: React's reconciliation algorithm detected the reference change and re-executed the effect, even though the functionality was identical.


### Bug 3 - Product search by code

1. **Incomplete URL Construction Logic**: The `ProductoAdapter.buscar` method only checked for empty query:
   ```typescript
   if (!trimmed) {
     url = `${API_BASE_URL}/products?type=all`;
   } else {
     url = `${API_BASE_URL}/products?type=name&q=${encodeURIComponent(trimmed)}`;
   }
   ```
   There was no logic to detect product codes (containing `-`) and use `type=code`.

2. **Missing Product Code Pattern Recognition**: The adapter did not recognize the `-` character as an indicator of product code format.

3. **Backend API Support Exists**: The backend Lambda already supported `type=code` parameter, but the frontend was not using it.

### Bug 4 - IVA rounding differences

1. **Floating-Point Comparison**: The validation logic compared doubles directly:
   ```java
   double change = amountPaid - total;
   if (change < 0) {
       throw new IllegalArgumentException("Monto insuficiente");
   }
   ```
   This is vulnerable to floating-point precision errors.

2. **Different Rounding Contexts**: Frontend (JavaScript) and backend (Java) may round intermediate calculations differently, leading to totals that differ by fractions of a centavo.

3. **Strict Inequality Check**: The `change < 0` check rejected sales even when the difference was negligible (< 1 centavo), which should be considered equal in monetary context.

4. **No Tolerance for Rounding Errors**: The code had no tolerance threshold for floating-point arithmetic errors, treating -0.001 as genuinely insufficient payment.


## Correctness Properties

Property 1: Bug Condition - estadoPrevio Preservation

_For any_ state transition where `setError` is called from a non-ERROR state, the fixed function SHALL save the current state in `estadoPrevio` before transitioning to ERROR, and `clearError` SHALL restore that saved state (or IDLE if estadoPrevio is null).

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Non-Error State Transitions

_For any_ state transition that does NOT involve `setError` or `clearError`, the fixed code SHALL produce exactly the same state transitions as the original code, preserving all valid state machine behavior defined in `TRANSICIONES_VALIDAS`.

**Validates: Requirements 3.1, 3.2, 3.3**

Property 3: Bug Condition - productoPort Stability

_For any_ render where `productoPort` reference changes but `query` remains the same, the fixed `useSearch` hook SHALL NOT trigger the search effect, preventing infinite re-renders and duplicate API calls.

**Validates: Requirements 2.4, 2.5**

Property 4: Preservation - Search Behavior

_For any_ query change, the fixed `useSearch` hook SHALL execute the search with the same debounce timing (300ms), validation rules (min 2 chars), and result handling as the original implementation.

**Validates: Requirements 3.4, 3.5, 3.6**

Property 5: Bug Condition - Product Code Search

_For any_ query containing the `-` character, the fixed `ProductoAdapter.buscar` method SHALL construct the API URL with `type=code` parameter, enabling product code search functionality.

**Validates: Requirements 2.6, 2.7, 2.8**


Property 6: Preservation - Name and All Search

_For any_ query that does NOT contain `-` (or is empty), the fixed `ProductoAdapter.buscar` method SHALL produce the same URL construction and results as the original implementation (type=name for non-empty, type=all for empty).

**Validates: Requirements 3.7, 3.8, 3.9**

Property 7: Bug Condition - IVA Rounding Tolerance

_For any_ sale where `Math.round(amountPaid * 100) >= Math.round(total * 100)` (payment is sufficient in centavos), the fixed `SaleService.createSale` method SHALL accept the sale without throwing IllegalArgumentException, even if floating-point subtraction produces a negative value.

**Validates: Requirements 2.9, 2.10, 2.11**

Property 8: Preservation - Genuine Insufficient Payment Rejection

_For any_ sale where `Math.round(amountPaid * 100) < Math.round(total * 100)` (payment is genuinely insufficient by more than rounding error), the fixed `SaleService.createSale` method SHALL reject the sale with IllegalArgumentException exactly as the original implementation did.

**Validates: Requirements 3.10, 3.11, 3.12**


## Fix Implementation

### Changes Required

All fixes have been implemented. This section documents the specific changes made to each component.

---

### Bug 1 - estadoPrevio not saved

**File**: `PROYECTPOS/frontend/pos-frontend/src/application/store/usePOSStore.ts`

**Function**: `setError` and `clearError` actions in the Zustand store

**Specific Changes**:

1. **Modified setError to save estadoPrevio**:
   ```typescript
   // BEFORE:
   setError: (error) => set({ error, estado: 'ERROR' })
   
   // AFTER:
   setError: (error) => set((s) => ({ error, estado: 'ERROR', estadoPrevio: s.estado }))
   ```
   Now captures the current state before transitioning to ERROR.

2. **Enhanced clearError recovery logic**:
   ```typescript
   // BEFORE:
   clearError: () => set({ error: null, estado: s.estadoPrevio ?? 'IDLE' })
   
   // AFTER:
   clearError: () => set((s) => {
     const destino: EstadoUI =
       s.estadoPrevio === 'PROCESANDO' ? 'CALCULANDO_PAGO'
       : s.estadoPrevio ?? 'IDLE';
     return { error: null, estado: destino, estadoPrevio: null };
   })
   ```
   Now restores the saved state, with special handling for PROCESANDO → CALCULANDO_PAGO to allow retry.

3. **State Machine Integrity**: The fix maintains all existing transition rules in `TRANSICIONES_VALIDAS` and only affects error/recovery paths.


---

### Bug 2 - productoPort instability

**File**: `PROYECTPOS/frontend/pos-frontend/src/application/hooks/useSearch.ts`

**Function**: `useSearch` hook

**Specific Changes**:

1. **Stabilized productoPort reference using useRef**:
   ```typescript
   // Added at top of hook:
   const productoPortRef = useRef(productoPort);
   productoPortRef.current = productoPort;
   ```
   This creates a stable reference that doesn't change between renders.

2. **Stabilized store action references**:
   ```typescript
   // Added for all store actions:
   const setEstadoRef   = useRef(setEstado);
   const setProductosRef = useRef(setProductos);
   const setErrorRef    = useRef(setError);
   setEstadoRef.current   = setEstado;
   setProductosRef.current = setProductos;
   setErrorRef.current    = setError;
   ```
   Prevents re-execution when Zustand recreates action functions.

3. **Updated useEffect dependencies**:
   ```typescript
   // BEFORE:
   useEffect(() => { /* ... */ }, [query, productoPort, setEstado, setProductos, setError])
   
   // AFTER:
   useEffect(() => {
     // Use productoPortRef.current, setEstadoRef.current, etc.
   }, [query, resetCount])
   ```
   Only `query` and `resetCount` trigger the effect; all other values accessed via refs.

4. **Preserved all search logic**: Debounce timing, min character validation, error handling remain identical.


---

### Bug 3 - Product search by code

**File**: `PROYECTPOS/frontend/pos-frontend/src/infrastructure/adapters/ProductoAdapter.ts`

**Function**: `buscar` method in `ProductoAdapter` class

**Specific Changes**:

1. **Added product code detection logic**:
   ```typescript
   // BEFORE:
   if (!trimmed) {
     url = `${API_BASE_URL}/products?type=all`;
   } else {
     url = `${API_BASE_URL}/products?type=name&q=${encodeURIComponent(trimmed)}`;
   }
   
   // AFTER:
   if (!trimmed) {
     url = `${API_BASE_URL}/products?type=all`;
   } else if (trimmed.includes('-')) {
     url = `${API_BASE_URL}/products?type=code&q=${encodeURIComponent(trimmed)}`;
   } else {
     url = `${API_BASE_URL}/products?type=name&q=${encodeURIComponent(trimmed)}`;
   }
   ```

2. **Pattern Recognition**: The fix uses `includes('-')` to detect product code format (e.g., "LAP-001", "MON-042").

3. **API Integration**: Leverages existing backend support for `type=code` parameter in the Lambda function.

4. **Preserved Behavior**: Empty query still uses `type=all`, name queries (without `-`) still use `type=name`, response mapping unchanged.


---

### Bug 4 - IVA rounding differences

**File**: `pos-sam/save-sale/src/main/java/com/pos/sam/sales/service/SaleService.java`

**Function**: `createSale` method in `SaleService` class

**Specific Changes**:

1. **Replaced floating-point comparison with integer centavo comparison**:
   ```java
   // BEFORE:
   double change = amountPaid - total;
   if (change < 0) {
       throw new IllegalArgumentException(
               "Monto insuficiente. Total: " + total + ", Pagado: " + req.getAmountPaid());
   }
   
   // AFTER:
   // Comparar en centavos enteros para evitar errores de punto flotante
   long totalCentavos  = Math.round(total * 100);
   long pagadoCentavos = Math.round(req.getAmountPaid() * 100);
   if (pagadoCentavos < totalCentavos) {
       throw new IllegalArgumentException(
               "Monto insuficiente. Total: " + total + ", Pagado: " + req.getAmountPaid());
   }
   double change = Math.round((req.getAmountPaid() - total) * 100.0) / 100.0;
   ```

2. **Centavo-Based Validation**: Converts both amounts to integer centavos using `Math.round(value * 100)` before comparison, eliminating floating-point precision issues.

3. **Preserved Change Calculation**: Still calculates `change` as a double for display/receipt purposes, with proper rounding.

4. **Preserved All Other Logic**: IVA calculation formula, total calculation formula, and all other validations remain unchanged.

5. **Monetary Precision**: The fix treats amounts that differ by less than 1 centavo as equal, which is correct for monetary transactions.


## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach for each bug:
1. **Exploratory Bug Condition Checking**: Demonstrate the bug on unfixed code to confirm root cause
2. **Fix and Preservation Checking**: Verify the fix works correctly and preserves existing behavior

Since all fixes have been implemented, testing focuses on regression prevention and validation that the fixes work as designed.

---

### Bug 1 - estadoPrevio not saved

#### Exploratory Bug Condition Checking

**Goal**: Confirm that the original code failed to save `estadoPrevio` and always returned to IDLE on error recovery.

**Test Plan**: Simulate errors from various states and observe that `clearError` always returns to IDLE instead of the previous state.

**Test Cases**:
1. **Error from BUSCANDO**: Trigger error during search → verify estadoPrevio is null → clearError returns to IDLE (incorrect)
2. **Error from CARRITO_ACTIVO**: Trigger error with items in cart → verify estadoPrevio is null → clearError returns to IDLE, losing cart context (incorrect)
3. **Error from CALCULANDO_PAGO**: Trigger error during payment → verify estadoPrevio is null → clearError returns to IDLE, losing sale context (incorrect)

**Expected Counterexamples**: All error recovery scenarios return to IDLE regardless of previous state.


#### Fix Checking

**Goal**: Verify that for all states where an error occurs, the fixed function saves `estadoPrevio` and restores it correctly.

**Pseudocode:**
```
FOR ALL estado WHERE estado != 'ERROR' DO
  initialState := estado
  setError'(someError)
  ASSERT store.estadoPrevio == initialState
  ASSERT store.estado == 'ERROR'
  clearError'()
  ASSERT store.estado == initialState OR (initialState == 'PROCESANDO' AND store.estado == 'CALCULANDO_PAGO')
  ASSERT store.estadoPrevio == null
END FOR
```

**Test Cases**:
1. **Error from BUSCANDO**: setError → estadoPrevio = BUSCANDO → clearError → estado = BUSCANDO
2. **Error from RESULTADOS**: setError → estadoPrevio = RESULTADOS → clearError → estado = RESULTADOS
3. **Error from CARRITO_ACTIVO**: setError → estadoPrevio = CARRITO_ACTIVO → clearError → estado = CARRITO_ACTIVO
4. **Error from CALCULANDO_PAGO**: setError → estadoPrevio = CALCULANDO_PAGO → clearError → estado = CALCULANDO_PAGO
5. **Error from PROCESANDO**: setError → estadoPrevio = PROCESANDO → clearError → estado = CALCULANDO_PAGO (special case for retry)
6. **Error from IDLE**: setError → estadoPrevio = IDLE → clearError → estado = IDLE

#### Preservation Checking

**Goal**: Verify that non-error state transitions work exactly as before.

**Pseudocode:**
```
FOR ALL transition WHERE transition NOT IN ['setError', 'clearError'] DO
  ASSERT F(transition) == F'(transition)
END FOR
```

**Test Cases**:
1. **Normal flow**: IDLE → BUSCANDO → RESULTADOS → CARRITO_ACTIVO → CALCULANDO_PAGO → PROCESANDO → VENTA_COMPLETA → IDLE
2. **Invalid transitions**: Verify TRANSICIONES_VALIDAS still blocks invalid transitions
3. **resetVenta**: Verify resetVenta still returns to IDLE without affecting estadoPrevio


---

### Bug 2 - productoPort instability

#### Exploratory Bug Condition Checking

**Goal**: Demonstrate that unstable `productoPort` reference causes infinite re-renders.

**Test Plan**: Create a test component that recreates `productoPort` on each render and observe infinite loop behavior.

**Test Cases**:
1. **Unstable reference**: Pass new productoPort instance on each render → observe infinite useEffect executions
2. **Render count explosion**: Track render count → verify it grows unbounded
3. **API call duplication**: Mock API calls → verify multiple calls for single query

**Expected Counterexamples**: useEffect fires repeatedly even when query doesn't change, causing performance degradation.

#### Fix Checking

**Goal**: Verify that productoPort reference changes do NOT trigger the search effect.

**Pseudocode:**
```
FOR ALL render WHERE productoPort changes BUT query unchanged DO
  effectExecutionCount := countEffectExecutions()
  ASSERT effectExecutionCount == 0
END FOR
```

**Test Cases**:
1. **Stable behavior with unstable prop**: Pass new productoPort instance → verify useEffect does NOT fire
2. **Query change still triggers**: Change query → verify useEffect DOES fire exactly once
3. **Render count stability**: Verify render count stays bounded even with unstable productoPort


#### Preservation Checking

**Goal**: Verify that search behavior remains unchanged when query changes.

**Pseudocode:**
```
FOR ALL query WHERE query changes DO
  ASSERT search executes with 300ms debounce
  ASSERT query < 2 chars clears results
  ASSERT query >= 2 chars triggers API call
  ASSERT results are mapped correctly
END FOR
```

**Test Cases**:
1. **Debounce timing**: Type query → verify API call happens after 300ms, not immediately
2. **Min character validation**: Type "A" → verify results cleared, no API call
3. **Empty query**: Clear query → verify type=all API call
4. **Error handling**: Mock API error → verify setError called correctly

---

### Bug 3 - Product search by code

#### Exploratory Bug Condition Checking

**Goal**: Demonstrate that product code queries return no results on unfixed code.

**Test Plan**: Search for valid product codes and observe empty results.

**Test Cases**:
1. **Valid product code**: Search "LAP-001" → verify URL has type=name → verify empty results (incorrect)
2. **Another product code**: Search "MON-042" → verify URL has type=name → verify empty results (incorrect)
3. **Name search works**: Search "Laptop" → verify URL has type=name → verify correct results (should be preserved)

**Expected Counterexamples**: Product code searches fail because URL uses type=name instead of type=code.


#### Fix Checking

**Goal**: Verify that queries containing `-` use type=code and return correct results.

**Pseudocode:**
```
FOR ALL query WHERE query.includes('-') DO
  url := buildUrl'(query)
  ASSERT url.includes('type=code')
  ASSERT url.includes('q=' + encodeURIComponent(query))
  results := buscar'(query)
  ASSERT results contain products with matching code
END FOR
```

**Test Cases**:
1. **Product code LAP-001**: Search "LAP-001" → verify URL has type=code → verify results contain laptop with code LAP-001
2. **Product code MON-042**: Search "MON-042" → verify URL has type=code → verify results contain monitor with code MON-042
3. **Partial code**: Search "LAP-" → verify URL has type=code → verify results contain all products with codes starting with LAP-

#### Preservation Checking

**Goal**: Verify that name searches and empty searches work exactly as before.

**Pseudocode:**
```
FOR ALL query WHERE NOT query.includes('-') DO
  url := buildUrl'(query)
  IF query == '' THEN
    ASSERT url.includes('type=all')
  ELSE
    ASSERT url.includes('type=name')
  END IF
  ASSERT buscar'(query) == buscar_original(query)
END FOR
```

**Test Cases**:
1. **Name search**: Search "Laptop" → verify URL has type=name → verify same results as original
2. **Empty search**: Search "" → verify URL has type=all → verify same results as original
3. **Response mapping**: Verify LambdaProducto → Producto mapping unchanged for all search types


---

### Bug 4 - IVA rounding differences

#### Exploratory Bug Condition Checking

**Goal**: Demonstrate that valid sales are rejected due to floating-point rounding errors.

**Test Plan**: Create sales where frontend and backend calculate slightly different totals due to rounding, and observe rejection.

**Test Cases**:
1. **Subtotal 10.00**: subtotal=10.00 → iva=1.90 → total=11.90 → pay 11.90 → backend may calculate 11.901 → change=-0.001 → exception (incorrect)
2. **Subtotal 25.00**: subtotal=25.00 → iva=4.75 → total=29.75 → pay 29.75 → backend may calculate 29.751 → change=-0.001 → exception (incorrect)
3. **Subtotal 100.00**: subtotal=100.00 → iva=19.00 → total=119.00 → pay 119.00 → backend may calculate 119.001 → change=-0.001 → exception (incorrect)

**Expected Counterexamples**: Sales with exact payment are rejected due to sub-centavo rounding differences.

#### Fix Checking

**Goal**: Verify that sales with payment sufficient in centavos are accepted.

**Pseudocode:**
```
FOR ALL sale WHERE Math.round(amountPaid * 100) >= Math.round(total * 100) DO
  result := createSale'(sale)
  ASSERT result does NOT throw IllegalArgumentException
  ASSERT result.detalle.status == 'COMPLETED'
END FOR
```

**Test Cases**:
1. **Exact payment with rounding**: total=11.90, pay=11.90 → verify accepted even if floating-point difference exists
2. **Overpayment**: total=11.90, pay=12.00 → verify accepted with change=0.10
3. **Centavo-level precision**: total=11.905 (rounded to 11.91), pay=11.91 → verify accepted


#### Preservation Checking

**Goal**: Verify that genuinely insufficient payments are still rejected.

**Pseudocode:**
```
FOR ALL sale WHERE Math.round(amountPaid * 100) < Math.round(total * 100) DO
  ASSERT createSale'(sale) throws IllegalArgumentException
  ASSERT exception.message contains "Monto insuficiente"
END FOR
```

**Test Cases**:
1. **Underpayment by 1 centavo**: total=11.90, pay=11.89 → verify rejected with exception
2. **Underpayment by 10 centavos**: total=11.90, pay=11.80 → verify rejected with exception
3. **Underpayment by 1 peso**: total=11.90, pay=10.90 → verify rejected with exception
4. **IVA calculation unchanged**: Verify iva = Math.round(subtotal * 0.19 * 100.0) / 100.0 still used
5. **Total calculation unchanged**: Verify total = Math.round((subtotal + iva) * 100.0) / 100.0 still used
6. **Change calculation**: Verify change is still calculated and rounded correctly for accepted sales

---

### Unit Tests

**Bug 1 - estadoPrevio**:
- Test setError saves estadoPrevio for each valid state
- Test clearError restores estadoPrevio correctly
- Test clearError with null estadoPrevio returns to IDLE
- Test PROCESANDO → ERROR → clearError returns to CALCULANDO_PAGO

**Bug 2 - productoPort**:
- Test useSearch with stable productoPort reference
- Test useSearch with unstable productoPort reference (should not re-execute)
- Test query change triggers search
- Test debounce timing
- Test min character validation


**Bug 3 - Product search by code**:
- Test query with `-` constructs URL with type=code
- Test query without `-` constructs URL with type=name
- Test empty query constructs URL with type=all
- Test response mapping for all search types

**Bug 4 - IVA rounding**:
- Test exact payment with potential rounding error is accepted
- Test underpayment by 1 centavo is rejected
- Test overpayment is accepted with correct change
- Test IVA calculation formula unchanged
- Test total calculation formula unchanged

### Property-Based Tests

**Bug 1 - estadoPrevio**:
- Generate random state transitions with errors → verify estadoPrevio always saved
- Generate random error recovery sequences → verify state always restored correctly
- Generate random non-error transitions → verify behavior unchanged

**Bug 2 - productoPort**:
- Generate random render sequences with unstable productoPort → verify no infinite loops
- Generate random query changes → verify search executes exactly once per query
- Generate random timing scenarios → verify debounce works correctly

**Bug 3 - Product search by code**:
- Generate random queries with `-` → verify all use type=code
- Generate random queries without `-` → verify all use type=name
- Generate random product codes → verify all return correct results

**Bug 4 - IVA rounding**:
- Generate random subtotals → calculate IVA and total → verify exact payment accepted
- Generate random underpayment amounts → verify all rejected
- Generate random overpayment amounts → verify all accepted with correct change
- Generate random floating-point edge cases → verify centavo comparison handles all correctly


### Integration Tests

**Bug 1 - estadoPrevio**:
- Test full error recovery flow: search → error → clear → retry search
- Test error during payment → clear → retry payment
- Test error with cart → clear → verify cart preserved

**Bug 2 - productoPort**:
- Test full search flow with real API calls
- Test search with parent component re-renders
- Test search with multiple rapid query changes

**Bug 3 - Product search by code**:
- Test end-to-end product code search with real backend
- Test switching between code search and name search
- Test adding products found by code to cart

**Bug 4 - IVA rounding**:
- Test full sale flow with exact payment from frontend calculation
- Test sale with multiple items and complex IVA calculation
- Test sale with payment methods (cash, card, mixed) and rounding edge cases
- Test sale persistence and receipt generation with corrected totals
