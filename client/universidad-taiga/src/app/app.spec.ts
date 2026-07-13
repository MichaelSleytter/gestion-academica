import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { App } from './app';
import { provideAngularComponentTest } from './testing/angular-test-providers';

@Component({
  template: '<p data-testid="route-content">Routed content</p>',
})
class TestRouteComponent {}

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App, TestRouteComponent],
      providers: [
        ...provideAngularComponentTest([
          {
            path: '',
            component: TestRouteComponent,
          },
        ]),
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;

    expect(app).toBeTruthy();
  });

  it('should render routed content inside the Taiga app shell', async () => {
    const fixture = TestBed.createComponent(App);
    const router = TestBed.inject(Router);

    fixture.detectChanges();
    await router.navigateByUrl('/');
    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('tui-root')).not.toBeNull();
    expect(compiled.querySelector('[data-testid="route-content"]')?.textContent).toContain(
      'Routed content',
    );
  });
});
