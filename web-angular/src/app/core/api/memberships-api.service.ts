import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { Membership } from '../models/membership.model';

@Injectable({ providedIn: 'root' })
export class MembershipsApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getTeamMemberships(teamId: string): Observable<Membership[]> {
    return this.http.get<Membership[]>(`${this.apiBaseUrl}/teams/${teamId}/memberships`);
  }
}
