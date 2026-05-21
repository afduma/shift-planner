import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { Team, TeamUpsertRequest } from '../models/team.model';

@Injectable({ providedIn: 'root' })
export class TeamsApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getTeams(): Observable<Team[]> {
    return this.http.get<Team[]>(`${this.apiBaseUrl}/teams`);
  }

  getTeamById(id: string): Observable<Team> {
    return this.http.get<Team>(`${this.apiBaseUrl}/teams/${id}`);
  }

  createTeam(request: TeamUpsertRequest): Observable<Team> {
    return this.http.post<Team>(`${this.apiBaseUrl}/teams`, request);
  }

  updateTeam(id: string, request: TeamUpsertRequest): Observable<Team> {
    return this.http.put<Team>(`${this.apiBaseUrl}/teams/${id}`, request);
  }

  deleteTeam(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/teams/${id}`);
  }
}
