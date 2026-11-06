export type Role = 'CUSTOMER' | 'KITCHEN' | 'ADMIN';
export type OrderMode = 'DINE_IN' | 'TAKEAWAY';
export type OrderStatus = 'NEW' | 'PREPARING' | 'READY' | 'SERVED' | 'CANCELLED';
export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'REFUSED' | 'CANCELLED' | 'COMPLETED';

export interface User { id: number; name: string; email: string; phone?: string; role: Role; active: boolean; }
export interface Category { id: number; name: string; description?: string; icon?: string; position: number; active: boolean; }
export interface OptionChoice { name: string; price: number; }
export interface OptionGroup { name: string; required: boolean; multi: boolean; choices: OptionChoice[]; }
export interface Product {
  id: number; name: string; description?: string; price: number; imageUrl?: string; category?: Category;
  available: boolean; active: boolean; position: number; prepMinutes?: number; tags?: string; optionsJson?: string;
}
export interface Menu { categories: Category[]; products: Product[]; }
export interface CartLine {
  key: string; productId: number; name: string; unitPrice: number; qty: number;
  choices: string[]; optionsText: string; note?: string;
}
export interface OrderItem { id: number; productId: number; name: string; unitPrice: number; quantity: number; optionsText?: string; note?: string; }
export interface Order {
  id: number; code: string; mode: OrderMode; status: OrderStatus; tableLabel?: string; customerName: string;
  phone?: string; email?: string; pickupAt?: string; note?: string; total: number; paid: boolean;
  createdAt: string; updatedAt: string; items: OrderItem[];
}
export interface RoomPackage { id: number; name: string; description?: string; pricePerPerson: number; minGuests: number; position: number; active: boolean; }
export interface RoomExtra { id: number; name: string; description?: string; price: number; perPerson: boolean; active: boolean; }
export interface RoomInfo { packages: RoomPackage[]; extras: RoomExtra[]; }
export interface DayInfo { date: string; closed: boolean; reason?: string; booked: string[]; }
export interface Reservation {
  id: number; code: string; customerName: string; email: string; phone: string; eventType?: string; date: string;
  startTime: string; endTime: string; guests: number; packageName?: string; extrasText?: string; note?: string;
  total: number; deposit: number; status: ReservationStatus; adminNote?: string; createdAt: string;
}
