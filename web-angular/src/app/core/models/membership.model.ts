import { User } from './user.model';

export type MembershipRole = 'LEAD' | 'PLANNER' | 'MEMBER';

export interface Membership {
  id: string;
  teamId: string;
  userId: string;
  role: MembershipRole;
  user?: User;
}

export interface CreateMembershipRequest {
  userId: string;
  role: MembershipRole;
}

export interface UpdateMembershipRequest {
  role: MembershipRole;
}
