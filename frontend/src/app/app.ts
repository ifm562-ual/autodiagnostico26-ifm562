import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/shared/header/header'; 
import { FooterComponent } from './components/shared/footer/footer';
import { BuscarMatricula } from './buscar-matricula/buscar-matricula';
import { SeleccionaProblema } from './selecciona-problema/selecciona-problema';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, BuscarMatricula, SeleccionaProblema],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('autodiagnostico');
}
