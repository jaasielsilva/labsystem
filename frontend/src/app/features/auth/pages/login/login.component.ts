import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { environment } from '../../../../../environments/environment';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

interface DevAccount {
  label: string;
  email: string;
  senha: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);

  loginForm!: FormGroup;
  loading = false;

  readonly showDevCredentials = environment.showDevCredentials;

  readonly devAccounts: DevAccount[] = [
    { label: 'Super Admin', email: 'super@labsystem.local', senha: 'super123' },
    { label: 'Admin', email: 'admin@labsystem.local', senha: 'admin123' },
    { label: 'Operador', email: 'operador@labsystem.local', senha: 'operador123' },
    { label: 'Visualizador', email: 'visualizador@labsystem.local', senha: 'visualizador123' }
  ];

  ngOnInit(): void {
    if (this.auth.isAuthenticated()) {
      this.router.navigate([this.auth.getHomeRoute()]);
      return;
    }
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  fillDevAccount(account: DevAccount): void {
    this.loginForm.patchValue({
      email: account.email,
      senha: account.senha
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    this.auth.login(this.loginForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          this.router.navigate([this.auth.getHomeRoute()]);
        } else {
          this.toast.error(response.message || 'Não foi possível entrar.');
          this.loading = false;
        }
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'E-mail ou senha inválidos.');
        this.loading = false;
      }
    });
  }

  get f() {
    return this.loginForm.controls;
  }
}
