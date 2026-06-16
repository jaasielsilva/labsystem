import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { Exame } from '../models/exame.model';

@Injectable({
  providedIn: 'root'
})
export class ExameService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/exames`;

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

  getById(id: number): Observable<ApiResponse<Exame>> {
    return this.http.get<ApiResponse<Exame>>(`${this.apiUrl}/${id}`);
  }

  create(exame: Exame): Observable<ApiResponse<Exame>> {
    return this.http.post<ApiResponse<Exame>>(this.apiUrl, exame);
  }

  update(id: number, exame: Exame): Observable<ApiResponse<Exame>> {
    return this.http.put<ApiResponse<Exame>>(`${this.apiUrl}/${id}`, exame);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
