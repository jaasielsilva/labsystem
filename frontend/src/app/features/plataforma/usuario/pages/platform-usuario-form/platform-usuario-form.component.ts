import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { PlatformUsuarioService } from '../../services/platform-usuario.service';
import { EmpresaService } from '../../../../governanca/empresa/services/empresa.service';
import { Usuario } from '../../models/usuario.model';
import { Empresa } from '../../../../governanca/empresa/models/empresa.model';
import { ToastService } from '../../../../../core/services/toast.service';

@Component({
  selector: 'app-platform-usuario-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './platform-usuario-form.component.html',
  styleUrls: ['./platform-usuario-form.component.css']
})
export class PlatformUsuarioFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private usuarioService = inject(PlatformUsuarioService);
  private empresaService = inject(EmpresaService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  usuarioForm!: FormGroup;
  isEditMode = false;
  usuarioId?: number;
  loading = false;
  saving = false;
  loadingLaboratorios = false;
  laboratorios: Empresa[] = [];

  ngOnInit(): void {
    this.initForm();
    this.loadLaboratorios();
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

  private loadLaboratorios(): void {
    this.loadingLaboratorios = true;
    this.empresaService
      .getAll(0, 100, 'nome', 'asc')
      .pipe(finalize(() => { this.loadingLaboratorios = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.laboratorios = response.data.content ?? [];
          } else {
            this.toast.error(response.message || 'Erro ao carregar laboratórios.');
          }
        },
        error: () => this.toast.error('Erro ao carregar laboratórios.')
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

  private applyUsuario(userData: Usuario & { perfil?: string }): void {
    const perfil = String(userData.perfil ?? '');
    if (perfil === 'SUPER_ADMIN') {
      this.toast.error('Contas de super administrador não podem ser editadas por esta tela.');
      this.router.navigate(['/plataforma/usuarios']);
      return;
    }

    this.usuarioForm.patchValue({
      nome: userData.nome,
      email: userData.email,
      perfil: perfil as Usuario['perfil'],
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
            this.applyUsuario(response.data as Usuario & { perfil?: string });
          } else {
            this.toast.error(response.message || 'Erro ao carregar dados do usuário.');
          }
        },
        error: () => this.toast.error('Erro ao se conectar com o servidor.')
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
          this.toast.success(this.isEditMode ? 'Usuário atualizado com sucesso.' : 'Usuário cadastrado com sucesso.');
          this.router.navigate(['/plataforma/usuarios']);
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
