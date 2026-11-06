import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Api } from '../core/api.service';
import { Reservation } from '../core/models';
import { dateFr, errMsg, money } from '../core/util';

interface Stats {
  ordersToday: number; revenueToday: number; orders30: number; revenue30: number; pendingReservations: number;
  days: { date: string; orders: number; revenue: number }[];
  topProducts: { name: string; quantity: number }[];
  upcomingReservations: Reservation[];
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <h1>Tableau de bord</h1>
    @if (error) { <div class="err">{{ error }}</div> }
    @if (s; as st) {
      <div class="grid c4">
        <div class="stat"><span class="muted">Commandes aujourd'hui</span><b>{{ st.ordersToday }}</b></div>
        <div class="stat"><span class="muted">Chiffre du jour</span><b>{{ money(st.revenueToday) }}</b></div>
        <div class="stat"><span class="muted">Chiffre sur 30 jours</span><b>{{ money(st.revenue30) }}</b></div>
        <a class="stat" routerLink="/admin/reservations" style="text-decoration:none;color:inherit"><span class="muted">Réservations à traiter</span><b>{{ st.pendingReservations }}</b></a>
      </div>
      <div class="grid c2" style="margin-top:16px">
        <section class="card">
          <h3>Chiffre d'affaires des 14 derniers jours</h3>
          <div class="bars-wrap"><div class="bars">
            @for (d of st.days; track d.date) {
              <div [style.height.%]="barHeight(d.revenue)" [title]="d.date + ' : ' + money(d.revenue) + ' (' + d.orders + ' commandes)'"><span>{{ d.date.substring(8) }}</span></div>
            }
          </div></div>
        </section>
        <section class="card">
          <h3>Produits les plus commandés (30 jours)</h3>
          @if (!st.topProducts.length) { <p class="muted">Pas encore de commandes.</p> }
          @for (p of st.topProducts; track p.name) {
            <div class="line"><span>{{ p.name }}</span><b>{{ p.quantity }}</b></div>
          }
        </section>
      </div>
      <section class="card" style="margin-top:16px">
        <h3>Prochains événements dans la salle</h3>
        @if (!st.upcomingReservations.length) { <p class="muted">Aucune réservation à venir.</p> }
        @for (r of st.upcomingReservations; track r.id) {
          <div class="line">
            <div><b>{{ dateText(r.date) }}</b> · {{ r.startTime.substring(0, 5) }}–{{ r.endTime.substring(0, 5) }}<div class="small muted">{{ r.customerName }} · {{ r.guests }} pers. · {{ r.eventType }}</div></div>
            <span class="badge" [class]="r.status">{{ r.status === 'PENDING' ? 'En attente' : 'Confirmée' }}</span>
          </div>
        }
      </section>
    }
  `,
})
export class DashboardPage implements OnInit {
  private api = inject(Api);
  money = money;
  dateText = dateFr;
  s: Stats | null = null;
  error = '';

  ngOnInit() {
    this.api.get<Stats>('/admin/stats').subscribe({ next: (x) => (this.s = x), error: (e) => (this.error = errMsg(e)) });
  }

  barHeight(v: number): number {
    const max = Math.max(1, ...(this.s?.days.map((d) => Number(d.revenue)) || [1]));
    return Math.max(2, (Number(v) / max) * 100);
  }
}
