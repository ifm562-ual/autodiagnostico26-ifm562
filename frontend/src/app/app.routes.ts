import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { DiagnosticoComponent } from './components/diagnostico/diagnostico';
import { TallerComponent } from './components/taller/taller';
import { RepuestosComponent } from './components/repuestos/repuestos';
import { SeguimientoComponent } from './components/seguimiento/seguimiento';
import { SeguimientoChatComponent } from './components/seguimiento/chat/chat';
import { PresupuestoComponent } from './components/presupuesto/presupuesto';
import { HistorialComponent } from './components/historial/historial';
import { ContactoComponent } from './components/contacto/contacto';
import { PerfilComponent } from './components/perfil/perfil';
import { MisVehiculosComponent } from './components/mis-vehiculos/mis-vehiculos';
import { LoginComponent } from './components/login/login';
import { seguimientoGuard } from './auth/seguimiento.guard';

export const routes: Routes = [
	{ path: '', pathMatch: 'full', redirectTo: 'home' },
	{ path: 'home', component: HomeComponent },
	{ path: 'diagnostico', component: DiagnosticoComponent },
	{ path: 'taller', component: TallerComponent },
	{ path: 'repuestos', component: RepuestosComponent },
	{
		path: 'seguimiento',
		component: SeguimientoComponent,
		canActivate: [seguimientoGuard],
		children: [
			{ path: '', pathMatch: 'full', redirectTo: 'chat' },
			{ path: 'chat', component: SeguimientoChatComponent }
		]
	},
	{ path: 'presupuesto', component: PresupuestoComponent },
	{ path: 'historial', component: HistorialComponent },
	{ path: 'contacto', component: ContactoComponent },
	{ path: 'perfil', component: PerfilComponent },
	{ path: 'mis-vehiculos', component: MisVehiculosComponent },
	{ path: 'login', component: LoginComponent },
	{ path: 'registro', component: LoginComponent },
	{ path: '**', redirectTo: 'home' }
];
