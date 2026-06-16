import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ExameService } from '../../services/exame.service';
import { Exame } from '../../models/exame.model';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-exame-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './exame-form.component.html',
  styleUrls: ['./exame-form.component.css']
})
export class ExameFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private exameService = inject(ExameService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  exameForm!: FormGroup;
  isEditMode = false;
  exameId?: number;
  loading = false;
  saving = false;

  ngOnInit(): void {
    this.initForm();
    this.checkEditMode();
  }

  private initForm(): void {
    this.exameForm = this.fb.group({
      codigo: ['', [Validators.required, Validators.maxLength(50)]],
      nome: ['', [Validators.required, Validators.maxLength(200)]],
      descricao: ['', [Validators.maxLength(500)]],
      categoria: ['', [Validators.maxLength(100)]],
      tipoAmostra: ['SANGUE', [Validators.required]],
      prazoDias: [7, [Validators.required, Validators.min(1), Validators.max(365)]],
      valor: [null, [Validators.min(0)]],
      ativo: [true]
    });
  }

  private checkEditMode(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }

    this.isEditMode = true;
    this.exameId = Number(idParam);

    const prefetched = history.state?.['exame'] as Exame | null | undefined;
    if (prefetched) {
      this.applyExame(prefetched);
      return;
    }

    this.loadExame(this.exameId);
  }

  private applyExame(exameData: Exame): void {
    this.exameForm.patchValue({
      codigo: exameData.codigo,
      nome: exameData.nome,
      descricao: exameData.descricao || '',
      categoria: exameData.categoria?.nome || '',
      tipoAmostra: exameData.tipoAmostra,
      prazoDias: exameData.prazoDias,
      valor: exameData.valor,
      ativo: exameData.ativo
    });
    this.loading = false;
  }

  private loadExame(id: number): void {
    this.loading = true;

    this.exameService.getById(id)
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.applyExame(response.data);
          } else {
            this.toast.error(response.message || 'Erro ao carregar dados do exame.');
          }
        },
        error: () => {
          this.toast.error('Erro ao se conectar com o servidor.');
        }
      });
  }

  onSubmit(): void {
    if (this.exameForm.invalid) {
      this.exameForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const exameData: Exame = this.exameForm.value;

    const request$ = this.isEditMode && this.exameId
      ? this.exameService.update(this.exameId, exameData)
      : this.exameService.create(exameData);

    request$.subscribe({
      next: (response) => {
        if (response.success) {
          this.toast.success(
            this.isEditMode ? 'Exame atualizado com sucesso.' : 'Exame cadastrado com sucesso.'
          );
          this.router.navigate(['/exames']);
        } else {
          this.toast.error(response.message || 'Erro ao salvar exame.');
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
    return this.exameForm.controls;
  }
}