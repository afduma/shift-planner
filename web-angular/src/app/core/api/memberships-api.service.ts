import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import {
  CreateMembershipRequest,
  Membership,
  UpdateMembershipRequest,
} from '../models/membership.model';

@Injectable({ providedIn: 'root' })
export class MembershipsApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getTeamMemberships(teamId: string): Observable<Membership[]> {
    return this.http.get<Membership[]>(`${this.apiBaseUrl}/teams/${teamId}/memberships`);
  }

  createTeamMembership(teamId: string, request: CreateMembershipRequest): Observable<Membership> {
    return this.http.post<Membership>(`${this.apiBaseUrl}/teams/${teamId}/memberships`, request);
  }

  updateTeamMembership(
    teamId: string,
    membershipId: string,
    request: UpdateMembershipRequest,
  ): Observable<Membership> {
    return this.http.put<Membership>(
      `${this.apiBaseUrl}/teams/${teamId}/memberships/${membershipId}`,
      request,
    );
  }

  deleteTeamMembership(teamId: string, membershipId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/teams/${teamId}/memberships/${membershipId}`);
  }
}
