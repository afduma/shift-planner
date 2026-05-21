import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, finalize, forkJoin, map, of, switchMap } from 'rxjs';
import { MembershipsApiService } from '../../../core/api/memberships-api.service';
import { TeamsApiService } from '../../../core/api/teams-api.service';
import { UsersApiService } from '../../../core/api/users-api.service';
import {
  CreateMembershipRequest,
  Membership,
  UpdateMembershipRequest,
} from '../../../core/models/membership.model';
import { Team } from '../../../core/models/team.model';
import { User } from '../../../core/models/user.model';
import { TeamMembersComponent } from '../team-members/team-members.component';

@Component({
  selector: 'app-team-detail',
  imports: [RouterLink, TeamMembersComponent],
  templateUrl: './team-detail.component.html',
  styleUrl: './team-detail.component.scss',
})
export class TeamDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly teamsApi = inject(TeamsApiService);
  private readonly membershipsApi = inject(MembershipsApiService);
  private readonly usersApi = inject(UsersApiService);

  protected readonly teamId = signal<string | null>(null);
  protected readonly team = signal<Team | null>(null);
  protected readonly users = signal<User[]>([]);
  protected readonly memberships = signal<Membership[]>([]);
  protected readonly errorMessage = signal('');
  protected readonly membershipErrorMessage = signal('');
  protected readonly isSavingMembership = signal(false);
  protected readonly isDeletingTeam = signal(false);

  constructor() {
    this.route.paramMap
      .pipe(
        map((params) => params.get('id')),
        switchMap((id) => {
          this.teamId.set(id);

          if (!id) {
            this.errorMessage.set('Team id is missing from the route.');
            return of(null);
          }

          return forkJoin({
            team: this.teamsApi.getTeamById(id),
            memberships: this.membershipsApi.getTeamMemberships(id).pipe(catchError(() => of([]))),
            users: this.usersApi.getUsers().pipe(catchError(() => of([]))),
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
        this.users.set(result?.users ?? []);
        this.memberships.set(this.attachUsers(result?.memberships ?? [], result?.users ?? []));
      });
  }

  protected createMembership(request: CreateMembershipRequest): void {
    const teamId = this.teamId();
    if (!teamId) {
      return;
    }

    this.membershipErrorMessage.set('');
    this.isSavingMembership.set(true);

    this.membershipsApi
      .createTeamMembership(teamId, request)
      .pipe(finalize(() => this.isSavingMembership.set(false)))
      .subscribe({
        next: (membership) => {
          const user = this.users().find((candidate) => candidate.id === membership.userId);
          this.memberships.set([...this.memberships(), { ...membership, user }]);
        },
        error: () =>
          this.membershipErrorMessage.set('Could not add the membership. Check permissions or duplicate entries.'),
      });
  }

  protected updateMembership(event: { membershipId: string; request: UpdateMembershipRequest }): void {
    const teamId = this.teamId();
    if (!teamId) {
      return;
    }

    this.membershipErrorMessage.set('');
    this.isSavingMembership.set(true);

    this.membershipsApi
      .updateTeamMembership(teamId, event.membershipId, event.request)
      .pipe(finalize(() => this.isSavingMembership.set(false)))
      .subscribe({
        next: (membership) => {
          const current = this.memberships();
          const existing = current.find((item) => item.id === membership.id);
          this.memberships.set(
            current.map((item) =>
              item.id === membership.id ? { ...membership, user: existing?.user } : item,
            ),
          );
        },
        error: () => this.membershipErrorMessage.set('Could not update the membership role.'),
      });
  }

  protected deleteMembership(membershipId: string): void {
    const teamId = this.teamId();
    if (!teamId || !window.confirm('Remove this team membership?')) {
      return;
    }

    this.membershipErrorMessage.set('');
    this.isSavingMembership.set(true);

    this.membershipsApi
      .deleteTeamMembership(teamId, membershipId)
      .pipe(finalize(() => this.isSavingMembership.set(false)))
      .subscribe({
        next: () => this.memberships.set(this.memberships().filter((membership) => membership.id !== membershipId)),
        error: () => this.membershipErrorMessage.set('Could not remove the membership.'),
      });
  }

  protected deleteTeam(team: Team): void {
    if (!window.confirm(`Delete team "${team.name}"?`)) {
      return;
    }

    this.errorMessage.set('');
    this.isDeletingTeam.set(true);

    this.teamsApi
      .deleteTeam(team.id)
      .pipe(finalize(() => this.isDeletingTeam.set(false)))
      .subscribe({
        next: () => void this.router.navigate(['/teams']),
        error: () =>
          this.errorMessage.set('Could not delete the team. Remove related records first if required.'),
      });
  }

  private attachUsers(memberships: Membership[], users: User[]): Membership[] {
    return memberships.map((membership) => ({
      ...membership,
      user: users.find((user) => user.id === membership.userId),
    }));
  }
}
