export type SystemRole = 'ADMIN' | 'MANAGER' | 'EMPLOYEE';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  active: boolean;
  systemRole: SystemRole;
}

export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  active: boolean;
  systemRole: SystemRole;
}
