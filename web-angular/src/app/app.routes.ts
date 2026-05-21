import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { TeamCreateComponent } from './features/teams/team-create/team-create.component';
import { TeamDetailComponent } from './features/teams/team-detail/team-detail.component';
import { TeamEditComponent } from './features/teams/team-edit/team-edit.component';
import { TeamsListComponent } from './features/teams/teams-list/teams-list.component';
import { UserDetailComponent } from './features/users/user-detail/user-detail.component';
import { UserEditComponent } from './features/users/user-edit/user-edit.component';
import { UsersListComponent } from './features/users/users-list/users-list.component';
import { ShellComponent } from './layout/shell/shell.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard',
      },
      {
        path: 'dashboard',
        component: DashboardComponent,
      },
      {
        path: 'users',
        component: UsersListComponent,
      },
      {
        path: 'users/:id',
        component: UserDetailComponent,
      },
      {
        path: 'users/:id/edit',
        component: UserEditComponent,
      },
      {
        path: 'teams',
        component: TeamsListComponent,
      },
      {
        path: 'teams/new',
        component: TeamCreateComponent,
      },
      {
        path: 'teams/:id',
        component: TeamDetailComponent,
      },
      {
        path: 'teams/:id/edit',
        component: TeamEditComponent,
      },
      {
        path: '**',
        redirectTo: 'dashboard',
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
