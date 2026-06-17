import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { AuditLog } from '../models/audit.model';
import { ApiResponse } from '../../../../shared/models/api-response.model';

@Injectable({ providedIn: 'root' })
export class AuditService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/auditoria`;

  getAll(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'createdAt',
    sortDir: string = 'desc',
    search?: string
  ): Observable<ApiResponse<any>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    const q = search?.trim();
    if (q) {
      params = params.set('q', q);
    }

    return this.http.get<ApiResponse<any>>(this.apiUrl, { params });
  }

  getById(id: number): Observable<ApiResponse<AuditLog>> {
    return this.http.get<ApiResponse<AuditLog>>(`${this.apiUrl}/${id}`);
  }
}
