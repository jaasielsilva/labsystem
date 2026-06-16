import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { UsuarioService } from '../../services/usuario.service';
import { Usuario } from '../../models/usuario.model';
import { ToastService } from '../../../../../core/services/toast.service';
import { TenantContextService } from '../../../../../core/services/tenant-context.service';
import { EmpresaService } from '../../../empresa/services/empresa.service';
import { Empresa } from '../../../empresa/models/empresa.model';

@Component({
  selector: 'app-usuario-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './usuario-form.component.html',
  styleUrls: ['./usuario-form.component.css']
})
export class UsuarioFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private usuarioService = inject(UsuarioService);
  private empresaService = inject(EmpresaService);
  private toast = inject(ToastService);
  private tenant = inject(TenantContextService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  usuarioForm!: FormGroup;
  isEditMode = false;
  usuarioId?: number;
  loading = false;
  saving = false;
  loadingEmpresas = false;
  empresas: Empresa[] = [];

  ngOnInit(): void {
    this.initForm();
    this.loadEmpresas();
    this.checkEditMode();
  }

  private initForm(): void {
    this.usuarioForm = this.fb.group({
      nome: ['', [Validators.required, Validators.maxLength(150)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      perfil: ['', [Validators.required]],
      empresaId: [null as number | null, [Validators.required]],
      ativo: [true, [Validators.required]],
      senha: ['', [Validators.minLength(6)]]
    });
  }

  private loadEmpresas(): void {
    this.loadingEmpresas = true;

    this.empresaService
      .getAll(0, 100, 'nome', 'asc')
      .pipe(finalize(() => { this.loadingEmpresas = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.empresas = response.data.content ?? [];

            if (!this.isEditMode && this.usuarioForm.get('empresaId')?.value == null) {
              const defaultEmpresaId = this.tenant.empresaId();
              if (defaultEmpresaId && this.empresas.some((e) => e.id === defaultEmpresaId)) {
                this.usuarioForm.patchValue({ empresaId: defaultEmpresaId });
              }
            }
          } else {
            this.toast.error(response.message || 'Erro ao carregar empresas.');
          }
        },
        error: () => {
          this.toast.error('Erro ao carregar lista de empresas.');
        }
      });
  }

  private checkEditMode(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.f['senha'].setValidators([Validators.required, Validators.minLength(6)]);
      this.f['senha'].updateValueAndValidity();
      return;
    }

    this.isEditMode = true;
    this.usuarioId = Number(idParam);
    this.f['senha'].setValidators([Validators.minLength(6)]);
    this.f['senha'].updateValueAndValidity();

    const prefetched = history.state?.['usuario'] as Usuario | null | undefined;
    if (prefetched) {
      this.applyUsuario(prefetched);
      return;
    }

    this.loadUsuario(this.usuarioId);
  }

  private applyUsuario(userData: Usuario): void {
    this.usuarioForm.patchValue({
      nome: userData.nome,
      email: userData.email,
      perfil: userData.perfil,
      empresaId: userData.empresaId ?? null,
      ativo: userData.ativo
    });
    this.loading = false;
  }

  private loadUsuario(id: number): void {
    this.loading = true;

    this.usuarioService.getById(id)
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.applyUsuario(response.data);
          } else {
            this.toast.error(response.message || 'Erro ao carregar dados do usuário.');
          }
        },
        error: () => {
          this.toast.error('Erro ao se conectar com o servidor.');
        }
      });
  }

  onSubmit(): void {
    if (this.usuarioForm.invalid) {
      this.usuarioForm.markAllAsTouched();
      return;
    }

    this.saving = true;

    const formValue = this.usuarioForm.value as Usuario;
    const payload: Partial<Usuario> = {
      nome: formValue.nome,
      email: formValue.email,
      perfil: formValue.perfil,
      empresaId: formValue.empresaId,
      ativo: formValue.ativo
    };

    const senha = formValue.senha?.trim();
    if (senha) {
      payload.senha = senha;
    }

    const request$ = this.isEditMode && this.usuarioId
      ? this.usuarioService.update(this.usuarioId, payload)
      : this.usuarioService.create(payload as Usuario);

    request$.subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success(
            this.isEditMode ? 'Usuário atualizado com sucesso.' : 'Usuário cadastrado com sucesso.'
          );
          this.router.navigate(['/governanca/usuarios']);
        } else {
          this.toast.error(response.message || 'Erro ao salvar usuário.');
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
    return this.usuarioForm.controls;
  }
}
