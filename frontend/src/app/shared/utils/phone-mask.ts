import { Directive, ElementRef, HostListener } from '@angular/core';

@Directive({
  selector: '[phoneMask]',
  standalone: true
})
export class PhoneMaskDirective {

  constructor(private el: ElementRef<HTMLInputElement>) {}

  @HostListener('input')
  onInput() {
    let value = this.el.nativeElement.value;

    // remove tudo que não for número
    value = value.replace(/\D/g, '');

    // aplica formatação
    if (value.length > 11) {
      value = value.slice(0, 11);
    }

    if (value.length > 10) {
      // celular (11 dígitos)
      value = value.replace(/^(\d{2})(\d{5})(\d{4})$/, '($1) $2-$3');
    } else if (value.length > 6) {
      value = value.replace(/^(\d{2})(\d{4})(\d{0,4})$/, '($1) $2-$3');
    } else if (value.length > 2) {
      value = value.replace(/^(\d{2})(\d{0,5})$/, '($1) $2');
    } else {
      value = value.replace(/^(\d*)$/, '($1');
    }

    this.el.nativeElement.value = value;
  }
}