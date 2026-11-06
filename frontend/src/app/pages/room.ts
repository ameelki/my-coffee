import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Api } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { SettingsService } from '../core/settings.service';
import { DayInfo, Reservation, RoomExtra, RoomInfo, RoomPackage } from '../core/models';
import { dateFr, errMsg, isoDate, money } from '../core/util';

@Component({
  selector: 'app-room',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="container" style="padding:24px 0">
      <h1 style="font-size:clamp(1.6rem,4vw,2.4rem)">{{ settings.str('room.name', 'Salle des fêtes') }}</h1>
      <p class="muted" style="max-width:62ch">{{ settings.str('room.description') }}</p>

      @if (settings.loaded() && !settings.bool('room.enabled')) {
        <div class="err">Les réservations de la salle sont fermées pour le moment. Contactez-nous au {{ settings.str('cafe.phone') }}.</div>
      } @else if (done; as r) {
        <div class="card stack">
          <h2>Demande envoyée</h2>
          <p>Votre code de réservation :</p>
          <div><span class="code">{{ r.code }}</span></div>
          <p>{{ dateText(r.date) }}, de {{ r.startTime.substring(0, 5) }} à {{ r.endTime.substring(0, 5) }}, {{ r.guests }} personnes.</p>
          <p>
            @if (r.status === 'CONFIRMED') { Votre réservation est <b>confirmée</b>. }
            @else { Nous la validons rapidement et vous écrivons à <b>{{ r.email }}</b>. }
            Total estimé {{ money(r.total) }}, acompte de {{ money(r.deposit) }} à régler sur place.
          </p>
          <div class="row"><a class="btn btn-primary" [routerLink]="['/reservation', r.code]">Suivre ma réservation</a><button class="btn btn-ghost" (click)="reset()">Nouvelle demande</button></div>
        </div>
      } @else {
        <div class="two-col">
          <div class="stack">
            <section class="card">
              <h2>Date</h2>
              <div class="row between" style="margin-bottom:12px">
                <button class="btn btn-ghost btn-sm" (click)="shift(-1)" [disabled]="!canPrev()" aria-label="Mois précédent">Précédent</button>
                <b>{{ monthLabel() }}</b>
                <button class="btn btn-ghost btn-sm" (click)="shift(1)" aria-label="Mois suivant">Suivant</button>
              </div>
              <div class="cal">
                @for (d of dow; track d) { <div class="dow">{{ d }}</div> }
                @for (c of cells(); track $index) {
                  @if (c) {
                    <button class="day" type="button" [disabled]="blocked(c)" [class.closed]="isClosed(c)" [class.part]="isPart(c)" [class.sel]="date === c"
                            [attr.aria-label]="dateText(c)" [attr.aria-pressed]="date === c" (click)="pick(c)">{{ dayNum(c) }}</button>
                  } @else { <div></div> }
                }
              </div>
              <p class="muted small" style="margin-top:10px">Un point orange indique une date déjà partiellement réservée. Les dates hachurées sont fermées.</p>
            </section>

            <section class="card">
              <h2>Horaires et invités</h2>
              @if (bookedToday().length) { <div class="table-banner">Déjà réservé ce jour-là : {{ bookedToday().join(', ') }}</div> }
              <div class="form-grid">
                <div class="field"><label for="start">Début</label>
                  <select id="start" [(ngModel)]="start" (ngModelChange)="fixEnd()">@for (t of times(); track t) { <option [value]="t">{{ t }}</option> }</select></div>
                <div class="field"><label for="end">Fin</label>
                  <select id="end" [(ngModel)]="end">@for (t of endTimes(); track t) { <option [value]="t">{{ t }}</option> }</select></div>
                <div class="field"><label for="guests">Nombre d'invités</label>
                  <input id="guests" type="number" min="1" [max]="settings.num('room.maxGuests')" [(ngModel)]="guests" /></div>
                <div class="field"><label for="type">Type d'événement</label>
                  <select id="type" [(ngModel)]="eventType">@for (t of eventTypes(); track t) { <option [value]="t">{{ t }}</option> }</select></div>
              </div>
              <p class="muted small">Durée de {{ settings.num('room.minHours') }} à {{ settings.num('room.maxHours') }} h, jusqu'à {{ settings.num('room.maxGuests') }} personnes.</p>
            </section>

            @if (info && info.packages.length) {
              <section class="card">
                <h2>Formule</h2>
                <div class="opt" [class.on]="packageId === null" role="button" tabindex="0" (click)="packageId = null" (keydown.enter)="packageId = null">
                  <span><b>Location seule</b><br><span class="muted small">Vous apportez ou commandez au bar.</span></span><span></span>
                </div>
                @for (p of info.packages; track p.id) {
                  <div class="opt" [class.on]="packageId === p.id" role="button" tabindex="0" (click)="packageId = p.id" (keydown.enter)="packageId = p.id">
                    <span><b>{{ p.name }}</b><br><span class="muted small">{{ p.description }} (dès {{ p.minGuests }} pers.)</span></span>
                    <span class="price">{{ money(p.pricePerPerson) }}<small>/pers.</small></span>
                  </div>
                }
              </section>
            }

            @if (info && info.extras.length) {
              <section class="card">
                <h2>Options</h2>
                @for (x of info.extras; track x.id) {
                  <div class="opt" [class.on]="hasExtra(x.id)" role="button" tabindex="0" (click)="toggleExtra(x.id)" (keydown.enter)="toggleExtra(x.id)">
                    <span><b>{{ x.name }}</b><br><span class="muted small">{{ x.description }}</span></span>
                    <span class="price">{{ money(x.price) }}{{ x.perPerson ? '/pers.' : '' }}</span>
                  </div>
                }
              </section>
            }

            <section class="card">
              <h2>Vos coordonnées</h2>
              <div class="form-grid">
                <div class="field"><label for="n">Nom</label><input id="n" [(ngModel)]="name" autocomplete="name" /></div>
                <div class="field"><label for="p">Téléphone</label><input id="p" [(ngModel)]="phone" type="tel" autocomplete="tel" /></div>
              </div>
              <div class="field"><label for="e">Email</label><input id="e" [(ngModel)]="email" type="email" autocomplete="email" /></div>
              <div class="field"><label for="nt">Un souhait particulier ?</label><textarea id="nt" [(ngModel)]="note" maxlength="500"></textarea></div>
            </section>
          </div>

          <aside class="card summary">
            <h3>Votre demande</h3>
            <dl>
              <dt>Date</dt><dd>{{ date ? dateText(date) : 'à choisir' }}</dd>
              <dt>Horaire</dt><dd>{{ start }} – {{ end }} ({{ hours() }} h)</dd>
              <dt>Invités</dt><dd>{{ guests }}</dd>
              <dt>Location</dt><dd>{{ money(base()) }}</dd>
              @if (pkg(); as p) { <dt>{{ p.name }}</dt><dd>{{ money(p.pricePerPerson * guests) }}</dd> }
              @for (x of chosenExtras(); track x.id) { <dt>{{ x.name }}</dt><dd>{{ money(x.perPerson ? x.price * guests : x.price) }}</dd> }
            </dl>
            <div class="row between" style="margin-top:14px"><b>Total estimé</b><span class="big-total">{{ money(total()) }}</span></div>
            <p class="muted small">Acompte de {{ settings.num('room.depositPercent') }} % : {{ money(deposit()) }}</p>
            @if (error) { <div class="err" role="alert">{{ error }}</div> }
            @if (problem()) { <p class="muted small">{{ problem() }}</p> }
            <button class="btn btn-primary btn-block" [disabled]="busy || !!problem()" (click)="submit()">{{ busy ? 'Envoi…' : 'Envoyer ma demande' }}</button>
            <p class="muted small" style="margin-top:10px">{{ settings.str('room.conditions') }}</p>
          </aside>
        </div>
      }
    </div>
  `,
})
export class RoomPage implements OnInit {
  private api = inject(Api);
  private auth = inject(AuthService);
  settings = inject(SettingsService);
  money = money;
  dateText = dateFr;
  dow = ['L', 'M', 'M', 'J', 'V', 'S', 'D'];

  info: RoomInfo | null = null;
  month = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
  dayMap: Record<string, DayInfo> = {};
  date = '';
  start = '18:00';
  end = '23:00';
  guests = 20;
  eventType = '';
  packageId: number | null = null;
  extraIds: number[] = [];
  name = '';
  phone = '';
  email = '';
  note = '';
  busy = false;
  error = '';
  done: Reservation | null = null;

  ngOnInit() {
    const u = this.auth.user();
    if (u) { this.name = u.name; this.phone = u.phone || ''; this.email = u.email; }
    this.api.get<RoomInfo>('/public/room').subscribe({ next: (r) => (this.info = r) });
    this.loadMonth();
    this.eventType = this.eventTypes()[0] || '';
  }

  eventTypes(): string[] { return this.settings.str('room.eventTypes').split(',').map((s) => s.trim()).filter(Boolean); }

  monthKey(): string { return this.month.getFullYear() + '-' + String(this.month.getMonth() + 1).padStart(2, '0'); }
  monthLabel(): string { return this.month.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' }); }

  loadMonth() {
    this.api.get<DayInfo[]>('/public/room/availability', { month: this.monthKey() }).subscribe({
      next: (days) => { const m: Record<string, DayInfo> = {}; for (const d of days) m[d.date] = d; this.dayMap = m; },
    });
  }

  canPrev(): boolean {
    const now = new Date();
    return this.month.getFullYear() > now.getFullYear() || this.month.getMonth() > now.getMonth();
  }

  shift(n: number) {
    this.month = new Date(this.month.getFullYear(), this.month.getMonth() + n, 1);
    this.loadMonth();
  }

  cells(): (string | null)[] {
    const first = new Date(this.month.getFullYear(), this.month.getMonth(), 1);
    const offset = (first.getDay() + 6) % 7;
    const count = new Date(this.month.getFullYear(), this.month.getMonth() + 1, 0).getDate();
    const out: (string | null)[] = [];
    for (let i = 0; i < offset; i++) out.push(null);
    for (let d = 1; d <= count; d++) out.push(isoDate(new Date(this.month.getFullYear(), this.month.getMonth(), d)));
    return out;
  }

  minDate(): string {
    const d = new Date();
    d.setDate(d.getDate() + this.settings.num('room.minNoticeDays'));
    return isoDate(d);
  }
  isClosed(iso: string): boolean { return !!this.dayMap[iso]?.closed; }
  isPart(iso: string): boolean { return (this.dayMap[iso]?.booked.length || 0) > 0; }
  blocked(iso: string): boolean { return iso < this.minDate() || this.isClosed(iso); }
  pick(iso: string) { this.date = iso; }
  dayNum(iso: string): number { return Number(iso.substring(8)); }
  bookedToday(): string[] { return this.date ? this.dayMap[this.date]?.booked || [] : []; }

  times(): string[] {
    const toMin = (t: string) => { const [h, m] = t.split(':').map(Number); return h * 60 + m; };
    const from = toMin(this.settings.str('room.openFrom') || '10:00');
    const to = toMin(this.settings.str('room.openTo') || '23:30');
    const out: string[] = [];
    for (let m = from; m <= to; m += 30) out.push(String(Math.floor(m / 60)).padStart(2, '0') + ':' + String(m % 60).padStart(2, '0'));
    return out;
  }
  endTimes(): string[] { return this.times().filter((t) => t > this.start); }
  fixEnd() {
    const opts = this.endTimes();
    if (!opts.includes(this.end)) this.end = opts[0] || this.end;
  }

  minutes(): number {
    const toMin = (t: string) => { const [h, m] = t.split(':').map(Number); return h * 60 + m; };
    return Math.max(0, toMin(this.end) - toMin(this.start));
  }
  hours(): number { return Math.round((this.minutes() / 60) * 10) / 10; }
  base(): number { return (this.settings.num('room.hourlyRate') * this.minutes()) / 60; }
  pkg(): RoomPackage | null { return this.info?.packages.find((p) => p.id === this.packageId) || null; }
  chosenExtras(): RoomExtra[] { return (this.info?.extras || []).filter((x) => this.extraIds.includes(x.id)); }
  total(): number {
    let t = this.base();
    const p = this.pkg();
    if (p) t += Number(p.pricePerPerson) * this.guests;
    for (const x of this.chosenExtras()) t += Number(x.price) * (x.perPerson ? this.guests : 1);
    return Math.round(t * 100) / 100;
  }
  deposit(): number { return Math.round(this.total() * this.settings.num('room.depositPercent')) / 100; }
  hasExtra(id: number): boolean { return this.extraIds.includes(id); }
  toggleExtra(id: number) { this.extraIds = this.hasExtra(id) ? this.extraIds.filter((x) => x !== id) : [...this.extraIds, id]; }

  problem(): string {
    if (!this.date) return 'Choisissez une date.';
    const min = this.settings.num('room.minHours') * 60;
    const max = this.settings.num('room.maxHours') * 60;
    if (this.minutes() < min) return 'Durée minimum : ' + this.settings.num('room.minHours') + ' h.';
    if (this.minutes() > max) return 'Durée maximum : ' + this.settings.num('room.maxHours') + ' h.';
    if (!this.guests || this.guests < 1) return 'Indiquez le nombre d\'invités.';
    if (this.guests > this.settings.num('room.maxGuests')) return 'Capacité maximum : ' + this.settings.num('room.maxGuests') + ' personnes.';
    const p = this.pkg();
    if (p && this.guests < p.minGuests) return 'Cette formule demande au moins ' + p.minGuests + ' personnes.';
    if (!this.name.trim() || !this.phone.trim() || !this.email.trim()) return 'Renseignez votre nom, téléphone et email.';
    return '';
  }

  submit() {
    this.error = '';
    this.busy = true;
    const body = {
      customerName: this.name, email: this.email, phone: this.phone, eventType: this.eventType || this.eventTypes()[0] || null,
      date: this.date, startTime: this.start, endTime: this.end, guests: this.guests,
      packageId: this.packageId, extraIds: this.extraIds, note: this.note || null,
    };
    this.api.post<Reservation>('/public/reservations', body).subscribe({
      next: (r) => {
        this.done = r;
        this.busy = false;
        try { localStorage.setItem('cafe_last_reservation', r.code); } catch { /* ignore */ }
      },
      error: (e) => { this.error = errMsg(e); this.busy = false; this.loadMonth(); },
    });
  }

  reset() { this.done = null; this.date = ''; this.loadMonth(); }
}
