import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { CartService } from './core/cart.service';
import { SettingsService } from './core/settings.service';
import { ToastService } from './core/toast.service';
import { CartDrawer } from './shared/cart-drawer';
import { I18n, LANGS } from './core/i18n.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CartDrawer],
  template: `
    <a class="sr" href="#contenu">Aller au contenu</a>
    <header class="top">
      <div class="container">
        <a routerLink="/" class="brand">
          <img src="assets/logo.png" alt="" width="44" height="44">
          {{ settings.str('cafe.name', 'Coffee Events') }}
        </a>
        <nav class="nav" id="menu-principal" [class.open]="menuOpen()" (click)="menuOpen.set(false)" aria-label="Navigation principale">
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
        </nav>
        <select class="lang" aria-label="Langue" [value]="i18n.lang" (change)="i18n.set($any($event.target).value)">
          @for (l of langs; track l.code) { <option [value]="l.code" [selected]="l.code === i18n.lang">{{ l.code.toUpperCase() }}</option> }
        </select>
        @if (settings.bool('orders.enabled')) {
          <button class="cart-btn" (click)="cart.open.set(true)">Panier<span class="n">{{ cart.count() }}</span></button>
        }
        <button class="burger" type="button" (click)="menuOpen.set(!menuOpen())" [attr.aria-expanded]="menuOpen()" aria-controls="menu-principal" aria-label="Menu">{{ menuOpen() ? '✕' : '☰' }}</button>
      </div>
    </header>
    <main id="contenu"><router-outlet /></main>
    <footer class="foot">
      <div class="container grid c3">
        <div><img class="foot-logo" src="assets/logo.png" alt="Foodelicious Team"><b style="color:#fff">{{ settings.str('cafe.name') }}</b><br>{{ settings.str('cafe.tagline') }}</div>
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
  i18n = inject(I18n);
  langs = LANGS;
  menuOpen = signal(false);

  ngOnInit() { this.settings.load(); }
}
