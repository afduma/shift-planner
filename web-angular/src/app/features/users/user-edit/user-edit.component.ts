import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, finalize, map, of, switchMap } from 'rxjs';
import { UsersApiService } from '../../../core/api/users-api.service';
import { User, UpdateUserRequest } from '../../../core/models/user.model';
import { UserFormComponent } from '../user-form/user-form.component';

@Component({
  selector: 'app-user-edit',
  imports: [RouterLink, UserFormComponent],
  template: `
    <section class="page">
      <a class="back-link" [routerLink]="userId() ? ['/users', userId()] : ['/users']">Back</a>

      @if (loadErrorMessage()) {
        <div class="card">
          <p class="empty-state">{{ loadErrorMessage() }}</p>
        </div>
      } @else if (initialValue(); as value) {
        <app-user-form
          title="Edit user"
          description="Update user profile details"
          submitLabel="Save changes"
          [initialValue]="value"
          [emailReadonly]="true"
          [isSubmitting]="isSubmitting()"
          [errorMessage]="saveErrorMessage()"
          (submitted)="updateUser($event)"
        ></app-user-form>
      } @else {
        <div class="card">
          <p class="empty-state">Loading user...</p>
        </div>
      }
    </section>
  `,
  styleUrl: './user-edit.component.scss',
})
export class UserEditComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly usersApi = inject(UsersApiService);

  protected readonly userId = signal<string | null>(null);
  protected readonly initialValue = signal<User | null>(null);
  protected readonly isSubmitting = signal(false);
  protected readonly loadErrorMessage = signal('');
  protected readonly saveErrorMessage = signal('');

  constructor() {
    this.route.paramMap.pipe(
      map((params) => params.get('id')),
      switchMap((id) => {
        this.userId.set(id);

        if (!id) {
          this.loadErrorMessage.set('User id is missing from the route.');
          return of(null);
        }

        return this.usersApi.getUserById(id).pipe(
          catchError(() => {
            this.loadErrorMessage.set('User details are not available.');
            return of(null);
          }),
        );
      }),
    ).subscribe((user) => {
      if (!user) {
        return;
      }

      this.initialValue.set(user);
    });
  }

  protected updateUser(request: UpdateUserRequest): void {
    const id = this.userId();
    if (!id) {
      return;
    }

    this.saveErrorMessage.set('');
    this.isSubmitting.set(true);

    this.usersApi.updateUser(id, request).pipe(
      finalize(() => this.isSubmitting.set(false)),
    ).subscribe({
      next: (user) => void this.router.navigate(['/users', user.id]),
      error: () => this.saveErrorMessage.set('Could not update the user. Check the data and try again.'),
    });
  }
}
