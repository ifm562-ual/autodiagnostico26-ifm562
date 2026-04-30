import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-buscar-matricula',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './buscar-matricula.html',
  styleUrl: './buscar-matricula.css',
})
export class BuscarMatricula implements OnInit {
  vehiculoForm!: FormGroup;

  constructor(private fb: FormBuilder) { }

  ngOnInit(): void {
    this.vehiculoForm = this.fb.group({
      matricula: [''],
      marca: [''],
      modelo: [''],
    });
  }
}
