import { Component, HostListener } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class HeaderComponent {

  isLoggedIn   = false;
  userName     = 'Pablo M.';
  userAvatar   = 'https://api.dicebear.com/9.x/initials/svg?seed=PM&backgroundColor=1a6bbd';
  menuOpen     = false;
  mobileMenuOpen = false;
  searchQuery  = '';

  toggleMenu(): void { this.menuOpen = !this.menuOpen; }
  toggleMobileMenu(): void { this.mobileMenuOpen = !this.mobileMenuOpen; }

  onLogin(): void {
    // TODO: router.navigate(['/login'])
    console.log('Navegar a login');
  }

  onLogout(): void {
    this.isLoggedIn = false;
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