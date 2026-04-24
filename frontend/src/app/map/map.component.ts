import { Component, OnDestroy, effect, inject, ElementRef, ViewChild, output, PLATFORM_ID, afterNextRender, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { GeolocationService } from '../services/geolocation.service';
import { WorkshopService } from '../services/workshop.service';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './map.component.html',
  styleUrl: './map.component.css'
})
export class MapComponent implements OnDestroy {
  public geoService = inject(GeolocationService);
  private workshopService = inject(WorkshopService);
  private platformId = inject(PLATFORM_ID);
  
  @ViewChild('mapContainer', { static: false }) mapContainer!: ElementRef;
  
  onAppointmentRequested = output<void>();
  
  private map: any;
  private L: any;
  private userMarker?: any;
  private workshopMarkers: any[] = [];
  private mapReady = signal(false);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      afterNextRender(async () => {
        const leafletModule = await import('leaflet');
        this.L = leafletModule.default || leafletModule;
        
        // Fix para iconos de Leaflet en Angular/Vite
        this.fixLeafletIcons();
        
        this.initMap();
      });

      effect(() => {
        const state = this.geoService.locationState();
        if (this.mapReady() && state.coords) {
          this.updateUserPosition(state.coords.lat, state.coords.lng);
        }
      });
    }
  }

  private fixLeafletIcons() {
    if (!this.L) return;
    const iconRetinaUrl = 'assets/leaflet/marker-icon-2x.png';
    const iconUrl = 'assets/leaflet/marker-icon.png';
    const shadowUrl = 'assets/leaflet/marker-shadow.png';
    
    // Si no tienes los assets locales, usamos un CDN para asegurar visibilidad inmediata
    const cdnBase = 'https://unpkg.com/leaflet@1.9.4/dist/images/';
    
    this.L.Icon.Default.mergeOptions({
      iconRetinaUrl: cdnBase + 'marker-icon-2x.png',
      iconUrl: cdnBase + 'marker-icon.png',
      shadowUrl: cdnBase + 'marker-shadow.png',
    });
  }

  private initMap() {
    if (!this.L || !this.mapContainer) return;

    // Inicializamos con un delay mínimo para asegurar que el DOM está listo
    setTimeout(() => {
      this.map = this.L.map(this.mapContainer.nativeElement).setView([0, 0], 2);
      
      this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.map);
      
      this.map.invalidateSize();
      this.mapReady.set(true);
    }, 100);
  }

  private updateUserPosition(lat: number, lng: number) {
    if (!this.L || !this.map) return;

    if (!this.userMarker) {
      this.map.setView([lat, lng], 15);
      this.userMarker = this.L.marker([lat, lng], {
        icon: this.L.divIcon({ 
          className: 'user-location-marker-container', 
          html: '<div class="user-location-marker"><div class="pulse"></div></div>',
          iconSize: [20, 20],
          iconAnchor: [10, 10]
        })
      }).addTo(this.map).bindPopup('Tu ubicación');
      
      this.loadWorkshops(lat, lng);
    } else {
      this.userMarker.setLatLng([lat, lng]);
    }
  }

  private loadWorkshops(lat: number, lng: number) {
    this.workshopService.getNearbyWorkshops(lat, lng).subscribe(workshops => {
      this.clearWorkshopMarkers();
      workshops.forEach(w => {
        const marker = this.L.marker([w.lat, w.lng])
          .addTo(this.map)
          .bindPopup(`<b>${w.name}</b><br>${w.address}`);
        this.workshopMarkers.push(marker);
      });
    });
  }

  private clearWorkshopMarkers() {
    if (!this.map) return;
    this.workshopMarkers.forEach(m => this.map.removeLayer(m));
    this.workshopMarkers = [];
  }

  handleRequestAppointment() {
    console.log('Log: Solicitud de cita iniciada.');
    this.onAppointmentRequested.emit();
  }

  ngOnDestroy() {
    if (this.map) this.map.remove();
  }
}
