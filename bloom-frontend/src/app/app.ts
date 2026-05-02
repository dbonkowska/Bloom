import { Component, inject, signal, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  private http = inject(HttpClient);
  protected message = signal('');

  ngOnInit() {
    this.http.get('/api/hello', { responseType: 'text' }).subscribe(response => {
      this.message.set(response);
    });
  }
}
