import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Api } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { CartService } from '../core/cart.service';
import { SettingsService } from '../core/settings.service';
import { Order, OrderMode } from '../core/models';
import { errMsg, isoDate, money } from '../core/util';
import { currentLang } from '../core/i18n.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="container" style="padding:24px 0">
      <h1 style="font-size:clamp(1.6rem,4vw,2.4rem)">Votre commande</h1>
      @if (!cart.lines().length) {
        <div class="card"><p>Votre panier est vide.</p><a routerLink="/carte" class="btn btn-primary">Voir la carte</a></div>
      } @else {
        <div class="two-col">
          <form class="card" (ngSubmit)="submit()" #f="ngForm">
            @if (error) { <div class="err" role="alert">{{ error }}</div> }
            <div class="field">
              <span class="lbl">Comment souhaitez-vous recevoir votre commande ?</span>
              <div class="row">
                @if (settings.bool('orders.dineIn.enabled')) {
                  <label class="check"><input type="radio" name="mode" value="DINE_IN" [(ngModel)]="mode" /> Sur place, à ma table</label>
                }
                @if (settings.bool('orders.takeaway.enabled')) {
                  <label class="check"><input type="radio" name="mode" value="TAKEAWAY" [(ngModel)]="mode" /> À emporter</label>
                }
              </div>
            </div>
            @if (mode === 'DINE_IN') {
              <div class="field">
                <label for="table">Numéro de table</label>
                <select id="table" name="table" [(ngModel)]="table" required>
                  <option value="">Choisir…</option>
                  @for (t of tables; track t) { <option [value]="t">{{ t }}</option> }
                </select>
              </div>
            } @else {
              <div class="field">
                <span class="lbl">Jour de retrait</span>
                <div class="chips">
                  @for (d of days; track d.iso) {
                    <button type="button" class="chip" [class.on]="pickupDate === d.iso" (click)="pickDay(d.iso)">{{ d.label }}</button>
                  }
                </div>
                <label for="otherday" class="small muted" style="margin-top:8px">Autre date</label>
                <input id="otherday" name="otherday" type="date" [min]="todayIso" [(ngModel)]="pickupDate" (ngModelChange)="pickDay($event)" />
              </div>
              <div class="field">
                <span class="lbl">Heure de retrait</span>
                <div class="chips">
                  @if (pickupDate === todayIso) {
                    <button type="button" class="chip" [class.on]="pickup === ''" (click)="pickup = ''">Dès que possible (~{{ settings.num('orders.takeaway.minMinutes') }} min)</button>
                  }
                  @for (t of slots(); track t) {
                    <button type="button" class="chip" [class.on]="pickup === t" (click)="pickup = t">{{ t }}</button>
                  }
                </div>
                @if (!slots().length && pickupDate === todayIso) { <span class="muted small">Plus de créneau aujourd'hui : choisissez un autre jour.</span> }
              </div>
            }
            <div class="form-grid">
              <div class="field"><label for="name">Nom</label><input id="name" name="name" [(ngModel)]="name" required autocomplete="name" /></div>
              <div class="field"><label for="phone">Téléphone</label><input id="phone" name="phone" [(ngModel)]="phone" type="tel" autocomplete="tel" /></div>
            </div>
            <div class="field"><label for="email">Email (pour être prévenu quand c'est prêt)</label><input id="email" name="email" [(ngModel)]="email" type="email" autocomplete="email" /></div>
            <div class="field"><label for="note">Remarque</label><textarea id="note" name="note" [(ngModel)]="note" maxlength="300"></textarea></div>
            <p class="muted small">Le paiement se fait au café, à la remise ou en fin de repas.</p>
            <button class="btn btn-primary btn-block" [disabled]="busy || f.invalid">{{ busy ? 'Envoi…' : 'Envoyer ma commande · ' + money(cart.total()) }}</button>
          </form>
          <aside class="card summary">
            <h3>Récapitulatif</h3>
            @for (l of cart.lines(); track l.key) {
              <div class="line">
                <div><b>{{ l.qty }} × {{ l.name }}</b>@if (l.optionsText) { <div class="small muted">{{ l.optionsText }}</div> }</div>
                <div class="price">{{ money(l.qty * l.unitPrice) }}</div>
              </div>
            }
            <div class="row between" style="margin-top:14px"><b>Total</b><b class="big-total">{{ money(cart.total()) }}</b></div>
          </aside>
        </div>
      }
    </div>
  `,
})
export class CheckoutPage implements OnInit {
  private api = inject(Api);
  private router = inject(Router);
  private auth = inject(AuthService);
  cart = inject(CartService);
  settings = inject(SettingsService);
  money = money;

  tables: string[] = [];
  mode: OrderMode = 'TAKEAWAY';
  table = '';
  pickup = '';
  todayIso = isoDate(new Date());
  pickupDate = isoDate(new Date());
  days = this.buildDays();
  name = '';
  phone = '';
  email = '';
  note = '';
  busy = false;
  error = '';

  ngOnInit() {
    this.mode = this.cart.defaultMode();
    if (this.mode === 'DINE_IN' && !this.settings.bool('orders.dineIn.enabled')) this.mode = 'TAKEAWAY';
    this.table = this.cart.table();
    const u = this.auth.user();
    // Champs vides par défaut ; seuls les clients connectés (pas l'équipe) retrouvent leurs coordonnées.
    if (u && u.role === 'CUSTOMER') { this.name = u.name; this.phone = u.phone || ''; this.email = u.email; }
    this.api.get<string[]>('/public/tables').subscribe({ next: (t) => (this.tables = t) });
  }

  private locale(): string {
    const l = currentLang();
    return l === 'pt' ? 'pt-PT' : l === 'de' ? 'de-DE' : l === 'lb' ? 'de-LU' : 'fr-FR';
  }

  private buildDays() {
    const out: { iso: string; label: string }[] = [];
    const now = new Date();
    for (let i = 0; i < 14; i++) {
      const d = new Date(now.getFullYear(), now.getMonth(), now.getDate() + i);
      out.push({ iso: isoDate(d), label: d.toLocaleDateString(this.locale(), { weekday: 'short', day: 'numeric', month: 'short' }) });
    }
    return out;
  }

  pickDay(iso: string) {
    if (!iso) return;
    this.pickupDate = iso;
    if (iso !== this.todayIso && !this.pickup) this.pickup = '12:00';
    if (iso === this.todayIso && this.pickup && !this.slots().includes(this.pickup)) this.pickup = '';
  }

  /** Créneaux de 30 min de 10 h à 21 h 30 ; aujourd'hui, seulement ceux qui respectent le délai minimum. */
  slots(): string[] {
    const out: string[] = [];
    const limit = new Date(Date.now() + this.settings.num('orders.takeaway.minMinutes') * 60000);
    for (let h = 10; h < 22; h++) for (const m of [0, 30]) {
      const t = String(h).padStart(2, '0') + ':' + String(m).padStart(2, '0');
      if (this.pickupDate === this.todayIso) {
        const at = new Date(); at.setHours(h, m, 0, 0);
        if (at < limit) continue;
      }
      out.push(t);
    }
    return out;
  }

  submit() {
    this.error = '';
    if (this.mode === 'DINE_IN' && !this.table) { this.error = 'Choisissez votre table.'; return; }
    if (this.mode === 'TAKEAWAY' && this.pickupDate !== this.todayIso && !this.pickup) { this.error = 'Choisissez une heure de retrait.'; return; }
    this.busy = true;
    const body = {
      mode: this.mode,
      tableLabel: this.mode === 'DINE_IN' ? this.table : null,
      customerName: this.name,
      phone: this.phone || null,
      email: this.email || null,
      pickupAt: this.mode === 'TAKEAWAY' && this.pickup ? this.pickupDate + 'T' + this.pickup + ':00' : null,
      note: this.note || null,
      items: this.cart.lines().map((l) => ({ productId: l.productId, quantity: l.qty, choices: l.choices, note: l.note || null })),
    };
    this.api.post<Order>('/public/orders', body).subscribe({
      next: (o) => {
        try { localStorage.setItem('cafe_last_order', o.code); } catch { /* ignore */ }
        this.cart.clear();
        this.router.navigate(['/suivi', o.code]);
      },
      error: (e) => { this.error = errMsg(e); this.busy = false; },
    });
  }
}
