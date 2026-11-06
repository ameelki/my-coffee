import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { CartService } from './core/cart.service';
import { SettingsService } from './core/settings.service';
import { ToastService } from './core/toast.service';
import { CartDrawer } from './shared/cart-drawer';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CartDrawer],
  template: `
    <a class="sr" href="#contenu">Aller au contenu</a>
    <header class="top">
      <div class="container">
        <a routerLink="/" class="brand">
          <svg viewBox="0 0 32 32" aria-hidden="true"><path d="M5 12h18v6a8 8 0 0 1-8 8h-2a8 8 0 0 1-8-8z" fill="#c2531b"/><path d="M23 14h2.5a3 3 0 0 1 0 6H22" fill="none" stroke="#b7cf7a" stroke-width="2"/><path d="M11 3c-1.5 2 1.5 3 0 5M16 3c-1.5 2 1.5 3 0 5" fill="none" stroke="#e9f1d6" stroke-width="1.6" stroke-linecap="round"/></svg>
          {{ settings.str('cafe.name', 'Coffee Events') }}
        </a>
        <nav class="nav" aria-label="Navigation principale">
          <a routerLink="/carte" routerLinkActive="active">La carte</a>
          @if (settings.bool('room.enabled')) { <a routerLink="/salle" routerLinkActive="active">Salle des fêtes</a> }
          <a routerLink="/suivi" routerLinkActive="active">Suivre ma commande</a>
          @if (auth.user(); as u) {
            @if (u.role === 'ADMIN') { <a routerLink="/admin">Admin</a> }
            @if (u.role !== 'CUSTOMER') { <a routerLink="/cuisine">Cuisine</a> }
            <a routerLink="/compte" routerLinkActive="active">{{ u.name }}</a>
            <a href="#" (click)="$event.preventDefault(); auth.logout()">Déconnexion</a>
          } @else {
            <a routerLink="/connexion" routerLinkActive="active">Connexion</a>
          }
          @if (settings.bool('orders.enabled')) {
            <button class="cart-btn" (click)="cart.open.set(true)">Panier<span class="n">{{ cart.count() }}</span></button>
          }
        </nav>
      </div>
    </header>
    <main id="contenu"><router-outlet /></main>
    <footer class="foot">
      <div class="container grid c3">
        <div><b style="color:#fff">{{ settings.str('cafe.name') }}</b><br>{{ settings.str('cafe.tagline') }}</div>
        <div>{{ settings.str('cafe.address') }}<br>{{ settings.str('cafe.hours') }}</div>
        <div>{{ settings.str('cafe.phone') }}<br><a href="mailto:{{ settings.str('cafe.email') }}">{{ settings.str('cafe.email') }}</a></div>
      </div>
    </footer>
    <app-cart-drawer />
    @if (toast.msg()) { <div class="toast" role="status">{{ toast.msg() }}</div> }
  `,
})
export class AppComponent implements OnInit {
  settings = inject(SettingsService);
  auth = inject(AuthService);
  cart = inject(CartService);
  toast = inject(ToastService);

  ngOnInit() { this.settings.load(); }
}
