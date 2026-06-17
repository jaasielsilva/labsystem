import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { Resultado, ResultadoUpdatePayload } from '../models/resultado.model';

@Injectable({ providedIn: 'root' })
export class ResultadoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/resultados`;

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

  getById(id: number): Observable<ApiResponse<Resultado>> {
    return this.http.get<ApiResponse<Resultado>>(`${this.apiUrl}/${id}`);
  }

  update(id: number, payload: ResultadoUpdatePayload): Observable<ApiResponse<Resultado>> {
    return this.http.put<ApiResponse<Resultado>>(`${this.apiUrl}/${id}`, payload);
  }

  iniciarAnalise(id: number): Observable<ApiResponse<Resultado>> {
    return this.http.post<ApiResponse<Resultado>>(`${this.apiUrl}/${id}/iniciar-analise`, {});
  }

  liberar(id: number, payload: ResultadoUpdatePayload): Observable<ApiResponse<Resultado>> {
    return this.http.post<ApiResponse<Resultado>>(`${this.apiUrl}/${id}/liberar`, payload);
  }

  cancelar(id: number): Observable<ApiResponse<Resultado>> {
    return this.http.post<ApiResponse<Resultado>>(`${this.apiUrl}/${id}/cancelar`, {});
  }
}
