import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { Empresa } from '../models/empresa.model';
import { ApiResponse } from '../../../../shared/models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class EmpresaService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/platform/empresas`;

  getAll(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'nome',
    sortDir: string = 'asc',
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

  getById(id: number): Observable<ApiResponse<Empresa>> {
    return this.http.get<ApiResponse<Empresa>>(`${this.apiUrl}/${id}`);
  }

  create(empresa: Empresa): Observable<ApiResponse<Empresa>> {
    return this.http.post<ApiResponse<Empresa>>(this.apiUrl, empresa);
  }

  update(id: number, empresa: Empresa): Observable<ApiResponse<Empresa>> {
    return this.http.put<ApiResponse<Empresa>>(`${this.apiUrl}/${id}`, empresa);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}