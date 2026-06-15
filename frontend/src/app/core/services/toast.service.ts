import { Injectable, signal } from '@angular/core';
import { ToastMessage, ToastType } from './toast.model';

const DEFAULT_DURATION: Record<ToastType, number> = {
  success: 4000,
  error: 6000,
  warning: 5000,
  info: 4000
};

const MAX_VISIBLE = 5;

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  private timers = new Map<number, ReturnType<typeof setTimeout>>();

  readonly toasts = signal<ToastMessage[]>([]);

  success(message: string, durationMs?: number): void {
    this.show('success', message, durationMs);
  }

  error(message: string, durationMs?: number): void {
    this.show('error', message, durationMs);
  }

  warning(message: string, durationMs?: number): void {
    this.show('warning', message, durationMs);
  }

  info(message: string, durationMs?: number): void {
    this.show('info', message, durationMs);
  }

  dismiss(id: number): void {
    const timer = this.timers.get(id);
    if (timer) {
      clearTimeout(timer);
      this.timers.delete(id);
    }
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }

  private show(type: ToastType, message: string, durationMs?: number): void {
    const trimmed = message?.trim();
    if (!trimmed) {
      return;
    }

    const id = this.nextId++;
    const toast: ToastMessage = { id, type, message: trimmed };

    this.toasts.update((list) => {
      const next = [...list, toast];
      return next.length > MAX_VISIBLE ? next.slice(-MAX_VISIBLE) : next;
    });

    const duration = durationMs ?? DEFAULT_DURATION[type];
    const timer = setTimeout(() => this.dismiss(id), duration);
    this.timers.set(id, timer);
  }
}
