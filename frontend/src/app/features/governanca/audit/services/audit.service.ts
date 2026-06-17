import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { AuditLog } from '../models/audit.model';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
}

@Injectable({ providedIn: 'root' })
export class AuditService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/audit`;

  getAll(
    page: number,
    size: number,
    sortBy: string,
    sortDir: string,
    search: string
  ): Observable<ApiResponse<PageResponse<AuditLog>>> {

    return this.http.get<ApiResponse<PageResponse<AuditLog>>>(
      `${this.apiUrl}?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}&q=${search}`
    );
  }
}