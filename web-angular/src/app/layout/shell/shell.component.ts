import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-shell',
  imports: [NavbarComponent, RouterOutlet],
  template: `
    <div class="shell">
      <app-navbar
        [currentUser]="authService.currentUser()"
        (logout)="authService.logout()"
      ></app-navbar>

      <main class="shell-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  protected readonly authService = inject(AuthService);
}
