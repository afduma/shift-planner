import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { TeamsApiService } from '../../../core/api/teams-api.service';
import { TeamUpsertRequest } from '../../../core/models/team.model';
import { TeamFormComponent } from '../team-form/team-form.component';

@Component({
  selector: 'app-team-create',
  imports: [TeamFormComponent],
  template: `
    <app-team-form
      title="Create team"
      descriptionText="Create a team before assigning memberships and planning work."
      submitLabel="Create team"
      [isSubmitting]="isSubmitting()"
      [errorMessage]="errorMessage()"
      (submitted)="createTeam($event)"
    ></app-team-form>
  `,
})
export class TeamCreateComponent {
  private readonly teamsApi = inject(TeamsApiService);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal('');

  protected createTeam(request: TeamUpsertRequest): void {
    this.errorMessage.set('');
    this.isSubmitting.set(true);

    this.teamsApi.createTeam(request).pipe(finalize(() => this.isSubmitting.set(false))).subscribe({
      next: (team) => void this.router.navigate(['/teams', team.id]),
      error: () => this.errorMessage.set('Could not create the team. Check the data and try again.'),
    });
  }
}
