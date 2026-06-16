import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ExameFormComponent } from './exame-form.component';

describe('ExameFormComponent', () => {
  let component: ExameFormComponent;
  let fixture: ComponentFixture<ExameFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExameFormComponent],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(ExameFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
