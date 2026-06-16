import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ExameListComponent } from './exame-list.component';

describe('ExameListComponent', () => {
  let component: ExameListComponent;
  let fixture: ComponentFixture<ExameListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExameListComponent],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(ExameListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
