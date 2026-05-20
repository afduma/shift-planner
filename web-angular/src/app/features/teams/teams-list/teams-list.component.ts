import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { TeamsApiService } from '../../../core/api/teams-api.service';
import { Team } from '../../../core/models/team.model';

@Component({
  selector: 'app-teams-list',
  imports: [RouterLink],
  templateUrl: './teams-list.component.html',
  styleUrl: './teams-list.component.scss',
})
export class TeamsListComponent {
  private readonly teamsApi = inject(TeamsApiService);

  protected readonly teams = signal<Team[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal('');

  constructor() {
    this.teamsApi
      .getTeams()
      .pipe(
        catchError(() => {
          this.errorMessage.set('Teams endpoint is unavailable right now.');
          return of([]);
        }),
      )
      .subscribe((teams) => {
        this.teams.set(teams);
        this.isLoading.set(false);
      });
  }
}
