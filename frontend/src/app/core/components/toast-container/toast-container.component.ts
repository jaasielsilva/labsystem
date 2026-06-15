import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';
import { ToastMessage } from '../../services/toast.model';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast-container.component.html',
  styleUrl: './toast-container.component.css'
})
export class ToastContainerComponent {
  protected toastService = inject(ToastService);

  icon(toast: ToastMessage): string {
    const icons: Record<ToastMessage['type'], string> = {
      success: '✓',
      error: '✕',
      warning: '!',
      info: 'i'
    };
    return icons[toast.type];
  }

  dismiss(id: number): void {
    this.toastService.dismiss(id);
  }
}
