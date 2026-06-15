import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { Exame } from '../models/exame.model';

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

@Injectable({
  providedIn: 'root'
})
export class ExameService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/exames`;

  getAll(
    page: number,
    size: number,
    sortBy: string,
    sortDir: string,
    search: string
  ): Observable<ApiResponse<PageResponse<Exame>>> {
    return this.http.get<ApiResponse<PageResponse<Exame>>>(
      `${this.apiUrl}?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}&search=${search}`
    );
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