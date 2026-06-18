import { InjectionToken } from '@angular/core';

/**
 * Token de inyección que provee la URL base de la API REST.
 * Se proveen diferentes valores según el ambiente usando file replacements de Angular.
 */
export const APP_API_URL = new InjectionToken<string>('APP_API_URL');
