import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api } from '../core/api.service';
import { Reservation, ReservationStatus } from '../core/models';
import { RESERVATION_LABEL, dateFr, errMsg, money } from '../core/util';

@Component({
  selector: 'app-admin-reservations',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="row between"><h1>Réservations de la salle</h1>
      <select [(ngModel)]="filter" style="width:auto" aria-label="Filtrer par statut">
        <option value="">Tous les statuts</option>
        @for (s of statuses; track s) { <option [value]="s">{{ label[s] }}</option> }
      </select></div>
    @if (error) { <div class="err">{{ error }}</div> }
    <div class="stack">
      @for (r of shown(); track r.id) {
        <article class="card">
          <div class="row between">
            <div><b>{{ dateText(r.date) }}</b> · {{ r.startTime.substring(0, 5) }}–{{ r.endTime.substring(0, 5) }}</div>
            <span class="badge" [class]="r.status">{{ label[r.status] }}</span>
          </div>
          <p style="margin:8px 0 4px">{{ r.customerName }} · {{ r.guests }} pers. · {{ r.eventType }} · <span class="muted">{{ r.code }}</span></p>
          <p class="small muted" style="margin:0">{{ r.phone }} · {{ r.email }}</p>
          @if (r.packageName || r.extrasText) { <p class="small" style="margin:6px 0 0">{{ r.packageName }}{{ r.extrasText ? ' + ' + r.extrasText : '' }}</p> }
          @if (r.note) { <p class="small" style="margin:6px 0 0">« {{ r.note }} »</p> }
          <p style="margin:8px 0"><b>{{ money(r.total) }}</b> <span class="muted">· acompte {{ money(r.deposit) }}</span></p>
          @if (r.status === 'PENDING' || r.status === 'CONFIRMED') {
            <div class="field"><input [(ngModel)]="notes[r.id]" placeholder="Message pour le client (facultatif)" aria-label="Message pour le client" /></div>
            <div class="row">
              @if (r.status === 'PENDING') {
                <button class="btn btn-primary btn-sm" (click)="act(r, 'CONFIRMED')">Confirmer</button>
                <button class="btn btn-danger btn-sm" (click)="act(r, 'REFUSED')">Refuser</button>
              }
              @if (r.status === 'CONFIRMED') {
                <button class="btn btn-dark btn-sm" (click)="act(r, 'COMPLETED')">Marquer terminée</button>
                <button class="btn btn-ghost btn-sm" (click)="act(r, 'CANCELLED')">Annuler</button>
              }
            </div>
          } @else if (r.adminNote) { <p class="small muted">Message envoyé : {{ r.adminNote }}</p> }
        </article>
      }
    </div>
    @if (!shown().length) { <p class="muted">Aucune réservation.</p> }
  `,
})
export class AdminReservationsPage implements OnInit {
  private api = inject(Api);
  money = money;
  label = RESERVATION_LABEL;
  dateText = dateFr;
  statuses: ReservationStatus[] = ['PENDING', 'CONFIRMED', 'COMPLETED', 'REFUSED', 'CANCELLED'];
  list: Reservation[] = [];
  notes: Record<number, string> = {};
  filter = 'PENDING';
  error = '';

  ngOnInit() { this.load(); }

  shown(): Reservation[] { return this.filter ? this.list.filter((r) => r.status === this.filter) : this.list; }

  load() { this.api.get<Reservation[]>('/admin/reservations').subscribe({ next: (l) => (this.list = l), error: (e) => (this.error = errMsg(e)) }); }

  act(r: Reservation, status: ReservationStatus) {
    this.error = '';
    this.api.put<Reservation>('/admin/reservations/' + r.id + '/status', { status, adminNote: this.notes[r.id] || null }).subscribe({
      next: () => this.load(),
      error: (e) => (this.error = errMsg(e)),
    });
  }
}
