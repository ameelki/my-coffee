import { Component } from '@angular/core';
import { Column, CrudList, Field } from '../shared/crud-list';
import { SettingField, SettingsForm } from '../shared/settings-form';

@Component({
  selector: 'app-room-admin',
  standalone: true,
  imports: [CrudList, SettingsForm],
  template: `
    <h1>Salle des fêtes</h1>
    <app-settings-form title="Règles de réservation" [fields]="rules" />
    <app-crud-list title="Formules" endpoint="/admin/packages" [fields]="pkgFields" [columns]="pkgCols" [defaults]="{ active: true, minGuests: 10, position: 0, pricePerPerson: 0 }" />
    <div style="height:32px"></div>
    <app-crud-list title="Options" endpoint="/admin/extras" [fields]="extraFields" [columns]="extraCols" [defaults]="{ active: true, perPerson: false, price: 0 }" />
    <div style="height:32px"></div>
    <app-crud-list title="Dates fermées" endpoint="/admin/closures" [fields]="closeFields" [columns]="closeCols" [defaults]="{}" />
  `,
})
export class RoomAdminPage {
  rules: SettingField[] = [
    { key: 'room.enabled', label: 'Réservations ouvertes', type: 'bool' },
    { key: 'room.autoConfirm', label: 'Confirmer automatiquement les demandes', type: 'bool' },
    { key: 'room.meals.enabled', label: 'Permettre de réserver des repas avec la salle', type: 'bool' },
    { key: 'room.name', label: 'Nom de la salle', type: 'text' },
    { key: 'room.description', label: 'Description', type: 'textarea' },
    { key: 'room.hourlyRate', label: 'Tarif horaire de la salle (€)', type: 'number' },
    { key: 'room.depositPercent', label: 'Acompte (%)', type: 'number' },
    { key: 'room.maxGuests', label: 'Capacité maximale', type: 'number' },
    { key: 'room.minHours', label: 'Durée minimum (heures)', type: 'number' },
    { key: 'room.maxHours', label: 'Durée maximum (heures)', type: 'number' },
    { key: 'room.openFrom', label: 'Ouverture de la salle', type: 'time' },
    { key: 'room.openTo', label: 'Fermeture de la salle', type: 'time' },
    { key: 'room.minNoticeDays', label: 'Délai minimum de réservation (jours)', type: 'number' },
    { key: 'room.bufferMinutes', label: 'Temps de battement entre deux événements (min)', type: 'number' },
    { key: 'room.closedWeekdays', label: 'Jours de fermeture fixes', type: 'text', hint: 'Numéros séparés par des virgules : 1 = lundi … 7 = dimanche. Exemple : 1,2' },
    { key: 'room.eventTypes', label: 'Types d\'événements proposés', type: 'text', hint: 'Séparés par des virgules.' },
    { key: 'room.conditions', label: 'Conditions affichées au client', type: 'textarea' },
  ];
  pkgFields: Field[] = [
    { key: 'name', label: 'Nom', type: 'text' },
    { key: 'description', label: 'Description', type: 'textarea' },
    { key: 'pricePerPerson', label: 'Prix par personne (€)', type: 'number' },
    { key: 'minGuests', label: 'Nombre minimum d\'invités', type: 'number' },
    { key: 'position', label: 'Position', type: 'number' },
    { key: 'active', label: 'Proposée aux clients', type: 'bool' },
  ];
  pkgCols: Column[] = [
    { key: 'name', label: 'Formule' }, { key: 'pricePerPerson', label: 'Par personne', type: 'money' },
    { key: 'minGuests', label: 'Minimum' }, { key: 'active', label: 'Active', type: 'bool' },
  ];
  extraFields: Field[] = [
    { key: 'name', label: 'Nom', type: 'text' },
    { key: 'description', label: 'Description', type: 'text' },
    { key: 'price', label: 'Prix (€)', type: 'number' },
    { key: 'perPerson', label: 'Prix multiplié par le nombre d\'invités', type: 'bool' },
    { key: 'active', label: 'Proposée aux clients', type: 'bool' },
  ];
  extraCols: Column[] = [
    { key: 'name', label: 'Option' }, { key: 'price', label: 'Prix', type: 'money' },
    { key: 'perPerson', label: 'Par personne', type: 'bool' }, { key: 'active', label: 'Active', type: 'bool' },
  ];
  closeFields: Field[] = [
    { key: 'date', label: 'Date', type: 'date' },
    { key: 'reason', label: 'Motif (affiché au client)', type: 'text' },
  ];
  closeCols: Column[] = [{ key: 'date', label: 'Date' }, { key: 'reason', label: 'Motif' }];
}
