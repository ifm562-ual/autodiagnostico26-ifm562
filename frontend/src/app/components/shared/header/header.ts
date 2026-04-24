import { Component, HostListener, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthStateService } from '../../../services/auth-state.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class HeaderComponent {
  readonly authStateService = inject(AuthStateService);
  private readonly router = inject(Router);

  menuOpen     = false;
  mobileMenuOpen = false;
  searchQuery  = '';

  toggleMenu(): void { this.menuOpen = !this.menuOpen; }
  toggleMobileMenu(): void { this.mobileMenuOpen = !this.mobileMenuOpen; }

  onLogin(): void {
    void this.router.navigate(['/login']);
  }

  onLogout(): void {
    this.authStateService.clearSession();
    this.menuOpen = false;
  }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      // TODO: router.navigate(['/buscar'], { queryParams: { q: this.searchQuery } })
      console.log('Buscar:', this.searchQuery);
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(e: MouseEvent): void {
    if (!(e.target as HTMLElement).closest('.header-actions')) {
      this.menuOpen = false;
    }
  }
}