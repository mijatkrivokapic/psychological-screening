import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

console.log('Bootstrap start');
bootstrapApplication(AppComponent, appConfig)
  .then(() => console.log('Bootstrap success'))
  .catch((err) => console.error('Bootstrap error', err));
