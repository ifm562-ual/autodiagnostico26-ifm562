import { Component } from '@angular/core';
import { BuscarMatricula } from '../buscar-matricula/buscar-matricula';
import { SeleccionaProblema } from '../selecciona-problema/selecciona-problema';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [BuscarMatricula, SeleccionaProblema],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomeComponent {}
