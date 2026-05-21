import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, finalize, map, of, switchMap } from 'rxjs';
import { TeamsApiService } from '../../../core/api/teams-api.service';
import { Team, TeamUpsertRequest } from '../../../core/models/team.model';
import { TeamFormComponent } from '../team-form/team-form.component';

@Component({
  selector: 'app-team-edit',
  imports: [RouterLink, TeamFormComponent],
  template: `
    <section class="page">
      <a class="back-link" [routerLink]="teamId() ? ['/teams', teamId()] : ['/teams']">Back</a>

      @if (loadErrorMessage()) {
        <div class="card">
          <p class="empty-state">{{ loadErrorMessage() }}</p>
        </div>
      } @else if (team(); as currentTeam) {
        <app-team-form
          title="Edit team"
          descriptionText="Update team details and active status."
          submitLabel="Save changes"
          [initialValue]="currentTeam"
          [isSubmitting]="isSubmitting()"
          [errorMessage]="saveErrorMessage()"
          (submitted)="updateTeam($event)"
        ></app-team-form>
      } @else {
        <div class="card">
          <p class="empty-state">Loading team...</p>
        </div>
      }
    </section>
  `,
  styleUrl: './team-edit.component.scss',
})
export class TeamEditComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly teamsApi = inject(TeamsApiService);

  protected readonly teamId = signal<string | null>(null);
  protected readonly team = signal<Team | null>(null);
  protected readonly isSubmitting = signal(false);
  protected readonly loadErrorMessage = signal('');
  protected readonly saveErrorMessage = signal('');

  constructor() {
    this.route.paramMap
      .pipe(
        map((params) => params.get('id')),
        switchMap((id) => {
          this.teamId.set(id);

          if (!id) {
            this.loadErrorMessage.set('Team id is missing from the route.');
            return of(null);
          }

          return this.teamsApi.getTeamById(id).pipe(
            catchError(() => {
              this.loadErrorMessage.set('Team details are not available.');
              return of(null);
            }),
          );
        }),
      )
      .subscribe((team) => this.team.set(team));
  }

  protected updateTeam(request: TeamUpsertRequest): void {
    const id = this.teamId();
    if (!id) {
      return;
    }

    this.saveErrorMessage.set('');
    this.isSubmitting.set(true);

    this.teamsApi.updateTeam(id, request).pipe(finalize(() => this.isSubmitting.set(false))).subscribe({
      next: (team) => void this.router.navigate(['/teams', team.id]),
      error: () => this.saveErrorMessage.set('Could not update the team. Check the data and try again.'),
    });
  }
}
