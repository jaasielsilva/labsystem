import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models/cliente.model';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './cliente-form.component.html',
  styleUrls: ['./cliente-form.component.css']
})
export class ClienteFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private clienteService = inject(ClienteService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  clienteForm!: FormGroup;
  isEditMode = false;
  clienteId?: number;
  loading = false;
  saving = false;

  ngOnInit(): void {
    this.initForm();
    this.checkEditMode();
  }

  private initForm(): void {
    this.clienteForm = this.fb.group({
      nome: ['', [Validators.required, Validators.maxLength(150)]],
      cpf: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      telefone: ['', [Validators.maxLength(20)]],
      dataNascimento: ['', [Validators.required]]
    });
  }

  private checkEditMode(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }

    this.isEditMode = true;
    this.clienteId = Number(idParam);

    const prefetched = history.state?.['cliente'] as Cliente | null | undefined;
    if (prefetched) {
      this.applyCliente(prefetched);
      return;
    }

    this.loadCliente(this.clienteId);
  }

  private applyCliente(clientData: Cliente): void {
    this.clienteForm.patchValue({
      nome: clientData.nome,
      cpf: clientData.cpf,
      email: clientData.email,
      telefone: clientData.telefone || '',
      dataNascimento: clientData.dataNascimento
    });
    this.loading = false;
  }

  private loadCliente(id: number): void {
    this.loading = true;

    this.clienteService.getById(id)
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.applyCliente(response.data);
          } else {
            this.toast.error(response.message || 'Erro ao carregar dados do cliente.');
          }
        },
        error: () => {
          this.toast.error('Erro ao se conectar com o servidor.');
        }
      });
  }

  onSubmit(): void {
    if (this.clienteForm.invalid) {
      this.clienteForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const clienteData: Cliente = this.clienteForm.value;

    const request$ = this.isEditMode && this.clienteId
      ? this.clienteService.update(this.clienteId, clienteData)
      : this.clienteService.create(clienteData);

    request$.subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success(
            this.isEditMode ? 'Cliente atualizado com sucesso.' : 'Cliente cadastrado com sucesso.'
          );
          this.router.navigate(['/clientes']);
        } else {
          this.toast.error(response.message || 'Erro ao salvar cliente.');
          this.saving = false;
        }
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Ocorreu um erro inesperado no servidor.');
        this.saving = false;
      }
    });
  }

  get f() {
    return this.clienteForm.controls;
  }
}
