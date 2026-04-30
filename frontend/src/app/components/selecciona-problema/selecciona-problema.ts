import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-selecciona-problema',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './selecciona-problema.html',
  styleUrl: './selecciona-problema.css',
})
export class SeleccionaProblema {
  //Tentativos, aun no sabemos cuales se pondran
  problemas: string[] = [
    'Motor no arranca',
    'Ruido extraño en el motor',
    'Frenos defectuosos',
    'Luces no funcionan',
    'Transmisión con problemas',
    'Otro',
  ];

  dropdownAbierto = false;
  problemaSeleccionado: string | null = null;

  toggleDropdown(): void {
    this.dropdownAbierto = !this.dropdownAbierto;
  }

  seleccionarProblema(problema: string): void {
    this.problemaSeleccionado = problema;
    this.dropdownAbierto = false;
  }
}
