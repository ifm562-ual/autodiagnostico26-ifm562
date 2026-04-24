import { isPlatformBrowser } from '@angular/common';
import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ChatApiService } from '../../services/chat-api.service';
import { ChatRoomType } from '../../services/api.models';
import { AuthStateService } from '../../services/auth-state.service';

@Component({
  selector: 'app-seguimiento-page',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './seguimiento.html',
  styleUrl: './seguimiento.css'
})
export class SeguimientoComponent implements OnInit {
  private readonly roomType: ChatRoomType = 'SEGUIMIENTO';
  private readonly participantId = 1;

  userOnline = false;
  unreadCount = 0;

  constructor(
    private readonly chatApiService: ChatApiService,
    private readonly authStateService: AuthStateService,
    @Inject(PLATFORM_ID) private readonly platformId: object
  ) {}

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId) || !this.authStateService.canAccessSeguimiento()) {
      return;
    }

    this.chatApiService.isUserOnline(this.roomType, this.participantId).subscribe({
      next: (isOnline) => {
        this.userOnline = isOnline;
      }
    });

    this.chatApiService.unreadCount(this.roomType).subscribe({
      next: (count) => {
        this.unreadCount = count;
      }
    });
  }
}
