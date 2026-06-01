import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {TemplateOverview} from '../models/template.overview.model';
import { environment } from '../../environment/environment';

@Injectable({
  providedIn: 'root'
})
export class TemplateService {

  constructor(private http: HttpClient) { }

  getOverview(): Observable<TemplateOverview[]> {
    return this.http.get<TemplateOverview[]>(`${environment.baseUrl}/templates`);
  }

  uploadVersion(kind: string, type: string, version: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${environment.baseUrl}/templates/${kind}/${type}/versions/${version}`, formData);
  }

  activateVersion(kind: string, type: string, version: string): Observable<any> {
    return this.http.put(`${environment.baseUrl}/templates/${kind}/${type}/active`, { version });
  }

  deleteVersion(kind: string, type: string, version: string): Observable<any> {
    return this.http.delete(`${environment.baseUrl}/templates/${kind}/${type}/versions/${version}`);
  }

  downloadVersion(kind: string, type: string, version: string): Observable<Blob> {
    return this.http.get(`${environment.baseUrl}/templates/${kind}/${type}/versions/${version}`, {
      responseType: 'blob'
    });
  }
}
