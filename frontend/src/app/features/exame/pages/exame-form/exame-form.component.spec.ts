import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExameForm } from './exame-form.component';

describe('ExameForm', () => {
  let component: ExameForm;
  let fixture: ComponentFixture<ExameForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExameForm],
    }).compileComponents();

    fixture = TestBed.createComponent(ExameForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
