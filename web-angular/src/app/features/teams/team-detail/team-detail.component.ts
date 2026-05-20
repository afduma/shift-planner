import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { MembershipsApiService } from '../../../core/api/memberships-api.service';
import { TeamsApiService } from '../../../core/api/teams-api.service';
import { Membership } from '../../../core/models/membership.model';
import { Team } from '../../../core/models/team.model';
import { TeamMembersComponent } from '../team-members/team-members.component';

@Component({
  selector: 'app-team-detail',
  imports: [RouterLink, TeamMembersComponent],
  templateUrl: './team-detail.component.html',
  styleUrl: './team-detail.component.scss',
})
export class TeamDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly teamsApi = inject(TeamsApiService);
  private readonly membershipsApi = inject(MembershipsApiService);

  protected readonly team = signal<Team | null>(null);
  protected readonly memberships = signal<Membership[]>([]);
  protected readonly errorMessage = signal('');

  constructor() {
    this.route.paramMap
      .pipe(
        map((params) => params.get('id')),
        switchMap((id) => {
          if (!id) {
            this.errorMessage.set('Team id is missing from the route.');
            return of(null);
          }

          return forkJoin({
            team: this.teamsApi.getTeamById(id),
            memberships: this.membershipsApi.getTeamMemberships(id).pipe(catchError(() => of([]))),
          }).pipe(
            catchError(() => {
              this.errorMessage.set('Team details are not available.');
              return of(null);
            }),
          );
        }),
      )
      .subscribe((result) => {
        this.team.set(result?.team ?? null);
        this.memberships.set(result?.memberships ?? []);
      });
  }
}
