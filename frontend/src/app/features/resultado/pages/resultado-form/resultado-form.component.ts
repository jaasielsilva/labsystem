import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { ResultadoService } from '../../services/resultado.service';
import { Resultado, ResultadoStatus } from '../../models/resultado.model';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-resultado-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './resultado-form.component.html',
  styleUrls: ['./resultado-form.component.css']
})
export class ResultadoFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private resultadoService = inject(ResultadoService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  protected auth = inject(AuthService);

  form!: FormGroup;
  resultado?: Resultado;
  resultadoId?: number;
  loading = false;
  saving = false;
  actionLoading = false;

  ngOnInit(): void {
    this.form = this.fb.group({
      observacaoInterna: ['', [Validators.maxLength(500)]],
      laudo: ['']
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.resultadoId = Number(idParam);
      this.loadResultado(this.resultadoId);
    }
  }

  private loadResultado(id: number): void {
    this.loading = true;
    this.resultadoService.getById(id)
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.applyResultado(response.data);
          } else {
            this.toast.error(response.message || 'Erro ao carregar resultado.');
          }
        },
        error: () => this.toast.error('Erro ao se conectar com o servidor.')
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
