import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, finalize, map, of, switchMap } from 'rxjs';
import { UsersApiService } from '../../../core/api/users-api.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-detail',
  imports: [RouterLink],
  template: `
    <section class="page">
      <div class="page-header">
        <div>
          <p class="eyebrow">User detail</p>
          <h1>User profile</h1>
        </div>
        @if (user(); as currentUser) {
          <div class="page-actions">
            <a class="primary-action" [routerLink]="['/users', currentUser.id, 'edit']">Edit user</a>
            <button type="button" class="danger-action" [disabled]="isDeleting()" (click)="deleteUser(currentUser)">
              {{ isDeleting() ? 'Deleting...' : 'Delete user' }}
            </button>
          </div>
        }
      </div>

      <div class="card">
        @if (errorMessage()) {
          <p class="empty-state">{{ errorMessage() }}</p>
        } @else if (user(); as currentUser) {
          <h2>{{ currentUser.firstName }} {{ currentUser.lastName }}</h2>
          <p>{{ currentUser.email }}</p>
          <p>Role: {{ currentUser.systemRole }}</p>
          <p>Status: {{ currentUser.active ? 'Active' : 'Inactive' }}</p>
          @if (deleteErrorMessage()) {
            <p class="delete-error">{{ deleteErrorMessage() }}</p>
          }
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
  private readonly router = inject(Router);
  private readonly usersApi = inject(UsersApiService);

  protected readonly user = signal<User | null>(null);
  protected readonly errorMessage = signal('');
  protected readonly deleteErrorMessage = signal('');
  protected readonly isDeleting = signal(false);

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

  protected deleteUser(user: User): void {
    const confirmed = window.confirm(`Delete ${user.firstName} ${user.lastName}?`);
    if (!confirmed) {
      return;
    }

    this.deleteErrorMessage.set('');
    this.isDeleting.set(true);

    this.usersApi.deleteUser(user.id).pipe(
      finalize(() => this.isDeleting.set(false)),
    ).subscribe({
      next: () => void this.router.navigate(['/users']),
      error: () => this.deleteErrorMessage.set('Could not delete the user. Try again.'),
    });
  }
}
