import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Cliente } from '../models/cliente.model';
import { ApiResponse } from '../../../shared/models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/clientes`;

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

  getById(id: number): Observable<ApiResponse<Cliente>> {
    return this.http.get<ApiResponse<Cliente>>(`${this.apiUrl}/${id}`);
  }

  create(cliente: Cliente): Observable<ApiResponse<Cliente>> {
    return this.http.post<ApiResponse<Cliente>>(this.apiUrl, cliente);
  }

  update(id: number, cliente: Cliente): Observable<ApiResponse<Cliente>> {
    return this.http.put<ApiResponse<Cliente>>(`${this.apiUrl}/${id}`, cliente);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
