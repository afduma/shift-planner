import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { catchError, map, of, switchMap } from 'rxjs';
import { UsersApiService } from '../../../core/api/users-api.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-detail',
  template: `
    <section class="page">
      <div class="page-header">
        <p class="eyebrow">User detail</p>
        <h1>User profile</h1>
      </div>

      <div class="card">
        @if (errorMessage()) {
          <p class="empty-state">{{ errorMessage() }}</p>
        } @else if (user(); as currentUser) {
          <h2>{{ currentUser.firstName }} {{ currentUser.lastName }}</h2>
          <p>{{ currentUser.email }}</p>
          <p>Role: {{ currentUser.systemRole }}</p>
          <p>Status: {{ currentUser.active ? 'Active' : 'Inactive' }}</p>
        } @else {
          <p class="empty-state">Loading user...</p>
        }
      </div>
    </section>
  `,
  styleUrl: './user-detail.component.scss',
})
export class UserDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly usersApi = inject(UsersApiService);

  protected readonly user = signal<User | null>(null);
  protected readonly errorMessage = signal('');

  constructor() {
    this.route.paramMap
      .pipe(
        map((params) => params.get('id')),
        switchMap((id) => {
          if (!id) {
            this.errorMessage.set('User id is missing from the route.');
            return of(null);
          }

          return this.usersApi.getUserById(id).pipe(
            catchError(() => {
              this.errorMessage.set('User details are not available.');
              return of(null);
            }),
          );
        }),
      )
      .subscribe((user) => this.user.set(user));
  }
}
