import { InjectionToken } from '@angular/core';
import { environment } from '../../../environments/environment';

export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  // Environment files define the deployment-specific base URL.
  // The InjectionToken keeps that configuration centralized and easy to override in tests.
  factory: () => environment.apiBaseUrl,
});
