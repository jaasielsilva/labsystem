import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { Pedido, PedidoPayload } from '../models/pedido.model';

@Injectable({
  providedIn: 'root'
})
export class PedidoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/pedidos`;

  getAll(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'dataPedido',
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

  getById(id: number): Observable<ApiResponse<Pedido>> {
    return this.http.get<ApiResponse<Pedido>>(`${this.apiUrl}/${id}`);
  }

  create(payload: PedidoPayload): Observable<ApiResponse<Pedido>> {
    return this.http.post<ApiResponse<Pedido>>(this.apiUrl, payload);
  }

  update(id: number, payload: PedidoPayload): Observable<ApiResponse<Pedido>> {
    return this.http.put<ApiResponse<Pedido>>(`${this.apiUrl}/${id}`, payload);
  }

  concluir(id: number): Observable<ApiResponse<Pedido>> {
    return this.http.post<ApiResponse<Pedido>>(`${this.apiUrl}/${id}/concluir`, {});
  }

  cancelar(id: number, motivo: string): Observable<ApiResponse<Pedido>> {
    return this.http.post<ApiResponse<Pedido>>(`${this.apiUrl}/${id}/cancelar`, { motivo });
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
