import { Routes } from '@angular/router';
import { adminOnly, kitchenOnly, loggedIn } from './core/guards';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./pages/home').then((m) => m.HomePage) },
  { path: 'carte', loadComponent: () => import('./pages/menu').then((m) => m.MenuPage) },
  { path: 'commande', loadComponent: () => import('./pages/checkout').then((m) => m.CheckoutPage) },
  { path: 'suivi', loadComponent: () => import('./pages/track-order').then((m) => m.TrackOrderPage) },
  { path: 'suivi/:code', loadComponent: () => import('./pages/track-order').then((m) => m.TrackOrderPage) },
  { path: 'salle', loadComponent: () => import('./pages/room').then((m) => m.RoomPage) },
  { path: 'reservation', loadComponent: () => import('./pages/track-reservation').then((m) => m.TrackReservationPage) },
  { path: 'reservation/:code', loadComponent: () => import('./pages/track-reservation').then((m) => m.TrackReservationPage) },
  { path: 'connexion', loadComponent: () => import('./pages/login').then((m) => m.LoginPage) },
  { path: 'inscription', loadComponent: () => import('./pages/register').then((m) => m.RegisterPage) },
  { path: 'compte', canActivate: [loggedIn], loadComponent: () => import('./pages/account').then((m) => m.AccountPage) },
  { path: 'cuisine', canActivate: [kitchenOnly], loadComponent: () => import('./pages/kitchen').then((m) => m.KitchenPage) },
  {
    path: 'admin', canActivate: [adminOnly], loadComponent: () => import('./admin/admin-shell').then((m) => m.AdminShell),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'tableau' },
      { path: 'tableau', loadComponent: () => import('./admin/dashboard').then((m) => m.DashboardPage) },
      { path: 'commandes', loadComponent: () => import('./admin/orders').then((m) => m.AdminOrdersPage) },
      { path: 'reservations', loadComponent: () => import('./admin/reservations').then((m) => m.AdminReservationsPage) },
      { path: 'carte', loadComponent: () => import('./admin/menu-admin').then((m) => m.MenuAdminPage) },
      { path: 'salle', loadComponent: () => import('./admin/room-admin').then((m) => m.RoomAdminPage) },
      { path: 'tables', loadComponent: () => import('./admin/tables-admin').then((m) => m.TablesAdminPage) },
      { path: 'equipe', loadComponent: () => import('./admin/team').then((m) => m.TeamPage) },
      { path: 'reglages', loadComponent: () => import('./admin/settings').then((m) => m.SettingsPage) },
    ],
  },
  { path: '**', redirectTo: '' },
];
