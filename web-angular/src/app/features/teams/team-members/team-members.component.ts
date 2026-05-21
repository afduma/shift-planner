import { Component, input, output } from '@angular/core';
import {
  CreateMembershipRequest,
  Membership,
  MembershipRole,
  UpdateMembershipRequest,
} from '../../../core/models/membership.model';
import { User } from '../../../core/models/user.model';

const MEMBERSHIP_ROLES: MembershipRole[] = ['LEAD', 'PLANNER', 'MEMBER'];

@Component({
  selector: 'app-team-members',
  templateUrl: './team-members.component.html',
  styleUrl: './team-members.component.scss',
})
export class TeamMembersComponent {
  readonly members = input<Membership[]>([]);
  readonly availableUsers = input<User[]>([]);
  readonly isSaving = input<boolean>(false);
  readonly errorMessage = input<string>('');
  readonly createMembership = output<CreateMembershipRequest>();
  readonly updateMembership = output<{ membershipId: string; request: UpdateMembershipRequest }>();
  readonly deleteMembership = output<string>();

  protected readonly roles = MEMBERSHIP_ROLES;

  protected addMembership(userId: string, role: string): void {
    if (!userId) {
      return;
    }

    this.createMembership.emit({
      userId,
      role: role as MembershipRole,
    });
  }

  protected saveMembershipRole(membershipId: string, role: string): void {
    this.updateMembership.emit({
      membershipId,
      request: {
        role: role as MembershipRole,
      },
    });
  }
}
