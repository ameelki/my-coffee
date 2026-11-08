import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SettingsService } from '../core/settings.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  template: `
    @if (settings.bool('banner.enabled') && items().length) {
      <div class="promo" role="region" [attr.aria-label]="settings.str('banner.label', 'Événements')">
        <span class="promo-label">{{ settings.str('banner.label', 'Événements') }}</span>
        <div class="promo-track" aria-live="off">
          <div class="promo-run">
            @for (round of [0, 1]; track round) {
              @for (it of items(); track $index) {<span class="promo-item" [attr.aria-hidden]="round === 1 ? 'true' : null">{{ it }}</span>}
            }
          </div>
        </div>
        @if (settings.str('banner.link')) {
          @if (settings.str('banner.link').startsWith('/')) {
            <a class="promo-cta" [routerLink]="settings.str('banner.link')">{{ settings.str('banner.linkLabel', 'Voir') }}</a>
          } @else {
            <a class="promo-cta" [href]="settings.str('banner.link')" target="_blank" rel="noopener">{{ settings.str('banner.linkLabel', 'Voir') }}</a>
          }
        }
      </div>
    }
    <section class="hero">
      <div class="container">
        <div>
          <h1>{{ settings.str('cafe.name', 'Coffee Events') }}</h1>
          <p class="lead">{{ settings.str('cafe.about') }}</p>
          <div class="cta">
            @if (settings.bool('orders.enabled')) { <a routerLink="/carte" class="btn btn-primary">Commander</a> }
            @if (settings.bool('room.enabled')) { <a routerLink="/salle" class="btn btn-ghost">Réserver la salle</a> }
          </div>
          <div class="hours"><span>{{ settings.str('cafe.hours') }}</span><span>{{ settings.str('cafe.address') }}</span></div>
        </div>
        <div class="cup-wrap">
          <img class="hero-logo" src="assets/logo.png" alt="Foodelicious Team">
        </div>
      </div>
    </section>
    <div class="info-band"><div class="container">
      @if (settings.bool('orders.dineIn.enabled')) { <span>Commande à table par QR code</span> }
      @if (settings.bool('orders.takeaway.enabled')) { <span>Retrait en {{ settings.num('orders.takeaway.minMinutes') }} min</span> }
      @if (settings.bool('room.enabled')) { <span>Salle jusqu'à {{ settings.num('room.maxGuests') }} personnes</span> }
    </div></div>
    @if (settings.bool('orders.enabled')) {
      <section class="section how">
        <div class="container">
          <h2>Commander en trois gestes</h2>
          <ol class="how-list">
            <li><b>Scannez le code de votre table</b><span>ou ouvrez la carte depuis votre téléphone pour emporter.</span></li>
            <li><b>Composez votre commande</b><span>Taille, lait, cuisson : tout se choisit en quelques touches.</span></li>
            <li><b>Suivez-la en direct</b><span>La cuisine la prépare, vous voyez chaque étape sur votre écran.</span></li>
          </ol>
        </div>
      </section>
    }
    <section class="section">
      <div class="container grid c2">
        @if (settings.bool('orders.enabled')) {
          <a routerLink="/carte" class="card" style="text-decoration:none;color:inherit">
            <h2>Au café</h2>
            <p class="muted">Boissons, viennoiseries, plats du jour et desserts maison. Commandez depuis votre table ou emportez, la cuisine reçoit votre commande tout de suite.</p>
            <span class="btn btn-dark btn-sm">Voir la carte</span>
          </a>
        }
        @if (settings.bool('room.enabled')) {
          <a routerLink="/salle" class="card" style="text-decoration:none;color:inherit">
            <h2>{{ settings.str('room.name', 'Salle des fêtes') }}</h2>
            <p class="muted">{{ settings.str('room.description') }}</p>
            <span class="btn btn-dark btn-sm">Vérifier les disponibilités</span>
          </a>
        }
      </div>
    </section>
  `,
})
export class HomePage {
  settings = inject(SettingsService);
  items = computed(() => (this.settings.str('banner.items') || '').split('\n').map((x) => x.trim()).filter(Boolean));
}
