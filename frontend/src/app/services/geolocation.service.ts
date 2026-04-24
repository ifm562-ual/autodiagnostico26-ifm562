import { Injectable, signal, WritableSignal } from '@angular/core';

export interface GeoLocationState {
  coords: { lat: number; lng: number } | null;
  error: string | null;
  loading: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class GeolocationService {
  private state: WritableSignal<GeoLocationState> = signal({
    coords: null,
    error: null,
    loading: true
  });

  readonly locationState = this.state.asReadonly();

  constructor() {
    this.initWatch();
  }

  private initWatch() {
    if (!navigator.geolocation) {
      this.state.set({ coords: null, error: 'Geolocalización no soportada', loading: false });
      return;
    }

    navigator.geolocation.watchPosition(
      (pos) => {
        this.state.set({
          coords: { lat: pos.coords.latitude, lng: pos.coords.longitude },
          error: null,
          loading: false
        });
      },
      (err) => {
        let msg = 'Error desconocido';
        switch (err.code) {
          case err.PERMISSION_DENIED: msg = 'Permiso denegado'; break;
          case err.POSITION_UNAVAILABLE: msg = 'Ubicación no disponible'; break;
          case err.TIMEOUT: msg = 'Tiempo de espera agotado'; break;
        }
        this.state.update(s => ({ ...s, error: msg, loading: false }));
      },
      { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
    );
  }
}
