import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { UsersApiService } from '../../../core/api/users-api.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-users-list',
  imports: [RouterLink],
  templateUrl: './users-list.component.html',
  styleUrl: './users-list.component.scss',
})
export class UsersListComponent {
  private readonly usersApi = inject(UsersApiService);

  protected readonly users = signal<User[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal('');

  constructor() {
    this.usersApi
      .getUsers()
      .pipe(
        catchError(() => {
          this.errorMessage.set('Users endpoint is unavailable right now.');
          return of([]);
        }),
      )
      .subscribe((users) => {
        this.users.set(users);
        this.isLoading.set(false);
      });
  }
}
