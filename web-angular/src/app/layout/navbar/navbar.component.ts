import { Component, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <header class="navbar">
      <a class="brand" routerLink="/dashboard">Shift Planner</a>

      <nav class="nav-links">
        <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
        <a routerLink="/users" routerLinkActive="active">Users</a>
        <a routerLink="/teams" routerLinkActive="active">Teams</a>
      </nav>

      <div class="nav-meta">
        <span class="user-email">{{ currentUser()?.email ?? 'Not signed in' }}</span>
        <button type="button" class="logout-button" (click)="logout.emit()">Logout</button>
      </div>
    </header>
  `,
  styleUrl: './navbar.component.scss',
})
export class NavbarComponent {
  readonly currentUser = input<User | null>(null);
  readonly logout = output<void>();
}
