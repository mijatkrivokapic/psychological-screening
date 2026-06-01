import {Component, OnInit} from '@angular/core';
import { CommonModule, NgForOf, NgIf } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TemplateService } from '../services/template.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import {TemplateOverview} from '../models/template.overview.model';

@Component({
  selector: 'app-templates',
  standalone: true,
  imports: [CommonModule, NgForOf, NgIf, RouterModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatListModule, MatIconModule, MatDividerModule],
  templateUrl: './templates.component.html',
  styleUrls: ['./templates.component.css']
})
export class TemplatesComponent implements OnInit{
  templates: TemplateOverview[] = [];

  targetKinds = ['item_scoring', 'subscale_classification', 'composite_classification'];

  constructor(private templateService: TemplateService) {
  }

  ngOnInit(): void {
    this.loadTemplates();
  }

  loadTemplates(): void {
    this.templateService.getOverview().subscribe({
      next: (data) => {
        this.templates = data.filter(t => this.targetKinds.includes(t.kind) && (t.type === 'xlsx'));
      },
      error: (err) => console.error('Greška pri dobavljanju templejta', err)
    });
  }

  onActivate(kind: string, type: string, version: string): void {
    this.templateService.activateVersion(kind, type, version).subscribe({
      next: () => this.loadTemplates(),
      error: (err) => {
        const msg = err.error?.message || 'Došlo je do greške prilikom aktivacije.';
        alert(`Aktivacija nije uspela:\n${msg}`);
        this.loadTemplates(); // Refresh u slučaju rollback-a
      }
    });
  }

  onDelete(kind: string, type: string, version: string): void {
    if (confirm(`Da li ste sigurni da želite da obrišete verziju "${version}"?`)) {
      this.templateService.deleteVersion(kind, type, version).subscribe({
        next: () => this.loadTemplates(),
        error: (err) => alert(`Brisanje nije uspelo: ${err.error?.error || 'Nepoznata greška'}`)
      });
    }
  }

  onUpload(kind: string, type: string, event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const version = prompt('Unesite jedinstveni naziv verzije (npr. v2, production-v1):');

      if (version && version.trim() !== '') {
        this.templateService.uploadVersion(kind, type, version, file).subscribe({
          next: () => {
            this.loadTemplates();
            input.value = ''; // Reset inputa
          },
          error: (err) => {
            const msg = err.error?.message || 'Greška pri upload-u fajla.';
            alert(`Upload nije uspeo:\n${msg}`);
            input.value = '';
          }
        });
      } else {
        input.value = '';
      }
    }
  }


  onDownload(kind: string, type: string, version: string): void {
    this.templateService.downloadVersion(kind, type, version).subscribe({
      next: (blob: Blob) => {

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;

        a.download = `${kind}_${version}.${type}`;

        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);

        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Greška pri preuzimanju fajla', err);
        alert('Nije moguće preuzeti fajl. Provjerite da li fajl postoji na serveru.');
      }
    });
  }
}
