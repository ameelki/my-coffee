import { Component, OnInit, inject } from '@angular/core';
import { Api } from '../core/api.service';
import { Column, CrudList, Field } from '../shared/crud-list';

interface Tbl { id: number; label: string; seats: number; active: boolean; }

@Component({
  selector: 'app-tables-admin',
  standalone: true,
  imports: [CrudList],
  template: `
    <h1>Tables et QR codes</h1>
    <app-crud-list title="Tables" endpoint="/admin/tables" [fields]="fields" [columns]="cols" [defaults]="{ active: true, seats: 2 }" (changed)="load()" />
    <div class="row between" style="margin:32px 0 12px">
      <h2 style="margin:0">QR codes à poser sur les tables</h2>
      <button class="btn btn-dark btn-sm no-print" (click)="print()">Imprimer</button>
    </div>
    <p class="muted no-print">En scannant le code, le client arrive sur la carte avec sa table déjà renseignée. Les images des QR codes sont générées par le service api.qrserver.com.</p>
    <div class="grid c4">
      @for (t of tables; track t.id) {
        @if (t.active) {
          <div class="qr-card">
            <img [src]="qr(t)" width="180" height="180" [alt]="'QR code de la table ' + t.label" />
            <b>Table {{ t.label }}</b>
            <div class="small muted">Scannez pour commander</div>
          </div>
        }
      }
    </div>
  `,
})
export class TablesAdminPage implements OnInit {
  private api = inject(Api);
  tables: Tbl[] = [];
  fields: Field[] = [
    { key: 'label', label: 'Nom de la table', type: 'text', hint: 'Exemple : T1, Terrasse 2.' },
    { key: 'seats', label: 'Nombre de places', type: 'number' },
    { key: 'active', label: 'Table utilisée', type: 'bool' },
  ];
  cols: Column[] = [{ key: 'label', label: 'Table' }, { key: 'seats', label: 'Places' }, { key: 'active', label: 'Active', type: 'bool' }];

  ngOnInit() { this.load(); }

  load() { this.api.get<Tbl[]>('/admin/tables').subscribe({ next: (t) => (this.tables = t) }); }

  qr(t: Tbl): string {
    const url = location.origin + '/carte?table=' + encodeURIComponent(t.label);
    return 'https://api.qrserver.com/v1/create-qr-code/?size=360x360&margin=8&data=' + encodeURIComponent(url);
  }

  print() { window.print(); }
}
