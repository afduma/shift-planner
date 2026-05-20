import { User } from './user.model';

export type MembershipRole = 'LEAD' | 'MEMBER';

export interface Membership {
  id: string;
  teamId: string;
  userId: string;
  role: MembershipRole;
  user?: User;
}
