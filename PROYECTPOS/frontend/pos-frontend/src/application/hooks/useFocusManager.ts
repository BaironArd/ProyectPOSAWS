import { create } from 'zustand';

/**
 * Secciones navegables del POS
 */
export type FocusSection = 'products' | 'cart' | 'payment';

interface FocusState {
  activeSection: FocusSection;
  setActiveSection: (section: FocusSection) => void;
  moveLeft: () => void;
  moveRight: () => void;
}

/**
 * Store para manejar el foco entre secciones del POS
 * Permite navegación con ←/→ entre Productos, Carrito y Pago
 */
export const useFocusManager = create<FocusState>((set, get) => ({
  activeSection: 'products',

  setActiveSection: (section) => set({ activeSection: section }),

  moveLeft: () => {
    const { activeSection } = get();
    if (activeSection === 'cart') {
      set({ activeSection: 'products' });
    } else if (activeSection === 'payment') {
      set({ activeSection: 'cart' });
    }
  },

  moveRight: () => {
    const { activeSection } = get();
    if (activeSection === 'products') {
      set({ activeSection: 'cart' });
    } else if (activeSection === 'cart') {
      set({ activeSection: 'payment' });
    }
  },
}));
