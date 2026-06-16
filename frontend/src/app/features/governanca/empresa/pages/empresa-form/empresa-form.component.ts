import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { EmpresaService } from '../../services/empresa.service';
import { Empresa } from '../../models/empresa.model';
import { ToastService } from '../../../../../core/services/toast.service';

@Component({
  selector: 'app-empresa-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './empresa-form.component.html',
  styleUrls: ['./empresa-form.component.css']
})
export class EmpresaFormComponent implements OnInit {

  private fb = inject(FormBuilder);
  private empresaService = inject(EmpresaService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  empresaForm!: FormGroup;
  isEditMode = false;
  empresaId?: number;
  loading = false;
  saving = false;

  ngOnInit(): void {
    this.initForm();
    this.checkEditMode();
  }

  private initForm(): void {
    this.empresaForm = this.fb.group({
      nome: ['', [Validators.required, Validators.maxLength(150)]],
      cnpj: ['', [Validators.required, Validators.pattern(/^\d{14}$/)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      telefone: ['', [Validators.maxLength(20)]],
      endereco: ['', [Validators.maxLength(200)]],
      cidade: ['', [Validators.maxLength(100)]],
      uf: ['', [Validators.maxLength(2)]],
      ativo: [true]
    });
  }

  private checkEditMode(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      return;
    }

    this.isEditMode = true;
    this.empresaId = Number(idParam);

    const prefetched = history.state?.['empresa'] as Empresa | null | undefined;

    if (prefetched) {
      this.applyEmpresa(prefetched);
      return;
    }

    this.loadEmpresa(this.empresaId);
  }

  private applyEmpresa(data: Empresa): void {
    this.empresaForm.patchValue({
      nome: data.nome,
      cnpj: data.cnpj,
      email: data.email,
      telefone: data.telefone || '',
      endereco: data.endereco || '',
      cidade: data.cidade || '',
      uf: data.uf || '',
      ativo: data.ativo
    });

    this.loading = false;
  }

  private loadEmpresa(id: number): void {
    this.loading = true;

    this.empresaService.getById(id)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.applyEmpresa(response.data);
          } else {
            this.toast.error(response.message || 'Erro ao carregar dados da empresa.');
          }
        },
        error: () => {
          this.toast.error('Erro ao se conectar com o servidor.');
        }
      });
  }

  onSubmit(): void {
    if (this.empresaForm.invalid) {
      this.empresaForm.markAllAsTouched();
      return;
    }

    this.saving = true;

    const empresaData: Empresa = this.empresaForm.value;

    const request$ = this.isEditMode && this.empresaId
      ? this.empresaService.update(this.empresaId, empresaData)
      : this.empresaService.create(empresaData);

    request$.subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success(
            this.isEditMode
              ? 'Empresa atualizada com sucesso.'
              : 'Empresa cadastrada com sucesso.'
          );

          this.router.navigate(['/plataforma/laboratorios']);
        } else {
          this.toast.error(response.message || 'Erro ao salvar empresa.');
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
    return this.empresaForm.controls;
  }
}