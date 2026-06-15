import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-ui-playground',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './ui-playground.component.html',
  styleUrls: ['./ui-playground.component.css']
})
export class UiPlaygroundComponent {
  private toast = inject(ToastService);

  readonly sections = [
    { id: 'tokens', label: 'Tokens' },
    { id: 'tipografia', label: 'Tipografia' },
    { id: 'botoes', label: 'Botões' },
    { id: 'formularios', label: 'Formulários' },
    { id: 'feedback', label: 'Feedback' },
    { id: 'badges', label: 'Badges' },
    { id: 'tabela', label: 'Tabela' },
    { id: 'layout', label: 'Layout' }
  ];

  readonly colorTokens = [
    { name: 'Primary (saúde)', var: '--color-primary', hex: '#0d9488' },
    { name: 'Accent (clínico)', var: '--color-accent', hex: '#0284c7' },
    { name: 'Danger', var: '--color-danger', hex: '#dc2626' },
    { name: 'Text', var: '--text-primary', hex: '#0f172a' },
    { name: 'Muted', var: '--text-secondary', hex: '#64748b' },
    { name: 'Page BG', var: '--bg-page', hex: '#eef6f9' },
    { name: 'Card', var: '--card-bg', hex: '#ffffff' }
  ];

  readonly demoRows = [
    { id: 1, nome: 'Ana Silva', status: 'ATIVO', email: 'ana@lab.com' },
    { id: 2, nome: 'Carlos Mendes', status: 'PENDENTE', email: 'carlos@lab.com' },
    { id: 3, nome: 'Maria Costa', status: 'INATIVO', email: 'maria@lab.com' }
  ];

  showLoading = false;
  demoForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.demoForm = this.fb.group({
      nome: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      data: ['']
    });
  }

  toggleLoading(): void {
    this.showLoading = !this.showLoading;
  }

  showToast(type: 'success' | 'error' | 'warning' | 'info'): void {
    const messages = {
      success: 'Cliente salvo com sucesso.',
      error: 'Não foi possível conectar. Tente novamente.',
      warning: 'Limite do plano quase atingido.',
      info: 'Novo resultado disponível para entrega.'
    };
    this.toast[type](messages[type]);
  }

  markFormTouched(): void {
    this.demoForm.markAllAsTouched();
  }

  get f() {
    return this.demoForm.controls;
  }

  badgeClass(status: string): string {
    const map: Record<string, string> = {
      ATIVO: 'badge-green',
      PENDENTE: 'badge-yellow',
      INATIVO: 'badge-gray'
    };
    return map[status] ?? 'badge-gray';
  }
}
