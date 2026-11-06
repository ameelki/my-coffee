import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SettingsService } from '../core/settings.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  template: `
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
        <div class="cup-wrap" aria-hidden="true">
          <svg class="cup" viewBox="0 0 320 300">
            <g class="steam">
              <path d="M120 90c-22-24 22-38 0-66"/>
              <path d="M160 96c-22-24 22-38 0-66"/>
              <path d="M200 90c-22-24 22-38 0-66"/>
            </g>
            <ellipse cx="160" cy="262" rx="130" ry="22" fill="#0b1712"/>
            <ellipse cx="160" cy="246" rx="112" ry="16" fill="#e9f1d6"/>
            <path d="M62 112h196v54c0 52-40 84-98 84s-98-32-98-84z" fill="#c2531b"/>
            <path d="M62 112h196v22H62z" fill="#8f3a10"/>
            <ellipse cx="160" cy="112" rx="98" ry="14" fill="#3a2418"/>
            <ellipse cx="160" cy="111" rx="84" ry="9" fill="#5b3a28"/>
            <path d="M258 128h20a30 30 0 0 1 0 60h-26" fill="none" stroke="#c2531b" stroke-width="14" stroke-linecap="round"/>
            <path d="M92 150c0 40 22 66 54 74" fill="none" stroke="rgba(255,255,255,.22)" stroke-width="8" stroke-linecap="round"/>
          </svg>
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
}
