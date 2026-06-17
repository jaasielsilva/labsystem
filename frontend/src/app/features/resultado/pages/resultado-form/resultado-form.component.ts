import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize, forkJoin, map, timeout, timer } from 'rxjs';

import { ResultadoService } from '../../services/resultado.service';
import { Resultado, ResultadoStatus } from '../../models/resultado.model';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ApiResponse } from '../../../../shared/models/api-response.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-resultado-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './resultado-form.component.html',
  styleUrls: ['./resultado-form.component.css']
})
export class ResultadoFormComponent implements OnInit {
  private static readonly MIN_LOADING_MS = 500;
  private fb = inject(FormBuilder);
  private resultadoService = inject(ResultadoService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);
  protected auth = inject(AuthService);

  form!: FormGroup;
  resultado?: Resultado;
  resultadoId?: number;
  loading = false;
  loadError = false;
  saving = false;
  actionLoading = false;

  ngOnInit(): void {
    this.form = this.fb.group({
      observacaoInterna: ['', [Validators.maxLength(500)]],
      laudo: ['']
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.router.navigate(['/resultados']);
      return;
    }

    const id = Number(idParam);
    if (!Number.isFinite(id) || id <= 0) {
      this.toast.error('Resultado inválido.');
      this.router.navigate(['/resultados']);
      return;
    }

    this.resultadoId = id;
    this.loadResultado(id);
  }

  private loadResultado(id: number): void {
    this.loading = true;
    this.loadError = false;
    this.resultado = undefined;
    this.cdr.detectChanges();

    const safetyTimer = window.setTimeout(() => {
      if (!this.loading) {
        return;
      }
      this.loading = false;
      this.loadError = true;
      this.toast.error('Tempo esgotado. Confirme o backend em localhost:8080 e reinicie o ng serve.');
      this.cdr.markForCheck();
    }, 16000);

    forkJoin({
      response: this.resultadoService.getById(id).pipe(timeout(15000)),
      minDelay: timer(ResultadoFormComponent.MIN_LOADING_MS)
    })
      .pipe(
        map(({ response }) => response),
        finalize(() => {
          window.clearTimeout(safetyTimer);
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (response: ApiResponse<Resultado>) => {
          if (response.success && response.data) {
            this.applyResultado(response.data);
          } else {
            this.loadError = true;
            this.toast.error(response.message || 'Erro ao carregar resultado.');
          }
          this.cdr.markForCheck();
        },
        error: (err: unknown) => {
          this.loadError = true;
          const httpErr = err instanceof HttpErrorResponse ? err : null;
          const isTimeout = err instanceof Error && err.name === 'TimeoutError';
          const message = httpErr?.error?.message
            || (isTimeout ? 'Tempo esgotado. Verifique se o backend está rodando (mvn spring-boot:run).' : 'Erro ao carregar resultado.');
          this.toast.error(message);

          if (httpErr && (httpErr.status === 401 || httpErr.status === 403 || httpErr.status === 404)) {
            setTimeout(() => this.router.navigate(['/resultados']), 1500);
          }
          this.cdr.markForCheck();
        }
      });
  }

  private applyResultado(data: Resultado): void {
    this.resultado = data;
    this.form.patchValue({
      observacaoInterna: data.observacaoInterna || '',
      laudo: data.laudo || ''
    });

    if (!this.isEditable) {
      this.form.disable();
    }
  }

  get isEditable(): boolean {
    return !!this.auth.canEdit()
      && !!this.resultado
      && this.resultado.status !== 'DISPONIVEL'
      && this.resultado.status !== 'CANCELADO'
      && this.resultado.pedidoStatus !== 'CANCELADO';
  }

  get canIniciarAnalise(): boolean {
    return this.isEditable && this.resultado?.status === 'PENDENTE';
  }

  get canLiberar(): boolean {
    return this.isEditable && (this.resultado?.status === 'PENDENTE' || this.resultado?.status === 'EM_ANALISE');
  }

  private payload() {
    const value = this.form.getRawValue();
    return {
      observacaoInterna: value.observacaoInterna?.trim() || undefined,
      laudo: value.laudo?.trim() || undefined
    };
  }

  salvarRascunho(): void {
    if (!this.resultadoId || !this.isEditable) return;

    this.saving = true;
    this.resultadoService.update(this.resultadoId, this.payload()).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.toast.success('Rascunho salvo com sucesso.');
          this.applyResultado(response.data);
        } else {
          this.toast.error(response.message || 'Erro ao salvar rascunho.');
        }
        this.saving = false;
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Erro ao salvar rascunho.');
        this.saving = false;
      }
    });
  }

  iniciarAnalise(): void {
    if (!this.resultadoId || !this.canIniciarAnalise) return;

    this.actionLoading = true;
    this.resultadoService.iniciarAnalise(this.resultadoId)
      .pipe(finalize(() => { this.actionLoading = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.toast.success('Análise iniciada. O pedido entrou em andamento.');
            this.applyResultado(response.data);
          } else {
            this.toast.error(response.message || 'Erro ao iniciar análise.');
          }
        },
        error: (err) => this.toast.error(err.error?.message || 'Erro ao iniciar análise.')
      });
  }

  liberar(): void {
    if (!this.resultadoId || !this.canLiberar) return;

    const laudo = this.form.getRawValue().laudo?.trim();
    if (!laudo) {
      this.toast.error('Informe o laudo antes de liberar o resultado.');
      return;
    }

    this.actionLoading = true;
    this.resultadoService.liberar(this.resultadoId, this.payload())
      .pipe(finalize(() => { this.actionLoading = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.toast.success('Resultado liberado com sucesso.');
            this.applyResultado(response.data);
          } else {
            this.toast.error(response.message || 'Erro ao liberar resultado.');
          }
        },
        error: (err) => this.toast.error(err.error?.message || 'Erro ao liberar resultado.')
      });
  }

  statusLabel(status?: ResultadoStatus): string {
    if (!status) return '—';
    const labels: Record<ResultadoStatus, string> = {
      PENDENTE: 'Pendente',
      EM_ANALISE: 'Em análise',
      DISPONIVEL: 'Disponível',
      CANCELADO: 'Cancelado'
    };
    return labels[status];
  }

  statusClass(status?: ResultadoStatus): string {
    if (!status) return 'badge-gray';
    const classes: Record<ResultadoStatus, string> = {
      PENDENTE: 'badge-gray',
      EM_ANALISE: 'badge-yellow',
      DISPONIVEL: 'badge-green',
      CANCELADO: 'badge-rose'
    };
    return classes[status];
  }
}
