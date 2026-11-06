import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Api } from '../core/api.service';
import { Reservation } from '../core/models';
import { RESERVATION_LABEL, dateFr, errMsg, money } from '../core/util';

@Component({
  selector: 'app-track-reservation',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="container" style="padding:24px 0; max-width:720px">
      <h1 style="font-size:clamp(1.6rem,4vw,2.4rem)">Ma réservation</h1>
      <form class="row" (ngSubmit)="search()" style="margin-bottom:20px">
        <input name="code" [(ngModel)]="input" placeholder="Votre code, ex : R-4MZ8QA" style="flex:1;min-width:200px" aria-label="Code de réservation" />
        <button class="btn btn-dark">Afficher</button>
      </form>
      @if (error) { <div class="err">{{ error }}</div> }
      @if (r; as x) {
        <div class="card stack">
          <div class="row between"><span class="code">{{ x.code }}</span><span class="badge" [class]="x.status">{{ label[x.status] }}</span></div>
          <p><b>{{ dateText(x.date) }}</b><br>de {{ x.startTime.substring(0, 5) }} à {{ x.endTime.substring(0, 5) }} · {{ x.guests }} personnes@if (x.eventType) { · {{ x.eventType }} }</p>
          @if (x.packageName) { <p>Formule : {{ x.packageName }}</p> }
          @if (x.extrasText) { <p>Options : {{ x.extrasText }}</p> }
          @if (x.mealsText) { <p>Repas : {{ x.mealsText }}</p> }
          <p>Total {{ money(x.total) }} · acompte {{ money(x.deposit) }} à régler sur place</p>
          @if (x.adminNote) { <div class="table-banner">Message du café : {{ x.adminNote }}</div> }
          @if (x.status === 'PENDING' || x.status === 'CONFIRMED') {
            <button class="btn btn-ghost btn-sm" (click)="cancel()">Annuler ma réservation</button>
          }
        </div>
      }
    </div>
  `,
})
export class TrackReservationPage implements OnInit {
  private api = inject(Api);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  label = RESERVATION_LABEL;
  money = money;
  dateText = dateFr;
  input = '';
  r: Reservation | null = null;
  error = '';

  ngOnInit() {
    this.route.paramMap.subscribe((p) => {
      let code = p.get('code') || '';
      if (!code) { try { code = localStorage.getItem('cafe_last_reservation') || ''; } catch { code = ''; } }
      this.input = code;
      if (code) this.load(code);
    });
  }

  search() {
    const c = this.input.trim();
    if (c) this.router.navigate(['/reservation', c.toUpperCase()]);
  }

  load(code: string) {
    this.api.get<Reservation>('/public/reservations/' + encodeURIComponent(code)).subscribe({
      next: (x) => { this.r = x; this.error = ''; },
      error: (e) => { this.r = null; this.error = errMsg(e); },
    });
  }

  cancel() {
    if (!this.r) return;
    this.api.post<Reservation>('/public/reservations/' + this.r.code + '/cancel').subscribe({
      next: (x) => { this.r = x; this.error = ''; },
      error: (e) => (this.error = errMsg(e)),
    });
  }
}
