import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subject, finalize, forkJoin, takeUntil, timeout } from 'rxjs';

import { PedidoService } from '../../services/pedido.service';
import { ClienteService } from '../../../cliente/services/cliente.service';
import { ExameService } from '../../../exame/services/exame.service';
import { Pedido, PedidoItem, PedidoPayload, PedidoStatus } from '../../models/pedido.model';
import { Cliente } from '../../../cliente/models/cliente.model';
import { Exame } from '../../../exame/models/exame.model';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-pedido-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink],
  templateUrl: './pedido-form.component.html',
  styleUrls: ['./pedido-form.component.css']
})
export class PedidoFormComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private pedidoService = inject(PedidoService);
  private clienteService = inject(ClienteService);
  private exameService = inject(ExameService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  protected auth = inject(AuthService);

  private readonly destroy$ = new Subject<void>();

  pedidoForm!: FormGroup;
  isEditMode = false;
  pedidoId?: number;
  loading = false;
  lookupsLoading = signal(false);
  lookupsError = signal(false);
  saving = false;
  actionLoading = false;

  clientes: Cliente[] = [];
  examesDisponiveis: Exame[] = [];
  itensSelecionados: PedidoItem[] = [];

  status?: PedidoStatus;
  motivoCancelamento?: string;
  dataPedido?: string;

  exameParaAdicionar: number | null = null;

  ngOnInit(): void {
    this.initForm();
    this.loadLookups();
    this.checkEditMode();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initForm(): void {
    this.pedidoForm = this.fb.group({
      clienteId: [null as number | null, [Validators.required]],
      observacao: ['', [Validators.maxLength(500)]]
    });
  }

  private loadLookups(): void {
    this.lookupsLoading.set(true);
    this.lookupsError.set(false);

    forkJoin({
      clientes: this.clienteService.getAll(0, 200, 'nome', 'asc'),
      exames: this.exameService.getAll(0, 200, 'nome', 'asc')
    })
      .pipe(
        timeout(15000),
        takeUntil(this.destroy$),
        finalize(() => this.lookupsLoading.set(false))
      )
      .subscribe({
        next: ({ clientes, exames }) => {
          if (clientes.success && clientes.data) {
            this.clientes = (clientes.data.content ?? []).filter((c: Cliente) => c.ativo !== false);
          } else {
            this.toast.error(clientes.message || 'Erro ao carregar clientes.');
            this.lookupsError.set(true);
          }

          if (exames.success && exames.data) {
            this.examesDisponiveis = (exames.data.content ?? []).filter((e: Exame) => e.ativo !== false);
          } else {
            this.toast.error(exames.message || 'Erro ao carregar exames.');
            this.lookupsError.set(true);
          }
        },
        error: (err) => {
          this.lookupsError.set(true);
          const isTimeout = err?.name === 'TimeoutError';
          this.toast.error(
            isTimeout
              ? 'Tempo esgotado ao carregar clientes e exames. Verifique se o backend está rodando.'
              : (err.error?.message || 'Erro ao carregar dados do formulário.')
          );
        }
      });
  }

  private checkEditMode(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }

    this.isEditMode = true;
    this.pedidoId = Number(idParam);

    const prefetched = history.state?.['pedido'] as Pedido | null | undefined;
    if (prefetched) {
      this.applyPedido(prefetched);
      return;
    }

    this.loadPedido(this.pedidoId);
  }

  private applyPedido(data: Pedido): void {
    this.status = data.status;
    this.motivoCancelamento = data.motivoCancelamento;
    this.dataPedido = data.dataPedido;

    this.pedidoForm.patchValue({
      clienteId: data.clienteId,
      observacao: data.observacao || ''
    });

    this.itensSelecionados = (data.itens ?? []).map((item) => ({ ...item }));

    if (!this.isEditable) {
      this.pedidoForm.disable();
    }

    this.loading = false;
  }

  private loadPedido(id: number): void {
    this.loading = true;
    this.pedidoService.getById(id)
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.applyPedido(response.data);
          } else {
            this.toast.error(response.message || 'Erro ao carregar pedido.');
          }
        },
        error: () => this.toast.error('Erro ao se conectar com o servidor.')
      });
  }

  get isEditable(): boolean {
    return !this.status || this.status === 'ABERTO' || this.status === 'EM_ANDAMENTO';
  }

  get canManageStatus(): boolean {
    return this.isEditMode && this.auth.canEdit() && this.status !== 'CONCLUIDO' && this.status !== 'CANCELADO';
  }

  get totalPedido(): number {
    return this.itensSelecionados.reduce((sum, item) => sum + (item.valorUnitario ?? 0), 0);
  }

  get examesParaSelect(): Exame[] {
    const ids = new Set(this.itensSelecionados.map((i) => i.exameId));
    return this.examesDisponiveis.filter((e) => e.id && !ids.has(e.id));
  }

  adicionarExame(): void {
    if (!this.exameParaAdicionar || !this.isEditable) {
      return;
    }

    const exame = this.examesDisponiveis.find((e) => e.id === this.exameParaAdicionar);
    if (!exame || !exame.id) {
      return;
    }

    this.itensSelecionados.push({
      exameId: exame.id,
      exameCodigo: exame.codigo,
      exameNome: exame.nome,
      valorUnitario: exame.valor ?? 0
    });
    this.exameParaAdicionar = null;
  }

  removerExame(index: number): void {
    if (!this.isEditable) {
      return;
    }
    this.itensSelecionados.splice(index, 1);
  }

  onSubmit(): void {
    if (!this.isEditable) {
      return;
    }

    if (this.pedidoForm.invalid) {
      this.pedidoForm.markAllAsTouched();
      return;
    }

    if (this.itensSelecionados.length === 0) {
      this.toast.error('Adicione pelo menos um exame ao pedido.');
      return;
    }

    const formValue = this.pedidoForm.getRawValue();
    const payload: PedidoPayload = {
      clienteId: formValue.clienteId,
      observacao: formValue.observacao?.trim() || undefined,
      itens: this.itensSelecionados.map((item) => ({ exameId: item.exameId }))
    };

    this.saving = true;
    const request$ = this.isEditMode && this.pedidoId
      ? this.pedidoService.update(this.pedidoId, payload)
      : this.pedidoService.create(payload);

    request$.subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success(this.isEditMode ? 'Pedido atualizado com sucesso.' : 'Pedido criado com sucesso.');
          this.router.navigate(['/pedidos']);
        } else {
          this.toast.error(response.message || 'Erro ao salvar pedido.');
          this.saving = false;
        }
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Erro ao salvar pedido.');
        this.saving = false;
      }
    });
  }

  concluirPedido(): void {
    if (!this.pedidoId || !this.canManageStatus) {
      return;
    }

    this.actionLoading = true;
    this.pedidoService.concluir(this.pedidoId)
      .pipe(finalize(() => { this.actionLoading = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.toast.success('Pedido concluído com sucesso.');
            this.applyPedido(response.data);
          } else {
            this.toast.error(response.message || 'Erro ao concluir pedido.');
          }
        },
        error: (err) => this.toast.error(err.error?.message || 'Erro ao concluir pedido.')
      });
  }

  cancelarPedido(): void {
    if (!this.pedidoId || !this.canManageStatus) {
      return;
    }

    const motivo = window.prompt('Informe o motivo do cancelamento:');
    if (!motivo?.trim()) {
      this.toast.warning('Cancelamento não realizado: motivo obrigatório.');
      return;
    }

    this.actionLoading = true;
    this.pedidoService.cancelar(this.pedidoId, motivo.trim())
      .pipe(finalize(() => { this.actionLoading = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.toast.success('Pedido cancelado com sucesso.');
            this.applyPedido(response.data);
          } else {
            this.toast.error(response.message || 'Erro ao cancelar pedido.');
          }
        },
        error: (err) => this.toast.error(err.error?.message || 'Erro ao cancelar pedido.')
      });
  }

  formatCurrency(value: number | undefined): string {
    return (value ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  statusLabel(status?: PedidoStatus): string {
    if (!status) return '—';
    const labels: Record<PedidoStatus, string> = {
      ABERTO: 'Aberto',
      EM_ANDAMENTO: 'Em andamento',
      CONCLUIDO: 'Concluído',
      CANCELADO: 'Cancelado'
    };
    return labels[status];
  }

  statusClass(status?: PedidoStatus): string {
    if (!status) return 'badge-gray';
    const classes: Record<PedidoStatus, string> = {
      ABERTO: 'badge-blue',
      EM_ANDAMENTO: 'badge-yellow',
      CONCLUIDO: 'badge-green',
      CANCELADO: 'badge-gray'
    };
    return classes[status];
  }

  get f() {
    return this.pedidoForm.controls;
  }
}
