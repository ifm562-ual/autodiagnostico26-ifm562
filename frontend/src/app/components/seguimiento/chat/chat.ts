import { isPlatformBrowser } from '@angular/common';
import { ChangeDetectorRef, Component, Inject, NgZone, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChatApiService } from '../../../services/chat-api.service';
import { ChatMessageRequest, ChatMessageResponse, ChatRoomType } from '../../../services/api.models';
import { AuthStateService } from '../../../services/auth-state.service';

type ChatAuthor = 'mecanico' | 'usuario';

interface ChatMessage {
  id: number;
  author: ChatAuthor;
  text: string;
  at: string;
  read: boolean;
}

@Component({
  selector: 'app-seguimiento-chat',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './chat.html',
  styleUrl: './chat.css'
})
export class SeguimientoChatComponent implements OnInit, OnDestroy {
  private readonly roomType: ChatRoomType = 'SEGUIMIENTO';
  private readonly participantId = 1;
  private messageRefreshTimerId: number | null = null;
  private latestMessageId: number | null = null;

  userOnline = false;
  draft = '';
  sending = false;

  messages: ChatMessage[] = [];

  constructor(
    private readonly chatApiService: ChatApiService,
    private readonly authStateService: AuthStateService,
    private readonly cdr: ChangeDetectorRef,
    private readonly ngZone: NgZone,
    @Inject(PLATFORM_ID) private readonly platformId: object
  ) {}

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId) || !this.authStateService.canAccessSeguimiento()) {
      return;
    }

    this.refreshPresence();
    this.chatApiService.joinRoom(this.roomType, this.participantId).subscribe({
      next: () => {
        this.userOnline = true;
        this.fetchMessages();
        this.startMessageRefresh();
        this.chatApiService.markReadByUser(this.roomType).subscribe();
      },
      error: () => {
        this.userOnline = false;
      }
    });
  }

  ngOnDestroy(): void {
    if (!isPlatformBrowser(this.platformId) || !this.authStateService.canAccessSeguimiento()) {
      return;
    }

    this.stopMessageRefresh();
    this.chatApiService.leaveRoom(this.roomType, this.participantId).subscribe();
  }

  get unreadCount(): number {
    return this.messages.filter((msg) => msg.author === 'mecanico' && !msg.read).length;
  }

  sendMessage(): void {
    const value = this.draft.trim();
    if (!value) {
      return;
    }

    this.sending = true;

    const payload: ChatMessageRequest = {
      participantId: this.participantId,
      roomType: this.roomType,
      senderRole: 'USUARIO',
      commentText: value
    };

    this.chatApiService.sendMessage(payload).subscribe({
      next: (sentMessage) => {
        this.sending = false;
        this.upsertMessages([sentMessage]);
      },
      error: () => {
        this.sending = false;
      }
    });

    this.draft = '';
  }

  private fetchMessages(): void {
    this.chatApiService.listMessages(this.roomType, 60).subscribe({
      next: (messages) => {
        this.messages = messages.map((message) => this.toViewMessage(message));
        this.latestMessageId = messages.length > 0 ? messages[messages.length - 1].id : null;
      }
    });
  }

  private fetchNewMessages(): void {
    if (!this.latestMessageId) {
      return;
    }

    this.chatApiService.listMessages(this.roomType, 60, this.latestMessageId).subscribe({
      next: (messages) => {
        this.upsertMessages(messages);
      }
    });
  }

  private refreshPresence(): void {
    this.chatApiService.isUserOnline(this.roomType, this.participantId).subscribe({
      next: (isOnline) => {
        this.userOnline = isOnline;
      }
    });
  }

  private startMessageRefresh(): void {
    this.stopMessageRefresh();
    this.ngZone.runOutsideAngular(() => {
      this.messageRefreshTimerId = window.setInterval(() => {
        this.ngZone.run(() => {
          this.fetchNewMessages();
          this.refreshPresence();
          this.cdr.detectChanges();
        });
      }, 2500);
    });
  }

  private stopMessageRefresh(): void {
    if (this.messageRefreshTimerId !== null) {
      window.clearInterval(this.messageRefreshTimerId);
      this.messageRefreshTimerId = null;
    }
  }

  private upsertMessages(incoming: ChatMessageResponse[]): void {
    if (!incoming.length) {
      return;
    }

    const lastId = this.latestMessageId ?? 0;
    const newMessages = incoming.filter((message) => message.id > lastId);
    if (!newMessages.length) {
      return;
    }

    this.messages = [
      ...this.messages,
      ...newMessages.map((message) => this.toViewMessage(message))
    ].slice(-120);

    this.latestMessageId = this.messages[this.messages.length - 1].id;
  }

  private toViewMessage(message: ChatMessageResponse): ChatMessage {
    const parsedDate = new Date(message.createdAt);
    const hasValidDate = !Number.isNaN(parsedDate.getTime());

    return {
      id: message.id,
      author: message.senderRole === 'MECANICO' ? 'mecanico' : 'usuario',
      text: message.commentText,
      at: hasValidDate ? parsedDate.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }) : '--:--',
      read: message.readByUser
    };
  }
}
