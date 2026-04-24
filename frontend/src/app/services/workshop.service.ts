import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

export interface Workshop {
  id: number;
  name: string;
  lat: number;
  lng: number;
  address: string;
}

@Injectable({
  providedIn: 'root'
})
export class WorkshopService {
  getNearbyWorkshops(lat: number, lng: number): Observable<Workshop[]> {
    // Simulamos una respuesta de API con talleres cercanos a la posición dada
    const mocks: Workshop[] = [
      { id: 1, name: 'Taller Mecánico Central', lat: lat + 0.005, lng: lng + 0.005, address: 'Calle Principal 123' },
      { id: 2, name: 'Auto Diagnosis Express', lat: lat - 0.003, lng: lng + 0.002, address: 'Av. Libertad 45' },
      { id: 3, name: 'Reparaciones Rápidas', lat: lat + 0.002, lng: lng - 0.004, address: 'Plaza Mayor 8' }
    ];
    return of(mocks);
  }
}
