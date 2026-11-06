import { Component } from '@angular/core';
import { SettingField, SettingsForm } from '../shared/settings-form';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [SettingsForm],
  template: `
    <h1>Réglages</h1>
    <app-settings-form title="Le café" [fields]="identity" />
    <app-settings-form title="Couleurs" [fields]="colors" />
    <app-settings-form title="Commandes" [fields]="orders" />
  `,
})
export class SettingsPage {
  identity: SettingField[] = [
    { key: 'cafe.name', label: 'Nom du café', type: 'text' },
    { key: 'cafe.tagline', label: 'Slogan', type: 'text' },
    { key: 'cafe.about', label: 'Texte de présentation (page d\'accueil)', type: 'textarea' },
    { key: 'cafe.address', label: 'Adresse', type: 'text' },
    { key: 'cafe.phone', label: 'Téléphone', type: 'text' },
    { key: 'cafe.email', label: 'Email (reçoit les demandes de salle)', type: 'text' },
    { key: 'cafe.hours', label: 'Horaires d\'ouverture', type: 'text' },
  ];
  colors: SettingField[] = [
    { key: 'brand.primary', label: 'Couleur principale (boutons)', type: 'color' },
    { key: 'brand.dark', label: 'Couleur sombre (en-tête, cuisine)', type: 'color' },
  ];
  orders: SettingField[] = [
    { key: 'orders.enabled', label: 'Commandes ouvertes', type: 'bool' },
    { key: 'orders.dineIn.enabled', label: 'Commande à table (QR code)', type: 'bool' },
    { key: 'orders.takeaway.enabled', label: 'Vente à emporter', type: 'bool' },
    { key: 'orders.takeaway.minMinutes', label: 'Délai minimum de retrait (minutes)', type: 'number' },
    { key: 'orders.message', label: 'Message affiché sur la carte', type: 'text', hint: 'Exemple : cuisine fermée entre 14 h et 18 h.' },
  ];
}
