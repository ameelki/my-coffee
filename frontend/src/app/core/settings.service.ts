import { Injectable, inject, signal } from '@angular/core';
import { Api } from './api.service';
import { currentLang } from './i18n.service';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private api = inject(Api);
  readonly s = signal<Record<string, string>>({});
  readonly loaded = signal(false);

  load() {
    this.api.get<Record<string, string>>('/public/settings').subscribe({
      next: (v) => { this.s.set(v); this.loaded.set(true); this.applyBrand(v); },
      error: () => this.loaded.set(true),
    });
  }

  str(k: string, fallback = ''): string {
    const l = currentLang();
    if (l !== 'fr') { const t = this.s()[k + '.' + l]; if (t && t.trim()) return t; }
    return this.s()[k] ?? fallback;
  }
  bool(k: string): boolean { return (this.s()[k] ?? '').toLowerCase() === 'true'; }
  num(k: string): number { const n = parseFloat((this.s()[k] ?? '').replace(',', '.')); return isNaN(n) ? 0 : n; }

  private applyBrand(v: Record<string, string>) {
    const root = document.documentElement.style;
    if (/^#[0-9a-fA-F]{6}$/.test(v['brand.primary'] || '')) root.setProperty('--copper', v['brand.primary']);
    if (/^#[0-9a-fA-F]{6}$/.test(v['brand.dark'] || '')) root.setProperty('--ink', v['brand.dark']);
    if (v['cafe.name']) document.title = v['cafe.name'];
  }
}
