import { Injectable, computed, signal } from '@angular/core';
import { CartLine, OrderMode } from './models';

@Injectable({ providedIn: 'root' })
export class CartService {
  readonly lines = signal<CartLine[]>(this.read());
  readonly open = signal(false);
  readonly table = signal<string>(this.readTable());
  readonly count = computed(() => this.lines().reduce((n, l) => n + l.qty, 0));
  readonly total = computed(() => this.lines().reduce((n, l) => n + l.qty * l.unitPrice, 0));

  private read(): CartLine[] {
    try { const v = JSON.parse(localStorage.getItem('cafe_cart') || '[]'); return Array.isArray(v) ? v : []; } catch { return []; }
  }

  private readTable(): string {
    try { return localStorage.getItem('cafe_table') || ''; } catch { return ''; }
  }

  private save() {
    try { localStorage.setItem('cafe_cart', JSON.stringify(this.lines())); } catch { /* ignore */ }
  }

  setTable(label: string) {
    this.table.set(label);
    try { localStorage.setItem('cafe_table', label); } catch { /* ignore */ }
  }

  add(line: Omit<CartLine, 'key'>) {
    const key = line.productId + '#' + [...line.choices].sort().join('+') + '#' + (line.note || '');
    const list = [...this.lines()];
    const i = list.findIndex((l) => l.key === key);
    if (i >= 0) list[i] = { ...list[i], qty: list[i].qty + line.qty };
    else list.push({ ...line, key });
    this.lines.set(list);
    this.save();
  }

  setQty(key: string, qty: number) {
    const list = this.lines().map((l) => (l.key === key ? { ...l, qty } : l)).filter((l) => l.qty > 0);
    this.lines.set(list);
    this.save();
  }

  clear() { this.lines.set([]); this.save(); }

  defaultMode(): OrderMode { return this.table() ? 'DINE_IN' : 'TAKEAWAY'; }
}
