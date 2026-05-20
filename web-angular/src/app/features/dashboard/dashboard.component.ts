import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-dashboard',
  template: `
    <section class="page">
      <div class="page-header">
        <p class="eyebrow">Dashboard</p>
        <h1>
          Welcome back{{
            authService.currentUser()?.firstName ? ', ' + authService.currentUser()?.firstName : ''
          }}.
        </h1>
      </div>

      <div class="card">
        <h2>Current user</h2>
        @if (authService.currentUser(); as user) {
          <dl class="details-grid">
            <div>
              <dt>Name</dt>
              <dd>{{ user.firstName }} {{ user.lastName }}</dd>
            </div>
            <div>
              <dt>Email</dt>
              <dd>{{ user.email }}</dd>
            </div>
            <div>
              <dt>Role</dt>
              <dd>{{ user.systemRole }}</dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>{{ user.active ? 'Active' : 'Inactive' }}</dd>
            </div>
          </dl>
        } @else {
          <p class="empty-state">No user data loaded.</p>
        }
      </div>
    </section>
  `,
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  protected readonly authService = inject(AuthService);
}
