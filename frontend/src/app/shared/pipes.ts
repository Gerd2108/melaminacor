import { Pipe, PipeTransform } from '@angular/core';
import { environment } from '../../environments/environment';

/** 1234.5 -> "S/ 1,234.50" */
@Pipe({ name: 'soles' })
export class SolesPipe implements PipeTransform {
  transform(valor: number | null | undefined): string {
    if (valor == null) return '';
    return 'S/ ' + Number(valor).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}

/** Las imágenes subidas vienen como "/uploads/x.jpg": les anteponemos la URL del backend. */
@Pipe({ name: 'imagen' })
export class ImagenPipe implements PipeTransform {
  transform(url: string | null | undefined): string | null {
    if (!url) return null;
    return url.startsWith('/uploads') ? environment.apiUrl + url : url;
  }
}
