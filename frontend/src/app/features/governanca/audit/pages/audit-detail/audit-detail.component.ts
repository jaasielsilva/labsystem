import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize, timeout } from 'rxjs';
import { AuditService } from '../../services/audit.service';
import { AuditLog } from '../../models/audit.model';
import { ToastService } from '../../../../../core/services/toast.service';

@Component({
  selector: 'app-audit-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './audit-detail.component.html',
  styleUrls: ['./audit-detail.component.css']
})
export class AuditDetailComponent implements OnInit {

  private auditService = inject(AuditService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  log?: AuditLog;
  loading = signal(false);
  loadError = signal(false);

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.router.navigate(['/governanca/auditoria']);
      return;
    }

    const id = Number(idParam);
    if (!Number.isFinite(id) || id <= 0) {
      this.router.navigate(['/governanca/auditoria']);
      return;
    }

    this.loadLog(id);
  }

  scopeLabel(scope: string): string {
    const labels: Record<string, string> = {
      TENANT: 'Laboratório',
      TENANT_IMPERSONATION: 'Suporte (impersonação)',
      PLATFORM: 'Plataforma'
    };
    return labels[scope] ?? scope;
  }

  private loadLog(id: number): void {
    this.loading.set(true);
    this.loadError.set(false);

    this.auditService
      .getById(id)
      .pipe(
        timeout(15000),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (res) => {
          if (res.success && res.data) {
            this.log = res.data;
          } else {
            this.loadError.set(true);
            this.toast.error(res.message || 'Registro de auditoria não encontrado.');
          }
        },
        error: (err) => {
          this.loadError.set(true);
          if (err.name === 'TimeoutError') {
            this.toast.error('O servidor demorou para responder.');
          } else {
            this.toast.error(err.error?.message || 'Erro ao carregar registro de auditoria.');
          }
        }
      });
  }
}
