import { AfterViewInit, Directive, ElementRef, HostListener, inject, Injector, OnDestroy } from '@angular/core';
import { NgControl } from '@angular/forms';
import { Subscription } from 'rxjs';
import { digitsOnlyCpf, formatCpf } from './cpf.utils';

@Directive({
  selector: '[cpfMask]',
  standalone: true
})
export class CpfMaskDirective implements AfterViewInit, OnDestroy {
  private el = inject(ElementRef<HTMLInputElement>);
  private injector = inject(Injector);
  private valueChangesSub?: Subscription;

  ngAfterViewInit(): void {
    const control = this.ngControl?.control;
    if (!control) {
      return;
    }

    this.syncDisplay(control.value);
    this.valueChangesSub = control.valueChanges.subscribe((value) => {
      this.syncDisplay(value);
    });
  }

  ngOnDestroy(): void {
    this.valueChangesSub?.unsubscribe();
  }

  @HostListener('input')
  onInput(): void {
    const control = this.ngControl?.control;
    if (!control) {
      return;
    }

    const digits = digitsOnlyCpf(this.el.nativeElement.value);
    control.setValue(digits, { emitEvent: false });
    this.el.nativeElement.value = formatCpf(digits);
  }

  private get ngControl(): NgControl | null {
    return this.injector.get(NgControl, null, { optional: true, self: true });
  }

  private syncDisplay(value: unknown): void {
    const digits = digitsOnlyCpf(String(value ?? ''));
    this.el.nativeElement.value = formatCpf(digits);
  }
}
